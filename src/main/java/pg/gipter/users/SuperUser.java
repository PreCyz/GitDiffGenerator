package pg.gipter.users;

import org.bson.types.ObjectId;

public class SuperUser {
    public static final String COLLECTION_NAME = "users";

    private ObjectId id;
    private String username;
    private String password;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
