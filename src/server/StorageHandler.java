package server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import common.Card;
import common.User;
import common.Workspace;

/**
 * Clase que se encarga del manejo de la persistencia
 */
public class StorageHandler {
	

	// -----------------------------------------------------------------
	// Constantes
	// -----------------------------------------------------------------
	
	/**
	 * Constante que representa la ruta en donde esta la base de datos
	 */
	private static final String DB_PATH ="./data";	
	
	// -----------------------------------------------------------------
	// Atributos
	// -----------------------------------------------------------------

	/**
	 * Conexión a la base de datos
	 */
	private Connection connection;

	// -----------------------------------------------------------------
	// Constructores
	// -----------------------------------------------------------------

	/**
	 * Construye un nuevo manejador de persistencia
	 * @param nProperties La configuración para la conexión a la base de datos. nProperties != null
	 * @throws Exception En caso de encontrar un error
	 */
	public StorageHandler() throws Exception {
		
		File data = new File(DB_PATH);
		System.setProperty("derby.system.home", data.getAbsolutePath());
		connectToDB();
		iniciliazarBd();
	}

	// -----------------------------------------------------------------
	// Métodos Admin DataBase
	// -----------------------------------------------------------------

	/**
	 * Método que inicializa la la base de datos
	 * @throws SQLException En caso de encontrar un error iniciando la base de datos
	 */
	public void iniciliazarBd() throws SQLException {
		Statement s = connection.createStatement();

		boolean crearTabla = false;
		try {
			s.executeQuery("SELECT * FROM users");
			s.executeQuery("SELECT * FROM cards");
			s.executeQuery("SELECT * FROM workspaces");
			s.executeQuery("SELECT * FROM user_card");
			s.executeQuery("SELECT * FROM workspace_user");
			s.executeQuery("SELECT * FROM workspace_card");
			
		} catch (SQLException se) {
			// La excepción se lanza si alaguna tabla no existe
			crearTabla = true;
		}
		// Se crea una nueva tabla vacía
		if (crearTabla)		{
			
			String sqlUsers = "create table users(id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),username varchar(32) UNIQUE NOT NULL,name varchar(50),password varchar(24),email varchar(32), PRIMARY KEY(id))";
			String sqlCards = "create table cards(id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), name varchar(32),description varchar(50),category varchar(30),place varchar(30),imageURL varchar(100),owner varchar(32) CONSTRAINT owner_fk REFERENCES users(username), PRIMARY KEY(id))";
			String sqlWorkspaces = "create table workspaces(id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),chat_history varchar(1000),status varchar(30),PRIMARY KEY(id))";
			String sqlUserCard = "create table user_card(username varchar(32) CONSTRAINT user_fk REFERENCES users(username),id_card integer CONSTRAINT card_fk REFERENCES cards(id), PRIMARY KEY (username,id_card))";
			String sqlWorkCard ="create table workspace_card(id_workspace integer CONSTRAINT work_card_fk REFERENCES workspaces(id), id_card integer CONSTRAINT card_work_fk REFERENCES cards(id),status varchar(15),added_date timestamp,count_votes integer, PRIMARY KEY(id_workspace,id_card))";
			String sqlWorkUser="create table workspace_user(id_workspace integer CONSTRAINT work_user_fk REFERENCES workspaces(id), username varchar(32) CONSTRAINT user_work_fk REFERENCES users(username), PRIMARY KEY (id_workspace,username))";
			
			
            s.execute( sqlUsers );
            s.execute( sqlCards );
            s.execute( sqlWorkspaces );
            s.execute( sqlUserCard);
            s.execute( sqlWorkCard );
            s.execute( sqlWorkUser );
            
            System.out.println("Database created");
           }														  

		s.close();
	}

