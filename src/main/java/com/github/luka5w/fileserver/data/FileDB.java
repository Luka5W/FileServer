package com.github.luka5w.fileserver.data;

import com.github.luka5w.fileserver.Main;
import com.github.luka5w.fileserver.api.HttpException;
import com.github.luka5w.util.cli.Logger;
import com.github.luka5w.util.data.FileUtils;
import org.ini4j.InvalidFileFormatException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * The internal API for the file database
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public class FileDB {

    private static final String EXTENSION = "db";
    private static final String FILE_REGEX = "[0-9A-Za-z]{1,32}\\.[0-9]{13}\\." + EXTENSION;
    private final Logger LOGGER = Main.getLogger("UserDB");
    private static FileDB INSTANCE;
    private final Path dir;
    private HashMap<String, ArrayList<Long>> files;

    /**
     * Initiates the user database.
     *
     * @param dir The database root directory.
     *
     * @throws IOException When a file can't be read.
     *
     * @since 1.0.0
     */
    public FileDB(String dir) throws IOException {
        INSTANCE = this;
        this.dir = Paths.get(dir);
        this.loadFromDB();
    }

    /**
     * Returns the instance of the user database
     *
     * @return The instance of the user database
     *
     * @since 1.0.0
     */
    public static FileDB getInstance() {
        return INSTANCE;
    }

    /**
     * Returns all files the user has access to.
     *
     * @param executingUser The ID of the executing user.
     * @param targetUser The ID of the user to get the files from.
     * @return A list of all file IDs the user has access to.
     *
     * @throws HttpException See {@link UserDB#checkAPIPermission(String, String)}.
     *
     * @since 1.0.0
     */
    public ArrayList<Long> listUserFiles(String executingUser, String targetUser) throws HttpException {
        if (!UserDB.getInstance().isUserValid(targetUser)) throw new HttpException(400, "Invalid User ID");
        UserDB.getInstance().checkAPIPermission(executingUser, targetUser);
        if (!this.files.containsKey(targetUser)) return new ArrayList<>();
        return this.files.get(targetUser);
    }

    /**
     * Returns either the metadata or the content of the file.
     *
     * @param user The owner of the file.
     * @param id The id of the file.
     * @param metadataOnly Request metadata only when true.
     * @return The metadata or the content of the file.
     *
     * @throws HttpException When the server is in an illegal IO state (i.e. {@link FileUtils#readUntil(File, char)}, {@link FileUtils#readFrom(File, char)} throws an exception).
     *
     * @since 1.0.0
     */
    public JSONObject getFile(String user, long id, boolean metadataOnly) throws HttpException {
        this.checkAccess(user, id);
        java.io.File f = this.getFileName(user, id);
        JSONObject json = new JSONObject();
        try {
            if (metadataOnly) {
                String[] data = FileUtils.readUntil(f, '\n').split(";");
                if (data.length == 0 || data.length > 2) throw new InvalidFileFormatException("Malformed metadata");
                json = new JSONObject()
                        .put("owner", user)
                        .put("created", id)
                        .put("modified", data[0])
                        .put("users", (data.length == 2 ? data[1] : new String[0]));
            }
            else {
                json = new JSONObject(FileUtils.readFrom(f, '\n'));
            }
        }
        catch (IOException e) {
            LOGGER.exception("Error while reading file: ", e);
            throw new HttpException(500, "Server Is In An Illegal IO State");
        }
        return json;
    }

    /**
     * Modifies the content of a file.
     *
     * @param user The owner of the file.
     * @param id The id of the file.
     * @param content The new content.
     *
     * @throws HttpException See {@link FileUtils#write(File, String)}.
     *
     * @since 1.0.0
     */
    public void modFileContent(String user, long id, JSONObject content) throws HttpException {
        this.checkAccess(user, id);
        File file = this.getFileName(user, id);
        JSONObject meta = this.getFile(user, id, true);
        try {
            FileUtils.write(file, meta.getLong("modified") + ";" + meta.getJSONArray("users").join(",") + "\n" + content.toString());
        } catch (IOException e) {
            LOGGER.exception("Error while reading file: ", e);
            throw new HttpException(500);
        }
    }

    /**
     * Deletes a file.
     *
     * @param user The owner of the file.
     * @param id The id of the file.
     *
     * @throws HttpException When the user has no access to the file.
     *
     * @since 1.0.0
     */
    public void deleteFile(String user, long id) throws HttpException {
        this.checkAccess(user, id);
        this.getFileName(user, id).delete();
        this.files.get(user).remove(id);
    }

    /**
     * Creates a new File with an empty content.
     *
     * @param user The owner of the new file.
     * @return The ID of the created file.
     *
     * @throws HttpException See {@link FileUtils#write(File, String)}.
     *
     * @since 1.0.0
     */
    public long createFile(String user) throws HttpException {
        return this.createFile(user, new JSONObject());
    }

    /**
     * Creates a new File with the given content.
     *
     * @param user The owner of the new file.
     * @param content The content of the new file.
     * @return The ID of the created file.
     *
     * @throws HttpException See {@link FileUtils#write(File, String)}.
     *
     * @since 1.0.0
     */
    public long createFile(String user, JSONObject content) throws HttpException {
        long id = (new Date()).getTime();
        try {
            FileUtils.write(this.getFileName(user, id), id + ";\n" + content.toString());
        } catch (IOException e) {
            LOGGER.exception("Error while writing file: ", e);
            throw new HttpException(500);
        }
        if (!this.files.containsKey(user)) this.files.put(user, new ArrayList<>());
        this.files.get(user).add(id);
        return id;
    }

    /**
     * Loads all files to the database.
     *
     * <p>
     *     This method iterates through the file database root dir, collects all database files and extracts the owner and the id from them.
     *     <br>
     *     After that, each known owner will be added to {@link #files} with a list of all IDs he owns.
     * </p>
     *
     * @throws IOException See {@link Files#createDirectories(Path, FileAttribute[])} and {@link Files#list(Path)}.
     *
     * @since 1.0.0
     */
    private void loadFromDB() throws IOException {
        File file = this.dir.toFile();
        if (!file.isDirectory()) {
            if (file.exists()) throw new InvalidParameterException("Files must be a directory");
            Files.createDirectories(this.dir);
        }
        this.files = new HashMap<>();
        Files.list(this.dir).filter(Files::isRegularFile).forEach(p -> {
            String f = p.getFileName().toString();
            if (f.matches(FILE_REGEX)) {
                String[] pp = f.split("\\.");
                if (pp.length == 3) {
                    if (!this.files.containsKey(pp[0])) this.files.put(pp[0], new ArrayList<>());
                    this.files.get(pp[0]).add(Long.valueOf(pp[1]));
                }
            }
        });
    }

    /**
     * Checks whether the user has access to a file or not.
     *
     * @param user The user to check.
     * @param id The ID of the file.
     *
     * @throws HttpException When the user has no access to the file.
     *
     * @since 1.0.0
     */
    private void checkAccess(String user, long id) throws HttpException {
        if (!(this.files.containsKey(user) && this.files.get(user).contains(id))) throw new HttpException(404, "File Not Found Or Access Denied");
    }

    /**
     * Formats a filename from an owner and an ID.
     *
     * @param user The owner of the file.
     * @param id The ID of the file.
     *
     * @return A new file matching the parameters.
     *
     * @since 1.0.0
     */
    private java.io.File getFileName(String user, long id) {
        return new java.io.File(this.dir.toString(), user + "." + id + "." + EXTENSION);
    }
}
