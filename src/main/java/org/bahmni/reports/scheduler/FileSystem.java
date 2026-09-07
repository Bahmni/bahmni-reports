package org.bahmni.reports.scheduler;

import java.io.File;

/**
 * This interface provides an abstraction over file system operations
 * to make testing easier without relying on PowerMock for constructor mocking.
 */
public interface FileSystem {

    /**
     * Gets a File object for the given path
     */
    default File getFile(String path) {
        return new File(path);
    }

    /**
     * Gets a File object by combining directory and filename
     */
    default File getFile(String directory, String filename) {
        return new File(directory, filename);
    }
}
