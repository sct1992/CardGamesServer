package server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import uniandes.cupi2.cupiEmail.cliente.mundo.CupiEmailException;
import uniandes.cupi2.cupiEmail.servidor.mundo.CorreoElectronico;
import uniandes.cupi2.cupiEmail.servidor.mundo.Usuario;

/**
 * Clase que se encarga del manejo de la persistencia
 */
public class ManejadorPersistencia {
	// -----------------------------------------------------------------
	// Atributos
	// -----------------------------------------------------------------

	/**
	 * Conexión a la base de datos
	 */
	private Connection conexion;

	/**
	 * Conjunto de propiedades que contienen la configuración de la aplicación
	 */
	private Properties config;

	// -----------------------------------------------------------------
	// Constructores
	// -----------------------------------------------------------------

	/**
	 * Construye un nuevo manejador de persistencia
	 * @param nProperties La configuración para la conexión a la base de datos. nProperties != null
	 * @throws Exception En caso de encontrar un error
	 */
	public ManejadorPersistencia(Properties nProperties) throws Exception {
		config = nProperties;

		// Establecer la ruta donde va a estar la base de datos.
		// Derby utiliza la propiedad del sistema derby.system.home para saber
		// donde están los datos
		File data = new File(config.getProperty("admin.db.path"));
		System.setProperty("derby.system.home", data.getAbsolutePath());
		conectarABD();
		iniciliazarBd();
	}

	// -----------------------------------------------------------------
	// Métodos
	// -----------------------------------------------------------------

	/**
	 * Método que inicializa la la base de datos
	 * @throws SQLException En caso de encontrar un error iniciando la base de datos
	 */
	public void iniciliazarBd() throws SQLException {
		Statement s = conexion.createStatement();

		boolean crearTabla = false;
		try {
			s.executeQuery("SELECT * FROM usuarios WHERE 1=2");
			s.executeUpdate("UPDATE usuarios SET conectado = 'N'");
			s.executeQuery("SELECT * FROM correos WHERE 1=2");
		} catch (SQLException se) {
			// La excepción se lanza si la tabla no existe
			crearTabla = true;
		}

		// Se crea una nueva tabla vacía
		if (crearTabla)
		{
			
			String sqlUsuarios = "create table usuarios(usuario varchar(32),nombre varchar(50),apellidos varchar(50),pwd varchar(12),total_correos integer,total_correos_sin_leer integer,conectado varchar(1), PRIMARY KEY(usuario))";
			String sqlCorreos = "create table correos(usuario varchar(32),usuarioDestinatario varchar(32),fechaEnvio varchar(20),asunto varchar(140),mensaje varchar(512), leido varchar(1), PRIMARY KEY(usuarioDestinatario,fechaEnvio,asunto))";
            s.execute( sqlUsuarios );
            s.execute( sqlCorreos );
           }														  

		s.close();
		verificarInvariante();
	}

	/**
	 * Conecta el administrador a la base de datos
	 * @throws SQLException Se lanza esta excepción si hay problemas realizando la
	 *  operación
	 * @throws Exception Se lanza esta excepción si hay problemas con los
	 *  controladores
	 */
	public void conectarABD() throws SQLException, Exception {
		String driver = config.getProperty("admin.db.driver");
		Class.forName(driver).newInstance();

		String url = config.getProperty("admin.db.url");
		conexion = DriverManager.getConnection(url);
		verificarInvariante();
	}

	/**
	 * Desconecta el administrador de la base de datos y la detiene
	 * @throws SQLException Se lanza esta excepción si hay problemas realizando la
	 *  operación
	 */
	public void desconectarBD() throws SQLException {
		conexion.close();
		String down = config.getProperty("admin.db.shutdown");
		try {
			DriverManager.getConnection(down);
		} catch (SQLException e) {
			// Al bajar la base de datos se produce siempre una excepción
		}
		verificarInvariante();
	}
	
	public void borrarBD()
	{
		try{
			Statement st = conexion.createStatement();
			String sql1= "Drop table usuarios";
			String sql2= "Drop table correos";
			st.execute(sql1);
			st.execute(sql2);
			st.close();
		}
		catch(Exception e)
		{
			//no hace nada
		}
	}
	
