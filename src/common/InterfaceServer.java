package common;
import java.rmi.Remote; 
import java.util.ArrayList;



public interface InterfaceServer extends Remote 
{ 
	public boolean login(String user, String password);
	
	public boolean signUp(String username, String name, String password, String email);
	
	public User getUser(String username);
	
	public Workspace getWorkspace(int id);
	
	public ArrayList<User> getActiveUsers(String username);
	
	public ArrayList<Card> getCards(String username);
	
	public ArrayList<Workspace> getMyWorkspaces( String username);
	
	public boolean createCard( String name, String description, String imageUrl, String place, String owner, String category);
	
	public boolean addCardToDeck( String username, int cardId);
	
	public boolean removeCardFromDeck(String username, int cardId);
	
	public Workspace startGame(String username, ArrayList<String> guests);
	
	public Workspace startGame(int cardId, String username, ArrayList<String> guests);
	
	public boolean proposeCard( int workspaceId, int cardId);
	
	public boolean voteCard( int workspaceId, int cardId);
	
	public boolean sendMessage( int workspaceId, String username, String message);
	
}