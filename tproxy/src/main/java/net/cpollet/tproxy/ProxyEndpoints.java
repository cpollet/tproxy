package net.cpollet.tproxy;

import net.cpollet.tproxy.configuration.SocketConfiguration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Managers the 2 endpoints of a proxy, providing socket when a new connection is established.
 * @author Christophe Pollet
 */
public class ProxyEndpoints {
    private static final int BACKLOG = 100;
    private static final int SO_LINGER = 10;
    private static final int SO_TIMEOUT = 500;

    private final SocketConfiguration local;
    private final SocketConfiguration remote;
    private ServerSocket serverSocket;

    public ProxyEndpoints(SocketConfiguration local, SocketConfiguration remote) {
        this.local = local;
        this.remote = remote;
    }

    /**
     * Returns a new socket to remote host.
     * @return a new socket
     * @throws IOException
     */
    public Socket remoteSocket() throws IOException {
        Socket socket = new Socket(remote.host(), remote.port());
        socket.setSoLinger(true, SO_LINGER);
        socket.setSoTimeout(SO_TIMEOUT);
        return socket;
    }

    /**
     * Accepts an incoming connection on local socket and returns the corresponding socket. This method is blocking.
     * @return a new socket
     * @throws IOException
     */
    public Socket localSocket() throws IOException {
        Socket socket = serverSocket().accept();
        socket.setSoLinger(true, SO_LINGER);
        socket.setSoTimeout(SO_TIMEOUT);
        return socket;
    }

    public ServerSocket serverSocket() throws IOException {
        if (serverSocket == null) {
            serverSocket = new ServerSocket(local.port(), BACKLOG, local.host());
            serverSocket.setSoTimeout(SO_TIMEOUT);
        }
        return serverSocket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        try {
            return local.host() + ":" + local.port() + " <-> " + remote.host() + ":" + remote.port();
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
