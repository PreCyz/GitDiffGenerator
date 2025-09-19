package pg.gipter.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pg.gipter.services.dto.CookieDetails;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BulkCookieManagerExample {

    static final Path COOKIES_PATH = Paths.get("cookies.json");

    private static Map<String, Collection<CookieDetails>> readCookiesFromFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type type = new TypeToken<Map<String, Collection<CookieDetails>>>() {}.getType();
        return gson.fromJson(Files.readString(COOKIES_PATH, StandardCharsets.UTF_8), type);
    }

    public static CookieManager createCookieManager(Map<String, Collection<CookieDetails>> cookiesToLoad) throws URISyntaxException {
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieStore cookieStore = cookieManager.getCookieStore();

        for (Map.Entry<String, Collection<CookieDetails>> entry : cookiesToLoad.entrySet()) {
            String domain = entry.getKey();
            Collection<CookieDetails> cookies = entry.getValue();

            URI baseUri = new URI("https://" + domain); // A base URI for adding cookies

            for (CookieDetails cookieData : cookies) {
                HttpCookie cookie = createHttpCookie(cookieData);
                cookieStore.add(baseUri, cookie);
                System.out.println("Added cookie: " + cookie.getName());
            }
        }
        return cookieManager;
    }


    public static void main(String[] args) throws URISyntaxException, IOException {
        Map<String, Collection<CookieDetails>> cookiesToLoad = readCookiesFromFile();
        CookieManager cookieManager = createCookieManager(cookiesToLoad);

        for (String domain : cookiesToLoad.keySet()) {
            System.out.println("\n--- Verifying cookies in store ---");
            verifyCookiesForPath(cookieManager.getCookieStore(), String.format("https://%s/", domain));
        }
        System.out.println("\n--- Verifying cookies in store ---");
        verifyCookiesForPath(cookieManager.getCookieStore(), "https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems");
    }

    /**
     * Helper method to convert a Map of data into an HttpCookie object.
     * This keeps the main loop clean and simple.
     */
    private static HttpCookie createHttpCookie(CookieDetails data) {
        HttpCookie cookie = new HttpCookie(data.name, data.value);

        cookie.setDomain(data.domain);
        cookie.setPath(data.path);
        cookie.setSecure(data.secureOnly);
        cookie.setHttpOnly(data.httpOnly);

        // A cookie is persistent if it has an expiry date.
        // If 'persistent' is false, it's a session cookie and we should not set Max-Age.
        if (data.persistent) {
            // The expiryTime 9223372036854775807L is Long.MAX_VALUE, essentially "never expires".
            // We still need to calculate a valid Max-Age in seconds from now.
            if (data.expiryTime < Long.MAX_VALUE) {
                long maxAgeSeconds = TimeUnit.MILLISECONDS.toSeconds(data.expiryTime - System.currentTimeMillis());
                if (maxAgeSeconds > 0) {
                    cookie.setMaxAge(maxAgeSeconds);
                }
            } else {
                // For "never expires", set a very large Max-Age.
                // Integer.MAX_VALUE is a safe upper bound for many older cookie implementations.
                cookie.setMaxAge(Integer.MAX_VALUE);
            }
        }

        return cookie;
    }

    /**
     * Helper method for verification to keep the main method clean.
     */
    private static void verifyCookiesForPath(CookieStore store, String uriString) throws URISyntaxException {
        System.out.println("\nCookies available for URI: " + uriString);
        URI uri = new URI(uriString);
        List<HttpCookie> cookies = store.get(uri);

        if (cookies.isEmpty()) {
            System.out.println("  -> None");
        } else {
            cookies.forEach(c -> System.out.println("  -> " + c.toString()));
        }
    }
}

