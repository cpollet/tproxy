package net.cpollet.tproxy;

import net.cpollet.tproxy.configuration.Configuration;
import net.cpollet.tproxy.configuration.ProxyConfiguration;
import net.cpollet.tproxy.configuration.StaticConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

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
            TProxy tproxy = new TProxy(
                    //new PropertiesConfiguration(args[0])
                    new StaticConfiguration()
            );

            tproxy.run();
        }
        catch (Exception e) {
            LOG.error("An error occurred: {}", e.getMessage(), e);
        }
    }

    private void run() throws Exception {
        configuration.load();
        ThreadId threadId = new ThreadId();
        for (ProxyConfiguration proxyConfiguration : configuration.proxyConfigurations()) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

            ProxyThread thread = new ProxyThread(
                    threadId,
                    new ProxyEndpoints(proxyConfiguration.in(), proxyConfiguration.out())
            );

            ObjectName name = new ObjectName("net.cpollet.tproxy:type=ProxyThread-" + thread.getId());

            mbs.registerMBean(thread, name);

            thread.start();
        }
    }
}
