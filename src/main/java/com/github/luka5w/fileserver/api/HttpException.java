package com.github.luka5w.fileserver.api;

import com.github.luka5w.http.HttpStatusCode;

/**
 * An exception which can be thrown during a request.
 * <p>
 *     This exception must be caught in the API version ({@link APIVersion#handle(com.sun.net.httpserver.HttpExchange)}).
 * </p>
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public class HttpException extends Exception {
    private final int status;
    private final String message;

    /**
     * Initiates an exception with a HTTP status code and a message depending on the status code.
     * @param status The status code.
     *
     * @since 1.0.0
     */
    public HttpException(int status) {
        this(status, HttpStatusCode.findByCode(status).getDescription());
    }

    /**
     * Initiates an exception with an HTTP status code and a custom message.
     *
     * @param status The status code.
     * @param message The custom status message.
     *
     * @since 1.0.0
     */
    public HttpException(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Returns the status message.
     *
     * @return The status message.
     *
     * @since 1.0.0
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns the status code.
     *
     * @return The HTTP status code.
     *
     * @since 1.0.0
     */
    public int getStatus() {
        return this.status;
    }


}
