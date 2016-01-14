package eu.zkkn.android.kaktus.backend;


import com.google.appengine.api.utils.SystemProperty;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Utils {

    public static String getAppId() {
        return SystemProperty.applicationId.get();
    }

    public static InternetAddress getLocalEmail(String user) throws UnsupportedEncodingException {
        String address = user + "@" + getAppId() + ".appspotmail.com";
        String personal = "Kaktus Dobijecka " + user;
        return new InternetAddress(address, personal, "UTF-8");
    }

    public static boolean sendEmail(String from, String to, String subject, String text) {
        try {

            Message message = new MimeMessage(Session.getDefaultInstance(new Properties(), null));
            message.setFrom(getLocalEmail(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(text);
            Transport.send(message);
            return true;

        } catch (UnsupportedEncodingException | MessagingException e) {
            // ignore exceptions and return false
        }
        return false;
    }

}
