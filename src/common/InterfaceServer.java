package common;
import java.rmi.Remote; 



public interface InterfaceServer extends Remote 
{ 
	public boolean login(String user, String password);
	
	public boolean signUp(String username, String name, String password, String email);
	
	public User getUser();
	
	public Card getCard();
	
	public Workspace getWorkspace();
	
	
    public String darNombre () throws java.rmi.RemoteException;
    public String darProducto () throws java.rmi.RemoteException;
}