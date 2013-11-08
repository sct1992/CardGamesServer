package server;
/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: readme.txt,v 1.1 2009/09/15 16:09:20 dav-vill Exp $
 * Universidad de los Andes (Bogotá - Colombia)
 * Departamento de Ingeniería de Sistemas y Computación 
 * Licenciado bajo el esquema Academic Free License version 2.1 
 *
 * Proyecto Cupi2 (http://cupi2.uniandes.edu.co)
 * Ejercicio: n6_elimiGemas
 * Autor: Juan David Villa - 31-ago-2009
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import common.Card;
import common.InterfaceServer;
import common.User;
import common.Workspace;



/**
 * Es la clase principal de la interfaz
 */
public class Server extends UnicastRemoteObject implements InterfaceServer
{
    //-----------------------------------------------------------------
    // Atributos
    //-----------------------------------------------------------------


	 //-----------------------------------------------------------------
    // Atributos de la Interfaz
    //-----------------------------------------------------------------
	
    //-----------------------------------------------------------------
    // Constructores
    //-----------------------------------------------------------------

    /**
     * Construye la interfaz e inicializa el juego indicando que no se ha cargado la información de ningún archivo
     * @throws RemoteException 
     */
    public Server( ) throws RemoteException
    {
    	super();
           
    }
    //-----------------------------------------------------------------
    // Puntos de Extensión
    //-----------------------------------------------------------------
	

	@Override
	public String darNombre() throws RemoteException {

		return "silva";
	}
	
	
	@Override
	public String darProducto() throws RemoteException {
		return "hola";
}
	
	
	
  //-----------------------------------------------------------------
    // Programa principal
    //-----------------------------------------------------------------

 
	
	
	
	   /**
     * Ejecuta la aplicación
     * @param args Estos parámetros no se usan.
     */
    public static void main( String[] args )
    {
    	try 
    	{
			Registry reg = LocateRegistry.createRegistry(1099);
			reg.rebind("server", new Server());
    		System.out.println("empeso esta mierda");
    		
		}
    	catch (Exception e) 
    	{	
    		// TODO: handle exception
		}
     
    }
    
	    
	@Override
	public boolean login(String user, String password) 
	{
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean signUp(String username, String name, String password,
			String email)
	{
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public User getUser() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Card getCard() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Workspace getWorkspace() 
	{
		// TODO Auto-generated method stub
		return null;
	}
}