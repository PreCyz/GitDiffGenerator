package pg.gipter.services.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.utils.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** A single, bounded stdio exchange with the Windows WebView2 host; no debugger endpoint or secret files. */
public final class WebView2AuthClient {
    private static final Gson GSON = new Gson();
    private static final int MAX_RESPONSE = 131072;
    private static final Logger log = LoggerFactory.getLogger(WebView2AuthClient.class);
    public static final String STATUS_VALUE = "status";
    private final URI startUri;
    private final URI probeUri;
    private final String expectedEmail;
    private final AtomicReference<Process> activeLogin = new AtomicReference<>();
    private final HostRunner runner;
    private final SessionVerifier verifier;

    @FunctionalInterface
    interface HostRunner {
        JsonObject run(JsonObject request, Duration timeout) throws IOException;
    }

    @FunctionalInterface
    interface SessionVerifier {
        boolean verify(ToolkitSession session) throws IOException;
    }

    public WebView2AuthClient(String startUrl, String probeUrl, String expectedEmail) {
        this(startUrl, probeUrl, expectedEmail, null);
    }

    WebView2AuthClient(String startUrl, String probeUrl, String expectedEmail, HostRunner runner) {
        this(startUrl, probeUrl, expectedEmail, runner, null);
    }

    WebView2AuthClient(String startUrl, String probeUrl, String expectedEmail, HostRunner runner, SessionVerifier verifier) {
        this.startUri = URI.create(startUrl);
        this.probeUri = URI.create(probeUrl);
        if (!"https".equalsIgnoreCase(startUri.getScheme()) || !sameOrigin(startUri, probeUri)
                || startUri.getRawUserInfo() != null || probeUri.getRawUserInfo() != null
                || expectedEmail == null || expectedEmail.isBlank()) {
            throw new IllegalArgumentException("Invalid Toolkit authentication configuration.");
        }
        this.expectedEmail = expectedEmail;
        this.runner = runner == null ? this::runHost : runner;
        this.verifier = verifier == null ? this::verifyJavaSession : verifier;
    }

    public Optional<ToolkitSession> restore() throws IOException {
        JsonObject response = exchange("load", null, Duration.ofSeconds(20));
        if ("no-session".equals(response.get(STATUS_VALUE).getAsString())) {
            return Optional.empty();
        }
        return Optional.of(readSession(response));
    }

