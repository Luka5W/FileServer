package com.github.luka5w.fileserver.data;

import com.github.luka5w.fileserver.Main;
import com.github.luka5w.util.cli.Logger;
import com.github.luka5w.util.data.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class DB {

    private final File file;
    protected final Logger LOGGER;

    public DB(String file, String dbName) throws IOException {
        LOGGER = Main.getLogger(dbName);
        this.file = new File(file);
        if (this.file.isDirectory()) throw new FileNotFoundException("File is a directory");
        if (!this.file.exists()) this.create();
    }

    protected void save(String database) throws IOException {
        FileUtils.write(this.file, database);
    }

    protected String load() throws IOException {
        return FileUtils.read(this.file);
    }

    protected void create() throws IOException {
        if (!this.file.exists()) FileUtils.createWithParents(this.file);
    }
}
