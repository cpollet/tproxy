package net.cpollet.tproxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Christophe Pollet
 */
public class StreamCopyThread extends Thread {
    private static final Logger LOG = LogManager.getLogger();
    private static final int BUFFER_SIZE = 1024;

    private final Socket source;
    private final Socket destination;
    private final ProxyThread proxyThread;
    private final Object lock = new Object();
    private boolean done;

    public StreamCopyThread(Socket source, Socket destination, ProxyThread proxyThread, String tag) {
        super(tag);
        this.source = source;
        this.destination = destination;
        this.proxyThread = proxyThread;
        this.done = false;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = inputStream();
            OutputStream outputStream = outputStream();

            byte[] buffer = new byte[BUFFER_SIZE];

            while (!isInterrupted()) {
                int bytesRead = read(inputStream, buffer);

                if (bytesRead < 0) {
                    break;
                }

                write(outputStream, buffer, bytesRead);
            }
        }
        catch (Exception e) {
            LOG.error("An error occurred: " + e.getMessage(), e);
        }

        terminate();
    }

    private InputStream inputStream() throws ProxyException {
        try {
            return source.getInputStream();
        }
        catch (IOException e) {
            throw new ProxyException("Unable to read from socket", e);
        }
    }

    private OutputStream outputStream() throws ProxyException {
        try {
            return destination.getOutputStream();
        }
        catch (IOException e) {
            throw new ProxyException("Unable to write to socket", e);
        }
    }

    private int read(InputStream inputStream, byte[] buffer) throws ProxyException {
        try {
            return inputStream.read(buffer, 0, BUFFER_SIZE);
        }
        catch (IOException e) {
            throw new ProxyException("Unable to read from socket", e);
        }
    }

    private void write(OutputStream outputStream, byte[] buffer, int bytesRead) throws ProxyException {
        try {
            outputStream.write(buffer, 0, bytesRead);
        }
        catch (IOException e) {
            throw new ProxyException("Unable to write to socket", e);
        }
    }

    private void terminate() {
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
