package net.cpollet.tproxy.api;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Christophe Pollet
 */
public interface Buffer {
    void writeTo(OutputStream stream) throws IOException;
}
