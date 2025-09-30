package pg.gipter.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.webkit.network.CookieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.services.dto.CookieDetails;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.CookieHandler;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class CookiesService {

    enum CookieName {
        FedAuth, Goto
    }

    private static final Logger logger = LoggerFactory.getLogger(CookiesService.class);
    private static final String[] DAYS = {"Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan"};
    private static final ZoneId GMT_ZONE_ID = TimeZone.getTimeZone("GMT").toZoneId();

    static final Path COOKIES_PATH = Paths.get("cookies.json");

    private CookiesService() {}

    public static boolean hasValidCookies() {
        try {
            LocalDateTime now = LocalDateTime.now();
            CookieDetails fedAuthCookie = loadFedAuthCookie()
                    .orElseThrow(() -> new IllegalStateException("The cookie FedAuth does not exist."));
            LocalDateTime fedAuthExpirationDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(fedAuthCookie.expiryTime),
                    GMT_ZONE_ID
            );
            boolean result = fedAuthExpirationDate.isAfter(now);

            Optional<CookieDetails> cookieDetails = loadGotoCookie();
            if (cookieDetails.isPresent()) {
                LocalDateTime goToExpirationDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(cookieDetails.get().expiryTime),
                        GMT_ZONE_ID
                );
                result &= goToExpirationDate.isAfter(now);
            }
            return result;
        } catch (Exception ex) {
            logger.error("Problem with cookies. Source of cookie [{}]. {}", COOKIES_PATH.toAbsolutePath(), ex.getMessage());
            return false;
        }
    }

    public static Optional<String> getFedAuthString() {
        try {
            return Optional.of(getCookieString(CookieName.FedAuth));
        } catch (IllegalStateException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public static Optional<String> getGotoString() {
        try {
            return Optional.of(getCookieString(CookieName.Goto));
        } catch (IllegalStateException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    private static String getCookieString(CookieName cookieName) {
        return getCookieDetails(cookieName)
                .map(cd -> cd.name + "=" + cd.value)
                .orElseGet(() -> {
                    logger.warn("Cookie name [{}] does not exist.", cookieName);
                    return "";
                });
    }

    private static Optional<CookieDetails> loadFedAuthCookie() {
        return getCookieDetails(CookieName.FedAuth);
    }

    private static Optional<CookieDetails> loadGotoCookie() {
        return getCookieDetails(CookieName.Goto);
    }

    private static Optional<CookieDetails> getCookieDetails(CookieName cookieName) {
        Optional<CookieDetails> result;
        if (isCookiesFileExist()) {
            try {
                Map<String, Collection<CookieDetails>> cookiesToLoad = readCookiesFromFile();
                result = cookiesToLoad.get(ArgName.toolkitHostUrl.defaultValue().replace("https://", ""))
                        .stream()
                        .filter(cookie -> cookieName.name().equals(cookie.name))
                        .findFirst();
            } catch (Exception e) {
                logger.error("Could not load cookies from [{}]. {}", COOKIES_PATH.toAbsolutePath(), e.getMessage());
                result = Optional.empty();
            }
        } else {
            result = Optional.empty();
        }
        return result;
    }

    public static boolean isCookiesFileExist() {
        return Files.exists(COOKIES_PATH);
    }

    private static Map<String, Collection<CookieDetails>> readCookiesFromFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type type = new TypeToken<Map<String, Collection<CookieDetails>>>() {}.getType();
        return gson.fromJson(Files.readString(COOKIES_PATH, StandardCharsets.UTF_8), type);
    }

    private static String format(CookieDetails cookie) {
        if (StringUtils.nullOrEmpty(cookie.name)) {
            throw new IllegalArgumentException("Bad cookie name");
        }

        final StringBuilder buf = new StringBuilder();
        buf.setLength(0);
        buf.append(cookie.name).append('=').append(Optional.ofNullable(cookie.value).orElse(""));

        if (StringUtils.notEmpty(cookie.path)) {
            buf.append(";Path=").append(cookie.path);
        }

        if (StringUtils.notEmpty(cookie.domain)) {
            buf.append(";Domain=").append(cookie.domain);
        }

        if (cookie.expiryTime >= 0) {
            buf.append(";Expires=");
            if (cookie.expiryTime == 0) {
                buf.append(formatCookieDate(0).trim());
            } else {
                buf.append(formatCookieDate(System.currentTimeMillis() + 1000L * cookie.expiryTime));
            }
            buf.append(";Max-Age=");
            buf.append(cookie.expiryTime);
        }

        if (cookie.secureOnly) {
            buf.append(";Secure");
        }
        if (cookie.httpOnly) {
            buf.append(";HttpOnly");
        }
        if (cookie.persistent) {
            buf.append(";Persistent");
        }
        if (cookie.hostOnly) {
            buf.append(";HostOnly");
        }

        return buf.toString();
    }

    /**
     * Format "EEE, dd-MMM-yy HH:mm:ss 'GMT'" for cookies
     * @param date the date in milliseconds
     */
    private static String formatCookieDate(long date) {
        LocalDateTime gc = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), GMT_ZONE_ID);

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
        if (isCookiesFileExist()) {
            try {
                Map<String, Collection<CookieDetails>> cookiesToLoad = readCookiesFromFile();
                for (String domain : cookiesToLoad.keySet()) {
                    Collection<CookieDetails> cookies = cookiesToLoad.get(domain);
                    List<String> list = cookies.stream()
                            .map(CookiesService::format)
                            .collect(Collectors.toList());
                    Map<String, List<String>> m = new LinkedHashMap<>();
                    m.put("Set-Cookie", list);
                    CookieHandler.getDefault().put(new URI(String.format("http://%s/", domain)), m);
                }
                logger.info("Cookies successfully loaded from [{}]", COOKIES_PATH.toAbsolutePath());
            } catch (Exception e) {
                logger.error("Could not load cookies from [{}]", COOKIES_PATH.toAbsolutePath(), e);
            }
        } else {
            logger.info("File with the cookies does not exist. [{}]", COOKIES_PATH.toAbsolutePath());
        }
    }

    public static void extractAndSaveCookies() throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, IOException {
        Map<String, Collection<?>> cookiesToSave = extractCookies();
        saveCookies(cookiesToSave);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Collection<?>> extractCookies() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        Field f = cookieManager.getClass().getDeclaredField("store");
        f.setAccessible(true);
        Object cookieStore = f.get(cookieManager);

        Field bucketsField = Class.forName("com.sun.webkit.network.CookieStore").getDeclaredField("buckets");
        bucketsField.setAccessible(true);
        Map<String, Collection<?>> buckets = (Map) bucketsField.get(cookieStore);
        f.setAccessible(true);
        Map<String, Collection<?>> cookiesToSave = new LinkedHashMap<>();
        for (Object o : buckets.entrySet()) {
            Map.Entry<String, Collection<?>> entry = (Map.Entry) o;
            String domain = entry.getKey();
            Map<String, ?> cookies = (Map) entry.getValue();
            cookiesToSave.put(domain, cookies.values());
        }
        return cookiesToSave;
    }

    private static void saveCookies(Map<String, Collection<?>> cookiesToSave) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(cookiesToSave);

        if (!json.isEmpty() && !"{}".equals(json)) {
            Files.writeString(CookiesService.COOKIES_PATH, json);
        }
        logger.info("Cookies saved in [{}]", CookiesService.COOKIES_PATH);
    }

    public static String expiryDate() {
        try {
            CookieDetails fedAuthCookie = loadFedAuthCookie()
                    .orElseThrow(() -> new IllegalStateException("The cookie FedAuth does not exist."));
            LocalDateTime expirationDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(fedAuthCookie.expiryTime),
                    GMT_ZONE_ID
            );
            return expirationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (IllegalStateException ex) {
            return "";
        }
    }
}
