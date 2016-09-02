package net.cpollet.tproxy;

/**
 * @author Christophe Pollet
 */
public class ProxyException extends Exception {
    public ProxyException(String s, Throwable e) {
        super(s, e);
    }
}
