package net.cpollet.tproxy.configuration;

/**
 * @author Christophe Pollet
 */
public class ConfigurationException extends Exception {
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
