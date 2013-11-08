/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 * $Id: CupiEmail.java 2090 2010-11-02 16:50:02Z cm.rodriguez155 $
 * Universidad de los Andes (Bogot� - Colombia)
 * Departamento de Ingenier�a de Sistemas y Computaci�n 
 * Licenciado bajo el esquema Academic Free License version 2.1 
 *
 * Proyecto Cupi2 (http://cupi2.uniandes.edu.co)
 * Ejercicio: n12_cupiEmail
 * Autor: Camilo Alvarez Duran - 12-ene-2010
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import uniandes.cupi2.cupiEmail.cliente.mundo.Mensaje;
import uniandes.cupi2.cupiEmail.comun.mundo.ProtocoloComunicacion;
import uniandes.cupi2.cupiEmail.servidor.mundo.comunicacion.ManejadorComunicacionesServidor;
import uniandes.cupi2.cupiEmail.servidor.mundo.excepciones.CupiEmailServidorException;
import uniandes.cupi2.cupiEmail.servidor.mundo.persistencia.ManejadorPersistencia;

/**
 * Clase que representa un servidor cupiEmail
 */
public class CardsGameServer {
	// -----------------------------------------------------------------
	// Atributos
	// -----------------------------------------------------------------

	/**
	 * Es el socket servidor a cual se conectan los usuarios
	 */
	private ServerSocket socketServidor;

	/**
	 * Es la configuraci�n del sistema
	 */
	private Properties config;

	/**
	 * Es el manejador de persistencia
	 */
	private ManejadorPersistencia manejadorPersistencia;

	/**
	 * Es la lista de usuarios conectados
	 */
	private ArrayList usuariosConectados;

	// -----------------------------------------------------------------
	// Constructores
	// -----------------------------------------------------------------

	/**
	 * Construye un nuevo cupiemail
	 * @throws CupiEmailServidorException En caso de que se encuentre un error en la inicializaci�n del
	 *  sistema
	 */
	public CardsGameServer() throws CupiEmailServidorException {
		config = new Properties();
		try {
			config.load(new FileInputStream("./data/servidor.properties"));
			manejadorPersistencia = new ManejadorPersistencia(config);
		} catch (Exception e) {
			throw new CupiEmailServidorException(e.getMessage());
		}
		usuariosConectados = new ArrayList();
	}

	/**
	 * M�todo que inicia el socket servidor para recibir conexiones de los
	 * clientes
	 * @throws CupiEmailServidorException En caso de encontrar un error
	 */
	public void esperarConexiones() throws CupiEmailServidorException {
		String aux = config.getProperty("servidor.puerto");
		int puerto = Integer.parseInt(aux);
		try {
			socketServidor = new ServerSocket(puerto);
			verificarInvariante();
			while (true) {
				// Esperar una nueva conexi�n
				
				// TODO Acepte una conexi�n usando el receptor
                
                
	               Socket socketNuevoCliente = socketServidor.accept( );
	               
	               
	               registrarUsuario(socketNuevoCliente);
	               
                // TODO Atender el nuevo cliente 
                // Ayuda: Use el m�todo registrarUsuario
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socketServidor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// -----------------------------------------------------------------
	// M�todos
	// -----------------------------------------------------------------

	/**
	 * M�todo que registra un nuevo usuario
	 * @throws CupiEmailServidorException En caso de encontrar un error
	 */
	private void registrarUsuario(Socket socketNuevoCliente)
			throws CupiEmailServidorException {
	    
	    ManejadorComunicacionesServidor temp = new ManejadorComunicacionesServidor(socketNuevoCliente);
		
		UserSession client = new UserSession(temp, manejadorPersistencia);
		
		client.start();
		
		usuariosConectados.add(client);
	
	    // TODO complete el m�todo de acuerdo con su contrato
        // 1. Cree un nuevo manejadorComunicaciones
        // 2. Cree un nuevo ClienteRemotoCupiEmail
        // 3. Inicie el thread del cliente remoto
        // 4. Agregue el nuevo cliente a la lista de clientes conectados
	    
	}

	/**
	 * M�todo que consulta la base de datos y retorna los detalles de un usuario
	 * @param usuario El nombre de usuario que se desea consultar. usuario != null y usuario != ""
	 * @return Objeto de tipo Usuario con la informaci�n detallada del usuario dado
	 * @throws CupiEmailServidorException En caso de encontrar un error
	 */
	public Usuario darDetallesUsuario(String usuario) throws CupiEmailServidorException {
	    
		try {
			return manejadorPersistencia.buscarUsuario(usuario);
		} catch (SQLException e) {
			throw new CupiEmailServidorException(e.getMessage());
		}

	}

	/**
	 * M�todo que retorna la lista de usuarios registrados en el sistema
	 * @return Lista de usuarios registrados
	 * @throws CupiEmailServidorException En caso de encontrar un error
	 */
	public ArrayList darUsuarios() throws CupiEmailServidorException {

		try {
			return manejadorPersistencia.consultarUsuariosRegistrados();
		} catch (SQLException e) {
			throw new CupiEmailServidorException(e.getMessage());
		}
	    
	}

	/**
	 * M�todo que retorna la lista de usuarios conectados
	 * @return La lista de usuarios conectados
	 * @throws CupiEmailServidorException En caso de encontrar un error
	 */
	public ArrayList darUsuariosConectados() throws CupiEmailServidorException {

		try {
			return manejadorPersistencia.consultarUsuariosConectados();
		} catch (SQLException e) {
			throw new CupiEmailServidorException(e.getMessage());
		}
	}

	// -----------------------------------------------------------------
	// Invariante
	// -----------------------------------------------------------------

	/**
	 * Verifica el invariante de la clase <br>
	 * <b>inv:</b><br>
	 * socketServidor!= null <br>
	 * config!=null <br>
	 * manejadorPersistencia!=null <br>
	 * usuariosConectados!=null <br>
	 */
	private void verificarInvariante() {
		assert socketServidor != null : "Canal inv�lido";
		assert config != null : "Conjunto de propiedades inv�lido";
		assert manejadorPersistencia != null : "El manejador de persistencia no deber�a ser null";
		assert usuariosConectados != null : "La lista de usuarios conectados no deber�a ser null";
	}

	// -----------------------------------------------------------------
	// Puntos de Extensi�n
	// -----------------------------------------------------------------

	/**
	 * M�todo para la extensi�n 1
	 * @param usuario 
	 * 
	 * @return respuesta1
	 */
	public String metodo1(String usuario) 
	{
		
		String rta = "se ha borrado con exito el usuario"; 
		try {
			manejadorPersistencia.borrarUsuario(usuario);
			
		} catch (SQLException e) {
			rta="error eliminado el usuario";
		}
		return rta;
	}

	/**
	 * M�todo para la extensi�n2
	 * 
	 * @return respuesta2
	 */
	public String metodo2() {
		manejadorPersistencia.borrarBD();
		try{
		manejadorPersistencia.iniciliazarBd();
		} catch(SQLException e)
		{
		return "Fatal Error: No se inicializaron correctamente las tablas";	
		}
		return "La base de datos se ha reiniciado";
	}

}