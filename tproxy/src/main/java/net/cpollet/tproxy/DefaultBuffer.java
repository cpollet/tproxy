package net.cpollet.tproxy;

import net.cpollet.tproxy.api.Buffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Christophe Pollet
 */
public class DefaultBuffer implements Buffer {
    private final byte[] buffer;
    private final int length;

    public DefaultBuffer(byte[] buffer, int length) {
        this.buffer = buffer;
        this.length = length;
    }

    public DefaultBuffer(String content) {
        this.buffer = content.getBytes();
        this.length = this.buffer.length;
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
        stream.write(buffer, 0, length);
    }

    @Override
    public String toString() {
        return new String(buffer, 0, length);
    }
}
