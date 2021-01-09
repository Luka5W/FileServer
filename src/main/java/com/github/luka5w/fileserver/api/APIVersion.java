package com.github.luka5w.fileserver.api;

import com.github.luka5w.util.cli.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.HashMap;

/**
 * An API version (aka {@link com.sun.net.httpserver.HttpContext} which contains the endpoints.
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public abstract class APIVersion implements HttpHandler {
    protected final Logger LOGGER;
    private final String version;

    /**
     * Creates a new API version (aka {@link com.sun.net.httpserver.HttpContext}.
     *
     * @param version The version (i.e. endpoint name) of the API version.
     *
     * @since 1.0.0
     */
    protected APIVersion(String version) {
        this.LOGGER = API.getLogger(version);
        this.version = version;
        LOGGER.log("Registering API...");
        this.init();
        LOGGER.log("Registered API.");
    }

    /**
     * Returns the version name (i.e. endpoint name) of this API version.
     *
     * @return The version name.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Version-specific initiation.
     * This method is called in the constructor between logging the registration of this API version.
     */
    protected abstract void init();

    /**
     * This method is called when the request to this API version should be served.
     *
     * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
     */
    public abstract void handle(HttpExchange httpExchange);

    /**
     * An API endpoint (e.g. path).
     * (No need to use this...)
     */
    public interface Endpoint {
        /**
         * This method is called when the request to this endpoint should be served.
         *
         * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
         * @param method The request method passed in the HTTP request header.
         * @param query A HashMap containing the query parameters (i.e. http://localhost:443?[parameters])
         * @param user The authenticated user.
         * @throws HttpException When anything (expected and unexpected) went wrong.
         */
        void handle(HttpExchange httpExchange, String method, HashMap<String, String> query, String user) throws HttpException;
    }
}
