package com.github.luka5w.fileserver.server;

/**
 * This exception is used to shutdown the {@link Server}.
 * When this exception is thrown, the responsible class for the server should call {@link Server#stop()}
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public class ShutdownException extends Exception {
}
