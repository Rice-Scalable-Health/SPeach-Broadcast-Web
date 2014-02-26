package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Represents an option for an utterance.
 */
@Entity
public class Option extends Model {
    /**
     * Id of the option in the database.
     */
    @Id
    private long id;

    /**
     * The text corresponding to the option.
     */
    private String text;
    
    private long numOfVotes;

    /**
     * The utterance that this option belongs to.
     * can have multiple options to a parent.
     */
    @ManyToOne
    private Utterance parent;

    /**
     * Finder for options.
     */
    public static Finder<Long, Option> find = new Finder<Long, Option>(Long.class, Option.class);

    /**
     * Constructor for Option.
     * @param text Takes in default String.
     * @param parent The utterance that this option belongs to.
     * @param numOfVotes default votes when an option is created.
     */
    public Option(String text, Utterance parent) {
        this.text = text;
        this.parent = parent;
        this.numOfVotes = 0;
    }

    /**
     * Static helper for creating an option and saving it in the database.
     * @param text The text to initialize the option with.
     * @return The option that is created.
     */
    public static Option create(String text, Utterance parent) {
        Option newOption = new Option(text, parent);
        newOption.save();
        return newOption;
    }
    
    public Utterance getUtter() {
    	return parent;
    }
    
    /**
     * Getting vote counts
     */
    public long getVotes(){
    	return this.numOfVotes;
    }
    
    public long getId() {
    	return id;
    }
    
    /**
     * Adding the ability to save
     */
    public void edit(String toMe) {
    	this.text = toMe;
    	this.save();
    }
    
    /**
     * increment the vote count for this option
     */
    public void increment() {
    	this.numOfVotes++;
    	this.save();
    }
    
    public void decrement() {
    	this.numOfVotes--;
    	this.save();
    }
    
    /**
     * Changes the value of the option.
     * @param newText The value to replace the old with.
     */
    public void changeText(String newText) {
        this.text = newText;
        this.save();
    }
    
    /**
     * create a new vote
     */
    public void addVote(String ip){
    	Vote.create(ip, this);
    	increment();
    }
    
    
    public String toString() {
        return "\""+this.id+"\":{\"upvotes\":"+1+",\"text\":\""+this.text+"\"}";
    }
}
