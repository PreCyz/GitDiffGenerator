package pg.gipter.toolkit.dto;

public class User {

    private final int id;
    private final String loginName;
    private final String fullName;
    private final String email;

    public User(int id, String loginName, String fullName, String email) {
        this.id = id;
        this.loginName = loginName;
        this.fullName = fullName;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}
