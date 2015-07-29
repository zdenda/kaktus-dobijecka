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

    private String text;

    public ParseResult() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
