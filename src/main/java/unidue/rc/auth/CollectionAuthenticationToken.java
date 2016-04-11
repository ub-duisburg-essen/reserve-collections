package unidue.rc.auth;


import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Created by nils on 05.11.15.
 */
public class CollectionAuthenticationToken extends UsernamePasswordToken {

    private String realm;

    public CollectionAuthenticationToken(String username, String password) {
        super(username, password);
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
