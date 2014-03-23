package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a shared transcript in the database.
 */
@Entity
public class SharedTranscript extends Model {

    /**
     * Id of the shared transcript in the database.
     */
    @Id
    private long id;

    /**
     * The list of utterances in the shared transcript.
     */
    @ManyToMany
    private List<Utterance> utteranceList = new ArrayList<Utterance>();

    /**
     * Finder for the SharedTranscript model.
     */
    public static Finder<Long, SharedTranscript> find = new Finder<Long, SharedTranscript>(Long.class, SharedTranscript.class);

    /**
     * Static method for encapsulating the creation and saving of a SharedTranscript.
     * @return A new Shared Transcript that is saved in the database.
     */
    public static SharedTranscript create() {
        SharedTranscript newText = new SharedTranscript();
        newText.save();
        return newText;
    }

    /**
     * Checks to see if a row exists in the shared transcript table.
     *
     * Method that will soon become legacy, because it operates on the assumption that there
     * is only one row in the SharedTranscript table.
     * @return Whether a row exists in the shared transcript table.
     */
    public static boolean sharedTextExists() {
        if (find.all().size() == 0 ) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Gets the list of utterances in this shared transcript.
     * @return The list of utterances.
     */
    public List<Utterance> getUtteranceList() {
        return this.utteranceList;
    }

    /**
     * Gets the JSON form of this SharedTranscript for client consumption.
     * @return The JSON form of this SharedTranscript.
     */
    public String toJSON() {
        List<Utterance> utteranceList = this.getUtteranceList();

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // go through each utterance and add it as an entry.
        for (int i = 0; i < utteranceList.size(); i++) {
            sb.append(utteranceList.get(i).toString());
            // if it is not the last element, add a comma.
            if (i < utteranceList.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("}");

        return sb.toString();
    }
//    public String toJSON() {
//        List<Utterance> sharedText = getUtteranceList();
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("[");
//
//        for (int i = 0; i < sharedText.size(); i++) {
//            sb.append(sharedText.get(i).toEscapedString());
//            // if it is the last element, don't add a comma.
//            if (i < sharedText.size() - 1) {
//                sb.append(",");
//            }
//        }
//        sb.append("]");
//
//        return sb.toString();
//    }

    /**
     * Gets this SharedTranscript in SSE + JSON form for client consumption.
     * @return Gets this SharedTranscript in SSE + JSON form for client consumption.
     */
    public String toSSEForm() {
        return toJSON();
    }

    /**
     * Adds new string to this SharedTranscript. Creates a new utterance in the database for this.
     * @param toAdd The text to add to the shared transcript.
     */
    public void addToSharedTranscript(String toAdd) {
        Utterance addedUtterance = Utterance.create(toAdd);
        this.utteranceList.add(addedUtterance);
        this.save();

        UpdateMessenger.singleton.tell("UPDATE", null);
    }

    /**
     * Blocks the utterance to be edited.
     * @param utteranceId The id of the utterance to block.
     * @param optionId The id of the option that needs to be blocked.
     */
    public void blockOption(int utteranceId, int optionId) {
        Utterance utteranceToChange = Utterance.find.byId((long) utteranceId);
        utteranceToChange.blockOption(optionId);

        UpdateMessenger.singleton.tell("UPDATE", null);
    }

    /**
     * Unblocks the utterance to be edited.
     * @param utteranceId The id of the utterance to unblock.
     * @param optionId The id of the option that needs to be unblocked.
     */
    public void unblockOption(int utteranceId, int optionId) {
        Utterance utteranceToChange = Utterance.find.byId((long) utteranceId);
        utteranceToChange.unblockOption(optionId);

        UpdateMessenger.singleton.tell("UPDATE", null);
    }

    /**
     * Changes the value of the shared transcript at an index.
     * @param utteranceId The id of the utterance that needs to be changed.
     * @param optionId The id of the option that needs to be changed.
     * @param newValue The value to change to.
     */
    public void modifySharedTranscript(int utteranceId, int optionId, String newValue) {
        Utterance utteranceToChange = Utterance.find.byId((long) utteranceId);
        utteranceToChange.changeText(optionId, newValue);

        UpdateMessenger.singleton.tell("UPDATE", null);
    }

    /**
     * For our demo, creates the only shared transcript if it does not exist and returns it.
     * @return The singular shared transcript in the database.
     */
    public static SharedTranscript getOnlySharedTranscript() {
        SharedTranscript ourText = SharedTranscript.find.byId((long)1);
        if (ourText == null) {
            SharedTranscript.create();
        }
        return SharedTranscript.find.byId((long)1);
    }

}
