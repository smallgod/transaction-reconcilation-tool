/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import java.io.File;
import java.io.FileNotFoundException;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 *
 * @author smallgod
 */
public class ServerConnection {

    //PUT IN CONFIGS FILE
    public static final int HTTP_PORT = 9007;
    public static final int HTTPS_PORT = 9005;
    public static final int ADMIN_PORT = 8180;
    
    public static final int IDLE_TIME = 30000;
    public static final int REQUEST_HEADER_SIZE = 8192;
    public static final int RESPONSE_HEADER_SIZE = 8192;
    public static final int OUTPUT_BUFFER_SIZE = 32768;
    public static final String JETTY_HOME = System.getProperty("jetty.home", "../jetty-distribution/target/distribution");
    public static final String JETTY_DIST_KEYSTORE = "../../jetty-distribution/target/distribution/demo-base/etc/keystore";
    public static final String KEYSTORE_PATH = System.getProperty("example.keystore", JETTY_DIST_KEYSTORE);
    public static final String KEYSTORE_PASS = "OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4";
    public static final String KEYSTORE_MGR_PASS = "OBF:1u2u1wml1z7s1z7a1wnl1u2g";
    
    private final HttpConfiguration httpConfig;

    private ServerConnection() {

        //put everything here that you need loaded once        
        httpConfig = initCommonConfigs();
    }

    private static class ServerConnectorSingletonHolder {

        private static final ServerConnection INSTANCE = new ServerConnection();
    }

    public static ServerConnection getInstance() {
        return ServerConnectorSingletonHolder.INSTANCE;
    }

    protected Object readResolve() {
        return getInstance();
    }

    /**
     * collection of configuration information appropriate for http and
     * httpsConnector
     *
     * @return
     */
    private HttpConfiguration initCommonConfigs() {

        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(HTTPS_PORT);
        config.setOutputBufferSize(OUTPUT_BUFFER_SIZE);
        config.setRequestHeaderSize(REQUEST_HEADER_SIZE);
        config.setResponseHeaderSize(RESPONSE_HEADER_SIZE);
        config.setSendServerVersion(true);
        config.setSendDateHeader(false);

        return config;
    }

    /**
     * 
     * @param server
     * @return 
     */
    public Connector getDefaultConnector(Server server) {

        ServerConnector httpDefaultConnector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        httpDefaultConnector.setPort(HTTP_PORT);
        httpDefaultConnector.setIdleTimeout(IDLE_TIME);

        return httpDefaultConnector;
    }

    /**
     * 
     * @param server
     * @return
     * @throws FileNotFoundException 
     */
    public Connector getHTTPSConnector(Server server) throws FileNotFoundException {

        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        File keystoreFile = new File(KEYSTORE_PATH);
        
        if (!keystoreFile.exists()) {
            //throw new FileNotFoundException(keystoreFile.getAbsolutePath());
        }
        
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
        sslContextFactory.setKeyStorePassword(KEYSTORE_PASS);
        sslContextFactory.setKeyManagerPassword(KEYSTORE_MGR_PASS);

        ServerConnector httpsConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));
        httpsConnector.setPort(HTTPS_PORT);

        return httpsConnector;
    }

    /**
     * 
     * @param server
     * @return 
     */
    public Connector getAdminDefaultConnector(Server server) {

        ServerConnector adminDefaultConnector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        adminDefaultConnector.setPort(ADMIN_PORT);
        adminDefaultConnector.setName("admin");

        return adminDefaultConnector;
    }
}
