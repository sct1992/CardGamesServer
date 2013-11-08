package common;

import java.util.ArrayList;

public class User {

	
	private int id;
	
	private String username;
	
	private String name;
	
	private String password;
	
	private String email;
	
	private ArrayList<Card> myDeck;

	public User(int id, String username, String name, String password,
			String email, ArrayList<Card> myDeck) {
		super();
		this.id = id;
		this.username = username;
		this.name = name;
		this.password = password;
		this.email = email;
		this.myDeck = myDeck;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ArrayList<Card> getMyDeck() {
		return myDeck;
	}

	public void setMyDeck(ArrayList<Card> myDeck) {
		this.myDeck = myDeck;
	}
	
	
}
