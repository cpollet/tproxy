package net.cpollet.tproxy.configuration;

/**
 * @author Christophe Pollet
 */
public interface ProxyConfiguration {
    String fromHost();

    int fromPort();

    String toHost();

    int toPort();

    void outputFiltersConfiguration();
}
