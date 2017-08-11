package org.apache.commons.net.ftp;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * This test determines if explicitly setting the PROT mode on an already established FTPS connection removes the Proxy settings.
 */
public class FTPSClientProxyPROTTest extends TestCase {

    /** We're using the open ftps server from rebex here because I found no other. */
    private String ftpsHost= "test.rebex.net";
    /** The port of the implicit ftps. */
    private int ftpsPort = 990;
    /** Username for the FTPS Server. */
    private String ftpsUser = "demo";
    /** Password for the FTPS Server. */
    private String ftpsPassword="password";
    /** Some socks proxy, taken from https://www.socks-proxy.net/ - YMMV. */
    private String proxyHost="207.55.44.35";
    /** Corresponding Socks proxy Port. */
    private int proxyPort = 35923;
    /** Proxy is not a HTTP Proxy. */
    private Proxy.Type proxyType = Proxy.Type.SOCKS;

    /**
     * Test whether setting the PROT mode affects the proxy setting in any way (it should not).
     * @throws IOException if the communication with either the ftps server or the proxy fails.
     */
    public void testFTPSClientWithProxy() throws IOException {
        FTPSClient client = new FTPSClient(true);
        client.setProxy(new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort)));
        client.setRemoteVerificationEnabled(false);
        client.connect(ftpsHost, ftpsPort);
        client.login(ftpsUser, ftpsPassword);
        String proxyRepresentation  = client.getProxy() == null? null : client.getProxy().toString();
        // This line triggers NET-642!
        client.execPROT("P");
        // In 3.6, after this line the proxy is now set back to null.
        String newProxyRepresentation = client.getProxy() == null ? null: client.getProxy().toString();
        Assert.assertNotNull(proxyRepresentation);
        Assert.assertNotNull(newProxyRepresentation);
        // We're doing string-comparison here to allow for object-recreation of the proxy (as long as the string representation matches, we don't care)
        Assert.assertEquals(proxyRepresentation,newProxyRepresentation);

    }
}
