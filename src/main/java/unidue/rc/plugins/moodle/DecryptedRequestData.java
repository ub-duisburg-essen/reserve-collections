package unidue.rc.plugins.moodle;


/**
 * Created by nils on 16.06.15.
 */
public class DecryptedRequestData {

    private String requestData;

    private byte[] symkey;

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public byte[] getSymkey() {
        return symkey;
    }

    public void setSymkey(byte[] symkey) {
        this.symkey = symkey;
    }
}
