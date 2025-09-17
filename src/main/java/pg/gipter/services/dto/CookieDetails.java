package pg.gipter.services.dto;

public class CookieDetails {
    public final String name;
    public final String value;
    public final Long expiryTime;
    public final String domain;
    public final String path;
    public final CreationTime creationTime;
    public final Long lastAccessTime;
    public final boolean persistent;
    public final boolean hostOnly;
    public final boolean secureOnly;
    public final boolean httpOnly;

    public CookieDetails(String name,
                         String value,
                         Long expiryTime,
                         String domain,
                         String path,
                         CreationTime creationTime,
                         Long lastAccessTime,
                         boolean persistent,
                         boolean hostOnly,
                         boolean secureOnly,
                         boolean httpOnly) {
        this.name = name;
        this.value = value;
        this.expiryTime = expiryTime;
        this.domain = domain;
        this.path = path;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.persistent = persistent;
        this.hostOnly = hostOnly;
        this.secureOnly = secureOnly;
        this.httpOnly = httpOnly;
    }
}
