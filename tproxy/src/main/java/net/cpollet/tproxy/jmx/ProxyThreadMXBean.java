package net.cpollet.tproxy.jmx;

/**
 * @author Christophe Pollet
 */
public interface ProxyThreadMXBean {
    String description();

    int connectionsCount();

    void closeAllStreams();

    void finish();
}
