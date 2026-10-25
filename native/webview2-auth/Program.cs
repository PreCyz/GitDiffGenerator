using System.Text;
using System.Text.Json;
using Microsoft.Web.WebView2.Core;

namespace Gipter.AuthHost;

internal static class Program
{
    [STAThread]
    private static int Main(string[] args)
    {
        object response;
        try
        {
            if (args.SequenceEqual(new[] { "--check-runtime" }))
            {
                response = new { status = "ok", version = CoreWebView2Environment.GetAvailableBrowserVersionString() };
            }
            else
            {
                using var input = new StreamReader(Console.OpenStandardInput(), Encoding.UTF8);
                var line = ReadBoundedLine(input);
                var request = JsonSerializer.Deserialize<Request>(line, Protocol.Json) ?? throw new ArgumentException();
                request.Validate();
                var store = new SessionStore(request);
                if (request.Action == "login")
                {
                    Directory.CreateDirectory(request.DataDirectory);
                    using var ownership = new FileStream(Path.Combine(request.DataDirectory, "login.lock"), FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.None);
                    ApplicationConfiguration.Initialize();
                    using var form = new AuthForm(request);
                    _ = Task.Run(() =>
                    {
                        if (input.ReadLine() == "cancel")
                        {
                            try { form.BeginInvoke(() => form.Close()); } catch (InvalidOperationException) { }
                        }
                    });
                    Application.Run(form);
                    response = form.Response;
                }
                else if (request.Action == "load")
                {
                    var session = store.Load();
                    response = new { status = session == null ? "no-session" : "ok", session };
                }
                else
                {
                    if (request.Action == "save") store.Save(request.Session ?? throw new ArgumentException());
                    else store.Clear();
                    response = new { status = "ok" };
                }
            }
        }
        catch (WebView2RuntimeNotFoundException) { response = new { status = "error", message = "Install the Microsoft Edge WebView2 Evergreen Runtime, then retry sign-in." }; }
        catch (IOException) { response = new { status = "error", message = "Cannot access the authentication profile. Close another Gipter sign-in window and retry." }; }
        catch (Exception) { response = new { status = "error", message = "WebView2 authentication could not complete. Retry sign-in or contact IT." }; }
        using var output = new StreamWriter(Console.OpenStandardOutput(), new UTF8Encoding(false));
        output.WriteLine(JsonSerializer.Serialize(response, Protocol.Json));
        return 0;
    }

    private static string ReadBoundedLine(TextReader input)
    {
        var buffer = new StringBuilder();
        for (var c = input.Read(); c != -1 && c != '\n'; c = input.Read())
        {
            if (buffer.Length >= 131072) throw new ArgumentException();
            buffer.Append((char)c);
        }
        return buffer.ToString();
    }
}
