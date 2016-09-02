package net.cpollet.tproxy.configuration;

/**
 * @author Christophe Pollet
 */
public class ProxyConfiguration {
    private final SocketConfiguration in;
    private final SocketConfiguration out;

    public ProxyConfiguration(SocketConfiguration in, SocketConfiguration out) {
        this.in = in;
        this.out = out;
    }

    public SocketConfiguration in() {
        return in;
    }

    public SocketConfiguration out() {
        return out;
    }
}
