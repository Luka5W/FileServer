package com.github.luka5w.fileserver.data.datatypes;

/**
 * A datatype containing all public userdata.
 *
 * <p>
 *     This datatype does not contain sensitive user information.
 * </p>
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public class User {

    private String id;
    private Type type;
    private boolean enabled;

    /**
     * Creates a new user.
     *
     * @param id The ID of the new user.
     * @param type The type of the new user.
     * @param enabled Whether the new user is enabled or disabled.
     *
     * @since 1.0.0
     */
    public User(String id, Type type, boolean enabled) {
        this.id = id;
        this.type = type;
        this.enabled = enabled;
    }

    /**
     * Returns the ID of the user.
     *
     * @return The ID of the user.
     *
     * @since 1.0.0
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the type of the user.
     *
     * @return The type.
     *
     * @since 1.0.0
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Returns whether the user is enabled or not.
     *
     * @return true when the user is enabled.
     *
     * @since 1.0.0
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the ID of the user.
     *
     * @param id The new ID of the user.
     *
     * @since 1.0.0
     */
    protected void setId(String id) {
        this.id = id;
    }

    /**
     *
     * Sets the type of the user.
     *
     * @param type The new user type.
     *
     * @since 1.0.0
     */
    protected void setType(Type type) {
        this.type = type;
    }

    /**
     * Sets the enabled flag of the user.
     *
     * @param enabled true to 'enable' the user, false to 'disable' the user.
     *
     * @since 1.0.0
     */
    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Possible types of users.
     *
     * @author Lukas // https://github.com/luka5w
     * @version 1.0.0
     */
    public enum Type {
        ADMIN("ADMIN"),
        USER("USER");

        private final String type;

        /**
         * Creates a new user type depending on the passed type.
         *
         * @param type The value of the type.
         *
         * @since 1.0.0
         */
        Type(String type) {
            this.type = type;
        }

        /**
         * Returns the value of the type.
         *
         * @return The value of the type.
         *
         * @since 1.0.0
         */
        @Override
        public String toString() {
            return this.type;
        }
    }
}
