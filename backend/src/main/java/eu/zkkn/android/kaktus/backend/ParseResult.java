package eu.zkkn.android.kaktus.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

/**
 * The Objectify object model for results of webpage parsing
 */
@Entity
public class ParseResult {

    @Id
    Long id;

    @Index
    private Date date;

    @Index
    private Boolean notificationsSent;

    private String text;

    public ParseResult() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getNotificationsSent() {
        return notificationsSent;
    }

    public void setNotificationsSent(boolean notificationsSent) {
        this.notificationsSent = notificationsSent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
