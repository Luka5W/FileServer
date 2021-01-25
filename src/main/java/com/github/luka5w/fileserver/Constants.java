package com.github.luka5w.fileserver;

import java.io.File;

/**
 * Constants for the program and the program loader (com.github.luka5w.util.program).
 *
 * @author Lukas // https://github.com/luka5w
 * @version 1.0.0
 */
public class Constants {
    public static final String PROGRAM_VERSION = (Constants.class.getPackage().getImplementationVersion() == null ? "debug-version" : Constants.class.getPackage().getImplementationVersion());
    public static final String PROGRAM_NAME = (Constants.class.getPackage().getImplementationTitle() == null ? "debug-program" : Constants.class.getPackage().getImplementationTitle());
    public static final File FILE_CONFIG = new File(PROGRAM_NAME.toLowerCase() + ".ini");
    public static final String HELP_HEADER = "";
    public static final String HELP_FOOTER = "fork me at github: https://github.com/luka5w/fileserver";
    public static final boolean HELP_AUTOUSAGE = true;
}
