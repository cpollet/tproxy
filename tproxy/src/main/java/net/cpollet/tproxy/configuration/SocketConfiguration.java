package net.cpollet.tproxy.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Christophe Pollet
 */
public class SocketConfiguration {
    private final String host;
    private final int port;

    public SocketConfiguration(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public InetAddress host() throws UnknownHostException {
        return InetAddress.getByName(host);
    }

    public int port() {
        return port;
    }
}
