package eu.zkkn.android.kaktus.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;


/**
 * The Objectify object model for logging of sent emails
 */
@Entity
public class EmailLog {

    @Id
    Long id;

    @Index
    private String recipient;

    @Index
    private Date date;

    public EmailLog() {
    }

    public EmailLog(String recipient, Date date) {
        this.recipient = recipient;
        this.date = date;
    }
}
