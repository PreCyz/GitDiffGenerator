package pg.gipter.services.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebView2AuthClientTest {
    private static final String START = "https://toolkit.example.com/cases/test/Lists/WorkItems/";
    private static final String PROBE = "https://toolkit.example.com/cases/test/_api/web/currentuser";

    @Test void restoresOnlyAScopedUnexpiredSessionAndUsesPipesForConfiguration() throws Exception {
        AtomicReference<JsonObject> request = new AtomicReference<>();
        ToolkitSession session = ToolkitSessionTest.session();
        session.cookies.getFirst().expiresUtcMillis = Instant.now().plusSeconds(3600).toEpochMilli();
        WebView2AuthClient client = new WebView2AuthClient(START, PROBE, session.email, (input, timeout) -> {
            request.set(input);
            JsonObject response = new JsonObject();
            response.addProperty("status", "ok");
            response.add("session", new Gson().toJsonTree(session));
            return response;
        });
        assertThat(client.restore()).isPresent();
        assertThat(request.get().get("action").getAsString()).isEqualTo("load");
        assertThat(request.get().get("probeUrl").getAsString()).isEqualTo(PROBE);
        assertThat(request.get().toString()).doesNotContain("test-secret");
        session.cookies.getFirst().domain = "evil.example.org";
        assertThatThrownBy(client::restore).isInstanceOf(IOException.class).hasMessageContaining("valid Toolkit session");
    }

    @Test void distinguishesMissingSessionCancellationAndProtocolFailure() throws Exception {
        for (String status : new String[]{"no-session", "cancelled", "unknown"}) {
            WebView2AuthClient client = new WebView2AuthClient(START, PROBE, "employee@example.com", (input, timeout) -> {
                JsonObject response = new JsonObject(); response.addProperty("status", status); return response;
            });
            if (status.equals("no-session")) assertThat(client.restore()).isEmpty();
            else assertThatThrownBy(client::restore).isInstanceOf(IOException.class);
        }
    }

    @Test void rejectsUntrustedAndInsecureConfiguration() {
        assertThatThrownBy(() -> new WebView2AuthClient("http://toolkit.example.com/", PROBE, "employee@example.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new WebView2AuthClient(START, "https://other.example.com/_api", "employee@example.com")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void persistsOnlyAfterJavaHasAcceptedTheBrowserSession() throws Exception {
        java.util.List<String> actions = new java.util.ArrayList<>();
        ToolkitSession session = ToolkitSessionTest.session();
        session.cookies.getFirst().expiresUtcMillis = Instant.now().plusSeconds(3600).toEpochMilli();
        WebView2AuthClient.HostRunner host = (request, timeout) -> {
            actions.add(request.get("action").getAsString());
            JsonObject response = new JsonObject();
            response.addProperty("status", "ok");
            response.add("session", new Gson().toJsonTree(session));
            return response;
        };
        WebView2AuthClient rejected = new WebView2AuthClient(START, PROBE, session.email, host, candidate -> false);
        assertThatThrownBy(rejected::authenticate).isInstanceOf(IOException.class).hasMessageContaining("Toolkit rejected access from Gipter");
        assertThat(actions).containsExactly("login");
        actions.clear();
        WebView2AuthClient accepted = new WebView2AuthClient(START, PROBE, session.email, host, candidate -> true);
        assertThat(accepted.authenticate().email).isEqualTo(session.email);
        assertThat(actions).containsExactly("login", "save");
    }

    @Test void boundsIdentityResponseBeforeAllocatingAnUncontrolledBody() {
        var subscriber = WebView2AuthClient.limitedBodyHandler().apply(null);
        java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean();
        subscriber.onSubscribe(new java.util.concurrent.Flow.Subscription() {
            public void request(long count) { }
            public void cancel() { cancelled.set(true); }
        });
        subscriber.onNext(java.util.List.of(java.nio.ByteBuffer.wrap(new byte[131073])));
        assertThat(cancelled).isTrue();
        assertThatThrownBy(() -> subscriber.getBody().toCompletableFuture().join()).hasCauseInstanceOf(IOException.class);
    }

    @Test void publishedWindowsHostSupportsTheRealStdioProtocolWithoutLogin() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        java.nio.file.Path helper;
        try { helper = WebView2AuthClient.resolveHost(); }
        catch (IOException ex) { org.junit.jupiter.api.Assumptions.abort("Build the native helper to run this integration test."); return; }
        WebView2AuthClient client = new WebView2AuthClient("https://gipter-fixture.invalid/site/Lists/Items/",
                "https://gipter-fixture.invalid/site/_api/web/currentuser", "fixture@example.invalid");
        assertThat(java.nio.file.Files.isRegularFile(helper)).isTrue();
        assertThat(client.restore()).isEmpty();
    }
}
