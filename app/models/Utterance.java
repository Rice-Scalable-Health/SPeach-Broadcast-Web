package models;

import org.apache.commons.lang3.StringEscapeUtils;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a single utterance in a shared transcript.
 */
@Entity
public class Utterance extends Model {
    /**
     * Id of the utterance, in the database.
     */
    @Id
    private long id;

    /**
     * The list of options for this utterance.
     */
    @ManyToMany
    private List<Option> optionList = new ArrayList<Option>();

    /**
     * Finder for utterances.
     */
    public static Finder<Long, Utterance> find = new Finder<Long, Utterance>(Long.class, Utterance.class);

    /**
     * Constructor for utterance.
     */
    public Utterance() {
    }

    /**
     * Static helper that creates a new utterance and saves it.
     * @param text The text that the utterance should be initialized with.
     * @return The newly created utterance.
     */
    public static Utterance create(String text) {
        Utterance utterance = new Utterance();
        utterance.save();

        // init option
        Option option = Option.create(text, utterance);
        utterance.optionList.add(option);
        utterance.save();

        return utterance;
    }
    
    public long getId() {
    	return id;
    }

    /**
     * Returns a JSON String of the utterance with all its options..
     * @return A JSON String representing the utterance with all its options.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\"");
        sb.append(this.id);
        sb.append("\":{");
        for (int i = 0; i < optionList.size(); i++) {
            Option option = optionList.get(i);

            sb.append(option.toString());

            // if it is not the last element, add a comma.
            if (i < optionList.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("}");

        return sb.toString();
    }

//    /**
//     * Gets the utterance wrapped with quotation marks, with the inner text escaped.
//     * @return The String representation of the utterance with characters escaped.
//     */
//    public String toEscapedString() {
//        return "\"" + StringEscapeUtils.escapeEcmaScript(this.text) + "\"";
//    }
//
//    /**
//     * Gets the String of the utterance as a key:pair in a JSON dict.
//     * @return The String of the utterance as a key:pair entry in a JSON dict.
//     */
//    public String toJSONEntry() {
//        return "\""+this.id+"\":{\"upvotes\":"+1+",\"text\":"+this.toString()+"}";
//    }

    /**
     * Changes the value of the utterance.
     * @param optionId The index of the option to change.
     * @param newValue The value to replace the old with.
     */
    public void changeText(int optionId, String newValue) {
        Option optionToChange = Option.find.byId((long) optionId);
        optionToChange.changeText(newValue);
        optionToChange.save();
    }
}
