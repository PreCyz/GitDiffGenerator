using System.Text.Json;
using Microsoft.Web.WebView2.Core;
using Microsoft.Web.WebView2.WinForms;

namespace Gipter.AuthHost;

internal sealed class AuthForm : Form
{
    private readonly Request request;
    private readonly WebView2 browser = new() { Dock = DockStyle.Fill };
    private readonly Label status = new() { Dock = DockStyle.Top, Height = 48, Padding = new Padding(8) };
    private readonly System.Windows.Forms.Timer timer = new() { Interval = 1200 };
    private readonly DateTimeOffset deadline;
    private bool probing;
    private readonly string? profileDirectory;
    private readonly Action<CoreWebView2>? initializeFixture;
    internal object Response { get; private set; } = new { status = "cancelled" };

    internal AuthForm(Request request, string? profileDirectory = null, Action<CoreWebView2>? initializeFixture = null)
    {
        this.request = request;
        this.profileDirectory = profileDirectory;
        this.initializeFixture = initializeFixture;
        deadline = DateTimeOffset.UtcNow.AddSeconds(request.TimeoutSeconds);
        Text = "Gipter — Sign in to Toolkit";
        Width = 960;
        Height = 760;
        StartPosition = FormStartPosition.CenterScreen;
        status.Text = "Sign in with " + request.ExpectedEmail + ". Complete any verification required by your organization.";
        Controls.Add(browser);
        Controls.Add(status);
        Shown += async (_, _) => await InitializeAsync();
        timer.Tick += async (_, _) => await ProbeAsync();
        FormClosed += (_, _) => { timer.Stop(); timer.Dispose(); browser.Dispose(); };
    }

    private async Task InitializeAsync()
    {
        try
        {
            var options = new CoreWebView2EnvironmentOptions { AllowSingleSignOnUsingOSPrimaryAccount = true };
            var environment = await CoreWebView2Environment.CreateAsync(null, profileDirectory ?? Path.Combine(request.DataDirectory, "Browser"), options);
            if (IsDisposed) return;
            await browser.EnsureCoreWebView2Async(environment);
            browser.CoreWebView2.Settings.AreDevToolsEnabled = false;
            browser.CoreWebView2.Settings.IsPasswordAutosaveEnabled = false;
            browser.CoreWebView2.Settings.IsGeneralAutofillEnabled = false;
            initializeFixture?.Invoke(browser.CoreWebView2);
            // Keep authentication popup navigation inside this visible, SSO-enabled control.
            browser.CoreWebView2.NewWindowRequested += (_, e) => { e.Handled = true; browser.CoreWebView2.Navigate(e.Uri); };
            browser.CoreWebView2.Navigate(request.StartUrl);
            timer.Start();
        }
        catch (WebView2RuntimeNotFoundException) { Fail("Install the Microsoft Edge WebView2 Evergreen Runtime, then retry sign-in."); }
        catch (Exception) { Fail("Unable to initialize WebView2. Contact IT if browser policy prevents sign-in."); }
    }

    private async Task ProbeAsync()
    {
        if (probing || IsDisposed) return;
        if (DateTimeOffset.UtcNow >= deadline) { Fail("Sign-in timed out. Retry when you are ready to complete verification."); return; }
        if (browser.Source == null || !Protocol.SameOrigin(browser.Source, request.StartUri)) return;
        probing = true;
        try
        {
            var script = "(async()=>{try{const r=await fetch(" + JsonSerializer.Serialize(request.ProbeUri.AbsoluteUri)
                + ",{credentials:'same-origin',mode:'same-origin',redirect:'error',signal:AbortSignal.timeout(10000),headers:{Accept:'application/json;odata=verbose'}});"
                + "if(!r.ok||!r.headers.get('content-type')?.includes('json'))return null;return await r.json();}catch{return null;}})()";
            // ExecuteScriptAsync does not await a returned Promise. Post the eventual result as JSON instead.
            var completed = new TaskCompletionSource<string>();
            var nonce = Guid.NewGuid().ToString("N");
            void OnMessage(object? sender, CoreWebView2WebMessageReceivedEventArgs e)
            {
                if (Uri.TryCreate(e.Source, UriKind.Absolute, out var source) && Protocol.SameOrigin(source, request.StartUri))
                {
                    try
                    {
                        using var message = JsonDocument.Parse(e.WebMessageAsJson);
                        if (message.RootElement.TryGetProperty("id", out var id) && id.GetString() == nonce
                            && message.RootElement.TryGetProperty("value", out var value)) completed.TrySetResult(value.GetRawText());
                    }
                    catch (JsonException) { }
                    catch (InvalidOperationException) { }
                }
            }
            browser.CoreWebView2.WebMessageReceived += OnMessage;
            string json;
            try
            {
                await browser.ExecuteScriptAsync(script + ".then(value=>window.chrome.webview.postMessage({id:"
                    + JsonSerializer.Serialize(nonce) + ",value}));");
                json = await completed.Task.WaitAsync(TimeSpan.FromSeconds(15));
            }
            finally { if (!IsDisposed) browser.CoreWebView2.WebMessageReceived -= OnMessage; }
            if (IsDisposed || json == "null") return;
            if (!Protocol.MatchesIdentity(json, request.ExpectedEmail))
            {
                status.Text = "The signed-in account does not match " + request.ExpectedEmail + ". Sign out of Toolkit and select the correct work account.";
                return;
            }
            var cookies = await browser.CoreWebView2.CookieManager.GetCookiesAsync(request.ProbeUri.AbsoluteUri);
            var selected = cookies.Where(c => c.Name is "FedAuth" or "Goto").Select(c => new Cookie(
                c.Name, c.Value, c.Domain, c.Path, c.IsSession ? null : new DateTimeOffset(c.Expires.ToUniversalTime()).ToUnixTimeMilliseconds(), c.IsSession)).ToList();
            var session = new Session(request.Origin, request.ExpectedEmail, selected);
            if (!new SessionStore(request).IsValid(session, true)) return;
            Response = new { status = "ok", session };
            Close();
        }
        catch (Exception) { /* Navigation during interactive sign-in is expected; the next timer tick probes again. */ }
        finally { probing = false; }
    }

    private void Fail(string message)
    {
        Response = new { status = "error", message };
        Close();
    }
}
