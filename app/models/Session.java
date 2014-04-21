package models;

import play.db.ebean.Model;
import tyrex.services.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Represents a broadcasting session
 */
@Entity
public class Session extends Model {

    /**
     * Id of the shared transcript in the database.
     */
    @Id
    private long id;

    /**
     * Gets the session id.
     * @return The session id.
     */
    public String getId() {
        return sessionHash;
    }

    /**
     * The hash of the session.
     */
    private String sessionHash;

    /**
     * Gets the session name.
     * @return The session name.
     */
    public String getName() {
        return name;
    }

    /**
     * The name of the session.
     */
    private String name;

    public SharedTranscript getTranscript() {
        return transcript;
    }

    /**
     * The transcript that belongs to this session.
     */
    @ManyToOne
    private SharedTranscript transcript;

    /**
     * Finder for the Session model.
     */
    public static Finder<Long, Session> find = new Finder<Long, Session>(Long.class, Session.class);

    public static Session findById(String sessionId) {
        for (Session session : Session.find.all()) {
            if (session.getId().equals(sessionId)) {
                return session;
            }
        }
        return null;
    }

    /**
     * Constructor for session.
     * @param name The name of the session.
     * @param transcript the transcript to initialize this session with.
     */
    public Session(String name, SharedTranscript transcript) {
        this.name = name;
        this.transcript = transcript;
        this.sessionHash = DigestUtils.shaHex(this.name + this.id).substring(34);
    }

    /**
     * Static creator for sessions; handles database saving.
     * @param name The name of the session being created.
     * @return The newly created session.
     */
    public static Session create(String name) {
        SharedTranscript transcript = SharedTranscript.create();

        Session session = new Session(name, transcript);
        session.save();

        return session;
    }

}
