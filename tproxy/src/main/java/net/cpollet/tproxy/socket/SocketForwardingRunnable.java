package net.cpollet.tproxy.socket;

import net.cpollet.tproxy.DefaultBuffer;
import net.cpollet.tproxy.api.Buffer;
import net.cpollet.tproxy.api.FilterChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Christophe Pollet
 */
public class SocketForwardingRunnable implements Runnable {
    private static final Logger LOG = LogManager.getLogger();
    private static final int BUFFER_SIZE = 1024;

    private final ForwardingSocket forwardingSocket;
    private final Socket source;
    private final Socket destination;
    private final FilterChain filterChain;
    private final Direction direction;

    private boolean done;

    public SocketForwardingRunnable(ForwardingSocket forwardingSocket, Socket socket1, Socket socket2, FilterChain filterChain, Direction direction) {
        this.forwardingSocket = forwardingSocket;
        this.source = socket1;
        this.destination = socket2;
        this.filterChain = filterChain;
        this.direction = direction;
        this.done = false;
    }

    private String label(Socket socket1, Socket socket2, Direction direction) {
        if (direction == Direction.LOCAL_TO_REMOTE) {
            return socket1.getLocalSocketAddress() + "->" + socket2.getRemoteSocketAddress();
        } else {
            return socket1.getRemoteSocketAddress() + "->" + socket2.getLocalSocketAddress();
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName(Thread.currentThread().getName() + "|" + label(source, destination, direction));
        LOG.info("Starting...");
        try {
            InputStream inputStream = source.getInputStream();
            OutputStream outputStream = destination.getOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            while (!done) {
                int bytesRead = read(inputStream, buffer);

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

    private int read(InputStream inputStream, byte[] buffer) {
        try {
            return inputStream.read(buffer, 0, BUFFER_SIZE);
        }
        catch (Exception e) {
            // we get here when the source was not able to read any data in READ_TIMEOUT ms. we have a timeout in order
            // to close connection when the thread receives an interruption without having to wait EOF.
            // Since we did not read any byte, we return 0. We don't return -1 because we did not get EOF either.
            return 0;
        }
    }

    public void terminate() {
        synchronized (forwardingSocket) {
            LOG.info("Should close connection");
            SocketForwardingRunnable peer = forwardingSocket.getPeer(this);
            done = true;

            if (peer.done()) {
                LOG.info("Peer already done, closing connection");
                closeSocket(source);
                closeSocket(destination);
            } else {
                LOG.info("Asking peer to close connection");
                peer.terminate();
            }

            LOG.debug("dying");
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

    public enum Direction {
        LOCAL_TO_REMOTE,
        REMOTE_TO_LOCAL
    }
}
