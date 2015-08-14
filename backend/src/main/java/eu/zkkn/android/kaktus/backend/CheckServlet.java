package eu.zkkn.android.kaktus.backend;

import com.google.android.gcm.server.Constants;
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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static eu.zkkn.android.kaktus.backend.OfyService.ofy;

/**
 * Checks Kaktus web whether Dobíječka is active
 */
public class CheckServlet extends HttpServlet {

    /**
     * Api Keys can be obtained from the google cloud console
     */
    private static final String API_KEY = System.getProperty("gcm.api.key");

    private static final Logger log = Logger.getLogger(MessagingEndpoint.class.getName());


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Document document = Jsoup.connect("https://www.mujkaktus.cz/chces-pridat").get();
        Elements elements = document.select("div.wrapper > h2.uppercase + h3.uppercase.text-drawn");

        // there should be only one element
        if (elements.size() != 1) {
            // something wrong happened if there's more or less than one element
            // for example the structure of kaktus web might have been changed
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "HTML Parse Error");
            return;
        }

        String text = elements.first().text();

        List<ParseResult> previousResults = ofy().load().type(ParseResult.class).order("-date")
                .limit(24).list();

        boolean changed = false;
        if (!previousResults.isEmpty()) {
            // set changed if the current text is different from text of the most recent result in database
            // TODO: Check more thoroughly before sending GCM (maybe if the date in text is today)
            changed = !previousResults.get(0).getText().equals(text);
        }

        saveParseResult(text);
        //TODO: delete too old records, so they wouldn't take up space in database

        // if change was detected, send GCM notifications
        if (changed) {
            sendMessage(text);
        }

        // send JSON response
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("result", text);
            jsonResponse.put("changed", changed);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            JSONArray jsonPrevious = new JSONArray();
            for (ParseResult parseResult : previousResults) {
                JSONObject jsonResult = new JSONObject();
                jsonResult.put("date", format.format(parseResult.getDate()));
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
     * Send to the first 10 devices (You can modify this to send to any number of devices or a specific device)
     *
     * @param message The message to send
     */
    public void sendMessage(String message) throws IOException {
        if (message == null || message.trim().length() == 0) {
            log.warning("Not sending message because it is empty");
            return;
        }
        // crop longer messages
        if (message.length() > 1000) {
            message = message.substring(0, 1000) + "[...]";
        }
        Sender sender = new Sender(API_KEY);
        //TODO: add time to live (probably 12 hours or less)
        //TODO: maybe use Notification instead of Data payload
        Message msg = new Message.Builder().addData("message", message).build();
        //TODO: increase/remove the limit
        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).limit(10).list();
        for (RegistrationRecord record : records) {
            //TODO: maybe use Topic or Group Messaging
            Result result = sender.send(msg, record.getRegId(), 5);
            if (result.getMessageId() != null) {
                log.info("Message sent to " + record.getRegId());
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // if the regId changed, we have to update the datastore
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
                    log.warning("Error when sending message : " + error);
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
