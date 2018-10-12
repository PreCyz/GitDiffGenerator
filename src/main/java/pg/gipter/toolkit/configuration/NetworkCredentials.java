package pg.gipter.toolkit.configuration;

/**Created by Pawel Gawedzki on 12-Oct-2018.*/
public class NetworkCredentials {

    private String username;
    private String password;
    private String domain;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomain() {
        return domain;
    }
}
