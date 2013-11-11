package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import common.Card;
import common.Protocol;
import common.User;


/**
 * Clase que representa un cliente remoto conectado al servidor
 */
public class UserSession extends Thread {
	
	// -----------------------------------------------------------------
	// Atributos
	// -----------------------------------------------------------------

	/**
	 * El usuario que ha iniciado sesión
	 */
	private String userName;

	  /**
     * El canal usado para comunicarse con el jugador 
     */
    private Socket socketJugador;

    /**
     * El flujo de escritura conectado con el jugador 
     */
    private PrintWriter out;

    /**
     * El flujo de lectura conectado con el jugador 
     */
    private BufferedReader in;
    
	// -----------------------------------------------------------------
	// Constructores
	// -----------------------------------------------------------------

	/**
	 * constructor de la clase userSession que represta el canal de comunicacion de un usuario
	 * @param sSocket socket con el usuario
	 */
    public UserSession ( Socket sSocket )
    {
    	socketJugador = sSocket;
		try {
			
			in = new BufferedReader(new InputStreamReader(sSocket.getInputStream()));
			out = new PrintWriter(sSocket.getOutputStream(), true);
			userName= in.readLine();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
    }

    /**
     * retorna el username de la sesion
     * @return username
     */
	public String getUserName() {
		return userName;
	}

	/**
	 * metodo encargado de enviar una notificacion de refrescar un workspace
	 * @param idWorkspace id del workspace a refrescar
	 */
	public void sendPushRefresh(int idWorkspace)
	{
		out.println(Protocol.REFRESH + Protocol.SEPARATOR1 + idWorkspace+Protocol.SEPARATOR2+true);
	}

	/**
	 * metodo encargado de enviar una notificacion de nueva partida motivada por una carta
	 * @param userCreator creador del workspace
	 * @param cardId id de la carta a motivar
	 * @param idThreat id temporal del workspace
	 */
	public void sendPushNewGameCard(String userCreator, int cardId, String idThreat) {
		
		out.println(Protocol.NEW_GAME_CARD + Protocol.SEPARATOR1 + userCreator + Protocol.SEPARATOR2 + cardId + Protocol.SEPARATOR2 + idThreat);
			
	}

	/**
	 * metodo encargado de enviar una notificacion de nueva partida
	 * @param userCreator creador del workspace
	 * @param idThreat id temporal del workspace
	 */
	public void sendPushNewGame(String userCreator, String idThreat) {

		System.out.println("Esta enviando push "+ idThreat);
		out.println(Protocol.NEW_GAME + Protocol.SEPARATOR1 + userCreator + Protocol.SEPARATOR2 + idThreat);
		
	}

	/**
	 * metodo encargado de enviar una notificacion de cancelacion de creacion de la partida
	 * @param message motivo de la cancelacion
	 */
	public void sendPushCancel(String message) {
		
		out.println(Protocol.WORKSPACE_REJECTED + Protocol.SEPARATOR1 + message);
			
	}
    
    
    
    
}