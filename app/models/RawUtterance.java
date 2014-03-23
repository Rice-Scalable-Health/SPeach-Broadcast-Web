package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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
     * Confidence score from the STT service.
     */
    private double confidence;

    /**
     * Finder for RawUtterance's
     */
    public static Finder<Long, RawUtterance> find = new Finder<Long, RawUtterance>(Long.class, RawUtterance.class);

    /**
     * Constructor for Raw Utterance.
     * @param text The text of the utterance, from a STT service.
     * @param confidence The confidence of the utterance, from a STT service.
     */
    public RawUtterance(String text, double confidence) {
        this.text = text;
        this.confidence = confidence;
    }

    /**
     * Static constructor of a raw utterance. Handles database saving as well.
     * @param text The text to create the raw utterance with.
     * @param confidence The confidence to create the raw utterance with.
     * @return The newly created RawUtterance.
     */
    public static RawUtterance create(String text, double confidence) {
        RawUtterance rawUtterance = new RawUtterance(text, confidence);
        rawUtterance.save();
        return rawUtterance;
    }
}