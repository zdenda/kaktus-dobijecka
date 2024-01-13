package eu.zkkn.android.kaktus.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * The Objectify object model for device registrations we are persisting
 */
@Deprecated
@Entity
public class RegistrationRecord {

    @Id
    Long id;

    @Index
    private String regId;

    @Index
    private Boolean topicNotifications;


    public RegistrationRecord() {
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public Boolean isTopicNotifications() {
        return topicNotifications;
    }

    public void setTopicNotifications(boolean topicNotifications) {
        this.topicNotifications = topicNotifications;
    }

}