	/**
     * Método que ejecuta una actualización en la base de datos
     * @param sql El código sql de actualización. sql != null y sql != "" 
     * @param params La lista de parámetros para ejecutar la actualización. params != null
     * @throws SQLException En caso de encontrar un error
     */
    private void ejecutarActualizacion(String sql, String[] params)
            throws SQLException {
        PreparedStatement ps = conexion.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            String p = params[i];
            ps.setString(i + 1, p);
        }
        ps.executeUpdate();
    }

    /**
     * Método que ejecuta una consulta en la base de datos y retorna el
     * resultado
     * @param sql El código sql de la consulta. sql != null y sql != "" 
     * @param params La lista de parámetros para ejecutar la consulta. params != null
     * @throws SQLException En caso de encontrar un error
     */
    private ResultSet ejecutarConsulta(String consulta, String[] params)
            throws SQLException {
        PreparedStatement ps = conexion.prepareStatement(consulta);
        for (int i = 0; i < params.length; i++) {
            String p = params[i];
            ps.setString(i + 1, p);
        }
        return ps.executeQuery();
    }

	/**
	 * Registra un correo electrónico con los datos dados por parámetro
	 * @param usuario Nombre de usuario que envía en correo. usuario != null y usuario != ""
	 * @param usuarioDestinatario Nombre de usuario destinatario del correo. usuarioDestinatario != null y usuarioDestinatario != ""
	 * @param fechaEnvio Fecha de envío del correo. dechaEnvio !0 null y fechaEnvio != ""
	 * @param asunto Asunto del correo. asunto != null y asunto != ""
	 * @param mensaje Texto del correo. mensaje != null y mensaje != ""
	 * @throws CupiEmailException Se dispara esta excepción si el usuario destinatario no se encuentra registrado
	 * @throws Exception Se dispara una excepción si hay problemas realizando la operación 
	 */
	public void registrarCorreo(String usuario, String usuarioDestinatario, String fechaEnvio, String asunto, String mensaje) throws CupiEmailException, Exception 
	{
		String sqlVerificarUsuariop = "select * from usuarios where usuario=?";
		String[] paramsVerificacion = { usuarioDestinatario };
		
		if (!ejecutarConsulta(sqlVerificarUsuariop, paramsVerificacion).next())
		{
			throw new CupiEmailException("No existe el usuario :" + usuarioDestinatario);
		}
		
		String sql = "insert into correos(usuario,usuarioDestinatario,fechaEnvio, asunto,mensaje,leido) values(?,?,?,?,?,?)";
		String[] params = { usuario, usuarioDestinatario, fechaEnvio, asunto, mensaje, "N" };
		ejecutarActualizacion(sql, params);
		String[] paramsUpdate = { usuarioDestinatario };
		String sqlActualizarTotalCorreos = "update usuarios set total_correos = total_correos +1,total_correos_sin_leer=total_correos_sin_leer+1 where usuario = ? ";
		ejecutarActualizacion(sqlActualizarTotalCorreos, paramsUpdate);
	}

	/**
	 * Método que cambia el estado de usuario a conectado o desconectado
	 * @param nUsuario El nombre del usuario. nUsuario != null y nUsuario != ""
	 * @param estado El estado del usuario. Verdadero en caso de estar conectado o
	 *  falso en caso contrario
	 * @throws SQLException En caso de encontrar un error
	 */
	public void cambiarEstado(String nUsuario, boolean estado)
			throws SQLException {
		
		char t = estado? 'S': 'N';
        String sql = "UPDATE usuarios SET conectado = '" + t + "' WHERE usuario = '" + nUsuario + "'";

        Statement st = conexion.createStatement( );
        st.executeUpdate( sql );
        st.close( );
        verificarInvariante();
	}

	/**
	 * Registra un usuario en la base de datos
	 * @param usuario El nombre de usuario. usuario != null y usuario != ""
	 * @param nombre El nombre del usuario. nombre != null y nombre != ""
	 * @param apellidos Los apellidos del usuario. apellidos != null y apellidos != ""
	 * @param pwd La contraseña del usuario. pwd != null y pwd != ""
	 * @throws SQLException En caso de encontrar un error
	 */
	public void registrarUsuario(String usuario, String nombre,
			String apellidos, String pwd) throws SQLException {

        String insert = "INSERT INTO usuarios VALUES('"+usuario+"', '"+nombre+"', '"+apellidos+"', '"+pwd+"', 0, 0, 'N')";

        Statement st = conexion.createStatement( );
        st.execute(insert);	
         st.close( );
        verificarInvariante();
	}
	
	/**
     * Método que construye un usuario a partir de un resultset
     * @param rs El resultset. rs != null
     * @return El usuario construido
     * @throws SQLException En caso de encontrar un error
     */
    private Usuario construirUsuario(ResultSet rs) throws SQLException {

    	
    	String usuario = rs.getString(1);
        String nombre = rs.getString(2);
        String apellidos = rs.getString(3);
        String pwd = rs.getString(4);
        int totalCorreos = rs.getInt(5);
        
        int totalCorreosSinLeer = rs.getInt(6);
        return new Usuario(usuario, nombre, apellidos, pwd, totalCorreos,totalCorreosSinLeer);
    }

	/**
	 * Método que busca un usuario en la base de datos teniendo en cuenta su
	 * nombre de usuario y contraseña
	 * @param nUsuario El nombre del usuario. nUsuario != null y nUsuario != ""
	 * @param nPwd La contraseña del usuario. nPwd != null y nPwd != ""
	 * @return El usuario con toda su información, null si no es encontrado o si la combinación usuario password no es correcta.
	 * @throws SQLException En caso de encontrar un error
	 */
	public Usuario buscarUsuario(String nUsuario, String nPwd)
			throws SQLException {
	    
        String busca = "SELECT * FROM usuarios WHERE usuario = '"+ nUsuario+"'";
		 
        Statement st = conexion.createStatement( );
        ResultSet rs= st.executeQuery(busca);
        Usuario rta = null;
        if(rs.next())
        {
        String pwd = rs.getString(4);
        if(!pwd.equals(nPwd))
        	return null;        
        rta = construirUsuario(rs);
        }
        
        st.close( ); 
        return rta;

	
	}

	/**
	 * Método que busca un usuario en la base de datos teniendo en cuenta su
	 * nombre de usuario
	 * @param nUsuario El nombre del usuario. nUsuario != null y nUsuario != ""
	 * @return El usuario con toda su información, null si el usuario no es encontrado
	 * @throws SQLException En caso de encontrar un error
	 */
	public Usuario buscarUsuario(String nUsuario) throws SQLException {
	
        String busca = "SELECT * FROM usuarios WHERE usuario = '"+ nUsuario+"'";
		 
        Statement st = conexion.createStatement( );
        ResultSet rs= st.executeQuery(busca);
        
        Usuario rta = null;
        if(rs.next())
          rta = construirUsuario(rs);
        
        rs.close();
        st.close( ); 
        return rta;
	}

	

	/**
	 * Método que construye un correo a partir de un resultset
	 * @param rs El resultset. rs != null
	 * @return El correo construido
	 * @throws SQLException En caso de encontrar un error
	 */
	private CorreoElectronico construirCorreoElectronico(ResultSet rs)
			throws SQLException {
		String usuario = rs.getString(1);
		String fechaPublicacion = rs.getString(3);
		String asunto = rs.getString(4);
		String mensaje = rs.getString(5);
		String leido = rs.getString(6);
		return new CorreoElectronico(usuario, fechaPublicacion, asunto,
				mensaje, leido);
	}

	public void borrarCorreo(String usuario,String asunto,String fecha) throws SQLException 
	{
		
		Statement st = conexion.createStatement();
		String ver = "select * from correos where usuarioDestinatario ='"+usuario+"' and asunto = '"+asunto+"' and fechaenvio='"+fecha+"' and leido = 'N'";
		ResultSet rta = st.executeQuery(ver);
		
		if(rta.next())
		{
			String mnl = "Update usuarios set total_correos_sin_leer=total_correos_sin_leer-1 where usuario ='"+usuario+"'";
			st.executeUpdate(mnl);	
		}
		
				String sql = "Delete from correos where usuarioDestinatario ='"+usuario+"' and asunto = '"+asunto+"' and fechaenvio='"+fecha+"'";
		st.executeUpdate(sql);
		
		String sql2 = "Update usuarios set total_correos=total_correos-1 where usuario ='"+usuario+"'";
		st.executeUpdate(sql2);
		
	
		rta.close();
		st.close();
		
	}
	
	public void borrarTodosCorreos(String usuario) throws SQLException
	{
		String mnl = "Update usuarios set total_correos_sin_leer=0 where usuario ='"+usuario+"'";
		String sql = "Delete from correos where usuarioDestinatario ='"+usuario+"'";
		String sql2 = "Update usuarios set total_correos=0 where usuario ='"+usuario+"'";
		
		Statement st = conexion.createStatement();
		st.executeUpdate(mnl);
		st.executeUpdate(sql2);
		st.execute(sql);
		st.close();
	}

	/**
	 * Método que consulta los correos disponibles para un usuario
	 * @param nUsuario El usuario al que se le quiere consultar los correos. nUsuario != null y nUsuario != ""
	 * @return La lista de correos
	 * @throws SQLException En caso de encontrar un error
	 */
	public ArrayList consultarCorreosParaUsuario(String nUsuario)
			throws SQLException {
        
		String busca = "SELECT * FROM correos WHERE usuarioDestinatario = '"+ nUsuario+"'";
	    ArrayList correos = new ArrayList( );
	    Statement st = conexion.createStatement( );
	    ResultSet resultado = st.executeQuery( busca );
	    

	        while( resultado.next( ) )
	        {
	        	CorreoElectronico s = construirCorreoElectronico(resultado);
	            correos.add( s );
	        }
	       resultado.close();
        st.close( ); 
        return correos;


	}

	/**
	 * Método que consulta la lista de usuarios registrados
	 * @return La lista de usuarios
	 * @throws SQLException En caso de encontrar un error
	 */
	public ArrayList consultarUsuariosRegistrados() throws SQLException {

		String busca = "SELECT * FROM usuarios";
	    ArrayList usuarios = new ArrayList( );
	    Statement st = conexion.createStatement( );
	    ResultSet resultado = st.executeQuery( busca );

	        while( resultado.next( ) )
	        {
	        	Usuario s = construirUsuario(resultado);
	            usuarios.add( s );
	        }
	    resultado.close();
        st.close( ); 
        return usuarios;

	}

	/**
	 * Método que consulta la lista de usuarios conectados
	 * @return La lista de usuarios conectados
	 * @throws SQLException En caso de encontrar un error
	 */
	public ArrayList consultarUsuariosConectados() throws SQLException {
		
		String busca = "SELECT * FROM usuarios WHERE conectado = 'S'";
	    ArrayList usuarios = new ArrayList( );
	    Statement st = conexion.createStatement( );
	    ResultSet resultado = st.executeQuery( busca );

	        while( resultado.next( ) )
	        {
	        	Usuario s = construirUsuario(resultado);
	            usuarios.add( s );
	        }
	    resultado.close();
        st.close( ); 
        return usuarios;

	}

	/**
	 * Método que actualiza el estado de un correo
	 * @param nUsuario Nombre de usuario destinatario del correo. nUsuario != null y nUsuario != ""
	 * @param fecha La fecha del correo que se quiera actualizar. fecha != null y fecha != ""
	 * @param asunto El asunto del correo que se quiere actualiza. asunto != null y asunto != ""
	 * @param estado El nuevo estado del correo. estado != null y estado != "" && estado == S || estado == N
	 * @throws SQLException En caso de encontrar un error
	 */
	public void actualizarEstado(String nUsuario, String fecha, String asunto, String estado) throws SQLException 
	{
		String sql1 = "SELECT leido FROM correos WHERE usuarioDestinatario = '"+ nUsuario+"' and fechaEnvio = '"+ fecha+ "' and asunto = '"+asunto+"'";
		
		Statement st = conexion.createStatement( );
        ResultSet rta = st.executeQuery( sql1 );
        String estadoActual = null;
        if(!rta.next())
        {
        	rta.close();
            st.close( );
            return;
        }
        
        estadoActual = rta.getString(1);
        String accion = null;
        if(!estadoActual.equalsIgnoreCase(estado) && estadoActual.equalsIgnoreCase("S"))
        {
        	 accion = "UPDATE usuarios SET total_correos_sin_leer = total_correos_sin_leer+1 WHERE usuario = '" + nUsuario + "'";
        }
        else if(!estadoActual.equalsIgnoreCase(estado) && estadoActual.equalsIgnoreCase("N"))
        {
        	 accion = "UPDATE usuarios SET total_correos_sin_leer = total_correos_sin_leer-1 WHERE usuario = '" + nUsuario + "'";
        }
        
        if(accion != null)
        {
            st.executeUpdate( accion);
        }
        
        String sql = "UPDATE correos SET leido = '" + estado + "' WHERE usuarioDestinatario = '" + nUsuario + "' and fechaEnvio = '" + fecha+ "' and asunto = '" + asunto + "'";
        st.executeUpdate( sql);
        rta.close();
        st.close( );
        verificarInvariante();  
	
	}
	
	public void borrarUsuario(String usuario) throws SQLException
	{
		
		Statement st = conexion.createStatement();
		String sql = "Delete from usuarios where usuario = '"+ usuario+"'";
		st.executeUpdate(sql);
		st.close();
	}
	

	// -----------------------------------------------------------------
	// Invariante
	// -----------------------------------------------------------------
	/**
	 * Verifica el invariante de la clase <br>
	 * <b>inv:</b><br>
	 * config!=null <br>
	 */
	private void verificarInvariante() {
		assert config != null : "Conjunto de propiedades inválido";
	}

}