    public ToolkitSession authenticate() throws IOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new IOException("Sign-in was cancelled.");
        }
        ToolkitSession session = readSession(exchange("login", null, Duration.ofSeconds(330)));
        if (Thread.currentThread().isInterrupted()) {
            throw new IOException("Sign-in was cancelled.");
        }
        // Prove cookie replay works in Java before persisting or allowing uploads.
        if (!verifier.verify(session)) {
            throw new IOException("Sign-in succeeded, but Toolkit rejected access from Gipter. Contact IT; this policy may require a browser.");
        }
        exchange("save", session, Duration.ofSeconds(20));
        return session;
    }

    private boolean verifyJavaSession(ToolkitSession session) throws IOException {
        HttpRequest probe = HttpRequest.newBuilder(probeUri).timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json;odata=verbose")
                .header("Cookie", session.cookieHeader())
                .GET()
                .build();
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NEVER).build()) {
            HttpResponse<String> result = client.send(probe, limitedBodyHandler());
            return ToolkitIdentity.matches(
                    result.statusCode(),
                    result.headers().firstValue("Content-Type").orElse(""),
                    result.body(),
                    expectedEmail
            );
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new IOException("Sign-in was cancelled.");
        }
    }

    // Complete only after the body arrives, so HttpRequest.timeout also bounds a stalled response body.
    static HttpResponse.BodyHandler<String> limitedBodyHandler() {
        return _ -> new HttpResponse.BodySubscriber<>() {
            private final CompletableFuture<String> result = new CompletableFuture<>();
            private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            private Flow.Subscription subscription;

            public CompletionStage<String> getBody() {
                return result;
            }

            public void onSubscribe(Flow.Subscription value) {
                subscription = value;
                value.request(1);
            }

            public void onNext(List<ByteBuffer> buffers) {
                if (result.isDone()) return;
                for (ByteBuffer buffer : buffers) {
                    if (buffer.remaining() > MAX_RESPONSE - bytes.size()) {
                        subscription.cancel();
                        result.completeExceptionally(new IOException("Toolkit identity response exceeded the supported limit."));
                        return;
                    }
                    byte[] chunk = new byte[buffer.remaining()];
                    buffer.get(chunk);
                    bytes.writeBytes(chunk);
                }
                subscription.request(1);
            }

            public void onError(Throwable error) {
                result.completeExceptionally(error);
            }

            public void onComplete() {
                result.complete(bytes.toString(StandardCharsets.UTF_8));
            }
        };
    }

    public void cancel() {
        Process process = activeLogin.get();
        if (process != null) {
            try {
                process.getOutputStream().write("cancel\n".getBytes(StandardCharsets.UTF_8));
                process.getOutputStream().flush();
            } catch (IOException _) {
                process.destroy();
            }
        }
    }

    private JsonObject exchange(String action, ToolkitSession session, Duration timeout) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", action);
        request.addProperty("startUrl", startUri.toString());
        request.addProperty("probeUrl", probeUri.toString());
        request.addProperty("expectedEmail", expectedEmail);
        request.addProperty("timeoutSeconds", 300);
        if (session != null) request.add("session", GSON.toJsonTree(session));
        JsonObject response = runner.run(request, timeout);
        String status = response.has(STATUS_VALUE) ? response.get(STATUS_VALUE).getAsString() : "invalid";
        if (!("ok".equals(status) || "no-session".equals(status))) {
            // Only fixed error text is produced by our helper; never include raw protocol/cookie data.
            if ("cancelled".equals(status)) {
                throw new IOException("Sign-in was cancelled.");
            }
            throw new IOException(response.has("message") ?
                            response.get("message").getAsString() : "Invalid WebView2 authentication response."
            );
        }
        return response;
    }

    private ToolkitSession readSession(JsonObject response) throws IOException {
        try {
            ToolkitSession session = GSON.fromJson(response.get("session"), ToolkitSession.class);
            if (session == null || !session.valid(probeUri, expectedEmail, Instant.now())) {
                throw new IllegalArgumentException();
            }
            return session;
        } catch (RuntimeException _) {
            throw new IOException("WebView2 did not return a valid Toolkit session.");
        }
    }

    private JsonObject runHost(JsonObject request, Duration timeout) throws IOException {
        if (!SystemUtils.isWindows()) {
            throw new IOException("Toolkit device SSO currently requires Windows and the WebView2 Runtime.");
        }
        Process process = new ProcessBuilder(resolveHost().toString()).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        boolean login = "login".equals(request.get("action").getAsString());
        if (login && !activeLogin.compareAndSet(null, process)) {
            process.destroy();
            throw new IOException("Sign-in is already running.");
        }
        FutureTask<String> reader = new FutureTask<>(() -> readBounded(process.getInputStream()));
        Thread.ofVirtual().start(reader);
        try {
            process.getOutputStream().write((GSON.toJson(request) + "\n").getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().flush();
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS))
                throw new IOException("WebView2 authentication timed out. Retry sign-in.");
            if (process.exitValue() != 0) throw new IOException("WebView2 authentication host exited unexpectedly.");
            try {
                return JsonParser.parseString(reader.get(5, TimeUnit.SECONDS)).getAsJsonObject();
            } catch (ExecutionException | TimeoutException | RuntimeException _) {
                throw new IOException("Invalid WebView2 authentication response.");
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new IOException("Sign-in was cancelled.");
        } finally {
            if (login) activeLogin.compareAndSet(process, null);
            if (process.isAlive()) process.destroyForcibly();
            reader.cancel(true);
            try {
                process.getInputStream().close();
            } catch (IOException ex) {
                log.error("Error while closing WebView2 authentication process.", ex);
            }
            try {
                process.getOutputStream().close();
            } catch (IOException ex) {
                log.error("Error while closing WebView2 authentication process.", ex);
            }
        }
    }

    static Path resolveHost() throws IOException {
        String configured = System.getProperty("gipter.webview2.helper");
        if (configured != null) {
            Path path = Path.of(configured).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path))
                throw new IOException("The configured Windows sign-in component could not be found. Contact IT.");
            return path;
        }
        List<Path> candidates = new ArrayList<>();
        try {
            Path location = Path.of(WebView2AuthClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path base = location.getParent();
            candidates.add(base.resolve("auth/Gipter.AuthHost.exe"));
            candidates.add(base.resolve("webview2-auth/Gipter.AuthHost.exe"));
        } catch (Exception ex) {
            log.error("Unable to resolve webview2 auth host.", ex);
        }
        candidates.add(Path.of("auth/Gipter.AuthHost.exe"));
        candidates.add(Path.of("target/webview2-auth/Gipter.AuthHost.exe"));
        return candidates.stream().filter(Files::isRegularFile).findFirst().map(p -> p.toAbsolutePath().normalize())
                .orElseThrow(() -> new IOException("The Windows sign-in component is missing. Reinstall Gipter or contact IT."));
    }

    private static String readBounded(InputStream input) throws IOException {
        byte[] bytes = input.readNBytes(MAX_RESPONSE + 1);
        if (bytes.length > MAX_RESPONSE) throw new IOException("Authentication response exceeded the supported limit.");
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static boolean sameOrigin(URI a, URI b) {
        return a.getHost() != null
                && b.getHost() != null
                && a.getScheme().equalsIgnoreCase(b.getScheme())
                && a.getHost().equalsIgnoreCase(b.getHost())
                && effectivePort(a) == effectivePort(b);
    }

    private static int effectivePort(URI uri) {
        int port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
        return uri.getPort() < 0 ? port : uri.getPort();
    }
}
