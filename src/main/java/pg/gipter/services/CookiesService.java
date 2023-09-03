package pg.gipter.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.services.dto.CookieDetails;

import java.lang.reflect.Type;
import java.net.CookieHandler;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

public final class CookiesService {

    private static final Logger logger = LoggerFactory.getLogger(CookiesService.class);
    private static final String[] DAYS = {"Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan"};

    static final Path COOKIES_PATH = Paths.get("cookies.json");

    private CookiesService() {}

    public static boolean hasValidFedAuth() {
        try {
            CookieDetails fedAuthCookie = loadFedAuthCookie()
                    .orElseThrow(() -> new IllegalStateException("The cookie FedAuth does not exist."));
            LocalDateTime expirationDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(fedAuthCookie.expiryTime),
                    TimeZone.getDefault().toZoneId()
            );
            return expirationDate.isAfter(LocalDateTime.now());
        } catch (Exception ex) {
            logger.error("Problem with FedAuth cookie. Source of cookie [{}].", COOKIES_PATH.toAbsolutePath());
            return false;
        }
    }

    public static String getFedAuthString() {
        CookieDetails fedAuthCookie = loadFedAuthCookie().orElseThrow(() -> new IllegalStateException("The cookie FedAuth does not exist."));
        return fedAuthCookie.name + "=" + fedAuthCookie.value;
    }

    private static Optional<CookieDetails> loadFedAuthCookie() {
        Optional<CookieDetails> result;
        if (isCookiesExist()) {
            try {
                byte[] bytes = Files.readAllBytes(COOKIES_PATH);
                String json = new String(bytes, StandardCharsets.UTF_8);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Type type = new TypeToken<Map<String, Collection<CookieDetails>>>() {}.getType();
                Map<String, Collection<CookieDetails>> cookiesToLoad = gson.fromJson(json, type);
                result = cookiesToLoad.get(ArgName.toolkitHostUrl.defaultValue().replace("https://", ""))
                        .stream()
                        .filter(cookie -> cookie.name.equals("FedAuth"))
                        .findFirst();
            } catch (Exception e) {
                logger.error("Could not load cookies from [{}]", COOKIES_PATH.toAbsolutePath());
                result = Optional.empty();
            }
        } else {
            result = Optional.empty();
        }
        return result;
    }

    public static boolean isCookiesExist() {
        return Files.exists(COOKIES_PATH);
    }

    private static String format(
            final String name,
            final String value,
            final String domain,
            final String path,
            final long maxAge,
            final boolean isSecure,
            final boolean isHttpOnly) {

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Bad cookie name");
        }

        StringBuilder buf = new StringBuilder();
        buf.setLength(0);
        buf.append(name).append('=').append(Optional.ofNullable(value).orElseGet(() -> ""));

        if (path != null && !path.isEmpty()) {
            buf.append(";Path=").append(path);
        }

        if (domain != null && !domain.isEmpty()) {
            buf.append(";Domain=").append(domain);
        }

        if (maxAge >= 0) {
            buf.append(";Expires=");
            if (maxAge == 0) {
                buf.append(formatCookieDate(0).trim());
            } else {
                buf.append(formatCookieDate(System.currentTimeMillis() + 1000L * maxAge));
            }
            buf.append(";Max-Age=");
            buf.append(maxAge);
        }

        if (isSecure) {
            buf.append(";Secure");
        }
        if (isHttpOnly) {
            buf.append(";HttpOnly");
        }

        return buf.toString();
    }

    /**
     * Format "EEE, dd-MMM-yy HH:mm:ss 'GMT'" for cookies
     * @param date the date in milliseconds
     */
    private static String formatCookieDate(long date) {
        LocalDateTime gc = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), TimeZone.getTimeZone("GMT").toZoneId());

        int day_of_week = gc.getDayOfWeek().getValue();
        int day_of_month = gc.getDayOfMonth();
        int month = gc.getMonthValue();
        int year = gc.getYear();
        year = year % 10000;

        int epoch = (int) ((date / 1000) % (60 * 60 * 24));
        int seconds = epoch % 60;
        epoch = epoch / 60;
        int minutes = epoch % 60;
        int hours = epoch / 60;

        StringBuilder buf = new StringBuilder();

        buf.append(DAYS[day_of_week]);
        buf.append(',');
        buf.append(' ');
        append2digits(buf, day_of_month);

        buf.append('-');
        buf.append(MONTHS[month]);
        buf.append('-');
        append2digits(buf, year / 100);
        append2digits(buf, year % 100);

        buf.append(' ');
        append2digits(buf, hours);
        buf.append(':');
        append2digits(buf, minutes);
        buf.append(':');
        append2digits(buf, seconds);
        buf.append(" GMT");

        return buf.toString();
    }

    /**
     * Append 2 digits (zero padded) to the StringBuilder
     * @param buf the buffer to append to
     * @param i   the value to append
     */
    private static void append2digits(StringBuilder buf, int i) {
        if (i < 100) {
            buf.append((char) (i / 10 + '0'));
            buf.append((char) (i % 10 + '0'));
        }
    }

    public static void loadCookies() {
        if (isCookiesExist()) {
            try {
                byte[] bytes = Files.readAllBytes(COOKIES_PATH);
                String json = new String(bytes, StandardCharsets.UTF_8);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Type type = new TypeToken<Map<String, Collection<CookieDetails>>>() {
                }.getType();
                Map<String, Collection<CookieDetails>> cookiesToLoad = gson.fromJson(json, type);
                for (String domain : cookiesToLoad.keySet()) {
                    Collection<CookieDetails> cookies = cookiesToLoad.get(domain);
                    List<String> list = cookies.stream()
                            .map(cookie -> format(
                                    cookie.name,
                                    cookie.value,
                                    cookie.domain,
                                    cookie.path,
                                    cookie.expiryTime,
                                    cookie.secureOnly,
                                    cookie.httpOnly))
                            .collect(Collectors.toList());
                    Map<String, List<String>> m = new LinkedHashMap<>();
                    m.put("Set-Cookie", list);
                    CookieHandler.getDefault().put(new URI(String.format("http://%s/", domain)), m);
                    logger.info("Cookies successfully loaded from [{}]", COOKIES_PATH.toAbsolutePath());
                }
            } catch (Exception e) {
                logger.error("Could not load cookies from [{}]", COOKIES_PATH.toAbsolutePath());
            }
        } else {
            logger.info("File with the cookies does not exist. [{}]", COOKIES_PATH.toAbsolutePath());
        }
    }

}
