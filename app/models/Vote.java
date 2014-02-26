package models;

import java.util.ArrayList;
import java.util.List;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Vote extends Model {
	@Id
	private long id;
	
	@ManyToOne
	private Option parent;
	
	private String ipAddress;
	
	private boolean valid = true;
	
	/**
   * Constructor for Option.
   * @param text Takes in default String.
   * @param parent The utterance that this option belongs to.
   */
  public Vote(String ipAddress, Option parent) {
      this.ipAddress = ipAddress;
      this.parent = parent;
  }
	
	/**
   * Finder for votes, using the IP
   */
  public static Finder<Long, Vote> find = new Finder<Long, Vote>(Long.class, Vote.class);
  
  /**
   * Static helper for creating a vote and saving it in the database.
   * @param ip The IP to initialize the option with.
   * @return The option that is created.
   */
  public static Vote create(String ip, Option parent) {
      Vote newVote = new Vote(ip, parent);
      newVote.save();
      return newVote;
  }
  
  /**
   * Finding the votes from a given IP
   * crawl through all votes in the database and find the ones voted by this IP
   */
  public static List<Vote> findByIP(String ipAddress) {
  	List<Vote> votes = Vote.find.all();
  	List<Vote> votesByIP = new ArrayList<Vote>();
  	for(Vote vote: votes) {
  		if(vote.getIP().equals(ipAddress)) {
  			votesByIP.add(vote);
  		}
  	}
  	return votesByIP;
  }
  
  /**
   * change the Option a client wants to vote to
   */
  public void changeVote(Option newOption) {
  	this.parent = newOption;
  	this.save();
  }
  
  /**
   * setting the valid flag of this vote
   * @param setToMe
   */
  public void setValid(boolean setToMe) {
  	this.valid = setToMe;
  	this.save();
  }
  
  /**
   * getter for ipAddress
   */
  public String getIP() {
  	return this.ipAddress;
  }
  
  /**
   * getting parent of this vote
   */
  public Option getOption() {
  	return parent;
  }
  
  public boolean getValid() {
  	return this.valid;
  }


}
