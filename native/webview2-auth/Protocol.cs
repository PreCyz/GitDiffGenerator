using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

namespace Gipter.AuthHost;

internal record Cookie(string Name, string Value, string Domain, string Path, long? ExpiresUtcMillis, bool Session)
{
    public override string ToString() => "Cookie[redacted]";
}
internal record Session(string Origin, string Email, List<Cookie> Cookies)
{
    public override string ToString() => "Session[redacted]";
}
internal record Request(string Action, string StartUrl, string ProbeUrl, string ExpectedEmail, int TimeoutSeconds = 300, Session? Session = null)
{
    internal Uri StartUri => new(StartUrl, UriKind.Absolute);
    internal string Origin => StartUri.GetLeftPart(UriPartial.Authority);
    internal Uri ProbeUri => new(ProbeUrl, UriKind.Absolute);

    internal void Validate()
    {
        if (StartUri.Scheme != Uri.UriSchemeHttps || !string.IsNullOrEmpty(StartUri.UserInfo)
            || !string.IsNullOrEmpty(StartUri.Query) || !string.IsNullOrEmpty(StartUri.Fragment)
            || !Protocol.SameOrigin(StartUri, ProbeUri) || !string.IsNullOrEmpty(ProbeUri.UserInfo)
            || string.IsNullOrWhiteSpace(ExpectedEmail) || ExpectedEmail.Length > 254
            || TimeoutSeconds is < 30 or > 600)
            throw new ArgumentException("Invalid authentication configuration.");
        if (Action is not ("login" or "load" or "save" or "clear"))
            throw new ArgumentException("Unsupported authentication operation.");
    }

    internal string DataDirectory => System.IO.Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Gipter", "WebView2Auth",
        Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(Origin))).ToLowerInvariant());
}

internal static class Protocol
{
    internal static readonly JsonSerializerOptions Json = new() { PropertyNamingPolicy = JsonNamingPolicy.CamelCase, PropertyNameCaseInsensitive = true };
    internal static bool SameOrigin(Uri left, Uri right) => left.Scheme == right.Scheme
        && left.IdnHost.Equals(right.IdnHost, StringComparison.OrdinalIgnoreCase) && left.Port == right.Port;

    internal static bool MatchesIdentity(string json, string expectedEmail)
    {
        using var doc = JsonDocument.Parse(json);
        var user = doc.RootElement;
        if (user.TryGetProperty("d", out var d)) user = d;
        return user.TryGetProperty("Id", out var id) && id.TryGetInt32(out var userId) && userId > 0
            && user.TryGetProperty("Email", out var email)
            && string.Equals(email.GetString(), expectedEmail, StringComparison.OrdinalIgnoreCase);
    }
}
