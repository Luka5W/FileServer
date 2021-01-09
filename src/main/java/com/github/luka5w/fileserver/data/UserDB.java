package com.github.luka5w.fileserver.data;

import com.github.luka5w.fileserver.api.HttpException;
import com.github.luka5w.fileserver.data.datatypes.User;
import com.github.luka5w.util.encryption.HashedPassword;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The internal API for the user database
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public class UserDB extends DB {

    private static final String DEFAULT_ID = "admin";
    private static final String DEFAULT_PW = "password";
    private static UserDB INSTANCE;
    private HashMap<String, UserData> users;

    /**
     * Initiates the user database.
     *
     * @param file The user file
     *
     * @since 1.0.0
     */
    public UserDB(String file) throws IOException {
        super(file, "UserDB");
        INSTANCE = this;
        this.loadFromDB();
    }

    /**
     * Returns the instance of the user database
     *
     * @return The instance of the user database
     *
     * @since 1.0.0
     */
    public static UserDB getInstance() {
        return INSTANCE;
    }

    /**
     * Checks whether the passed username and password is valid using {@link #isUserValid(String)} and {@link #isPasswordValid(String)}
     * and whether the user itself is valid and enabled using {@link #checkUser(String, String)} {@link User#isEnabled()}.
     *
     * @param user The user ID.
     * @param password The password.
     *
     * @throws HttpException When the checks of the passed credentials failed.
     *
     * @since 1.0.0
     */
    public void checkAuthorization(String user, String password) throws HttpException {
        if (!this.isUserValid(user) || !this.isPasswordValid(password) || !this.checkUser(user, password)) throw new HttpException(401, "Invalid Credentials");
        if (!this.users.get(user).isEnabled()) throw new HttpException(401, "User Disabled");
    }

    /**
     * Returns all user IDs.
     *
     * <p>
     *     The executing user must have admin privileges (i.e. must be of type {@link com.github.luka5w.fileserver.data.datatypes.User.Type#ADMIN}) to perform this action.
     * </p>
     *
     * @param executingUser The ID of the executing user.
     * @return A set of all user IDs.
     *
     * @throws HttpException When the executing user has insufficient permissions.
     *
     * @since 1.0.0
     */
    public Set<String> getUsernames(String executingUser) throws HttpException {
        this.checkAPIPermission(executingUser);
        return this.users.keySet();
    }

    /**
     * Returns all users with their non-sensitive data.
     *
     * <p>
     *     The executing user must have admin privileges (i.e. must be of type {@link com.github.luka5w.fileserver.data.datatypes.User.Type#ADMIN}) to perform this action.
     * </p>
     *
     * @param executingUser The ID of the executing user.
     * @return A set of all users.
     *
     * @throws HttpException When the executing user has insufficient permissions.
     *
     * @since 1.0.0
     */
    public Set<User> getUsers(String executingUser) throws HttpException {
        this.checkAPIPermission(executingUser);
        return this.users.values().stream().map(userData -> new User(userData.getId(), userData.getType(), userData.isEnabled())).collect(Collectors.toSet());
    }

    /**
     * Returns all non-sensitive userdata.
     *
     * @param user The user ID.
     * @return The userdata.
     *
     * @throws HttpException When the user with this ID does not exist.
     *
     * @since 1.0.0
     */
    public User getUser(String user) throws HttpException {
        if (!this.users.containsKey(user)) throw new HttpException(404);
        UserData u = this.users.get(user);
        return new User(u.getId(), u.getType(), u.isEnabled());
    }

    /**
     * Modifies the user ID of an existing user.
     *
     * <p>
     *     The executing user must have admin privileges (i.e. must be of type {@link com.github.luka5w.fileserver.data.datatypes.User.Type#ADMIN}) to perform this action.
     * </p>
     *
     * @param executingUser The ID of the executing user.
     * @param targetUser The ID of the target user to modify.
     * @param newId The new ID of the target user.
     *
     * @throws HttpException When the executing user has insufficient permissions.
     *
     * @since 1.0.0
     */
    public void setUserId(String executingUser, String targetUser, String newId) throws HttpException {
        this.checkAPIPermission(executingUser);
        this.users.get(targetUser).setId(newId);
        this.save();
    }

    /**
     * Modifies the user password of an existing user.
     *
     * <p>
     *     The executing user must either have admin privileges (i.e. must be of type {@link com.github.luka5w.fileserver.data.datatypes.User.Type#ADMIN}) or be the target user to perform this action.
     * </p>
     *
     * @param executingUser The ID of the executing user.
     * @param targetUser The ID of the target user to modify.
     * @param password The new password of the target user.
     *
     * @throws HttpException When the executing user has insufficient permissions.
     *
     * @since 1.0.0
     */
    public void setUserPassword(String executingUser, String targetUser, String password) throws HttpException {
        this.checkAPIPermission(executingUser, targetUser);
        if (!this.isPasswordValid(password)) throw new HttpException(401, "Invalid Password");
        this.users.get(targetUser).setPassword(password);
        this.save();
    }

    /**
     * Disables an existing user.
     *
     * <p>
     *     The executing user must either have admin privileges (i.e. must be of type {@link com.github.luka5w.fileserver.data.datatypes.User.Type#ADMIN}) or be the target user to perform this action.
     * </p>
     *
     * @param executingUser The ID of the executing user.
     * @param targetUser The ID of the target user to modify.
     *
     * @throws HttpException When the executing user has insufficient permissions.
     *
     * @since 1.0.0
     */
    public void disableUser(String executingUser, String targetUser) throws HttpException {
        this.checkAPIPermission(executingUser, targetUser);
        this.users.get(targetUser).setEnabled(false);
        this.save();
    }

    /**
     * Enables or disables an existing user.
     *
     * <p>
     *     The executing user must have admin privileges (i.e. must be of type {@link com.github.luka5w.fileserver.data.datatypes.User.Type#ADMIN}) to perform this action.
     * </p>
     *
     * @param executingUser The ID of the executing user.
     * @param targetUser The ID of the target user to modify.
     * @param enabled Whether the target user should be enabled or disabled.
     *
     * @throws HttpException When the executing user has insufficient permissions.
     *
     * @since 1.0.0
     */
    public void setUserEnabled(String executingUser, String targetUser, boolean enabled) throws HttpException {
        this.checkAPIPermission(executingUser);
        this.users.get(targetUser).setEnabled(enabled);
        this.save();
    }

    /**
     * Modifies the user type of an existing user.
     *
     * <p>
     *     The executing user must have admin privileges (i.e. must be of type {@link com.github.luka5w.fileserver.data.datatypes.User.Type#ADMIN}) to perform this action.
     * </p>
     *
     * @param executingUser The ID of the executing user.
     * @param targetUser The ID of the target user to modify.
     * @param type The new type of the target user.
     *
     * @throws HttpException When the executing user has insufficient permissions.
     *
     * @since 1.0.0
     */
    public void setUserType(String executingUser, String targetUser, User.Type type) throws HttpException {
        this.checkAPIPermission(executingUser);
        this.users.get(targetUser).setType(type);
        this.save();
    }

    /**
     * Creates a new user with the given data.
     *
     * <p>
     *     The executing user must have admin privileges (i.e. must be of type {@link com.github.luka5w.fileserver.data.datatypes.User.Type#ADMIN}) to perform this action.
     * </p>
     *
     * @param executingUser The ID of the executing user.
     * @param targetUser The ID of the new user.
     * @param password The password of the new user.
     * @param type The type of the new user.
     * @param enabled Whether the user is enabled or not.
     *
     * @throws HttpException When the executing user has insufficient permissions.
     *
     * @since 1.0.0
     */
    public void addUser(String executingUser, String targetUser, String password, User.Type type, boolean enabled) throws HttpException {
        this.checkAPIPermission(executingUser);
        if (!this.isUserValid(targetUser) || !this.isPasswordValid(password)) throw new HttpException(401, "Invalid User ID or Password");
        if (this.users.containsKey(targetUser)) throw new HttpException(409, "User Already Exist");
        this.users.put(targetUser, new UserData(targetUser, type, enabled, password));
        this.save();
    }

    /**
     * Deletes an existing user.
     *
     * <p>
     *     The executing user must have admin privileges (i.e. must be of type {@link com.github.luka5w.fileserver.data.datatypes.User.Type#ADMIN}) to perform this action.
     * </p>
     *
     * @param executingUser The ID of the executing user.
     * @param targetUser The ID of the target user.
     *
     * @throws HttpException When the executing user has insufficient permissions.
     *
     * @since 1.0.0
     */
    public void delUser(String executingUser, String targetUser) throws HttpException {
        this.checkAPIPermission(executingUser);
        if (!this.users.containsKey(targetUser)) throw new HttpException(409, "User Does Not Exist");
        this.users.remove(targetUser);
        this.save();
    }

    /**
     * Returns whether the characters in an user ID are valid and the length of the ID is valid.
     *
     * <p>
     *     Only numbers and letters at least 1 character, maximal 32 characters are allowed.
     *     <br />
     *     <code>[0-9A-Za-z]\{1,32\}</code>
     * </p>
     *
     * @param user The user ID to check.
     *
     * @return true when the ID is valid.
     *
     * @since 1.0.0
     */
    public boolean isUserValid(String user) {
        return user.matches("[0-9A-Za-z]{1,32}");
    }

    /**
     * Returns whether the characters in a password are valid and the length of the password is valid.
     *
     * <p>
     *     No linebreaks, carriage returns, tabs and ':' are allowed, the password must have at least 4, maximal 32 characters.
     *     <br />
     *     <code>[^:\n\r\t]{4,32}</code>
     * </p>
     *
     * @param password The password to check.
     *
     * @return true when password is valid.
     *
     * @since 1.0.0
     */
    public boolean isPasswordValid(String password) {
        return password.matches("[^:\n\r\t]{4,32}");
    }

    /**
     * Checks whether an user exists and whether the credentials are correct.
     *
     * @param user The user ID to check.
     * @param password The password to check.
     *
     * @return true, when the user exists and the password is valid.
     *
     * @since 1.0.0
     */
    public boolean checkUser(String user, String password) {
        return (this.users.containsKey(user) && this.users.get(user).verifyPassword(password));
    }

    /**
     * Checks whether an user has the permission to perform an action.
     *
     * <p>
     *     When the executing user is the target user, permission is granted.
     *     Otherwise, permission is only granted when the user has admin privileges.
     * </p>
     *
     * @param executingUser The executing user.
     * @param targetUser The target user.
     *
     * @throws HttpException When the executing user has insufficient permission.
     *
     * @since 1.0.0
     */
    public void checkAPIPermission(String executingUser, String targetUser) throws HttpException {
        if (!executingUser.equals(targetUser)) this.checkAPIPermission(executingUser);
    }

    /**
     * Checks whether the executing user has admin privileges.
     *
     * @param executingUser The executing user.
     *
     * @throws HttpException When the executing user has insufficient permission.
     *
     * @since 1.0.0
     */
    public void checkAPIPermission(String executingUser) throws HttpException {
        if (this.users.get(executingUser).getType() != User.Type.ADMIN) throw new HttpException(403);
    }

    /**
     * Saves the database to the file.
     *
     * @throws HttpException When an IO Error occurs.
     *
     * @since 1.0.0
     */
    private void save() throws HttpException {
        JSONArray json = new JSONArray();
        this.users.values().forEach(u -> {
            json.put(u.toJSON());
        });
        try {
            super.save(json.toString());
        }
        catch (IOException e) {
            LOGGER.exception("Can't save database: ", e);
            throw new HttpException(500, "IO Error");
        }
    }

    /**
     * Loads the database from the file.
     *
     * @throws IOException When an IO Error occurs.
     *
     * @since 1.0.0
     */
    private void loadFromDB() throws IOException {
        this.users = new HashMap<>();
        JSONArray json = new JSONArray(super.load());
        json.forEach(u -> {
            UserData u1 = new UserData((JSONObject) u);
            this.users.put(u1.getId(), u1);
        });
    }

    @Override
    protected void create() throws IOException {
        super.create();
        super.save("[" + new UserData(DEFAULT_ID, User.Type.ADMIN, true, DEFAULT_PW).toString() + "]");
        LOGGER.log("Created default user:\n id: " + DEFAULT_ID + "\n password: " + DEFAULT_PW);
        LOGGER.warn("You should change the credentials of the default user!");
    }

    /**
     * A datatype containing all userdata.
     *
     * <p>
     *     This datatype contains sensitive user information and must <b>not</b> be published!
     *     <br />
     *     The password is never stored raw anywhere but in hashed form using my {@link HashedPassword} datatype.
     * </p>
     *
     * @author Lukas // https://github.com/luka5w
     * @version 1.0.0
     */
    public class UserData extends User {

        private static final String JSON_KEY_ID = "i";
        private static final String JSON_KEY_TYPE = "t";
        private static final String JSON_KEY_ENABLED = "e";
        private static final String JSON_KEY_PASSWORD = "p";

        private HashedPassword password;

        /**
         * Creates a new user from a JSONObject.
         *
         * <code>
         *     {
         *         "i":String,
         *         "t":String,
         *         "e":boolean,
         *         "p":String
         *     }
         * </code>
         *
         * @param json The json object.
         *
         * @since 1.0.0
         */
        public UserData(JSONObject json) {
            super(json.getString(JSON_KEY_ID), User.Type.valueOf(json.getString(JSON_KEY_TYPE)), json.getBoolean(JSON_KEY_ENABLED));
            this.password = HashedPassword.fromString(json.getString(JSON_KEY_PASSWORD));
        }

        /**
         * Creates a new user.
         *
         * @param id The ID of the new user.
         * @param type The type of the new user.
         * @param enabled Whether the new user is enabled.
         * @param password The password of the new user.
         *
         * @since 1.0.0
         */
        public UserData(String id, Type type, boolean enabled, String password) {
            super(id, type, enabled);
            try {
                this.password = new HashedPassword(password);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Unexpected Exception: Default algorithm should exist.\n" + e.getMessage());
            }
        }

        /**
         * Checks whether the given password matches the hashed password.
         *
         * @param password The password to check.
         * @return true when the passwords matches.
         *
         * @since 1.0.0
         */
        public boolean verifyPassword(String password) {
            try {
                return this.password.verify(password);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Unexpected Exception: This exception should be occurred in constructor already.\n" + e.getMessage());
            }
        }

        @Override
        public void setId(String id) {
            super.setId(id);
        }

        @Override
        public void setType(Type type) {
            super.setType(type);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
        }

        /**
         * Sets a new password for the user.
         *
         * @see HashedPassword#update(String)
         *
         * @param password The new password
         *
         * @since 1.0.0
         */
        public void setPassword(String password) {
            this.password.update(password);
        }

        /**
         * Creates a new JSONObject from the UserData containing all user data.
         *
         * @return The JSONObject.
         *
         * @since 1.0.0
         */
        public JSONObject toJSON() {
            return new JSONObject()
                    .put(JSON_KEY_ID, super.getId())
                    .put(JSON_KEY_TYPE, super.getType().toString())
                    .put(JSON_KEY_ENABLED, super.isEnabled())
                    .put(JSON_KEY_PASSWORD, this.password.toString());

        }

        @Override
        public String toString() {
            return this.toJSON().toString();
        }
    }
}
