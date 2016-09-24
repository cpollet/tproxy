package net.cpollet.tproxy.filters.http;

/**
 * @author Christophe Pollet
 */
public interface HttpRequestHeaders {
    void replace(String name, String value);

    void remove(String header);

    void add(String name, String value);
}
