package net.cpollet.tproxy.configuration;

import java.util.List;

/**
 * @author Christophe Pollet
 */
public interface Configuration {
    void load() throws Exception;

    List<? extends ProxyConfiguration> proxies();
}
