package pg.gipter.services.auth;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Cookies are kept out of generated record toString implementations and diagnostics. */
public final class ToolkitSession {
    public static final class Cookie {
        public String name;
        public String value;
        public String domain;
        public String path;
        public Long expiresUtcMillis;
        public boolean session;

        boolean valid(URI resource, Instant now) {
            if (!("FedAuth".equals(name) || "Goto".equals(name)) || value == null || value.isEmpty()
                    || value.length() > 32768 || domain == null || domain.isBlank() || path == null || !path.startsWith("/"))
                return false;
            if (value.chars().anyMatch(c -> c < 0x21 || c > 0x7e || c == ';')) return false;
            String host = resource.getHost().toLowerCase(java.util.Locale.ROOT);
            String cookieDomain = domain.replaceFirst("^\\.", "").toLowerCase(java.util.Locale.ROOT);
            String requestPath = resource.getPath();
            boolean pathMatches = requestPath.equals(path) || requestPath.startsWith(path.endsWith("/") ? path : path + "/");
            return (host.equals(cookieDomain) || host.endsWith("." + cookieDomain)) && pathMatches
                    && (session || expiresUtcMillis != null && expiresUtcMillis > now.toEpochMilli());
        }
    }

    public String origin;
    public String email;
    public List<Cookie> cookies;

    public boolean valid(URI probe, String expectedEmail, Instant now) {
        return validOrigin(origin, probe)
                && email != null && email.equalsIgnoreCase(expectedEmail)
                && cookies != null && !cookies.isEmpty() && cookies.size() <= 2
                && cookies.stream().allMatch(c -> c != null && c.valid(probe, now))
                && cookies.stream().map(c -> c.name).distinct().count() == cookies.size()
                && cookies.stream().anyMatch(c -> "FedAuth".equals(c.name));
    }

    private static boolean validOrigin(String origin, URI probe) {
        try {
            URI uri = URI.create(origin);
            return uri.getUserInfo() == null && uri.getQuery() == null && uri.getFragment() == null
                    && (uri.getPath().isEmpty() || "/".equals(uri.getPath()))
                    && WebView2AuthClient.sameOrigin(uri, probe);
        } catch (RuntimeException _) {
            return false;
        }
    }

    public Optional<String> cookie(String name) {
        return cookies.stream().filter(cookie -> name.equals(cookie.name)).findFirst().map(c -> c.name + "=" + c.value);
    }

    public String cookieHeader() {
        return cookies.stream().map(cookie -> cookie.name + "=" + cookie.value).collect(java.util.stream.Collectors.joining("; "));
    }

    public Optional<Instant> expiry() {
        return cookies.stream().filter(cookie -> !cookie.session && cookie.expiresUtcMillis != null)
                .map(c -> Instant.ofEpochMilli(c.expiresUtcMillis)).min(Instant::compareTo);
    }

    @Override
    public String toString() {
        return "ToolkitSession[redacted]";
    }
}
