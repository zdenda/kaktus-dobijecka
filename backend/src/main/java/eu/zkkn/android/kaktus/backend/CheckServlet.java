package eu.zkkn.android.kaktus.backend;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Checks Kaktus web whether Dobíječka is active
 */
public class CheckServlet extends HttpServlet {

    public static final String KAKTUS_DOBIJECKA_URL = "https://www.mujkaktus.cz/chces-pridat";
    private static final String KAKTUS_WEB_HOMEPAGE = "https://www.mujkaktus.cz/";

    private static final Logger LOG = Logger.getLogger(CheckServlet.class.getName());

    // App Engine request has to return within 60 seconds
    private static final int APP_ENGINE_REQ_TIMEOUT = 59 * 1000;

    @SuppressWarnings("unchecked") //TODO: remove
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String link = loadLinkToPdf();
        LOG.info("Link: " + link);

        // return error if there's no link
        if (link == null || link.isEmpty()) {

            // send notification email to admin, but only one email in 12 hours
            String recipient = System.getProperty("admin.email");
            Calendar since = Calendar.getInstance();
            since.add(Calendar.DAY_OF_MONTH, -1);

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
            // send notifications if the current text is different from text of the most recent result in database
            // and if it contains today's date
            sendNotifications = !previousResults.get(0).getText().equals(link)
                    && containsDate(link, new Date());
        }

        //TODO: save all results, even those which didn't cause sending notifications
        saveParseResult(link, sendNotifications);

        // if change was detected, send GCM notifications
        if (sendNotifications) {
            LOG.info("Send notifications!");
            String text = "Dneska to vypadá na Dobíječku! Pro přesný čas mrkněte na web nebo Facebook Kaktusu.";
            FcmSender.sendFcmToAll(text);
        }

        // send JSON response
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("result", link);
        jsonResponse.put("notifications", sendNotifications);

        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        JSONArray jsonPrevious = new JSONArray();
        for (ParseResult parseResult : previousResults) {
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("date", isoFormat.format(parseResult.getDate()));
            jsonResult.put("text", parseResult.getText());
            jsonResult.put("notificationSent", parseResult.getNotificationsSent());
            jsonPrevious.add(jsonResult);
        }
        jsonResponse.put("previous", jsonPrevious);

        resp.setContentType("application/json; charset=UTF-8");
        jsonResponse.writeJSONString(resp.getWriter());

    }


    /**
     * Get link to the PDF file with the conditions
     * @return link or null if it could not be found
     * @throws IOException if the web page could not be found or read
     */
    @Nullable
    private String loadLinkToPdf() throws IOException {

        Map<String, String> cookies = getCookies();
        if (cookies.isEmpty()) LOG.warning("Cookies are empty");

        Document document = Jsoup.connect(KAKTUS_DOBIJECKA_URL).cookies(cookies)
                .timeout(APP_ENGINE_REQ_TIMEOUT).get();

        // Try query here: https://try.jsoup.org/
        Elements elements = document.select("a:contains(Celé podmínky v PDF)");

        // there should be only one element
        if (elements.size() != 1) {
            // something wrong happened if there's more or less than one element
            // for example the structure of kaktus web might have been changed
            LOG.warning("Unexpected count (" + elements.size() + ") of matching elements.");
            return null;
        }

        Element linkElement = elements.first();
        if (linkElement == null) return null;

        String link = elements.attr("href");

        // the text of the link should match the pattern
        if (!linkMatchesPattern(link)) {
            // something wrong happened if there's no match
            // for example the structure of kaktus web might have been changed
            LOG.warning("Link: '" + link + "' doesn't match RegEx");
            return null;
        }

        return link;
    }

    /**
     * Checks whether the link matches the pattern with date
     * @param link link to check
     * @return true if the link matches, false otherwise
     */
    public static boolean linkMatchesPattern(@Nonnull String link) {
        String dayRegExp = "(0[1-9]|[12]\\d|3[01])";
        String monthRegExp = "(0[1-9]|1[0-2])";
        String yearRegExp = "(20)\\d{2}";
        return link.matches(".+filename=OP-Odmena-za-dobiti-FB_"+ dayRegExp + monthRegExp + yearRegExp +"\\.pdf$");
    }

    /**
     * Checks whether the text contains date in format ddMMyyyy, for example 12012020
     * @param text text to check
     * @param date date to find
     * @return true if the text contains date, false otherwise
     */
    public static boolean containsDate(@Nonnull String text, Date date) {
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.forLanguageTag("cs-CZ"));
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Prague"));
        return text.matches(".+" + dateFormat.format(date) + ".+");
    }



    /**
     * Checks whether the text matches one of the regular expressions
     * @param text text to match
     * @return true if the text matches, false otherwise
     */
    @Deprecated
    public static boolean textMatchesPattern(@Nonnull String text) {
        List<String> regularExpressions = Arrays.asList(
                // the czech characters must be encoded to ASCII using Unicode escapes (native2ascii)
                "dnes(ka)?",
                "\\d+\\.\\s?\\d+\\.(\\s?20[0-9]{2})?",
                "(dva|dvoj|2x|jednou tolik)"
        );
        return regularExpressions.stream()
                .map(regex -> ".+" + regex + ".+")
                .allMatch(text.toLowerCase(Locale.forLanguageTag("cs-CZ"))::matches);
    }

    // Get cookies by loading Homepage
    private Map<String, String> getCookies() {
        try {

            Map<String, String> cookies = new HashMap<>();

            URLConnection connection = (new URL(KAKTUS_WEB_HOMEPAGE)).openConnection();
            connection.setConnectTimeout(APP_ENGINE_REQ_TIMEOUT);
            connection.setReadTimeout(APP_ENGINE_REQ_TIMEOUT / 2);

            @Nullable
            List<String> cookieFields = connection.getHeaderFields().get("set-cookie");
            if (cookieFields != null) {
                cookieFields.forEach(cookieField ->
                        HttpCookie.parse(cookieField).forEach(httpCookie ->
                                cookies.put(httpCookie.getName(), httpCookie.getValue())
                        )
                );
            }

            return cookies;

        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    private void saveParseResult(String text, boolean notificationsSent) {
        ParseResult result = new ParseResult();
        result.setDate(new Date());
        result.setNotificationsSent(notificationsSent);
        result.setText(text);
        ofy().save().entity(result).now();
    }

}
