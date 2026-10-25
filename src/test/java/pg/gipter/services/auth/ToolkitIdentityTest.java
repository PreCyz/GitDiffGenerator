package pg.gipter.services.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolkitIdentityTest {
    private static final String USER = "{\"d\":{\"Id\":42,\"Email\":\"employee@example.com\"}}";

    @Test void acceptsOnlyTheActualConfiguredIdentity() {
        assertThat(ToolkitIdentity.matches(200, "application/json;odata=verbose", USER, "EMPLOYEE@example.com")).isTrue();
        assertThat(ToolkitIdentity.matches(200, "application/json", USER, "other@example.com")).isFalse();
    }

    @Test void errorsAndRedirectsCannotEstablishAuthentication() {
        for (int status : new int[]{204, 302, 401, 403, 404, 429, 500, 503}) {
            assertThat(ToolkitIdentity.matches(status, "application/json", USER, "employee@example.com")).isFalse();
        }
    }

    @Test void rejectsLoginHtmlAndInvalidIdentityPayloads() {
        assertThat(ToolkitIdentity.matches(200, "text/html", "<html>Sign in</html>", "employee@example.com")).isFalse();
        for (String body : new String[]{"null", "[]", "{}", "{\"error\":{}}", "{\"Id\":0,\"Email\":\"employee@example.com\"}"}) {
            assertThat(ToolkitIdentity.matches(200, "application/json", body, "employee@example.com")).isFalse();
        }
    }
}
