package miless.model;


import miless.model.auto._User;
import unidue.rc.model.IntPrimaryKey;

public class User extends _User implements IntPrimaryKey {

    private static final long serialVersionUID = 5276803396295368737L;
    
    public static final String USER_SESSION_ATTRIBUTE = "unidue.rc.currentuser";

    String come;
    String get;
    String some;

    @Override
    public Integer getId() {
        return getUserid();
    }

    public void setUsername(String username) {
        if (username != null)
            username = username.trim().toLowerCase();

        super.setUsername(username);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id='").append(getUserid()).append('\'');
        sb.append(", username='").append(getUsername()).append('\'');
        sb.append(", realm='").append(getRealm()).append('\'');
        sb.append(", realname='").append(getRealname()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
