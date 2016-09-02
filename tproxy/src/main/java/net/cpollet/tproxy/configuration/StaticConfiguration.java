package net.cpollet.tproxy.configuration;

import java.util.Arrays;
import java.util.List;

/**
 * @author Christophe Pollet
 */
public class StaticConfiguration implements Configuration {
    @Override
    public void load() throws ConfigurationException {
        // do nothing
    }

    @Override
    public List<ProxyConfiguration> proxyConfigurations() {
        return Arrays.asList(
                new ProxyConfiguration(
                        new SocketConfiguration("localhost", 8080),
                        new SocketConfiguration("example.com", 80)
                )
        );
    }
}
