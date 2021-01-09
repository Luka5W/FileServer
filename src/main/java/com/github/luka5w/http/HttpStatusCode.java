package com.github.luka5w.http;

import jdk.jfr.Experimental;

/**
 * All HTTP status codes and their descriptions.
 *
 * <p>source: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status</p>
 *
 * @author Lukas https://github.com/luka5w
 * @version 1.0.0
 */
public enum HttpStatusCode {
    // 1XX Information Response
    /**
     * This interim response indicates that everything so far is OK and that the client should continue the request, or ignore the response if the request is already finished.
     *
     * @since 1.0.0
     */
    CONTINUE(100, "Continue"),
    /**
     * This code is sent in response to an Upgrade request header from the client, and indicates the protocol the server is switching to.
     *
     * @since 1.0.0
     */
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    /**
     * This code indicates that the server has received and is processing the request, but no response is available yet.
     *
     * @since 1.0.0
     */
    PROCESSING(102, "Processing"),
    /**
     * This status code is primarily intended to be used with the Link header, letting the user agent start preloading resources while the server prepares a response.
     *
     * @since 1.0.0
     */
    EARLY_HINTS(103, "Early Hints"),

    // 2XX Success
    /**
     * The request has succeeded. The meaning of the success depends on the HTTP method:
     * - GET: The resource has been fetched and is transmitted in the message body.
     * - HEAD: The entity headers are in the message body.
     * - PUT or POST: The resource describing the result of the action is transmitted in the message body.
     * - TRACE: The message body contains the request message as received by the server.
     *
     * @since 1.0.0
     */
    OK(200, "OK"),
    /**
     * The request has succeeded and a new resource has been created as a result. This is typically the response sent after POST requests, or some PUT requests.
     *
     * @since 1.0.0
     */
    CREATED(201, "Created"),
    /**
     * The request has been received but not yet acted upon. It is noncommittal, since there is no way in HTTP to later send an asynchronous response indicating the outcome of the request. It is intended for cases where another process or server handles the request, or for batch processing.
     *
     * @since 1.0.0
     */
    ACCEPTED(202, "Accepted"),
    /**
     * This response code means the returned meta-information is not exactly the same as is available from the origin server, but is collected from a local or a third-party copy. This is mostly used for mirrors or backups of another resource. Except for that specific case, the "200 OK" response is preferred to this status.
     *
     * @since 1.0.0
     */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    /**
     * There is no content to send for this request, but the headers may be useful. The user-agent may update its cached headers for this resource with the new ones.
     * @since 1.0.0
     */
    NO_CONTENT(204,  "No Content"),
    /**
     * Tells the user-agent to reset the document which sent this request.
     *
     * @since 1.0.0
     */
    RESET_CONTENT(205, "Reset Content"),
    /**
     * This response code is used when the Range header is sent from the client to request only part of a resource.
     *
     * @since 1.0.0
     */
    PARTIAL_CONTENT(206, "Partial Content"),
    /**
     * Conveys information about multiple resources, for situations where multiple status codes might be appropriate.
     *
     * @since 1.0.0
     */
    MULTI_STATUS(207, "Multi-Status"),
    /**
     * Used inside a &lt;dav:propstat>&gt; response element to avoid repeatedly enumerating the internal members of multiple bindings to the same collection.
     *
     * @since 1.0.0
     */
    ALREADY_REPORTED(208, "Already Reported"),
    /**
     * The server has fulfilled a GET request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance.
     *
     * @since 1.0.0
     */
    IM_USED(226, "IM Used"),

