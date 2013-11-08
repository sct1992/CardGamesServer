package common;

import java.util.ArrayList;

public class Workspace {

	
	private int id;
	
	private String date;
	
	private String chat;
	
	private ArrayList<User> users;
	
	private ArrayList<Card> playedCards;
	
	private ArrayList<Card> proposedCards;

	
	

	public Workspace(int id, String date, String chat, ArrayList<User> users,
			ArrayList<Card> playedCards, ArrayList<Card> proposedCards) {
		super();
		this.id = id;
		this.date = date;
		this.chat = chat;
		this.users = users;
		this.playedCards = playedCards;
		this.proposedCards = proposedCards;
	}
	
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public ArrayList<User> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<User> users) {
		this.users = users;
	}

	public ArrayList<Card> getPlayedCards() {
		return playedCards;
	}

	public void setPlayedCards(ArrayList<Card> playedCards) {
		this.playedCards = playedCards;
	}

	public ArrayList<Card> getProposedCards() {
		return proposedCards;
	}

	public void setProposedCards(ArrayList<Card> proposedCards) {
		this.proposedCards = proposedCards;
	}

	public String getChat() {
		return chat;
	}

	public void setChat(String chat) {
		this.chat = chat;
	}
	
	public String toString()
	{
		return id + " - " + users.toString();
	}
	
	
	
	
}
