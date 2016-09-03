package net.cpollet.tproxy;

import net.cpollet.tproxy.api.Buffer;
import net.cpollet.tproxy.api.Filter;
import net.cpollet.tproxy.api.FilterChain;
import net.cpollet.tproxy.filters.DefaultFilterChain;
import net.cpollet.tproxy.filters.HttpHostFilter;
import net.cpollet.tproxy.filters.LoggingFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * @author Christophe Pollet
 */
public class StreamCopyThread extends Thread {
    private static final Logger LOG = LogManager.getLogger();
    private static final int BUFFER_SIZE = 1024;

    private final Socket source;
    private final Socket destination;
    private final ProxyThread proxyThread;
    private final FilterChain filterChain;

    private boolean done;

    public StreamCopyThread(Socket source, Socket destination, ProxyThread proxyThread, String tag, FilterChain filterChain) {
        super(tag);
        this.source = source;
        this.destination = destination;
        this.proxyThread = proxyThread;
        this.filterChain = filterChain;
        this.done = false;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = source.getInputStream();
            OutputStream outputStream = destination.getOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            while (!isInterrupted()) {
                int bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE);

                if (bytesRead < 0) {
                    break;
                }

                Buffer filteredBuffer = filterChain.doFilter(new DefaultBuffer(buffer, bytesRead));
                filteredBuffer.writeTo(outputStream);
            }
        }
        catch (Exception e) {
            LOG.error("An error occurred: " + e.getMessage(), e);
        }

        terminate();
    }

    private void terminate() {
        LOG.info("Should close connection");
        StreamCopyThread peer = proxyThread.getPeer(this);
        done = true;

        synchronized (proxyThread.getLock()) {
            if (peer.done()) {
                LOG.info("Peer [{}] already done, closing connection", peer.getName());
                closeSocket(source);
                closeSocket(destination);
                proxyThread.closeConnection(this);
            } else {
                LOG.info("Asking peer [{}] to close connection", peer.getName());
                peer.interrupt();
            }
        }
    }

    private boolean done() {
        return done;
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        }
        catch (IOException e) {
            LOG.warn("Unable to close socket: " + e.getMessage(), e);
        }
    }
}
