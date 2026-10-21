package pg.gipter.toolkit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Executes Toolkit (SharePoint) requests inside an application-owned Microsoft Edge profile using the
 * Chrome DevTools Protocol. The browser holds the authenticated session, so no cookies are handled by the application.
 */
public final class EdgeCdpClient {

    public record Response(int status, byte[] body) {}

    private static final Logger logger = LoggerFactory.getLogger(EdgeCdpClient.class);
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String DEBUG_ADDRESS = "127.0.0.1";
    private static final String DEBUG_PORT_PROPERTY = "gipter.edgeDebugPort";
    private static final int DEFAULT_DEBUG_PORT = 9222;
    private static final int BROWSER_START_TIMEOUT_SECONDS = 10;
    private static final long POLL_MILLIS = 200L;
    private static final int REQUEST_TIMEOUT_SECONDS = 120;
    private static final Object BROWSER_LOCK = new Object();
    private static Process edgeProcess;

    private EdgeCdpClient() {}

    public static Response request(String method, String url, Map<String, String> headers, byte[] body) throws IOException {
        String debuggerUrl = findPage(origin(url));
        JsonObject result = evaluate(debuggerUrl, Map.of(
                "expression", fetchScript(method, url, headers, body),
                "awaitPromise", true,
                "returnByValue", true,
                "userGesture", true
        ));
        if (result.has("exceptionDetails")) {
            throw new IOException("Browser request failed, Microsoft Edge is probably not authenticated: "
                    + result.get("exceptionDetails"));
        }
        JsonObject value = result.getAsJsonObject("result").getAsJsonObject("value");
        return new Response(value.get("status").getAsInt(), Base64.getDecoder().decode(value.get("body").getAsString()));
    }

    private static String fetchScript(String method, String url, Map<String, String> headers, byte[] body) {
        String bodyExpression = body == null
                ? "undefined"
                : "Uint8Array.from(atob(" + GSON.toJson(Base64.getEncoder().encodeToString(body)) + "), c => c.charCodeAt(0))";
        return "(async () => {"
                + " const r = await fetch(" + GSON.toJson(url) + ", {"
                + " method: " + GSON.toJson(method) + ", credentials: 'include',"
                + " headers: " + GSON.toJson(headers) + ", body: " + bodyExpression + " });"
                + " const bytes = new Uint8Array(await r.arrayBuffer());"
                + " let binary = '';"
                + " for (let i = 0; i < bytes.length; i += 0x8000) binary += String.fromCharCode(...bytes.subarray(i, i + 0x8000));"
                + " return { status: r.status, body: btoa(binary) };"
                + " })()";
    }

