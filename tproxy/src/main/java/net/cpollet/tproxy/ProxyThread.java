package net.cpollet.tproxy;

import net.cpollet.tproxy.api.Filter;
import net.cpollet.tproxy.api.FilterChain;
import net.cpollet.tproxy.configuration.ProxyConfiguration;
import net.cpollet.tproxy.filters.DefaultFilterChain;
import net.cpollet.tproxy.filters.HttpHostFilter;
import net.cpollet.tproxy.filters.LoggingFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Christophe Pollet
 */
public class ProxyThread extends Thread {
    private static final Logger LOG = LogManager.getLogger();
    public static final int SO_LINGER = 10;

    private final ServerSocket listeningSocket;
    private final InetAddress dstHost;
    private final int dstPort;
    private final List<StreamCopyThread> connections;
    private final Map<StreamCopyThread, StreamCopyThread> peers;

    private final Object lock = new Object();
    private final ThreadId threadId;

    public ProxyThread(ProxyConfiguration proxyConfiguration, ThreadId threadId) throws ProxyException, UnknownHostException {
        this.connections = new LinkedList<>();
        this.peers = new HashMap<>();

        this.dstHost = proxyConfiguration.out().host();
        this.dstPort = proxyConfiguration.out().port();
        this.threadId = threadId;

        InetAddress host = proxyConfiguration.in().host();
        int port = proxyConfiguration.in().port();

        setName(threadId.get() + "|" + host + ":" + port + " <-> " + dstHost + ":" + dstPort);

        try {
            listeningSocket = new ServerSocket(port, 100, host);
        }
        catch (IOException e) {
            throw new ProxyException("Unable to start listening " + host + " on port " + port, e);
        }
    }

    @Override
    public void run() {
        LOG.info("Starting...");
        try {
            while (!interrupted()) {
                Socket localSocket = accept();

                LOG.info("New connection from {}:{}", localSocket.getInetAddress(), localSocket.getPort());

                Socket remoteSocket = openSocketToRemote();

                String localToRemoteName = threadId.get() + "|" + localSocket.getInetAddress() + ":" + localSocket.getLocalPort() + " -> " + remoteSocket.getInetAddress() + ":" + remoteSocket.getPort();
                String remoteToLocalName = threadId.get() + "|" + remoteSocket.getInetAddress() + ":" + remoteSocket.getPort() + " -> " + localSocket.getInetAddress() + ":" + localSocket.getLocalPort();

                Filter httpHostFilter = new HttpHostFilter();
                Filter loggingFilter = new LoggingFilter();

                FilterChain filterChain = new DefaultFilterChain(Arrays.asList(
                        httpHostFilter, loggingFilter
                ));

                StreamCopyThread copyLocalToRemote = new StreamCopyThread(localSocket, remoteSocket, this, localToRemoteName, filterChain);
                StreamCopyThread copyRemoteToLocal = new StreamCopyThread(remoteSocket, localSocket, this, remoteToLocalName, filterChain);

                synchronized (lock) {
                    peers.put(copyLocalToRemote, copyRemoteToLocal);
                    peers.put(copyRemoteToLocal, copyLocalToRemote);
                    connections.add(copyLocalToRemote);
                    connections.add(copyRemoteToLocal);
                    LOG.info("Connections [{}] and [{}] open", copyLocalToRemote.getName(), copyRemoteToLocal.getName());
                    LOG.info("Active connections: {}", connections.size());
                    copyLocalToRemote.start();
                    copyRemoteToLocal.start();
                }
            }
        }
        catch (Exception e) {
            LOG.error("An error occurred: " + e.getMessage(), e);
        }
        cleanup();
    }

    private Socket accept() throws ProxyException {
        try {
            Socket socket = listeningSocket.accept();
            socket.setSoLinger(true, SO_LINGER);
            return socket;
        }
        catch (IOException e) {
            throw new ProxyException("Unable to accept connection", e);
        }
    }

    private Socket openSocketToRemote() throws ProxyException {
        try {
            Socket socket = new Socket(dstHost, dstPort);
            socket.setSoLinger(true, SO_LINGER);
            return socket;
        }
        catch (IOException e) {
            throw new ProxyException("Unable to connect to remote host " + dstHost + ":" + dstPort, e);
        }
    }

    private void cleanup() {
        synchronized (lock) {
            for (StreamCopyThread connection : connections) {
                LOG.info("Closing connection [{}]", connection.getName());
                connection.interrupt();
            }
        }

        LOG.info("Waiting for all connections to close...");

        while (true) {
            synchronized (lock) {
                if (connections.isEmpty()) {
                    return;
                }
            }
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                LOG.warn("Interrupted");
            }
        }
    }

    public void closeConnection(StreamCopyThread initiatorThread) {
        StreamCopyThread peer = getPeer(initiatorThread);

        connections.remove(initiatorThread);
        connections.remove(peer);

        LOG.info("Connections [{}] and [{}] closed", initiatorThread.getName(), peer.getName());
        LOG.info("Active connections: {}", connections.size());
    }

    public Object getLock() {
        return lock;
    }

    public StreamCopyThread getPeer(StreamCopyThread thread) {
        if (!peers.containsKey(thread)) {
            throw new IllegalArgumentException("Thread " + thread.getName() + " has no peer");
        }

        return peers.get(thread);
    }
}
