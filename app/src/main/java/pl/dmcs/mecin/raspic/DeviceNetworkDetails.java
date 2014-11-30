package pl.dmcs.mecin.raspic;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mecin on 20.10.14.
 */
public class DeviceNetworkDetails implements Parcelable {

    public DeviceNetworkDetails(String ssid, String ip) {
        this.ip = ip;
        this.ssid = ssid;
    }

    private String ip;

    private String ssid;

    public String getSSID() {
        return this.ssid;
    }

    public String getIP() {
        return this.ip;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("SSID: ");
        result.append(this.ssid);
        result.append("\nIP: ");
        result.append(this.ip);

        return result.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
