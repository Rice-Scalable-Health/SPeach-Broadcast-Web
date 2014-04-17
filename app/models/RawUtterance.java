package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Class that represents a raw utterance with text and confidence score.
 */
@Entity
public class RawUtterance extends Model {
    /**
     * The id in the database.
     */
    @Id
    private long id;

    /**
     * The text from the STT service.
     */
    @Column(columnDefinition = "TEXT")
    private String text;

    /**
     * The timestamp of the utterance.
     */
    @Column(columnDefinition = "TEXT")
    private String timestamp;

    /**
     * Confidence score from the STT service.
     */
    @Column(columnDefinition = "NUMERIC")
    private BigDecimal confidence;

    /**
     * Finder for RawUtterance's
     */
    public static Finder<Long, RawUtterance> find = new Finder<Long, RawUtterance>(Long.class, RawUtterance.class);

    /**
     * Constructor for Raw Utterance.
     * @param text The text of the utterance, from a STT service.
     * @param confidence The confidence of the utterance, from a STT service.
     */
    public RawUtterance(String text, BigDecimal confidence) {
        this.timestamp = new Timestamp(new java.util.Date().getTime()).toString();
        this.text = text;
        this.confidence = confidence;
    }

    /**
     * Static method that writes all the utterances to a file.
     * @param filename The filename for the .csv file.
     */
    public static void WriteToFile(String filename) {
        List<RawUtterance> listOfUtterances = RawUtterance.find.all();
        PrintWriter writer;
        try {
            writer = new PrintWriter(filename+".csv", "UTF-8");
            for (int i = 0; i < listOfUtterances.size(); i++) {
                RawUtterance curUtter = listOfUtterances.get(i);
                writer.println(curUtter.timestamp+","+curUtter.text+","+curUtter.confidence);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Static constructor of a raw utterance. Handles database saving as well.
     * @param text The text to create the raw utterance with.
     * @param confidence The confidence to create the raw utterance with.
     * @return The newly created RawUtterance.
     */
    public static RawUtterance create(String text, BigDecimal confidence) {
        RawUtterance rawUtterance = new RawUtterance(text, confidence);
        rawUtterance.save();
        return rawUtterance;
    }
}
