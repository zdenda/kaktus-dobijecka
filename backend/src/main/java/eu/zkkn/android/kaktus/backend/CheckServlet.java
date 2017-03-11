package eu.zkkn.android.kaktus.backend;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Endpoint;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Checks Kaktus web whether Dobíječka is active
 */
public class CheckServlet extends HttpServlet {

    private static final String KAKTUS_WEB_URL = "https://www.mujkaktus.cz/chces-pridat";

    /**
     * Api Keys can be obtained from the google cloud console
     */
    private static final String API_KEY = System.getProperty("gcm.api.key");

    private static final Logger log = Logger.getLogger(CheckServlet.class.getName());


    //TODO: check if there's some timeout for response (might be 60 seconds)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String text = loadTextFromWeb();

        // return error if there's no text
        if (text == null || text.length() == 0) {

            // send notification email to admin, but only one email in 12 hours
            String recipient = System.getProperty("admin.email");
            Calendar since = Calendar.getInstance();
            since.add(Calendar.HOUR_OF_DAY, -12);

            List<EmailLog> emailsSince = ofy().load().type(EmailLog.class)
                    .filter("recipient", recipient).filter("date >", since.getTime()).list();

            if (emailsSince.isEmpty()) {
                if (Utils.sendEmail("admin", recipient, "HTML Parse Error",
                        "Hello,\n\nplease have a look at it.\n\nThank you,\nyour users")) {
                    ofy().save().entity(new EmailLog(recipient, new Date())).now();
                }
            }

            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "HTML Parse Error");
            return;
        }

        List<ParseResult> previousResults = ofy().load().type(ParseResult.class).order("-date")
                .limit(24).list();

        boolean sendNotifications = false;
        if (!previousResults.isEmpty()) {
            DateFormat dayMonthRegExpFormat = new SimpleDateFormat("d\\.\\'s?'M\\.", Locale.US);
            dayMonthRegExpFormat.setTimeZone(TimeZone.getTimeZone("Europe/Prague"));
            // set changed if the current text is different from text of the most recent result in database
            // and if it contains today's date
            sendNotifications = !previousResults.get(0).getText().equals(text)
                    && text.matches(".+ " + dayMonthRegExpFormat.format(new Date()) + " .+");
        }

        saveParseResult(text);
        //TODO: delete too old records, so they wouldn't take up space in database

        // if change was detected, send GCM notifications
        if (sendNotifications) {
            sendMessage(text);
        }

        // send JSON response
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("result", text);
            jsonResponse.put("notifications", sendNotifications);

            DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            JSONArray jsonPrevious = new JSONArray();
            for (ParseResult parseResult : previousResults) {
                JSONObject jsonResult = new JSONObject();
                jsonResult.put("date", isoFormat.format(parseResult.getDate()));
                jsonResult.put("text", parseResult.getText());
                jsonPrevious.put(jsonResult);
            }
            jsonResponse.put("previous", jsonPrevious);


            resp.setContentType("application/json; charset=UTF-8");
            jsonResponse.write(resp.getWriter());

        } catch (JSONException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    /**
     * It loads and validates a text of specific element on the Kaktus' web page.
     * The text must match a specific pattern.
     *
     * @return text or null if the text doesn't match specific pattern
     * @throws IOException if the kaktus' web page could not be found or read
     */
    private String loadTextFromWeb() throws IOException {
        Document document = Jsoup.connect(KAKTUS_WEB_URL).timeout(0).get();
        Elements elements = document.select("div.wrapper > h2.uppercase + h3.uppercase.text-drawn");

        // there should be only one element
        if (elements.size() != 1) {
            // something wrong happened if there's more or less than one element
            // for example the structure of kaktus web might have been changed
            return null;
        }

        String text = elements.first().text();

        // the czech characters must be encoded to ASCII using Unicode escapes (native2ascii)
        String regex = "Pokud si dneska \\d+\\.\\s?\\d+\\.(\\s?20[0-9]{2})? od \\d+:\\d+ do \\d+:\\d+ hodin dobije\u0161 alespo\u0148 \\d+ K\u010d, d\u00e1me ti dvojn\u00e1sob .*";
        // the text of that element should match pattern
        if (!text.matches(regex)) {
            // something wrong happened if there's no match
            // for example the structure of kaktus web might have been changed
            return null;
        }

        return text;
    }

    /**
     * Send to the first 10 devices (You can modify this to send to any number of devices or a specific device)
     *
     * @param message The message to send
     */
    private void sendMessage(String message) throws IOException {
        if (message == null || message.trim().length() == 0) {
            log.warning("Not sending message because it is empty");
            return;
        }
        // crop longer messages
        if (message.length() > 1000) {
            message = message.substring(0, 1000) + "[...]";
        }
        Sender sender = new Sender(API_KEY, Endpoint.FCM);
        //TODO: maybe use Notification instead of Data payload
        Message msg = new Message.Builder()
                .addData("type", "notification")
                .addData("message", message)
                .addData("uri", KAKTUS_WEB_URL)
                .build();
        //TODO: increase/remove the limit
        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).limit(1000).list();
        for (RegistrationRecord record : records) {
            //TODO: maybe use Topic or Group Messaging
            Result result = sender.send(msg, record.getRegId(), 5);
            if (result.getMessageId() != null) {
                log.info("Message sent to " + record.getRegId());
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // if the regId changed, we have to update it in the datastore
                    log.info("Registration Id changed for " + record.getRegId() + " updating to " + canonicalRegId);
                    record.setRegId(canonicalRegId);
                    ofy().save().entity(record).now();
                }
            } else {
                String error = result.getErrorCodeName();
                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                    log.warning("Registration Id " + record.getRegId() + " no longer registered with GCM, removing from datastore");
                    // if the device is no longer registered with Gcm, remove it from the datastore
                    ofy().delete().entity(record).now();
                } else {
                    log.warning("Error when sending message to Registration Id [" + record.getRegId() + "]: " + error);
                }
            }
        }
    }

    private void saveParseResult(String text) {
        ParseResult result = new ParseResult();
        result.setDate(new Date());
        result.setText(text);
        ofy().save().entity(result).now();
    }

}
