# Toolkit sign-in

Toolkit uploads and document retrieval use the Windows native WebView2 sign-in helper. Configure your Toolkit username and destination folder, then sign in with the corresponding work account. The expected email is built from the configured username and email domain; these identify the account, not a stored password.

## Sign in

1. Start Gipter on Windows with WebView2 Evergreen Runtime installed.
2. If no usable saved session is available, Gipter opens a sign-in status window and the native work-account sign-in window.
3. Complete your organization's work-account sign-in, including any required MFA or device checks.
4. Gipter verifies the returned Toolkit identity against the configured account and checks that the session works for Java's Toolkit requests.
5. Successful initial sign-in continues startup in the same application process. If no configurations exist, the wizard follows.

Use **Cancel** in the status window to stop an in-progress attempt. A failed/cancelled attempt can be retried with **Retry** when offered. Startup and main-window connection checks can request another sign-in when the session is unusable. The session-expiry label is a status display, not a refresh button. Operations wait for authentication rather than submitting an upload while sign-in is incomplete.

Signing in successfully in a browser is not sufficient by itself: the expected account and subsequent Toolkit request must also succeed. If the site requires browser-only access that Java cannot use, Gipter reports the failure; contact IT with the error details.

## Saved sessions

Persistent session data is stored below `%LOCALAPPDATA%\Gipter\WebView2Auth\<site-origin-hash>`. The helper maintains a WebView2 profile and encrypts its saved session for the current Windows user with DPAPI. The stored session is checked for the site, account, cookie scope, and expiry before reuse.

Only eligible persistent cookies can be saved for reuse. A session composed only of session cookies requires another sign-in after that application session ends. Expired, incompatible, or unreadable saved data also causes another sign-in. Encryption does not make a copied session portable between Windows accounts.

The application does not ask you to place authentication cookies or a Toolkit password in configuration files. For local generation without Toolkit authentication, follow [Examples](https://github.com/PreCyz/GitDiffGenerator/wiki/Examples). Skipping authentication cannot provide access to Toolkit documents or uploads.

## Scheduled work

Scheduled and accepted missed uploads request authentication when necessary and wait for it to complete. Cancelled or failed sign-in prevents that submission. Keep the application running and be available to complete interactive sign-in when required; scheduling does not bypass account or device policies.

See [Troubleshooting](https://github.com/PreCyz/GitDiffGenerator/wiki/Troubleshooting) for missing components, account mismatch, and failed-session checks.

Source: [Main.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/Main.java), [FXWebService.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/services/FXWebService.java), [WebView2AuthClient.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/services/auth/WebView2AuthClient.java), [SessionStore.cs](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/native/webview2-auth/SessionStore.cs), [Protocol.cs](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/native/webview2-auth/Protocol.cs).