    // 3XX Redirecton messages
    /**
     * The request has more than one possible response. The user-agent or user should choose one of them. (There is no standardized way of choosing one of the responses, but HTML links to the possibilities are recommended so the user can pick.)
     *
     * @since 1.0.0
     */
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    /**
     * The URL of the requested resource has been changed permanently. The new URL is given in the response.
     *
     * @since 1.0.0
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /**
     * This response code means that the URI of requested resource has been changed temporarily. Further changes in the URI might be made in the future. Therefore, this same URI should be used by the client in future requests.
     *
     * @since 1.0.0
     */
    FOUND(302, "Found"),
    /**
     * The server sent this response to direct the client to get the requested resource at another URI with a GET request.
     *
     * @since 1.0.0
     */
    SEE_OTHER(303, "See Other"),
    /**
     * This is used for caching purposes. It tells the client that the response has not been modified, so the client can continue to use the same cached version of the response.
     *
     * @since 1.0.0
     */
    NOT_MODIFIED(304, "Not Modified"),
    /**
     * Defined in a previous version of the HTTP specification to indicate that a requested response must be accessed by a proxy. It has been deprecated due to security concerns regarding in-band configuration of a proxy.
     *
     * @since 1.0.0
     */
    @Deprecated
    USE_PROXY(305, "Use Proxy"),
    /**
     * This response code is no longer used; it is just reserved. It was used in a previous version of the HTTP/1.1 specification.
     *
     * @since 1.0.0
     */
    @Deprecated
    SWITCH_PROXY(306, "Switch Proxy"),
    /**
     * The server sends this response to direct the client to get the requested resource at another URI with same method that was used in the prior request. This has the same semantics as the 302 Found HTTP response code, with the exception that the user agent must not change the HTTP method used: If a POST was used in the first request, a POST must be used in the second request.
     *
     * @since 1.0.0
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    /**
     * This means that the resource is now permanently located at another URI, specified by the Location: HTTP Response header. This has the same semantics as the 301 Moved Permanently HTTP response code, with the exception that the user agent must not change the HTTP method used: If a POST was used in the first request, a POST must be used in the second request.
     *
     * @since 1.0.0
     */
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    // 4XX Client error responses
    /**
     * The server could not understand the request due to invalid syntax.
     *
     * @since 1.0.0
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * Although the HTTP standard specifies "unauthorized", semantically this response means "unauthenticated". That is, the client must authenticate itself to get the requested response.
     *
     * @since 1.0.0
     */
    @Experimental
    UNAUTHORIZED(401, "Unauthorized"),
    /**
     * This response code is reserved for future use. The initial aim for creating this code was using it for digital payment systems, however this status code is used very rarely and no standard convention exists.
     *
     * @since 1.0.0
     */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /**
     * The client does not have access rights to the content; that is, it is unauthorized, so the server is refusing to give the requested resource. Unlike 401, the client's identity is known to the server.
     *
     * @since 1.0.0
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * The server can not find the requested resource. In the browser, this means the URL is not recognized. In an API, this can also mean that the endpoint is valid but the resource itself does not exist. Servers may also send this response instead of 403 to hide the existence of a resource from an unauthorized client. This response code is probably the most famous one due to its frequent occurrence on the web.
     *
     * @since 1.0.0
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * The request method is known by the server but has been disabled and cannot be used. For example, an API may forbid DELETE-ing a resource. The two mandatory methods, GET and HEAD, must never be disabled and should not return this error code.
     *
     * @since 1.0.0
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /**
     * This response is sent when the web server, after performing server-driven content negotiation, doesn't find any content that conforms to the criteria given by the user agent.
     *
     * @since 1.0.0
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /**
     * This is similar to 401 but authentication is needed to be done by a proxy.
     *
     * @since 1.0.0
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    /**
     * This response is sent on an idle connection by some servers, even without any previous request by the client. It means that the server would like to shut down this unused connection. This response is used much more since some browsers, like Chrome, Firefox 27+, or IE9, use HTTP pre-connection mechanisms to speed up surfing. Also note that some servers merely shut down the connection without sending this message.
     *
     * @since 1.0.0
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /**
     * This response is sent when a request conflicts with the current state of the server.
     *
     * @since 1.0.0
     */
    CONFLICT(409, "Conflict"),
    /**
     * This response is sent when the requested content has been permanently deleted from server, with no forwarding address. Clients are expected to remove their caches and links to the resource. The HTTP specification intends this status code to be used for "limited-time, promotional services". APIs should not feel compelled to indicate resources that have been deleted with this status code.
     *
     * @since 1.0.0
     */
    GONE(410, "Gone"),
    /**
     * Server rejected the request because the Content-Length header field is not defined and the server requires it. */
    LENGTH_REQUIRED(411, "Length Required"),
    /**
     * The client has indicated preconditions in its headers which the server does not meet.
     *
     * @since 1.0.0
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /**
     * Request entity is larger than limits defined by server; the server might close the connection or return an Retry-After header field.
     *
     * @since 1.0.0
     */
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    /**
     * The URI requested by the client is longer than the server is willing to interpret.
     *
     * @since 1.0.0
     */
    URI_TOO_LONG(414, "URI Too Long"),
    /**
     * The media format of the requested data is not supported by the server, so the server is rejecting the request.
     *
     * @since 1.0.0
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /**
     * The range specified by the Range header field in the request can't be fulfilled; it's possible that the range is outside the size of the target URI's data.
     *
     * @since 1.0.0
     */
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
    /**
     * This response code means the expectation indicated by the Expect request header field can't be met by the server.
     *
     * @since 1.0.0
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),
    /**
     * The server refuses the attempt to brew coffee with a teapot.
     *
     * @since 1.0.0
     */
    IM_A_TEAPOT(418, "I'm a teapot"),
    /**
     * The request was directed at a server that is not able to produce a response. This can be sent by a server that is not configured to produce responses for the combination of scheme and authority that are included in the request URI.
     *
     * @since 1.0.0
     */
    MISDIRECTED_REQUEST(421, "Misdirected Request"),
    /**
     * The request was well-formed but was unable to be followed due to semantic errors.
     *
     * @since 1.0.0
     */
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    /**
     * The resource that is being accessed is locked.
     *
     * @since 1.0.0
     */
    LOCKED(423, "Locked"),
    /**
     * The request failed due to failure of a previous request.
     *
     * @since 1.0.0
     */
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    /**
     * Indicates that the server is unwilling to risk processing a request that might be replayed.
     *
     * @since 1.0.0
     */
    @Experimental
    TOO_EARLY(425, "Too Early"),
    /**
     * The server refuses to perform the request using the current protocol but might be willing to do so after the client upgrades to a different protocol. The server sends an Upgrade header in a 426 response to indicate the required protocol(s).
     *
     * @since 1.0.0
     */
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    /**
     * The origin server requires the request to be conditional. This response is intended to prevent the 'lost update' problem, where a client GETs a resource's state, modifies it, and PUTs it back to the server, when meanwhile a third party has modified the state on the server, leading to a conflict.
     *
     * @since 1.0.0
     */
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    /**
     * The user has sent too many requests in a given amount of time ("rate limiting").
     *
     * @since 1.0.0
     */
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    /**
     * The server is unwilling to process the request because its header fields are too large. The request may be resubmitted after reducing the size of the request header fields.
     *
     * @since 1.0.0
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    /**
     * The user-agent requested a resource that cannot legally be provided, such as a web page censored by a government.
     *
     * @since 1.0.0
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    // 5XX SERVER ERRORS
    /**
     * The server has encountered a situation it doesn't know how to handle.
     *
     * @since 1.0.0
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /**
     * The request method is not supported by the server and cannot be handled. The only methods that servers are required to support (and therefore that must not return this code) are GET and HEAD.
     *
     * @since 1.0.0
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /**
     * This error response means that the server, while working as a gateway to get a response needed to handle the request, got an invalid response.
     *
     * @since 1.0.0
     */
    BAD_GATEWAY(502, "Bad Gateway"),
    /**
     * The server is not ready to handle the request. Common causes are a server that is down for maintenance or that is overloaded. Note that together with this response, a user-friendly page explaining the problem should be sent. This responses should be used for temporary conditions and the Retry-After: HTTP header should, if possible, contain the estimated time before the recovery of the service. The webmaster must also take care about the caching-related headers that are sent along with this response, as these temporary condition responses should usually not be cached.
     *
     * @since 1.0.0
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /**
     * This error response is given when the server is acting as a gateway and cannot get a response in time.
     *
     * @since 1.0.0
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /**
     * The HTTP version used in the request is not supported by the server.
     *
     * @since 1.0.0
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    /**
     * The server has an internal configuration error: the chosen variant resource is configured to engage in transparent content negotiation itself, and is therefore not a proper end point in the negotiation process.
     *
     * @since 1.0.0
     */
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    /**
     * The method could not be performed on the resource because the server is unable to store the representation needed to successfully complete the request.
     *
     * @since 1.0.0
     */
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    /**
     * The server detected an infinite loop while processing the request.
     *
     * @since 1.0.0
     */
    LOOP_DETECTED(508, "Loop Detected"),
    /**
     * Further extensions to the request are required for the server to fulfil it.
     *
     * @since 1.0.0
     */
    NOT_EXTENDED(510, "Not Extended"),
    /**
     * The 511 status code indicates that the client needs to authenticate to gain network access.
     *
     * @since 1.0.0
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    private int code;
    private String description;

    /**
     * Creates a new HTTP status code.
     *
     * @param code The status code.
     * @param description The description of the status code.
     */
    HttpStatusCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Gets the HTTP status code.
     *
     * @return The status code number.
     *
     * @since 1.0.0
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Gets the Description.
     *
     * @return The description of the status code.
     *
     * @since 1.0.0
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the enum by its HTTP status code.
     *
     * @param code The status code.
     * @return The enum with the status code or {null} when the enum does not exist
     *
     * @since 1.0.0
     */
    public static HttpStatusCode findByCode(int code) {
        HttpStatusCode[] enums = HttpStatusCode.values();
        for (HttpStatusCode _enum : enums) {
            if (_enum.getCode() == code) {
                return _enum;
            }
        }
        return null;
    }
}
