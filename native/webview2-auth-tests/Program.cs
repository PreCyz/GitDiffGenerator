using System.Security.Cryptography;
using System.Text;
using Gipter.AuthHost;
using Cookie = Gipter.AuthHost.Cookie;
using Microsoft.Web.WebView2.Core;
using System.Text.Json;

// Small executable regression harness: nonzero exit on failure; never opens a browser or contacts a tenant.
var request = new Request("save", "https://toolkit.example.test/site/Lists/Items/",
    "https://toolkit.example.test/site/_api/web/currentuser", "employee@example.test");
request.Validate();
var testDirectory = Path.Combine(Path.GetTempPath(), "gipter-auth-tests-" + Guid.NewGuid().ToString("N"));
Directory.CreateDirectory(testDirectory);
var store = new SessionStore(request, testDirectory);
var cookie = new Cookie("FedAuth", "synthetic-test-secret", ".example.test", "/", DateTimeOffset.UtcNow.AddHours(1).ToUnixTimeMilliseconds(), false);
var session = new Session(request.Origin, request.ExpectedEmail, [cookie]);
var passed = 0;
void Check(bool condition, string description)
{
    if (!condition) throw new Exception("FAIL: " + description);
    passed++;
}
try
{
    Check(!Protocol.SameOrigin(request.StartUri, new Uri("https://toolkit.example.test.evil.test/")), "exact origin matching");
    Check(Protocol.MatchesIdentity("{\"d\":{\"Id\":42,\"Email\":\"EMPLOYEE@example.test\"}}", request.ExpectedEmail), "expected principal");
    Check(!Protocol.MatchesIdentity("{\"Id\":42,\"Email\":\"other@example.test\"}", request.ExpectedEmail), "wrong account rejection");
    Check(store.Load() == null, "missing session");
    Check(store.IsValid(session, true), "valid session");
    store.Save(session);
    var encryptedPath = Path.Combine(testDirectory, "session.bin");
    Check(!Encoding.UTF8.GetString(File.ReadAllBytes(encryptedPath)).Contains(cookie.Value), "no plaintext credential persistence");
    Check(store.Load()?.Cookies.Single().Value == cookie.Value, "user-scoped DPAPI roundtrip");
    var encrypted = File.ReadAllBytes(encryptedPath);
    encrypted[encrypted.Length / 2] ^= 0x55;
    File.WriteAllBytes(encryptedPath, encrypted);
    var rejected = false;
    try { store.Load(); } catch (CryptographicException) { rejected = true; }
    Check(rejected, "tampered ciphertext rejection");
    store.Save(session with { Cookies = [cookie with { Session = true, ExpiresUtcMillis = null }] });
    Check(store.Load() == null, "session-only credentials are not persisted");
    Check(!store.IsValid(session with { Cookies = [cookie with { Domain = "evil.test" }] }, true), "cookie domain restriction");
    Check(!store.IsValid(session with { Cookies = [cookie with { Path = "/sit" }] }, true), "cookie path boundary");
    Check(!store.IsValid(session with { Cookies = [cookie with { Value = "value\r\nInjected: header" }] }, true), "header injection rejection");
    Check(!store.IsValid(session with { Cookies = [cookie with { ExpiresUtcMillis = 1 }] }, true), "expired cookie rejection");
    Exception? browserFailure = null;
    var fixturePassed = false;
    var fixtureThread = new Thread(() =>
    {
        try
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            using var form = new AuthForm(request with { Action = "login" }, Path.Combine(testDirectory, "Browser"), webview =>
            {
                var synthetic = webview.CookieManager.CreateCookie(cookie.Name, cookie.Value, request.StartUri.Host, "/");
                synthetic.IsSecure = true;
                synthetic.Expires = DateTime.UtcNow.AddHours(1);
                webview.CookieManager.AddOrUpdateCookie(synthetic);
                webview.AddWebResourceRequestedFilter("*", CoreWebView2WebResourceContext.All);
                webview.WebResourceRequested += (_, e) =>
                {
                    // Every request is intercepted. The test never contacts Toolkit or an identity provider.
                    var probe = e.Request.Uri == request.ProbeUri.AbsoluteUri;
                    var body = probe ? "{\"d\":{\"Id\":42,\"Email\":\"employee@example.test\"}}" : "<html><body>Local authentication fixture</body></html>";
                    e.Response = webview.Environment.CreateWebResourceResponse(new MemoryStream(Encoding.UTF8.GetBytes(body)), 200, "OK",
                        "Content-Type: " + (probe ? "application/json" : "text/html"));
                };
            }) { Opacity = 0, ShowInTaskbar = false };
            using var timeout = new System.Windows.Forms.Timer { Interval = 20000 };
            timeout.Tick += (_, _) => form.Close();
            timeout.Start();
            Application.Run(form);
            using var response = JsonDocument.Parse(JsonSerializer.Serialize(form.Response, Protocol.Json));
            fixturePassed = response.RootElement.GetProperty("status").GetString() == "ok"
                && response.RootElement.GetProperty("session").GetProperty("cookies")[0].GetProperty("value").GetString() == cookie.Value;
        }
        catch (Exception ex) { browserFailure = ex; }
    });
    fixtureThread.SetApartmentState(ApartmentState.STA);
    fixtureThread.Start();
    if (!fixtureThread.Join(TimeSpan.FromSeconds(40))) throw new Exception("WebView2 fixture timed out.");
    if (browserFailure != null) throw new Exception("WebView2 fixture failed.", browserFailure);
    Check(fixturePassed, "real WebView2 initialization, promise probe and supported cookie retrieval");
    Console.WriteLine($"Passed {passed} native authentication checks, including the isolated WebView2 fixture.");
}
finally
{
    // Only this uniquely named directory created by this harness is removed.
    var resolved = Path.GetFullPath(testDirectory);
    if (!resolved.StartsWith(Path.GetFullPath(Path.GetTempPath()), StringComparison.OrdinalIgnoreCase)
        || !Path.GetFileName(resolved).StartsWith("gipter-auth-tests-", StringComparison.Ordinal)) throw new InvalidOperationException();
    for (var attempt = 0; attempt < 50 && Directory.Exists(resolved); attempt++)
    {
        try { Directory.Delete(resolved, true); }
        catch (IOException) { Thread.Sleep(100); }
        catch (UnauthorizedAccessException) { Thread.Sleep(100); }
    }
}
