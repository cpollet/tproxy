package net.cpollet.tproxy.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author Christophe Pollet
 */
public class PropertiesConfiguration implements Configuration {
    private final String filePath;
    private Properties properties;

    public PropertiesConfiguration(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void load() throws ConfigurationException {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(filePath));
        }
        catch (IOException e) {
            throw new ConfigurationException("Unable to read configuration from " + filePath, e);
        }
    }

    @Override
    public List<ProxyConfiguration> proxyConfigurations() {
        return null;
    }
}
