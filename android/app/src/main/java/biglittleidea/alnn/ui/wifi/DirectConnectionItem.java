package biglittleidea.alnn.ui.wifi;

public class DirectConnectionItem {
    public String title;
    public String protocol;
    public String host;
    public short port;
    public String node;

    public DirectConnectionItem(String title, String protocol, String host, short port, String node) {
        this.title = title;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.node = node;
    }
}
