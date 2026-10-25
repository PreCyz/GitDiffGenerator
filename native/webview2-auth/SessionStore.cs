using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

namespace Gipter.AuthHost;

internal sealed class SessionStore(Request request, string? directory = null)
{
    private string DirectoryPath => directory ?? request.DataDirectory;
    private string FilePath => Path.Combine(DirectoryPath, "session.bin");
    private byte[] Entropy => Encoding.UTF8.GetBytes("Gipter.WebView2.v1:" + request.Origin);

    internal Session? Load()
    {
        if (!File.Exists(FilePath)) return null;
        if (new FileInfo(FilePath).Length > 131072) throw new InvalidDataException();
        var decrypted = ProtectedData.Unprotect(File.ReadAllBytes(FilePath), Entropy, DataProtectionScope.CurrentUser);
        try
        {
            var session = JsonSerializer.Deserialize<Session>(decrypted, Protocol.Json);
            return session != null && IsValid(session, false) ? session : null;
        }
        finally { CryptographicOperations.ZeroMemory(decrypted); }
    }

    internal void Save(Session session)
    {
        if (!IsValid(session, true)) throw new InvalidDataException("Invalid Toolkit session.");
        // Session-only cookies must never become persistent credentials.
        var persistent = session with { Cookies = session.Cookies.Where(c => !c.Session).ToList() };
        if (!IsValid(persistent, false)) { Clear(); return; }
        Directory.CreateDirectory(DirectoryPath);
        var plain = JsonSerializer.SerializeToUtf8Bytes(persistent, Protocol.Json);
        var temporary = FilePath + "." + Guid.NewGuid().ToString("N") + ".tmp";
        try
        {
            File.WriteAllBytes(temporary, ProtectedData.Protect(plain, Entropy, DataProtectionScope.CurrentUser));
            File.Move(temporary, FilePath, true);
        }
        finally
        {
            CryptographicOperations.ZeroMemory(plain);
            if (File.Exists(temporary)) File.Delete(temporary);
        }
    }

    internal void Clear() { if (File.Exists(FilePath)) File.Delete(FilePath); }

    internal bool IsValid(Session session, bool allowSessionCookies)
    {
        if (session.Origin != request.Origin || !session.Email.Equals(request.ExpectedEmail, StringComparison.OrdinalIgnoreCase)
            || session.Cookies.Count is < 1 or > 2 || session.Cookies.Select(c => c.Name).Distinct().Count() != session.Cookies.Count)
            return false;
        var now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        foreach (var cookie in session.Cookies)
        {
            var domain = cookie.Domain.TrimStart('.');
            if (cookie.Name is not ("FedAuth" or "Goto") || string.IsNullOrEmpty(cookie.Value)
                || cookie.Value.Length > 32768 || cookie.Value.Any(c => c < 0x21 || c > 0x7e || c == ';')
                || !(request.StartUri.IdnHost.Equals(domain, StringComparison.OrdinalIgnoreCase)
                    || request.StartUri.IdnHost.EndsWith("." + domain, StringComparison.OrdinalIgnoreCase))
                || string.IsNullOrEmpty(domain) || string.IsNullOrEmpty(cookie.Path) || !cookie.Path.StartsWith('/')
                || !(request.ProbeUri.AbsolutePath == cookie.Path || request.ProbeUri.AbsolutePath.StartsWith(cookie.Path.EndsWith('/') ? cookie.Path : cookie.Path + "/", StringComparison.Ordinal))
                || (cookie.Session ? !allowSessionCookies : cookie.ExpiresUtcMillis == null || cookie.ExpiresUtcMillis <= now))
                return false;
        }
        return session.Cookies.Any(c => c.Name == "FedAuth");
    }
}
