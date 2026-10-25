package pg.gipter.services.auth;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ToolkitSessionTest {
    private static final URI PROBE = URI.create("https://toolkit.example.com/cases/test/_api/web/currentuser");
    private static final Instant NOW = Instant.parse("2026-10-25T12:00:00Z");

    static ToolkitSession session() {
        ToolkitSession session = new ToolkitSession();
        session.origin = "https://toolkit.example.com";
        session.email = "employee@example.com";
        ToolkitSession.Cookie cookie = new ToolkitSession.Cookie();
        cookie.name = "FedAuth";
        cookie.value = "test-secret";
        cookie.domain = ".example.com";
        cookie.path = "/";
        cookie.expiresUtcMillis = NOW.plusSeconds(3600).toEpochMilli();
        session.cookies = List.of(cookie);
        return session;
    }

    @Test void checksScopeExpiryAndIdentity() {
        ToolkitSession session = session();
        assertThat(session.valid(PROBE, "employee@example.com", NOW)).isTrue();
        assertThat(session.valid(URI.create("https://TOOLKIT.example.com:443/cases/test/_api/web/currentuser"), "employee@example.com", NOW)).isTrue();
        assertThat(session.valid(PROBE, "other@example.com", NOW)).isFalse();
        assertThat(session.valid(URI.create("https://toolkit.example.com.evil.test/_api"), "employee@example.com", NOW)).isFalse();
        assertThat(session.valid(PROBE, "employee@example.com", NOW.plusSeconds(7200))).isFalse();
        assertThat(session.toString()).doesNotContain("test-secret");
    }

    @Test void rejectsHeaderInjectionDuplicateAndWrongPathCookies() {
        ToolkitSession session = session();
        ToolkitSession.Cookie cookie = session.cookies.getFirst();
        cookie.value = "value\r\nInjected: header";
        assertThat(session.valid(PROBE, session.email, NOW)).isFalse();
        cookie.value = "value";
        cookie.path = "/cases/tes";
        assertThat(session.valid(PROBE, session.email, NOW)).isFalse();
        cookie.path = "/";
        session.cookies = List.of(cookie, cookie);
        assertThat(session.valid(PROBE, session.email, NOW)).isFalse();
    }

    @Test void supportsSessionCookiesOnlyDuringTheRunningApplication() {
        ToolkitSession session = session();
        session.cookies.getFirst().session = true;
        session.cookies.getFirst().expiresUtcMillis = null;
        assertThat(session.valid(PROBE, session.email, NOW)).isTrue();
        assertThat(session.expiry()).isEmpty();
    }
}
