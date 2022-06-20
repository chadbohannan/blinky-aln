package biglittleidea.alnn;

import java.net.InetAddress;

public class BeaconInfo {
    public String protocol;
    public String host;
    public short port;

    public BeaconInfo(String protocol, String host, short port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public boolean equals(BeaconInfo info) {
        return info.protocol.equals(protocol) &&
            info.host.equals(host) &&
            info.port == port;
    }
}
