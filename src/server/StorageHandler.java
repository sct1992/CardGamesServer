package server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
			String sqlCards = "create table cards(id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), name varchar(32),description varchar(50),category varchar(30),place varchar(30),imageURL varchar(100),owner integer CONSTRAINT owner_fk REFERENCES users(id), PRIMARY KEY(id))";
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
	 * 
	 * @param name
	 * @param username
	 * @param password
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public boolean registerUser(String name, String username, String password, String email)throws Exception
	{
		String query = "select * from users where username='"+username+"'";

		Statement st;
		try {
			st = connection.createStatement();		
			ResultSet rs = st.executeQuery(query);
			//Se valida que no exista un usuario con el mismo username
			if(!rs.next())
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;	
	}
	
	/**
	 * 
	 * @param password
	 * @param email
	 * @return
	 */
	public boolean updateUser(String password, String email)
	{
		return false;
	}
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	public boolean deleteUser(String username)
	{
		return false;
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public Workspace getWorkspace(int id)
	{		
		String query2 = "select * from workspace where id="+id;
		
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
				ArrayList<Card> accepted = getProposedCardsWork(id);
				ArrayList<User> users = getUsersWork(id);

				Workspace work = new Workspace(id, chatHistory, users, accepted, proposed);
				st.close();
				return work;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * @param username
	 * @return
	 */
	public ArrayList<Workspace> getUserWorkspaces(String username)
	{		
		ArrayList<Workspace> works = new ArrayList<Workspace>();
		//Se obtiene los ids de los workspaces en donde el usuario participa
		String query1 ="Select id_workspace from workspace_user where username='"+username+"'";
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query1);
			//Por cada id se arma el worskpace
			while(rs.next())
			{
				int id = rs.getInt(1);
				String query2 = "select * from workspace where id="+id;
				ResultSet rs2 = st.executeQuery(query2);
				if(rs.next())
				{
					//Se arma cada workspace con sus usaurios y cartas
					String chatHistory = rs2.getString(2);
					String status = rs2.getString(3);
					ArrayList<Card> proposed = getProposedCardsWork(id);
					ArrayList<Card> accepted = getProposedCardsWork(id);
					ArrayList<User> users = getUsersWork(id);
					
					Workspace work = new Workspace(id, chatHistory, users, accepted, proposed);
					works.add(work);
				}				
			}
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return works;
	}
	
	/**
	 * 
	 * @param workspaceid
	 * @return
	 */
	public ArrayList<User> getUsersWork(int workspaceid)
	{
		ArrayList<User> users = new ArrayList<User>();
		String query ="select users.* from users,workspace_user where workspace_user.id_user=users.id AND workspace_user.id_workspace="+workspaceid;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return users;
	}
	
	/**
	 * 
	 * @param workspaceId
	 * @return
	 */
	public ArrayList<Card> getProposedCardsWork(int workspaceId)
	{
		ArrayList<Card> cards = new ArrayList<>();
		String query = "select cards.*,workspace_card.count_votes from cards,workspace_card where cards.id = workspace_card.id_card AND workspace_card.status='"+Card.PROPOSED+"' AND workspace_card.id_workspace="+workspaceId;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cards;
	}
	
	/**
	 * 
	 * @param workspaceId
	 * @return
	 */
	public ArrayList<Card> getPlayedCardsWork(int workspaceId)
	{
		ArrayList<Card> cards = new ArrayList<>();
		String query = "select cards.*,workspace_card.count_votes from cards,workspace_card where cards.id = workspace_card.id_card AND workspace_card.status='"+Card.ACCEPTED+"' AND workspace_card.id_workspace="+workspaceId;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cards;
	}
	
	/**
	 * 
	 * @param name
	 * @param description
	 * @param imageURL
	 * @param category
	 * @param place
	 * @param owner
	 * @return
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 
	 * @param users
	 * @return
	 */
	public Workspace createWorkspace(ArrayList<String> usernames)
	{
		String sql = "insert into workspaces(chat_history,status) values ('','ACTIVE')";
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}
	
	/**
	 * 
	 * @param usernames
	 * @return
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return users;
	}
	
	/**
	 * 
	 * @param workspaceId
	 * @param cardId
	 * @return
	 */
	public boolean voteCard(int workspaceId, int cardId)
	{
		return false;
	}
	
	/**
	 * 
	 * @param username
	 * @param cardId
	 * @return
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 
	 * @param username
	 * @param cardId
	 * @return
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param cardId
	 * @param workspaceId
	 * @return
	 */
	public boolean proposeCard(int cardId, int workspaceId)
	{
		return false;
	}
	
	/**
	 * 
	 * @param rs
	 * @return
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
				return us;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param username
	 * @return
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return deck;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public Card getCard(int id)
	{
		String query = "select * from cards where id="+id;
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			if(rs.next()){
				Card card = new Card(id,rs.getString(2),rs.getString("description"),rs.getString("imageURL"),rs.getString("category"),rs.getString("place"), rs.getString("owner"), 0);
				return card;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}