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

    /**
     * The utterance that this option belongs to.
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
     */
    public Option(String text, Utterance parent) {
        this.text = text;
        this.parent = parent;
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

    /**
     * Changes the value of the option.
     * @param newText The value to replace the old with.
     */
    public void changeText(String newText) {
        this.text = newText;
        this.save();
    }

    public String toString() {
        return "\""+this.id+"\":{\"upvotes\":"+1+",\"text\":\""+this.text+"\"}";
    }
}
