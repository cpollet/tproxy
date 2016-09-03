package net.cpollet.tproxy;

import net.cpollet.tproxy.configuration.Configuration;
import net.cpollet.tproxy.configuration.ConfigurationException;
import net.cpollet.tproxy.configuration.ProxyConfiguration;
import net.cpollet.tproxy.configuration.StaticConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;

public class TProxy {
    private final static Logger LOG = LogManager.getLogger();
    private final Configuration configuration;

    private TProxy(Configuration configuration) {
        this.configuration = configuration;
    }

    public static void main(String[] args) {
        LOG.info("Starting TProxy...");

        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java -jar tproxy.jar CONFIG_FILE");
        }

        try {
            new TProxy(
                    //new PropertiesConfiguration(args[0])
                    new StaticConfiguration()
            ).run();
        }
        catch (Exception e) {
            LOG.error("An error occurred: {}", e.getMessage(), e);
        }
    }

    private void run() throws ConfigurationException, UnknownHostException, ProxyException {
        configuration.load();
        ThreadId threadId = new ThreadId();
        for (ProxyConfiguration proxyConfiguration : configuration.proxyConfigurations()) {
            new ProxyThread(
                    proxyConfiguration,
                    threadId
            ).start();
        }
    }


}