	/**
	 * Conecta el administrador a la base de datos
	 * @throws SQLException Se lanza esta excepción si hay problemas realizando la
	 *  operación
	 * @throws Exception Se lanza esta excepción si hay problemas con los
	 *  controladores
	 */
	public void connectToDB() throws SQLException, Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		connection = DriverManager.getConnection("jdbc:derby:CardsGameDB;create=true");
	}

	/**
	 * Desconecta el administrador de la base de datos y la detiene
	 * @throws SQLException Se lanza esta excepción si hay problemas realizando la
	 *  operación
	 */
	public void desconectarBD() throws SQLException {
		connection.close();
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			// Al bajar la base de datos se produce siempre una excepción
		}
	}
	
	// -----------------------------------------------------------------
	// Métodos
	// -----------------------------------------------------------------

	/**
	 * Metodo que registra un nuevo usuario en el sistema
	 * @param name
	 * @param username
	 * @param password
	 * @param email
	 * @return true en caso de que se haya registrado correctamente, false de lo contrario
	 * @throws Exception en caso de que ya existe un usuario con el login ingresado
	 */
	public boolean registerUser(String name, String username, String password, String email)throws Exception
	{
		String query = "select * from users where username='"+username+"'";

		Statement st;
		try {
			st = connection.createStatement();		
			ResultSet rs = st.executeQuery(query);
			//Se valida que no exista un usuario con el mismo username
			if(rs.next())
			{
				throw new Exception("Ya existe un usuario registrado con ese login.");
			}else
			{
				rs.close();
				String sql = "insert into users(name,username,password,email) values('"+name+"','"+username+"','"+password+"','"+email+"')";
				st.executeUpdate(sql);
				st.close();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;	
	}
	
	/**
	 * Metodo que actualiza la información de un usuario
	 * @param password
	 * @param email
	 * @return true en caso exitoso, false de lo contrario
	 */
	public boolean updateUser(String password, String email)
	{
		//TODO
		return false;
	}
	
	/**
	 * Método que elimina un usuario dado su login
	 * @param username
	 * @return true en caso exitoso, false de lo contrario
	 */
	public boolean deleteUser(String username)
	{
		//TODO
		return false;
	}
	
	/**
	 * Método que permite iniciar sesión aun usuario dado su usuario y su contraseña
	 * @param username
	 * @param password
	 * @return true en caso de que las credenciales sean correctas, false de lo contrario
	 * @throws Exception en caso de que no exista el usuario o la contraseña sea incorrecta
	 */
	public boolean logIn(String username, String password)throws Exception
	{
		String query = "Select password from users where username= '"+username+"'";
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			//Se valida que exista el usuario
			if(rs.next())
			{
				String pwd = rs.getString(1);
				//Se valida que la constraseña enviada corresponda a la misma almacenada
				if(pwd.equals(password))
				{
					st.close();
					return true;
				}else
				{
					throw new Exception("Contraseña incorrecta");
				}
			}else
			{
				throw new Exception("No existe un usuario registrado con ese login");
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return false;
	}
	
	/**
	 * Método que retorna un Workspace dado su Id
	 * @param id
	 * @return el workspace esperado
	 */
	public Workspace getWorkspace(int id)
	{		
		String query2 = "select * from workspaces where id="+id;
		
		Statement st;
		try {
			st = connection.createStatement();
			ResultSet rs = st.executeQuery(query2);
			if(rs.next())
			{
				//Se arma cada workspace con sus usaurios y cartas
				String chatHistory = rs.getString(2);
				String status = rs.getString(3);
				ArrayList<Card> proposed = getProposedCardsWork(id);
				ArrayList<Card> accepted = getPlayedCardsWork(id);
				ArrayList<User> users = getUsersWork(id);

				Workspace work = new Workspace(id, chatHistory, users, accepted, proposed);
				st.close();
				return work;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Retorna los workspaces activos en los que participa un usuario
	 * @param username del usuario buscado
	 * @return Arreglo con los workspaces en los que el usuario participa
	 */
	public ArrayList<Workspace> getUserWorkspaces(String username)
	{		
		ArrayList<Workspace> works = new ArrayList<Workspace>();
		//Se obtiene los ids de los workspaces en donde el usuario participa
		String query1 ="Select id_workspace from workspace_user where username='"+username+"' ";
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query1);
			//Por cada id se arma el worskpace
			while(rs.next())
			{
				int id = rs.getInt(1);
				String query2 = "select * from workspaces where id="+id +" AND status = '"+Workspace.ACTIVO+"'";
				Statement st2 = connection.createStatement();
				ResultSet rs2 = st2.executeQuery(query2);
				if(rs2.next())
				{
					//Se arma cada workspace con sus usaurios y cartas
					String chatHistory = rs2.getString(2);
					String status = rs2.getString(3);
					ArrayList<Card> proposed = getProposedCardsWork(id);
					ArrayList<Card> accepted = getPlayedCardsWork(id);
					ArrayList<User> users = getUsersWork(id);
					
					Workspace work = new Workspace(id, chatHistory, users, accepted, proposed);
					works.add(work);
				}	
				st2.close();
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return works;
	}
	
	/**
	 * Retorna los usuarios que partenecen a un workspace
	 * @param workspaceid
	 * @return Arreglo con los usuarios participantes en un workspace
	 */
	public ArrayList<User> getUsersWork(int workspaceid)
	{
		ArrayList<User> users = new ArrayList<User>();
		String query ="select users.* from users,workspace_user where workspace_user.username=users.username AND workspace_user.id_workspace="+workspaceid;
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
			{
				int id = rs.getInt(1);
				User user = new User(id,rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5), new ArrayList<Card>());
				users.add(user);
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}
	
	/**
	 * Retorna las cartas que han sido propuestas en un workspace pero q no han sido aceptadas
	 * @param workspaceId
	 * @return Arreglo con las cartas propuestas de un workspace
	 */
	public ArrayList<Card> getProposedCardsWork(int workspaceId)
	{
		ArrayList<Card> cards = new ArrayList<Card>();
		String query = "select cards.*,workspace_card.count_votes from cards,workspace_card where cards.id = workspace_card.id_card AND workspace_card.status='"+Card.PROPOSED+"' AND workspace_card.id_workspace="+workspaceId+" order by workspace_card.added_date";
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
			{
				Card card = new Card(rs.getInt(1),rs.getString(2),rs.getString("description"),rs.getString("imageURL"),rs.getString("category"),rs.getString("place"), rs.getString("owner"), rs.getInt("count_votes"));
				cards.add(card);
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return cards;
	}
	
	/**
	 * Retorna las cartas que han sido propuestas en un workspace y que han sido aceptadas
	 * @param workspaceId
	 * @return Arreglo con las cartas jugadas de un workspace
	 */
	public ArrayList<Card> getPlayedCardsWork(int workspaceId)
	{
		ArrayList<Card> cards = new ArrayList<Card>();
		String query = "select cards.*,workspace_card.count_votes from cards,workspace_card where cards.id = workspace_card.id_card AND workspace_card.status='"+Card.ACCEPTED+"' AND workspace_card.id_workspace="+workspaceId+" order by workspace_card.added_date";
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
			{
				Card card = new Card(rs.getInt(1),rs.getString(2),rs.getString("description"),rs.getString("imageURL"),rs.getString("category"),rs.getString("place"), rs.getString("owner"), rs.getInt("count_votes"));
				cards.add(card);
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return cards;
	}
	
	/**
	 * Método que permite agregar una carta al sistema
	 * @param name
	 * @param description
	 * @param imageURL Url de la imagen asociada a la carta
	 * @param category
	 * @param place
	 * @param owner Usuario creador de la carta
	 * @return True en caso de que se cree la carta exitosamente, false de lo contrario.
	 */
	public boolean createCard(String name, String description, String imageURL, String category, String place, String owner)
	{
		String sql = "insert into cards(name,description,category,place,imageURL,owner) values('"+name+"','"+description+"','"+category+"','"+place+"','"+imageURL+"','"+owner+"')";
		try {
			PreparedStatement st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			st.executeUpdate();
			st.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Método que crea un workspace a partir de la lista de usuarios participantes
	 * @param users Arreglo con los nombre de usuario de los jugadores
	 * @return el Workspace creado
	 */
	public Workspace createWorkspace(ArrayList<String> usernames)
	{
		String sql = "insert into workspaces(chat_history,status) values ('','"+Workspace.ACTIVO+"')";
		try {
			PreparedStatement st = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			st.executeUpdate();			
			ResultSet generatedKeys = st.getGeneratedKeys();
	        if (generatedKeys.next()) {
	        	//Se obtiene el id del workspace recien creado
	            int workId = generatedKeys.getInt(1);
	            st.close();
	            //Se crean las asociaciones de usuario-workspace
	            ArrayList<User> users = getUsersArray(usernames);
	            for(String login: usernames)
	            {
	            	String query = "insert into workspace_user(username,id_workspace)values('"+login+"',"+workId+")";
	            	Statement st2 = connection.createStatement();
	            	st2.executeUpdate(query);
	            	st2.close();
	            }
	            //Se crea instancia del workspace recien creado
	            Workspace work = new Workspace(workId, "", users, new ArrayList<Card>(), new ArrayList<Card>());
	            st.close();	            
	            return work;
	        }
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	
	/**
	 * Obtiene el arreglo de Usuario (User) a partir de un arreglo de strings con sus username
	 * @param usernames
	 * @return Arreglo con los usuarios buscados
	 */
	public ArrayList<User> getUsersArray(ArrayList<String> usernames)
	{
		ArrayList<User> users = new ArrayList<User>();
		for(String us :usernames)
		{
			String query = "select * from users where username='"+us+"'";
			Statement st;
			try {
				st = connection.createStatement();
				ResultSet rs = st.executeQuery(query);
				while(rs.next())
				{
					int id = rs.getInt(1);
					User user = new User(id,rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5), new ArrayList<Card>());
					users.add(user);
				}		
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return users;
	}
	
	/**
	 * Método que permite realizar un voto sobre una carta en un workspace
	 * @param workspaceId 
	 * @param cardId
	 * @return el numero de votos que tiene esa carta en el workspace
	 */
	public int voteCard(int workspaceId, int cardId)
	{
		String sql = "update workspace_card set count_votes = count_votes +1 where id_workspace="+workspaceId+" AND id_card = "+cardId;
		try {
			Statement st = connection.createStatement();
			st.executeUpdate(sql);
			String query =  "select count_votes from workspace_card where id_card="+cardId+" AND id_workspace="+workspaceId;
			ResultSet rs = st.executeQuery(query);
			if(rs.next())
			{
				int votes = rs.getInt(1);
				st.close();
				return votes;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return -1;
	}
	
	/**
	 * Método que agrega una carta a la baraja de un usuario
	 * @param username
	 * @param cardId
	 * @return true en caso de que se agregue sin problema, false de lo contrario
	 */
	public boolean addCardToDeck(String username, int cardId)
	{
		String sql = "insert into user_card(username,id_card)values('"+username+"',"+cardId+")";
		try {
			Statement st = connection.createStatement();
			st.executeUpdate(sql);
			st.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Método que elimina una carta de la baraja de un usuario
	 * @param username
	 * @param cardId
	 * @return true en caso de que se elimine sin problema, false de lo contrario
	 */
	public boolean removeCardfromDeck(String username,int cardId)
	{
		String sql = "delete from user_card where username='"+username+"' AND id_card="+cardId;
		try {
			Statement st = connection.createStatement();
			st.executeUpdate(sql);
			st.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return false;
	}
	
	/**
	 * Método que permite proponer una carta en un workspace
	 * @param cardId
	 * @param workspaceId
	 * @return true en caso de que se realice de manera exitosa, false de lo contrario
	 */
	public boolean proposeCard(int cardId, int workspaceId)
	{
		//Se crea el timestamp del momento en q se propone la carta
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		String sql = "insert into workspace_card(id_card,id_workspace,count_votes,added_date,status)values("+cardId+","+workspaceId+",0,'"+ts+"','"+Card.PROPOSED+"')";
		try {
			Statement st = connection.createStatement();
			st.executeUpdate(sql);
			st.close();
			return true;
		} catch (SQLException e) {
		
		}
		
		return false;
	}
	
	/**
	 * Método que obtiene la información de un usuario
	 * @param username
	 * @return objeto User con los datos de usuario
	 */
	public User getUser(String username)
	{
		String query ="Select * from users where username='"+username+"'";
		try {
			
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			if(rs.next())
			{
				int id = rs.getInt(1);
				String name = rs.getString(3);
				String email = rs.getString(5);
				ArrayList<Card> deck = getUserDeck(username);				
				User us = new User(id,username,name,"",email, deck);
				st.close();
				return us;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Método que obtiene la baraja de un usuario
	 * @param username
	 * @return Arreglo con las cartas que hacen parte de la baraja del usuario
	 */
	public ArrayList<Card> getUserDeck(String username)
	{
		ArrayList<Card> deck = new ArrayList<Card>();
		String query = "select cards.* from cards,user_card where cards.id = user_card.id_card AND user_card.username ='"+username+"'";
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
			{
				Card card = new Card(rs.getInt(1),rs.getString(2),rs.getString("description"),rs.getString("imageURL"),rs.getString("category"),rs.getString("place"), rs.getString("owner"), 0);
				deck.add(card);
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return deck;
	}	
	/**
	 * Método que obtiene la información de una carta dado su Id
	 * @param id
	 * @return objeto Card con la información de la carta buscada
	 */
	public Card getCard(int id)
	{
		String query = "select * from cards where id="+id;
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			if(rs.next()){
				Card card = new Card(id,rs.getString(2),rs.getString("description"),rs.getString("imageURL"),rs.getString("category"),rs.getString("place"), rs.getString("owner"), 0);
				st.close();
				return card;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	/**
	 * Metodo que actualiza el cht del workspace
	 * @param newMessage
	 * @param workspaceId
	 * @return
	 */
	public boolean updateChatHistory(String newMessage,int workspaceId)
	{
		String query = "select chat_history from workspaces where id ="+workspaceId;
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			if(rs.next())
			{
				String chat = newMessage + "\n\r" + rs.getString(1);
				if(chat.length()>950)
				{
					chat=chat.substring(0, 950);
				}
				
				
				String update ="update workspaces set chat_history='"+chat+"' where id ="+workspaceId;
				st.executeUpdate(update);
				st.close();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Método que devuelve todas las cartas que un usuario puede agregar a su baraja, es decir las cartas del 
	 * sistema y las que han sido creadas por el mismo usuario
	 * @param username
	 * @return arreglo con las cartas qie puede agregar
	 */
	public ArrayList<Card> getCards(String username)
	{
		ArrayList<Card> cards = new ArrayList<Card>();
		String query = "Select * from cards where owner ='"+username+"' OR owner = 'SISTEMA'";
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
			{
				Card card = new Card(rs.getInt(1),rs.getString(2),rs.getString("description"),rs.getString("imageURL"),rs.getString("category"),rs.getString("place"), rs.getString("owner"), 0);
				cards.add(card);
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return cards;
	}
	
	/**
	 * Método que obtiene un workspace a partir de los usuarios que participan en el
	 * @param usernames 
	 * @return el workspace con los usuarios, o null si no existe un workspace con estos usuarios.
	 */
	public Workspace getWorkspaceFromUsers(ArrayList<String> usernames)
	{
		StringBuffer set = new StringBuffer("(");
		for(int i =0; i < usernames.size();i++)
		{
			String username = usernames.get(i);
			if(i == usernames.size()-1)
				set.append("'"+username+"'");
			else
				set.append("'"+username+"',");				
		}
		set.append(")");
		//Busco el id del workspace en el que participan todos los usuarios
		String sql = "select result.id_workspace from (select workspace_user.id_workspace,count(username) as veces from workspace_user where username IN "+set.toString()+" group by id_workspace) as result where result.veces ="+usernames.size();
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(sql);
			
			
			ArrayList<Workspace> posibleWorkspace = new ArrayList<Workspace>();
			
			
			while(rs.next())
			{
				int workID = rs.getInt(1);
				posibleWorkspace.add(getWorkspace(workID));
			}
			rs.close();
			st.close();
			
			//valido que workspace es el correcto
			for (int i = 0; i < posibleWorkspace.size(); i++) {
				
				Workspace tmp = posibleWorkspace.get(i);
				ArrayList<User> usuarios = tmp.getUsers();
				
				ArrayList<String> usuariosS = new ArrayList<String>();
				for (int j = 0; j < usuarios.size(); j++) 
				{
				usuariosS.add(usuarios.get(i).getUsername());
				}		 
				
				if(usernames.containsAll(usuariosS) && usuariosS.containsAll(usernames))
				{
					return tmp;
				}
				
			}
			return null;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Método que cambia el estado de Propuesta a Aceptada de una carta en un workspace
	 * @param cardId
	 * @param workspaceId
	 * @return
	 */
	public boolean playCard(int cardId, int workspaceId)
	{
		String query = "update workspace_card set status='"+Card.ACCEPTED+"' where id_workspace="+workspaceId+" AND id_card="+cardId;
		try {
			Statement st = connection.createStatement();
			st.executeUpdate(query);
			st.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Método que inactiva el workspace en el q participa  un usuario.
	 * @param workspaceId
	 * @return true en caso de realizarlo exitosamente.
	 */
	public boolean setInactiveWorkspace(int workspaceid)
	{			
		String sentence = "update workspaces set status = '"+Workspace.INACTIVO+"' where id ="+workspaceid;
		Statement st2;
		try {
			st2 = connection.createStatement();
			st2.executeUpdate(sentence);
			st2.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	/**
	 * Método que inactiva el workspace en el q participa  un usuario.
	 * @param workspaceId
	 * @return true en caso de realizarlo exitosamente.
	 */
	public boolean setActiveWorkspace(int workspaceid)
	{			
		String sentence = "update workspaces set status = '"+Workspace.ACTIVO+"' where id ="+workspaceid;
		Statement st2;
		try {
			st2 = connection.createStatement();
			st2.executeUpdate(sentence);
			st2.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}