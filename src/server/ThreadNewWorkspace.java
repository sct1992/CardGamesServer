package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import common.Card;


public class ThreadNewWorkspace extends Thread {

	// -----------------------------------------------------------------
	// Atributos
	// -----------------------------------------------------------------

	/**
	 * constante que indica que el workspace no se creara a partir de una carta
	 */
	public final static int NO_CARD=-1;
	
	/**
	 * numero de participantes que han confirmado
	 */
	private int confirms;
	
	/**
	 * numero de rechazos, con 1 es mas que suficiente para cancelar la partida
	 */
	private int rejects;
	
	 /**
     * El canal usado para comunicarse con el jugador 1
     */
    private ArrayList<UserSession> users;

    /**
     * La sesssion del creador del workspace
     */
    private UserSession userCreator;
    
    /**
     * marca de tiempo para el timeout
     */
    private long timeStamp;
    
    /**
     * id temporal del workspace (thread) que se cargara
     */
    private String idThread;
    
    /**
     * numero de la carta que se invita, -1 si no se crea  partir de una carta
     */
    private int cardId; 


	// -----------------------------------------------------------------
	// Constructores
	// -----------------------------------------------------------------

	/**
	 * constructor del thread que se encarga de coordinar la creacion de workspaces
	 * @param userSessionCreator session del creado
	 * @param users los participantes del workspaces, excluyendo al creador
	 * @param card carta inicial que se motiva a usar, puede ser -1 y no haber ninguna carta
	 */
	public ThreadNewWorkspace (UserSession userSessionCreator, ArrayList<UserSession> users, int card) 
	{			
	
		// 1 voto del creador
		confirms = 0;
		userCreator = userSessionCreator;
		this.users = users; 
		timeStamp = System.currentTimeMillis();
		idThread= userSessionCreator.getUserName() + " inviting to: " + users.toString();
		this.cardId=card;
		rejects=0;
		
	}

	// -----------------------------------------------------------------
	// Métodos
	// -----------------------------------------------------------------

	/**
	 * Método que se invoca cuando inicia la ejecución del thread
	 * envia un mensaje de invitacion a la partida
	 * coordina la creacion de workspace
	 */
	 public void run( )
	    {		   
	        try
	        {	      
	        	
	        	//se envia por socket a los participantes de la nueva partida
	        	
	        	for (int i = 0; i < users.size(); i++) {
					
	        		UserSession tmp = users.get(i);
	        		
	        		if(cardId!=NO_CARD)
	        		{
	        		tmp.sendPushNewGameCard(userCreator.getUserName(), cardId, idThread);
	        		}
	        		else
	        		{
	               	tmp.sendPushNewGame(userCreator.getUserName(), idThread);
	        		}
	        		
				}
	      
	        	// se inicia un temporizador, si alguien cancela o pasan 15 seg se termina
	        	int confirmsNeeded=users.size();	        	
	        	long millisPassed = System.currentTimeMillis()-timeStamp;
	            while(confirms<confirmsNeeded && millisPassed < 15000 && rejects==0)
	            {
	            	wait(500);
	            	millisPassed = System.currentTimeMillis()-timeStamp;
	            }
	                        
	            // en caso de que haya pasado el tiempo y uno no haya respondido, se cancela del error por timeout
	            if(confirms!=confirmsNeeded && rejects==0)
	            {
	            	sendCancelation("Time Out: se ha agotado el tiempo de espera \n" +
	            			"uno de los participantes no respondio a la solicitud \n" +
	            			"La partida: \n" + idThread + " no se cargara");
	            }
	            
	            //se acaba el thread, la confirmacion o rechazo se llaman desde el servidor
	        }
	        catch( Exception e )
	        {
	        	//enviar un error en caso de alguna excepcion no contemplada
	         	sendCancelation("Error cargando la partida " + idThread + " no se cargara");
	        }
	    }

	   /**
	    * envia a todos los particiapntes un anuncion de cancelacion de la partida
	    * @param message el motivo de la cancelacion
	    */
	public void sendCancelation(String message)
	{
		userCreator.sendPushCancel(message);
    	
    	for (int i = 0; i < users.size(); i++) {
    		UserSession tmp = users.get(i);
    		tmp.sendPushCancel(message);
		}
		
	}
	
	/**
	 * envia una notificacion de refrescar con el id del workspace creado o cargado
	 * @param idWorkspace el id del workspace a refrescar (el reciente cargado o creado)
	 */
	public void sendConfirmation(int idWorkspace)
	{
    	userCreator.sendPushRefresh(idWorkspace);
    	
    	for (int i = 0; i < users.size(); i++) {
    		UserSession tmp = users.get(i);
    		tmp.sendPushRefresh(idWorkspace);
		}
    
	}

	/**
	 * confirmacion de un particiapnte
	 */
	public void confirm() {
		confirms++;
	}

	/**
	 * rechazo de un participante
	 */
	public void reject() {
		rejects++;
	}
	
	/**
	 * tomar el id temporal del threat
	 * @return id del threat
	 */
	public String getIdThreat() {
		return idThread;
	}

	/**
	 * metodo que valida si ya esta listo para crear el workspace
	 * @return true si ya todos confirmaron, false en caso contrario
	 */
	public boolean readyToCreate() {
		
		return (users.size()==confirms);
	}

	   

}
