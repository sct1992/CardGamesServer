package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import javax.swing.*;

import sun.awt.windows.ThemeReader;

import common.Card;
import common.InterfaceServer;
import common.Protocol;
import common.User;
import common.Workspace;



/**
 * Es la clase principal de la interfaz
 */
public class Server extends UnicastRemoteObject implements InterfaceServer
{
    //-----------------------------------------------------------------
    // Atributos
    //-----------------------------------------------------------------


	/**
	 * workspaces activos
	 */
	public ArrayList<Workspace> activeWorkspaces;
	
	/**
	 * 
	 */
	public ArrayList<UserSession> activeUsers;
	
	   /**
     * Es el punto por el cual los clientes solicitan conexiones
     */
    private ServerSocket receptor;
    
    /**
     * los workspaces que se pueden crear si los jugadores aceptan jugar
     */
    private ArrayList<ThreadNewWorkspace> futuredWorkspaces;
    
    /**
	 * Es el manejador de persistencia
	 */
	private StorageHandler storageHandler;
	
    //-----------------------------------------------------------------
    // Constructores
    //-----------------------------------------------------------------

    /**
     * Construye la interfaz e inicializa el juego indicando que no se ha cargado la información de ningún archivo
     * @throws RemoteException 
     */
    public Server( ) throws RemoteException
    {
    	super();

    	activeUsers = new ArrayList<UserSession>();
    	futuredWorkspaces = new ArrayList<ThreadNewWorkspace>();
    	try {
			storageHandler = new StorageHandler();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
           
    }
	
    
    /**
     * Este método se encarga de recibir todas las conexiones entrantes y crear las partidas cuando sea necesario.
     * las conexiones son de usuarios ya registrados.
     */
    public void recibirConexiones( )
    {
		int puerto = Protocol.PUERTO_SERVER_SOCKET;
		
		try {
			receptor = new ServerSocket(puerto);
		
			while (true) {
                   Socket socketJugador = receptor.accept( );
                   UserSession tmp = new UserSession(socketJugador);
                   activeUsers.add(tmp);	
			}
		}
		catch (Exception e) 
		{	
			e.printStackTrace();
		}
		finally
		{
			try {
				receptor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

	    
	@Override
	public boolean login(String user, String password)throws Exception 
	{
		boolean response = storageHandler.logIn(user, password);
		
		return response;
			
	}
	@Override
	public boolean signUp(String username, String name, String password,
			String email) throws Exception
	{
		return storageHandler.registerUser(name, username, password, email);
	}

	@Override
	public User getUser(String username) {
		return storageHandler.getUser(username);
	}


	@Override
	public Workspace getWorkspace(int id) {
		return storageHandler.getWorkspace(id);
	}


	@Override
	public ArrayList<User> getActiveUsers(String username) {
		ArrayList<User> users = new ArrayList<User>();
		
		for(UserSession session: activeUsers)
		{
			User us = new User(0,session.getUserName(),"","","",new ArrayList<Card>());
			users.add(us);
		}		
		return users;
	}


	@Override
	public ArrayList<Card> getCards(String username) {
		return storageHandler.getCards(username);
	}


	@Override
	public ArrayList<Workspace> getMyWorkspaces(String username) {
		return storageHandler.getUserWorkspaces(username);
	}


	@Override
	public boolean createCard(String name, String description, String imageUrl,
			String place, String owner, String category) throws Exception{
		return storageHandler.createCard(name, description, imageUrl, category, place, owner);
	}


	@Override
	public boolean addCardToDeck(String username, int cardId) {
		return storageHandler.addCardToDeck(username, cardId);
	}


	@Override
	public boolean removeCardFromDeck(String username, int cardId) {
		return storageHandler.removeCardfromDeck(username, cardId);
	}


	@Override
	public boolean proposeCard(int workspaceId, int cardId) {
		boolean response = storageHandler.proposeCard(cardId, workspaceId);
		if (response)
			sendNotifyRefresh(workspaceId);
		return response;
	}


	@Override
	public boolean voteCard(int workspaceId, int cardId) {
		boolean response = storageHandler.voteCard(workspaceId, cardId);
		if (response)
			sendNotifyRefresh(workspaceId);
		return response;
	}


	@Override
	public boolean sendMessage(int workspaceId, String username, String message) {
		String the_message = username+" dijo: "+message;
		boolean response = storageHandler.updateChatHistory(the_message, workspaceId);
		if(response)
			sendNotifyRefresh(workspaceId);
		return false;
	}


	@Override
	public Card getCard(int id) {
		return storageHandler.getCard(id);
	}

	/**
	 * Metodo que es llamado por un jugador que desea comenzar un juego, no importa si esta creado o no
	 * el metodo lanza un thread que se encarga de coordinar a los jugadores "ThreadNewWorkspace"
     * @return true si todos los jugadores estan activos y se puede coordinarlos, false en caso de que no se encuentre uno
	 */
	public boolean startGame(String username, ArrayList<String> guests) {
		
		System.out.println(username+" "+guests);
		ArrayList<UserSession> guestsUsers = new ArrayList<UserSession>();
		
		// cojo los ussersesion q participan en el thread
		
		UserSession creator = null;
		
		for (int i = 0; i < guests.size(); i++) {

			String tmp = guests.get(i);
			boolean termino = false;
			for (int j = 0; j < activeUsers.size()&& !termino; j++) {

				UserSession participanteTmp = activeUsers.get(j);

				if(tmp.equals(participanteTmp.getUserName()))
				{
					termino = true;
					guestsUsers.add(participanteTmp);
				}
				
			}
			//si no se encontro 
			if(termino ==false)
			{
				return false;
			}
			for(UserSession session: activeUsers)
			{
				if(username.equals(session.getUserName()))
				{
					creator=session;
				}	
			}
		}
		
		if(creator == null)
		{
			return false;
		}
		
		ThreadNewWorkspace tmp = new ThreadNewWorkspace(creator, guestsUsers, ThreadNewWorkspace.NO_CARD );
		futuredWorkspaces.add(tmp);
		tmp.start();
		
		return true;
	}



	/**
	 * Metodo que es llamado por un jugador que desea comenzar un juego a partir de una carta
	 * el metodo lanza un thread que se encarga de coordinar a los jugadores "ThreadNewWorkspace"
	 * Pre: el workspace entre los jugadores no ha sido nunca creado
     * @return true si todos los jugadores estan activos y se puede coordinarlos, false en caso de que no se encuentre uno
	 */
	public boolean startGame(int cardId, String username,ArrayList<String> guests) {

		ArrayList<UserSession> guestsUsers = new ArrayList<UserSession>();
		
		// cojo los ussersesion q participan en el thread
		
		UserSession creator = null;
		
		for (int i = 0; i < guests.size(); i++) {
		
			String tmp = guests.get(i);
			boolean termino = false;
			for (int j = 0; j < activeUsers.size()&& !termino; j++) {
				
				UserSession participanteTmp = activeUsers.get(j);
				
				if(tmp.equals(participanteTmp.getUserName()))
						{
					termino = true;
					guestsUsers.add(participanteTmp);
						}
				if(username.equals(participanteTmp.getUserName()))
				{
					creator=participanteTmp;
				}		
			}
			//si no se encontro 
			if(termino ==false)
			{
				return false;
			}
		}
		
		if(creator == null)
		{
			return false;
		}
		
		ThreadNewWorkspace tmp = new ThreadNewWorkspace(creator, guestsUsers, cardId);
		tmp.start();
		
		return true;
	}
	
	
	/**
	 * este metodo es la aceptacion de un jugador a una invitacion que le hicieron
	 * al final verifica si se puede crear la partida
	 * true en caso de hacerlo, false en caso de un error
	 */
	public boolean acceptGame(String threadId, String username) {
		
		ThreadNewWorkspace buscado = null;
		
		for (int i = 0; i < futuredWorkspaces.size(); i++) {
			
			ThreadNewWorkspace tmp = futuredWorkspaces.get(i);
			
			if( tmp.getIdThreat().equals(threadId))
			{
				buscado=tmp;
				break;
			}
		}
		
		if( buscado==null)
			return false;
		
		buscado.confirm();
		if(buscado.readyToCreate())
		{
			//en este punto toca crear el juego a partir del thread
			// se guarda en bd y se envia el id del workspace recien creado o cargado
			Workspace work = storageHandler.createWorkspace(buscado.getUsernamesList());
			
			int idWorkspace = work.getId();
			buscado.sendConfirmation(idWorkspace);
		}
		return true;
	}


	/**
	 * este metodo es el rechazo de un jugador a una invitacion que le hicieron
	 * al final llama y comunica a los participantes del rechazo de la partida
	 * true en caso de hacerlo, false en caso de un error
	 */
	public boolean rejectGame(String threadId, String username) {

		ThreadNewWorkspace buscado = null;
		
		for (int i = 0; i < futuredWorkspaces.size(); i++) {
			
			ThreadNewWorkspace tmp = futuredWorkspaces.get(i);
			
			if( tmp.getIdThreat().equals(threadId))
			{
				buscado=tmp;
				break;
			}
		}
		
		if( buscado==null)
			return false;
		
		buscado.reject();
		buscado.sendCancelation("El usuario: "+ username + " ha rechazado la partida: \n" +  threadId );
		return true;
	}

	
	/**
	 * 
	 * @param idWorkspace
	 * @param commandPush debe ser REFRESH, NEW_GAME;p1:p2:p3:...:pn, NEW_GAME_CARD;p1:p2:p3:...:pn
	 */
	private void sendNotifyRefresh(int idWorkspace)
	{
		//buscar usuarios del workspace por parametro
		// realizar push de Refresh
		//TODO completar,
		// en la clase participantesno se debe inicializar de ceros, se supone que estan los users del workspace
		ArrayList<User> participantes = new ArrayList<User>();
		
		for (int i = 0; i < activeUsers.size(); i++) {
		
			UserSession tmp = activeUsers.get(i);
			
			for (int j = 0; j < participantes.size(); j++) {
				
				User participanteTmp = participantes.get(j);
				
				if(tmp.getUserName().equals(participanteTmp.getUsername()))
						{
						tmp.sendPushRefresh(idWorkspace);
						break;
						}
			}
			
		}
		
	}
	
    //-----------------------------------------------------------------
    // Programa principal
    //-----------------------------------------------------------------
	
	   /**
     * Ejecuta la aplicación
     * @param args Estos parámetros no se usan.
     */
    public static void main( String[] args )
    {
    	try 
    	{
    		Server server = new Server();
			Registry reg = LocateRegistry.createRegistry(Protocol.PUERTO_SERVER_RMI);
			reg.rebind(Protocol.ID_SERVER_RMI, server);
    		System.out.println("Server Started");
			server.recibirConexiones();
    		
		}
    	catch (Exception e) 
    	{	
    		e.printStackTrace();
		}
     
    }




}