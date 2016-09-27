package net.cpollet.tproxy.configuration.json;

import net.cpollet.tproxy.configuration.ProxyConfiguration;

import java.util.List;

/**
 * @author Christophe Pollet
 */
public class RootJsonConfiguration {
    private List<ProxyJsonConfiguration> proxies;

    public List<? extends ProxyConfiguration> proxies() {
        return proxies;
    }

    public void initialize() throws Exception {
        for (ProxyJsonConfiguration proxy : proxies) {
            proxy.initialize();
        }
    }
}
