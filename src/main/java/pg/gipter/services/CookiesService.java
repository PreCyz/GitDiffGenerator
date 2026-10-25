package pg.gipter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.services.auth.ToolkitSession;
import pg.gipter.services.auth.WebView2AuthClient;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** Keeps the existing Java request API, using only cookies issued by the SSO-enabled WebView2 host. */
public final class CookiesService {
    private static final Logger logger = LoggerFactory.getLogger(CookiesService.class);
    private static String startUrl = ArgName.toolkitUserFolderUrl.defaultValue();
    private static String probeUrl = ArgName.toolkitHostUrl.defaultValue() + ArgName.toolkitCopyCase.defaultValue() + "/_api/web/currentuser";
    private static String email = ArgName.toolkitUsername.defaultValue() + ArgName.emailDomain.defaultValue();
    private static WebView2AuthClient client = new WebView2AuthClient(startUrl, probeUrl, email);
    private static ToolkitSession session;
    private static boolean restored;

    private CookiesService() { }

    public static synchronized void configure(String loginUrl, String identityUrl, String expectedEmail) {
        if (!startUrl.equals(loginUrl) || !probeUrl.equals(identityUrl) || !email.equalsIgnoreCase(expectedEmail)) {
            startUrl = loginUrl;
            probeUrl = identityUrl;
            email = expectedEmail;
            client = new WebView2AuthClient(startUrl, probeUrl, email);
            session = null;
            restored = false;
        }
    }

    private static synchronized Optional<ToolkitSession> current() {
        if (!restored) {
            restored = true;
            try {
                session = client.restore().orElse(null);
            } catch (IOException ex) {
                logger.warn("Saved Toolkit session unavailable: {}", ex.getMessage());
            }
        }
        return Optional.ofNullable(session).filter(it -> it.valid(URI.create(probeUrl), email, Instant.now()));
    }

    public static boolean hasValidCookies() {
        return current().isPresent();
    }

    public static boolean isCookiesFileExist() {
        return current().isPresent();
    }

    public static Optional<String> getFedAuthString() {
        return current().flatMap(it -> it.cookie("FedAuth"));
    }

    public static Optional<String> getGotoString() {
        return current().flatMap(it -> it.cookie("Goto"));
    }

    public static void authenticate() throws IOException {
        WebView2AuthClient selected;
        synchronized (CookiesService.class) { selected = client; }
        ToolkitSession authenticated = selected.authenticate();
        synchronized (CookiesService.class) {
            if (client != selected) throw new IOException("Toolkit configuration changed during sign-in. Retry sign-in.");
            session = authenticated;
            restored = true;
        }
    }

    public static void cancelAuthentication() {
        WebView2AuthClient selected;
        synchronized (CookiesService.class) { selected = client; }
        selected.cancel();
    }

    public static String expiryDate() {
        return current().map(it -> it.expiry().map(expiry -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault()).format(expiry)).orElse("Current application session")).orElse("");
    }
}
