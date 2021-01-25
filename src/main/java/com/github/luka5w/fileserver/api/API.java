package com.github.luka5w.fileserver.api;

import com.github.luka5w.fileserver.Main;
import com.github.luka5w.fileserver.data.FileDB;
import com.github.luka5w.fileserver.data.UserDB;
import com.github.luka5w.fileserver.data.datatypes.User;
import com.github.luka5w.fileserver.server.Server;
import com.github.luka5w.util.cli.Logger;
import com.github.luka5w.util.data.Utils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains all API versions (aka {@link com.sun.net.httpserver.HttpContext}s which contain the endpoints.
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public class API {
    private static final Logger LOGGER = Main.getLogger("API");

    private final int ratelimit;
    private final int ratetime;
    private final List<APIVersion> versions;
    private final boolean sendCors;
    private final String cors;
    private final String serverName;

    /**
     * Creates a new API for an {@link com.sun.net.httpserver.HttpServer}.
     *
     * @param ratelimit The maximal amount of requests per {rateLimitVanishTime}, a client can perform before getting 429 http status responses (RFC 6585: Too Many Requests).
     * @param ratelimitVanishTime The time, a request takes to "vanish" and reduces the amount of requests in this time.
     * @param cors When this argument is not empty (""), The 'Access-Control-Allow-Origin' header will be passed on response with the parameter as value.
     * @param serverName The name of the server, (probably - depending on API version) passed in the response headers.
     *
     * @since 1.0.0
     */
    public API(int ratelimit, int ratelimitVanishTime, String cors, String serverName) {
        this.ratelimit = ratelimit;
        this.ratetime = ratelimitVanishTime;
        this.versions = new ArrayList<>();
        this.sendCors = !(cors == null || cors.isEmpty());
        this.cors = cors;
        this.serverName = serverName;
        this.addVersions();
    }

    /**
     * Returns all API versions.
     *
     * @return The API versions.
     *
     * @since 1.0.0
     */
    public List<APIVersion> getVersions() {
        return this.versions;
    }

    /**
     * The endpoint (aka {@link com.sun.net.httpserver.HttpContext}s to inform the client about the supported API versions.
     *
     * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
     *
     * @since 1.0.0
     */
    public void info(HttpExchange httpExchange) {
        switch (httpExchange.getRequestMethod()) {
            case "GET":
                if (httpExchange.getRequestURI().getPath().equals("/")) {
                    this.modResponse(httpExchange);
                    Server.sendResponse(httpExchange, 200, this.getVersions().stream().map(APIVersion::getVersion).collect(Collectors.joining("\n")), "text/plain");
                }
                else {
                    this.modResponse(httpExchange);
                    Server.sendResponse(httpExchange, 404);
                }
                break;
            default:
                this.modResponse(httpExchange);
                Server.sendResponse(httpExchange, 400);
                break;
        }
    }

    /**
     * Adds the API versions (aka {@link APIVersion}).
     *
     * @since 1.0.0
     */
    private void addVersions() {
        LOGGER.log("Registering APIs...");
        this.versions.add(new APIVersion("1.0") {

            private HashMap<String, Endpoint> endpoints;
            private String endpoint;
            private String authUser;

            @Override
            protected void init() {
                this.endpoints = new HashMap<>();
                this.registerEndpoint("user/self", (httpExchange, method, query, user) -> {
                    switch (method.toUpperCase()) {
                        case "GET":
                            // Obtains (own) user info
                            User u = UserDB.getInstance().getUser(user);
                            JSONObject userData = new JSONObject();
                            userData.put("id", u.getId());
                            userData.put("type", u.getType().toString());
                            userData.put("enabled", u.isEnabled());
                            this.sendResponse(httpExchange, 200, userData);
                            break;
                        case "PATCH":
                            // Modifies (own) user info
                            String password = query.get("password");
                            if (password != null) {
                                if (password.isEmpty()) throw new HttpException(400, "Password must not be empty");
                                UserDB.getInstance().setUserPassword(user, user, password);
                                this.sendResponse(httpExchange, 200, "Success");
                                break;
                            }
                            throw new HttpException(400, "Missing Parameters");
                        case "DELETE":
                            // Disables (own) user
                            UserDB.getInstance().disableUser(user, user);
                            this.sendResponse(httpExchange, 200, "Success");
                            break;
                        default:
                            throw new HttpException(400, "Unknown Request Method");
                    }
                });
                this.registerEndpoint("user/list", (httpExchange, method, query, user) -> {
                    switch (method.toUpperCase()) {
                        case "GET":
                            JSONArray users;
                            if (UserDB.getInstance().getUser(user).getType() != com.github.luka5w.fileserver.data.datatypes.User.Type.ADMIN) throw new HttpException(403);
                            if (query.get("full") == null) {
                                // Obtains all user names
                                users = new JSONArray(UserDB.getInstance().getUsernames(user));
                            }
                            else {
                                // Obtains all user names with data
                                users = new JSONArray();
                                UserDB.getInstance().getUsers(user).forEach(u -> {
                                    JSONObject u1 = new JSONObject();
                                    u1.put("id", u.getId());
                                    u1.put("type", u.getType().toString());
                                    u1.put("enabled", u.isEnabled());
                                    users.put(u1);
                                });
                            }
                            this.sendResponse(httpExchange, 200, users);
                            break;
                        default:
                            throw new HttpException(400);
                    }
                });
                this.registerEndpoint("user/other", (httpExchange, method, query, user) -> {
                    if (UserDB.getInstance().getUser(user).getType() != com.github.luka5w.fileserver.data.datatypes.User.Type.ADMIN) throw new HttpException(403);
                    String targetUser = query.get("user");
                    String targetUserType = query.get("type");
                    String targetUserPassword = query.get("password");
                    String targetUserEnabled = query.get("enabled");
                    if (targetUser == null || targetUser.isEmpty()) throw new HttpException(400, "Missing Parameters");
                    switch (method.toUpperCase()) {
                        case "GET":
                            // Obtains (other) user info
                            User u = UserDB.getInstance().getUser(targetUser);
                            JSONObject userData = new JSONObject();
                            userData.put("id", u.getId());
                            userData.put("type", u.getType().toString());
                            userData.put("enabled", u.isEnabled());
                            this.sendResponse(httpExchange, 200, userData);
                            break;
                        case "POST":
                            // Creates new user
                            if (targetUserType == null || targetUserType.isEmpty() ||
                                    targetUserPassword == null || targetUserPassword.isEmpty() ||
                                    targetUserEnabled == null || targetUserEnabled.isEmpty()) throw new HttpException(400, "Missing Parameters");
                            UserDB.getInstance().addUser(user, targetUser, targetUserPassword, this.getTypeFromString(targetUserType), this.isTrue(targetUserEnabled));
                            this.sendResponse(httpExchange, 200);
                            break;
                        case "PATCH":
                            // Modifies other user
                            boolean updateType = false;
                            boolean updatePassword = false;
                            boolean updateEnabled = false;
                            if (targetUserType != null) {
                                if (targetUserType.isEmpty()) throw new HttpException(400, "Parameters Must Not Be Empty");
                                updateType = true;
                            }
                            if (targetUserPassword != null) {
                                if (targetUserPassword.isEmpty()) throw new HttpException(400, "Parameters Must Not Be Empty");
                                updatePassword = true;
                            }
                            if (targetUserEnabled != null) {
                                if (targetUserEnabled.isEmpty()) throw new HttpException(400, "Parameters Must Not Be Empty");
                                updateEnabled = true;
                            }
                            if (!updateType && !updatePassword && !updateEnabled) throw new HttpException(400, "Missing Parameters");
                            if (updateType) UserDB.getInstance().setUserType(user, targetUser, this.getTypeFromString(targetUserType));
                            if (updatePassword) UserDB.getInstance().setUserPassword(user, targetUser, targetUserPassword);
                            if (updateEnabled) UserDB.getInstance().setUserEnabled(user, targetUser, this.isTrue(targetUserEnabled));
                            this.sendResponse(httpExchange, 200);
                        case "DELETE":
                            // Deletes other user
                            UserDB.getInstance().delUser(user, targetUser);
                            this.sendResponse(httpExchange, 200);
                            break;
                        default:
                            throw new HttpException(400);
                    }
                });
                this.registerEndpoint("file/list", ((httpExchange, method, query, user) -> {
                    switch(method.toUpperCase()) {
                        case "GET":
                            String targetUser = user;
                            if (query.containsKey("user")) {
                                targetUser = query.get("user");
                                if (targetUser == null || targetUser.isEmpty()) throw new HttpException(400, "Invalid Parameters");
                            }
                            ArrayList files = FileDB.getInstance().listUserFiles(user, targetUser);
                            JSONArray json = new JSONArray(files);
                            this.sendResponse(httpExchange, 200, json);
                            break;
                        default:
                            throw new HttpException(400);
                    }
                }));
                this.registerEndpoint("file/file", ((httpExchange, method, query, user) -> {
                    String file = query.get("id");
                    String content = query.get("content");
                    JSONObject json;
                    switch(method.toUpperCase()) {
                        case "GET":
                            if (file == null || file.isEmpty()) throw new HttpException(400, "Missing Parameters");
                            boolean metadataOnly = query.containsKey("meta");
                            json = FileDB.getInstance().getFile(user, Long.parseLong(file), metadataOnly);
                            if (json == null) throw new HttpException(500);
                            this.sendResponse(httpExchange, 200, json);
                            break;
                        case "POST":
                            long id;
                            if (content == null || content.isEmpty()) {
                                json = new JSONObject();
                            } else {
                                try {
                                    json = new JSONObject(content);
                                }
                                catch (JSONException e) {
                                    throw new HttpException(400, "Malformed Input");
                                }
                            }
                            id = FileDB.getInstance().createFile(user, json);
                            this.sendResponse(httpExchange, 200, id);
                            break;
                        case "PATCH":
                            if (content == null || content.isEmpty()) throw new HttpException(400, "Missing Parameters");
                            try {
                                json = new JSONObject(content);
                            }
                            catch (JSONException e) {
                                throw new HttpException(400, "Malformed Input");
                            }
                            FileDB.getInstance().modFileContent(user, Long.parseLong(file), json);
                            this.sendResponse(httpExchange, 200);
                            break;
                        case "DELETE":
                            FileDB.getInstance().deleteFile(user, Long.parseLong(file));
                            this.sendResponse(httpExchange, 200);
                            break;
                        default:
                            throw new HttpException(400);
                    }
                }));
            }

            @Override
            public void handle(HttpExchange httpExchange) {
                try {
                    this.checkRemote(httpExchange.getRemoteAddress().getHostName());
                    this.checkAuthentication(httpExchange.getRequestHeaders());
                    this.getEndpoint(httpExchange.getRequestURI().getPath());

                    if (endpoint.isEmpty() || !this.endpoints.containsKey(endpoint)) throw new HttpException(404);
                    HashMap<String, String> query = this.getQueryParams(httpExchange);
                    this.endpoints.get(endpoint).handle(httpExchange, httpExchange.getRequestMethod(), query, this.authUser);
                }
                catch (HttpException e) {
                    this.sendError(httpExchange, e.getStatus(), e.getMessage());
                }
            }

            /**
             * Returns the query parameters of the request (http://localhost:443?[parameters]) as an HashMap.
             * <p>
             *     When a key has no value, the value is an empty {@link String}.
             * </p>
             *
             * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
             * @return An empty HashMap when no query parameters are passed
             */
            private HashMap<String, String> getQueryParams(HttpExchange httpExchange) {
                HashMap<String, String> query = new HashMap<>();
                String queryString = httpExchange.getRequestURI().getQuery();
                if (queryString != null && !queryString.isEmpty()) {
                    String[] params = httpExchange.getRequestURI().getQuery().split("&");
                    for (String param : params) {
                        String[] param1 = param.split("=");
                        query.put(param1[0], (param1.length == 1 ? "" : param1[1]));
                    }
                }
                return query;
            }

            /**
             * Checks whether the remote is blacklisted or the rate limit has exceeded.
             *
             * @param remote The IP of the remote.
             */
            private void checkRemote(String remote) {
                // TODO: 20.12.2020 @pre0.0.2 [impl feats] Remote check is not implemented
                //  - Is remote blacklisted?
                super.LOGGER.warn("[TODO 20.12.2020] Remote check is not implemented in API/Anonymous(1.0)::checkRemote(String)");
            }

            /**
             * Checks whether the remote is authenticated via basic authentication and the passed credentials are valid.
             *
             * @param headers The Headers retrieved from {@link HttpExchange#getRequestHeaders()}.
             *
             * @throws HttpException With an HTTP status code and a message when anything went wrong (expected and unexpected).
             */
            private void checkAuthentication(Headers headers) throws HttpException {
                if (!headers.containsKey("authorization")) throw new HttpException(401, "Unauthorized");
                String[] rawAuth = headers.getFirst("authorization").split(" ");
                if (rawAuth.length != 2) throw new HttpException(400, "Invalid Authorization");
                if (!rawAuth[0].equalsIgnoreCase("basic")) throw new HttpException(400, "Invalid Authorization Method");
                String[] decAuth;
                try {
                    decAuth = new String(Base64.getDecoder().decode(rawAuth[1])).split(":");
                    if (decAuth.length != 2) throw new IllegalArgumentException();
                }
                catch(IllegalArgumentException e) {
                    throw new HttpException(400, "Invalid Authorization");
                }
                UserDB.getInstance().checkAuthorization(decAuth[0], decAuth[1]);
                this.authUser = decAuth[0];
            }

            /**
             * Retrieves the requested endpoint using the path of the URL.
             *
             * @param requestPath The path retrieved from {@link HttpExchange#getRequestURI()} {@link java.net.URI#getPath()}.
             */
            private void getEndpoint(String requestPath) {
                List<String> requestPathParts = new LinkedList<>(Arrays.asList(requestPath.split("/")));
                if (requestPathParts.size() < 2) throw new IllegalArgumentException("Internal: Illegal State: path must have a size of 2 at least");
                if (requestPathParts.size() == 2) {
                    this.endpoint = "";
                    return;
                }
                if (requestPathParts.get(0).isEmpty()) requestPathParts.remove(0);
                if (requestPathParts.get(0).equals(this.getVersion())) requestPathParts.remove(0);
                int lastIndex = requestPathParts.size() - 1;
                if (requestPathParts.get(lastIndex).isEmpty()) requestPathParts.remove(lastIndex);
                this.endpoint = String.join("/", requestPathParts);
            }

            /**
             * Sends a successful response to the client with a {@link JSONObject} containing the timestamp and status code.
             *
             * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
             * @param code The HTTP status code.
             */
            private void sendResponse(HttpExchange httpExchange, int code) {
                long ts = (new Date()).getTime();
                String json = "{\"ts\":" + ts + ",\"status\":" + code + "}";
                modResponse(httpExchange);
                Server.sendResponse(httpExchange, code, json, "application/json");
            }

            /**
             * Sends a successful response to the client with a {@link JSONObject} containing the timestamp, status code and a content key containing the response.
             *
             * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
             * @param code The HTTP status code.
             * @param content The response.
             */
            private void sendResponse(HttpExchange httpExchange, int code, Object content) {
                long ts = (new Date()).getTime();
                JSONObject json = new JSONObject();
                json.put("ts", ts);
                json.put("status", code);
                json.put("content", content);
                modResponse(httpExchange);
                Server.sendResponse(httpExchange, code, json.toString(), "application/json");
            }

            /**
             * Sends a error response to the client with a {@link JSONObject} containing the timestamp, status code and a status message, describing what went wrong.
             *
             * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
             * @param code The HTTP status code.
             * @param message The status message.
             */
            private void sendError(HttpExchange httpExchange, int code, String message) {
                long ts = (new Date()).getTime();
                String json = "{\"ts\":" + ts + ",\"status\":{\"code\":" + code + ",\"message\":\"" + message + "\"}}";
                modResponse(httpExchange);
                Server.sendResponse(httpExchange, code, json, "application/json");
            }

            /**
             * Returns whether a String has a value which can be interpreted as true or false
             * @see Utils#isTrue(String)
             *
             * @param s The string which will be interpreted.
             * @return The boolean value retrieved from the String.
             *
             * @throws HttpException When {@link Utils#isTrue(String)} throws an {@link IllegalArgumentException}, a new HttpException with status code 400 and "Invalid Parameter" is thrown.
             */
            private boolean isTrue(String s) throws HttpException {
                boolean response;
                try {
                    response = Utils.isTrue(s);
                }
                catch (IllegalArgumentException e) {
                    throw new HttpException(400, "Invalid Parameter");
                }
                return response;
            }

            /**
             * Returns an {@link com.github.luka5w.fileserver.data.datatypes.User.Type} which matches the String.
             *
             * @param s The string to retrieve the user type from.
             * @return The user type which matches the String.
             *
             * @throws HttpException When no user type matches the String (i.e. {@link com.github.luka5w.fileserver.data.datatypes.User.Type#valueOf(String)} returns null.
             */
            private com.github.luka5w.fileserver.data.datatypes.User.Type getTypeFromString(String s) throws HttpException {
                com.github.luka5w.fileserver.data.datatypes.User.Type type = com.github.luka5w.fileserver.data.datatypes.User.Type.valueOf(s);
                if (type == null) throw new HttpException(400, "Invalid Paramter");
                return type;
            }

            /**
             * Registers an endpoint and logs the registration.
             *
             * @param path The path of the endpoint without leading/ ending '/'.
             * @param endpoint A new endpoint for this path.
             */
            private void registerEndpoint(String path, Endpoint endpoint) {
                LOGGER.log("Registering endpoint " + path + "...");
                this.endpoints.put(path, endpoint);
                LOGGER.debug("Done.");
            }
        });
        LOGGER.debug("Registered APIs.");
    }

    /**
     * This method adds some important response headers to the response.
     *
     * @param httpExchange The HttpExchange supplied by the {@link com.sun.net.httpserver.HttpServer}.
     */
    private void modResponse(HttpExchange httpExchange) {
        // Send CORS header with its value when required.
        if (this.sendCors) httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", this.cors);
        // Send Server Name
        httpExchange.getResponseHeaders().set("Server", this.serverName);
    }

    /**
     * Returns a logger for the versions.
     *
     * @param version The version, the logger should initiated for.
     * @return The logger for the passed version
     *
     * @since 1.0.0
     */
    protected static Logger getLogger(String version) {
        return new Logger(version, LOGGER);
    }
}
