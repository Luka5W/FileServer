package com.github.luka5w.fileserver;

import com.github.luka5w.fileserver.api.API;
import com.github.luka5w.fileserver.data.FileDB;
import com.github.luka5w.fileserver.data.UserDB;
import com.github.luka5w.fileserver.server.Server;
import com.github.luka5w.util.cli.Logger;
import com.github.luka5w.util.data.Utils;
import com.github.luka5w.util.program.MainClass;
import com.github.luka5w.util.program.Program;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * FileServer
 *
 * GitHub: https://github.com/luka5w/fileserver
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public class Main implements MainClass {

    private static int LOG_LEVEL = 0;
    private static Logger LOGGER = new Logger(Constants.PROGRAM_NAME, LOG_LEVEL);

    private Ini ini;
    private API api;
    private Server server;
    private UserDB userDB;
    private FileDB fileDB;

    public static void main(String[] args) {
        Main main = new Main();
        Program program = new Program(main, args, Program.DEFAULT_OPTIONS, Constants.FILE_CONFIG, Constants.PROGRAM_NAME, Constants.PROGRAM_VERSION, Constants.HELP_HEADER, Constants.HELP_FOOTER, Constants.HELP_AUTOUSAGE);
        program.init();
        program.exec();
    }

    /**
     * Returns the logger for the specific part of the program.
     *
     * @param subLogger The name of the part of the program.
     *
     * @return The logger for the part of the program.
     */
    public static Logger getLogger(String subLogger) {
        return new Logger(subLogger, LOGGER);
    }

    @Override
    public void getOptions(Options options) {
        /**
         * usage
         * @see Options#addOption(Option)
         * @see Options#addOption(String, String)
         * @see Options#addOption(String, boolean, String)
         * @see Options#addOption(String, String, boolean, String)
         */
    }

    @Override
    public void setup(CommandLine cmd, File configFile) {

    }

    @Override
    public void main(CommandLine cmd, File configFile) {
        LOGGER.log("Reading Config...");
        try {
            this.ini = new Ini(configFile);
        }
        catch (IOException e) {
            LOGGER.exception("Can't read config: ", e, true);
        }
        LOGGER.debug("Done.");
        LOGGER.log("Updating log level...");
        try {
            LOG_LEVEL = Integer.parseInt(this.ini.get("main", "log-level"));
            LOGGER = new Logger(Constants.PROGRAM_NAME, LOG_LEVEL);
        }
        catch (NumberFormatException e) {
            LOGGER.warn("Can't update log level: " + e.getMessage() + " Keeping default level (" + LOG_LEVEL + ").");
        }
        LOGGER.debug("Done.");

        LOGGER.log("Initializing databases...");
        try {
            this.userDB = new UserDB(this.ini.get("database", "users"));
        } catch (IOException e) {
            LOGGER.exception("Can't initiate user database: ", e, true);
        }
        try {
            this.fileDB = new FileDB(this.ini.get("database", "dir"));
        } catch (IOException e) {
            LOGGER.exception("Can't initiate file database: ", e, true);
        }
        LOGGER.debug("Done.");

        this.api = new API(Integer.parseInt(this.ini.get("api", "rate-limit")), Integer.parseInt(this.ini.get("api", "rate-limit-vanish-time")), Constants.PROGRAM_NAME + "@" + Constants.PROGRAM_VERSION);
        try {
            if (Utils.isTrue(this.ini.get("tls", "enabled"))) {
                this.server = new Server(this.api, this.ini.get("server", "address"), Integer.parseInt(this.ini.get("server", "port")), 50, this.ini.get("tls", "keystore-path"), this.ini.get("tls", "keystore-password"));
            }
            else {
                this.server = new Server(this.api, this.ini.get("server", "address"), Integer.parseInt(this.ini.get("server", "port")), 50);
            }
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.exception("Can't initialize server: ", e, true);
        }
        this.server.start();
    }
}
