package common;

import java.io.Serializable;

public class Card implements Serializable{

	public static final String PROPOSED = "PROPOSED";

	public static final String ACCEPTED = "ACCEPTED";
	
	private int id;
	
	private String name;
	
	private String description;
	
	private String imageUrl;
	
	private String category;
	
	private String place;
	
	private String owner;
	
	private int votes;

	
	
	public Card(int id, String name, String description, String imageUrl,
			String category, String place, String owner, int votes) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.imageUrl = imageUrl;
		this.category = category;
		this.place = place;
		this.owner = owner;
		this.votes = votes;
	}

	public int getVotes() {
		return votes;
	}

	public void setVotes(int votes) {
		this.votes = votes;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String toString()
	{
		return id + " - " + name; 
	}	
	
}
