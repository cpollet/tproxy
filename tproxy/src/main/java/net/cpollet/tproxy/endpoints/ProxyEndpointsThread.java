package net.cpollet.tproxy.endpoints;

import net.cpollet.tproxy.api.FilterChain;
import net.cpollet.tproxy.filters.DefaultFilterChain;
import net.cpollet.tproxy.filters.HttpHostFilter;
import net.cpollet.tproxy.jmx.ProxyThreadMXBean;
import net.cpollet.tproxy.socket.ForwardingSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Christophe Pollet
 */
public class ProxyEndpointsThread extends Thread implements ProxyThreadMXBean {
    private static final Logger LOG = LogManager.getLogger();

    private final ProxyEndpoints proxyEndpoints;
    private final List<ForwardingSocket> forwardingSockets;

    public ProxyEndpointsThread(ProxyEndpoints proxyEndpoints) throws UnknownHostException {
        this.forwardingSockets = Collections.synchronizedList(new LinkedList<>());
        this.proxyEndpoints = proxyEndpoints;
        setName(getName() + "|" + proxyEndpoints.toString());
    }

    @Override
    public void run() {
        LOG.info("Starting...");
        try {
            while (!isInterrupted()) {
                Socket localSocket;

                try {
                    localSocket = proxyEndpoints.localSocket();
                }
                catch (SocketTimeoutException e) {
                    continue;
                }

                Socket remoteSocket = proxyEndpoints.remoteSocket();

                LOG.info("New connection from {}", localSocket.getLocalSocketAddress());

                FilterChain filterChain = new DefaultFilterChain(Arrays.asList(
                        new HttpHostFilter()//, new LoggingFilter()
                ));

                ForwardingSocket forwardingSocket = new ForwardingSocket(this, localSocket, remoteSocket, filterChain);
                forwardingSockets.add(forwardingSocket);

                forwardingSocket.start();
                LOG.info("Active streams: {}", forwardingSockets.size());
            }
        }
        catch (Exception e) {
            LOG.error("An error occurred: " + e.getMessage(), e);
        }

        if (isInterrupted()) {
            LOG.info("Received close event");
        }

        cleanup();
    }

    private void cleanup() {
        forwardingSockets.forEach(ForwardingSocket::close);

        LOG.info("Waiting for all connections to close...");

        while (true) {
            if (forwardingSockets.isEmpty()) {
                return;
            }
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                LOG.warn("Interrupted");
            }
        }
    }

    public void closed(ForwardingSocket forwardingSocket) {
        forwardingSockets.remove(forwardingSocket);
        LOG.info("Active streams: {}", forwardingSockets.size());
    }

    /**
     * MBean method
     *
     * @return
     */
    @Override
    public String description() {
        return proxyEndpoints.toString();
    }

    @Override
    public int connectionsCount() {
        return forwardingSockets.size();
    }

    /**
     * MBean method
     */
    @Override
    public void closeAllStreams() {
        forwardingSockets.forEach(ForwardingSocket::close);
    }

    @Override
    public void finish() {
        interrupt();
    }
}
