package eu.zkkn.android.kaktus.backend;


import com.google.appengine.api.utils.SystemProperty;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.annotation.Nonnull;
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

    static String cropText(@Nonnull String text, int maxLength) {
        text = text.trim();
        // if the text is longer than the limit, crop it and add the three dots symbol at the end
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength - 1) + "\u2026";
        }
        return text;
    }


    /**
     * Waits a given number of milliseconds (of uptimeMillis) before returning.
     * Similar to {@link java.lang.Thread#sleep(long)}, but does not throw
     * {@link InterruptedException}; {@link Thread#interrupt()} events are
     * deferred until the next interruptible operation.  Does not return until
     * at least the specified number of milliseconds has elapsed.
     *
     * Copied from Android SystemClock.sleep()
     *
     * @param ms to sleep before returning, in milliseconds of uptime.
     */
    public static void sleep(long ms) {
        long start = System.currentTimeMillis();
        long duration = ms;
        boolean interrupted = false;
        do {
            try {
                Thread.sleep(duration);
            }
            catch (InterruptedException e) {
                interrupted = true;
            }
            duration = start + ms - System.currentTimeMillis();
        } while (duration > 0);

        if (interrupted) {
            // Important: we don't want to quietly eat an interrupt() event,
            // so we make sure to re-interrupt the thread so that the next
            // call to Thread.sleep() or Object.wait() will be interrupted.
            Thread.currentThread().interrupt();
        }
    }

}
