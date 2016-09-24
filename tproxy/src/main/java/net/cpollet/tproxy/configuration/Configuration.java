package net.cpollet.tproxy.configuration;

import java.util.List;

/**
 * @author Christophe Pollet
 */
public interface Configuration {
    List<ProxyConfiguration> proxiesConfiguration();

    void load() throws Exception;
}
