package com.github.luka5w.fileserver.server;

import com.github.luka5w.fileserver.Main;
import com.github.luka5w.fileserver.api.API;
import com.github.luka5w.util.cli.Logger;
import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * A webserver using {@link com.github.luka5w.fileserver.api.APIVersion}s from an {@link API} as contexts.
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public class Server {
    private static final Logger LOGGER = Main.getLogger("Server");

    private final API api;
    private final HttpServer server;

    private boolean running = false;

    /**
     * Initializes an {@link HttpServer}.
     *
     * @param api The API to use.
     * @param port The port to use.
     * @param backlog The maximum amount of requests.
     *
     * @throws IOException When the address is invalid or the HttpServer could not be created.
     *
     * @since 1.0.0
     */
    public Server(API api, String address, int port, int backlog) throws IOException {
        this.api = api;
        LOGGER.warn("Running an unencrypted Server is insecure.");
        LOGGER.log("Initializing Server...");
        InetSocketAddress sockAddress = this.getAddress(address, port);
        this.server = HttpServer.create(sockAddress, backlog);
        LOGGER.debug("Initialized Server.");
        this.addContexts();
    }

    /**
     * Initializes an {@link HttpsServer}.
     * <p>
     *     command to generate the (self-signed) keystore:
     *     keytool -genkeypair -keyalg RSA -alias selfsigned -keystore [filename].jks -storepass [password] -validity 360 -keysize 2048
     * </p>
     *
     * @param api The API to use.
     * @param port The port to use.
     * @param backlog The maximum amount of requests.
     * @param keystorePath The path to the keystore.
     * @param keystorePassword The password of the keystore.
     * @throws IOException When the address is invalid or the HttpServer could not be created.
     * @throws GeneralSecurityException When something went wrong with the certificate.
     *
     * @since 1.0.0
     */
    public Server(API api, String address, int port, int backlog, String keystorePath, String keystorePassword) throws IOException, GeneralSecurityException {
        // TODO: 28.12.2020 @pre0.0.2 [bug] anything here screws up the server if a request comes from curl. for dev: using com.sun.net.httpserver.HttpServer "fixes" the issue...
        this.api = api;
        LOGGER.log("Initializing Server...");
        InetSocketAddress sockAddress = this.getAddress(address, port);
        /*
         * Server::server is instance of HttpServer.
         * The additional variable is to avoid unnecessary casts to HttpsServer
         * during tls initialization.
        */
        HttpsServer server = HttpsServer.create(sockAddress, backlog);
        this.server = server;

        SSLContext context = SSLContext.getInstance("TLS");
        char[] pw = keystorePassword.toCharArray();

        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fIn = new FileInputStream(keystorePath);
        ks.load(fIn, pw);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, pw);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        server.setHttpsConfigurator(new HttpsConfigurator(context) {
            @Override
            public void configure(HttpsParameters params) {
                SSLContext context = getSSLContext();
                SSLEngine engine = context.createSSLEngine();
                params.setNeedClientAuth(false);
                params.setCipherSuites(engine.getEnabledCipherSuites());
                params.setProtocols(engine.getEnabledProtocols());
                SSLParameters sslParams = context.getSupportedSSLParameters();
                params.setSSLParameters(sslParams);
            }
        });

        LOGGER.debug("Initialized Server.");
        this.addContexts();
    }

    /**
     * Starts the server.
     *
     * @since 1.0.0
     */
    public void start() {
        if (this.running) {
            LOGGER.warn("Server already running. Ignoring start request.");
        }
        else {
            LOGGER.log("Server started. Listening at " + (this.server instanceof HttpsServer ? "https://" : "http://") + this.server.getAddress().toString());
            this.server.start();
            this.running = true;
        }
    }

    /**
     * Stops the server.
     *
     * @since 1.0.0
     */
    public void stop() {
        if (this.running) {
            LOGGER.log("Server stopped.");
            this.server.stop(0);
        }
        else {
            LOGGER.warn("Server is not running. Ignoring stop request.");
        }
    }

    /**
     * Creates a new address from an IP address or a hostname and a port.
     *
     * @param address The IP address or hostname.
     * @param port The port.
     * @return The new address.
     *
     * @throws UnknownHostException See {@link InetAddress#getByName(String)}.
     *
     * @since 1.0.0
     */
    private InetSocketAddress getAddress(String address, int port) throws UnknownHostException {
        if (address == null || address.isEmpty()) return new InetSocketAddress(port);
        return new InetSocketAddress(InetAddress.getByName(address), port);
    }

    /**
     * Adds the root context of the {@link API} and the {@link com.github.luka5w.fileserver.api.APIVersion}s as contexts to the server contexts.
     * @see HttpServer#createContext(String, HttpHandler)
     *
     * @since 1.0.0
     */
    private void addContexts() {
        LOGGER.log("Adding API Contexts...");
        LOGGER.log("Adding root context (/)...");
        this.server.createContext("/", this.api::info);
        LOGGER.debug("Done.");
        this.api.getVersions().forEach(apiVersion -> {
            LOGGER.log("Adding Context /" + apiVersion.getVersion() + "...");
            this.server.createContext("/" + apiVersion.getVersion(), apiVersion::handle);
            LOGGER.debug("Done.");
        });
        LOGGER.debug("Added API Contexts.");
    }

    /**
     * Sends an empty response to the client using {@link #sendResponse(HttpExchange, int, String, String)}.
     *
     * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
     * @param status The HTTP status code.
     *
     * @since 1.0.0
     */
    public static void sendResponse(HttpExchange httpExchange, int status) {
        sendResponse(httpExchange, status, null, "text/plain");
    }
    /**
     * Sends a response with a payload and a content type to the client.
     *
     * <p>
     *     Charset: UTF-8
     * </p>
     *
     * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
     * @param status The HTTP status code.
     * @param payload The response body.
     * @param contentType The content type.
     *
     * @since 1.0.0
     */
    public static void sendResponse(HttpExchange httpExchange, int status, String payload, String contentType) {
        LOGGER.debug("Response: c=" + status + " t=" + contentType + " p=" + payload);
        httpExchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        byte[] response = new byte[0];
        if (payload != null && !payload.isEmpty()) response = payload.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(status, response.length);
            os.write(response);
        }
        catch (IOException e) {
            LOGGER.exception("Failed to respond: ", e);
        }
    }
}
