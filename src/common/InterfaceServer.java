package common;
import java.rmi.Remote; 
import java.util.ArrayList;



public interface InterfaceServer extends Remote 
{ 
	public boolean login(String user, String password) throws Exception;
	
	public boolean signUp(String username, String name, String password, String email) throws Exception;
	
	public User getUser(String username)throws Exception;
	
	public Card getCard(int id)throws Exception;
	
	public Workspace getWorkspace(int id)throws Exception; 
	
	public ArrayList<User> getActiveUsers(String username)throws Exception;
	
	public ArrayList<Card> getCards(String username)throws Exception;
	
	public ArrayList<Workspace> getMyWorkspaces( String username)throws Exception;
	
	public boolean createCard( String name, String description, String imageUrl, String place, String owner, String category) throws Exception;
	
	public boolean addCardToDeck( String username, int cardId)throws Exception;
	
	public boolean removeCardFromDeck(String username, int cardId)throws Exception;
	
	public boolean startGame(String username, ArrayList<String> guests)throws Exception;
	
	public boolean startGame(int cardId, String username, ArrayList<String> guests)throws Exception;
	
	public boolean proposeCard( int workspaceId, int cardId)throws Exception;
	
	public boolean voteCard( int workspaceId, int cardId)throws Exception;
	
	public boolean sendMessage( int workspaceId, String username, String message)throws Exception;

	public boolean acceptGame(String threadId, String username)throws Exception;

	public boolean rejectGame(String threadId, String username)throws Exception;
	
}