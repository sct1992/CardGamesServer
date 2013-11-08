package server;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import uniandes.cupi2.cupiEmail.comun.mundo.ProtocoloComunicacion;
import uniandes.cupi2.cupiEmail.servidor.mundo.comunicacion.ManejadorComunicacionesServidor;
import uniandes.cupi2.cupiEmail.servidor.mundo.comunicacion.Mensaje;
import uniandes.cupi2.cupiEmail.servidor.mundo.excepciones.CupiEmailServidorException;
import uniandes.cupi2.cupiEmail.servidor.mundo.excepciones.UsuariosDesconectadoException;
import uniandes.cupi2.cupiEmail.servidor.mundo.persistencia.ManejadorPersistencia;


/**
 * Clase que representa un cliente remoto conectado al servidor
 */
public class UserSession extends Thread {
	// -----------------------------------------------------------------
	// Constantes
	// -----------------------------------------------------------------

	
	
	// -----------------------------------------------------------------
	// Atributos
	// -----------------------------------------------------------------

	/**
	 * El usuario que ha iniciado sesión
	 */
	private Usuario usuario;

	/**
	 * El manejador de persistencia
	 */
	private ManejadorPersistencia manejadorPersistencia;

	/**
	 * El manejador de las comunicaciones
	 */
	private ManejadorComunicacionesServidor manejadorComunicaciones;

	// -----------------------------------------------------------------
	// Constructores
	// -----------------------------------------------------------------

	
}