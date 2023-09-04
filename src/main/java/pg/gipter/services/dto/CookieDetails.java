package pg.gipter.services.dto;

public class CookieDetails {
    public final String name;
    public final String value;
    public final Long expiryTime;
    public final String domain;
    public final String path;
    public final boolean secureOnly;
    public final boolean httpOnly;

    public CookieDetails(String name,
                         String value,
                         Long expiryTime,
                         String domain,
                         String path,
                         boolean secureOnly,
                         boolean httpOnly) {
        this.name = name;
        this.value = value;
        this.expiryTime = expiryTime;
        this.domain = domain;
        this.path = path;
        this.secureOnly = secureOnly;
        this.httpOnly = httpOnly;
    }
}
