package pl.dmcs.mecin.raspic;

/**
 * Created by mecin on 20.10.14.
 */
public class DeviceNetworkDetails {
    public DeviceNetworkDetails(String ip) {
        this.ip = ip;
    }
    private String ip;


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.ip);
        result.append("\n");
        result.append("kolejny jakis parametr");

        return result.toString();
    }
}