    private static String origin(String url) {
        URI uri = URI.create(url);
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    private static String findPage(String origin) throws IOException {
        ensureBrowserRunning();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(BROWSER_START_TIMEOUT_SECONDS);
        do {
            for (JsonElement element : JsonParser.parseString(httpGet(debugPages())).getAsJsonArray()) {
                JsonObject page = element.getAsJsonObject();
                if ("page".equals(page.get("type").getAsString()) && page.get("url").getAsString().startsWith(origin)) {
                    return page.get("webSocketDebuggerUrl").getAsString();
                }
            }
            sleep();
        } while (System.nanoTime() < deadline);
        throw new IOException("Microsoft Edge has no open Toolkit page. Authenticate in Microsoft Edge and try again.");
    }

    private static void ensureBrowserRunning() throws IOException {
        synchronized (BROWSER_LOCK) {
            if (edgeProcess != null && edgeProcess.isAlive()) {
                return;
            }
            edgeProcess = startEdge();
            waitForDebugging();
        }
    }

    private static Process startEdge() throws IOException {
        Path profile = Path.of(
                System.getenv().getOrDefault("LOCALAPPDATA", Path.of(System.getProperty("user.home"), "AppData", "Local").toString()),
                "GitDiffGenerator", "EdgeProfile"
        );
        Files.createDirectories(profile);
        List<String> command = new java.util.ArrayList<>(List.of(
                findEdgeExecutable(),
                "--remote-debugging-address=" + DEBUG_ADDRESS,
                "--remote-debugging-port=" + debugPort(),
                "--user-data-dir=" + profile,
                "--headless=new",
                ArgName.toolkitUserFolderUrl.defaultValue()
        ));
        logger.info("Starting headless Microsoft Edge with profile [{}].", profile);
        Process process = new ProcessBuilder(command).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> process.destroy()));
        return process;
    }

    private static void waitForDebugging() throws IOException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(BROWSER_START_TIMEOUT_SECONDS);
        while (System.nanoTime() < deadline) {
            if (isDebuggingAvailable()) {
                return;
            }
            sleep();
        }
        throw new IOException("Microsoft Edge DevTools endpoint did not become available within "
                + BROWSER_START_TIMEOUT_SECONDS + " seconds.");
    }

    private static boolean isDebuggingAvailable() {
        try {
            httpGet(debugPages());
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static URI debugPages() {
        return URI.create("http://" + DEBUG_ADDRESS + ":" + debugPort() + "/json");
    }

    private static int debugPort() {
        String configuredPort = System.getProperty(DEBUG_PORT_PROPERTY, String.valueOf(DEFAULT_DEBUG_PORT));
        try {
            int port = Integer.parseInt(configuredPort);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
            return port;
        } catch (NumberFormatException ex) {
            logger.warn("Invalid {} value [{}]. Using default port {}.",
                    DEBUG_PORT_PROPERTY, configuredPort, DEFAULT_DEBUG_PORT);
            return DEFAULT_DEBUG_PORT;
        }
    }

    private static String httpGet(URI uri) throws IOException {
        try {
            HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected status " + response.statusCode() + " from " + uri);
            }
            return response.body();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
    }

    private static void sleep() throws IOException {
        try {
            Thread.sleep(POLL_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
    }

    private static JsonObject evaluate(String debuggerUrl, Map<String, Object> params) throws IOException {
        CompletableFuture<JsonObject> result = new CompletableFuture<>();
        WebSocket socket = null;
        try {
            socket = HTTP.newWebSocketBuilder().buildAsync(URI.create(debuggerUrl), new DevToolsListener(result)).join();
            socket.sendText(GSON.toJson(Map.of("id", 1, "method", "Runtime.evaluate", "params", params)), true).join();
            return result.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        } catch (Exception ex) {
            throw new IOException("DevTools command [Runtime.evaluate] failed: " + ex.getMessage(), ex);
        } finally {
            if (socket != null) {
                socket.sendClose(WebSocket.NORMAL_CLOSURE, "complete").exceptionally(ignored -> null);
            }
        }
    }

    private static String findEdgeExecutable() throws IOException {
        for (String variable : List.of("ProgramFiles", "ProgramFiles(x86)", "LOCALAPPDATA")) {
            String root = System.getenv(variable);
            if (root != null) {
                Path edge = Path.of(root, "Microsoft", "Edge", "Application", "msedge.exe");
                if (Files.isRegularFile(edge)) {
                    return edge.toString();
                }
            }
        }
        throw new IOException("Microsoft Edge was not found in a standard Windows installation location.");
    }

    private static final class DevToolsListener implements WebSocket.Listener {
        private final CompletableFuture<JsonObject> result;
        private final StringBuilder message = new StringBuilder();

        private DevToolsListener(CompletableFuture<JsonObject> result) {
            this.result = result;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            message.append(data);
            if (last) {
                JsonObject payload = JsonParser.parseString(message.toString()).getAsJsonObject();
                message.setLength(0);
                if (payload.has("id") && payload.get("id").getAsInt() == 1) {
                    if (payload.has("error")) {
                        result.completeExceptionally(new IOException(payload.get("error").toString()));
                    } else {
                        result.complete(payload.getAsJsonObject("result"));
                    }
                }
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            result.completeExceptionally(new IOException("DevTools connection closed: " + reason));
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            result.completeExceptionally(error);
        }
    }
}
