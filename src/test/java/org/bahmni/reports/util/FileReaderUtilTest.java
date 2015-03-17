package org.bahmni.reports.util;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class FileReaderUtilTest {

    @Test
    public void shouldReadFileContent() {
        String content = FileReaderUtil.getFileContent("sql/codedObsCount.sql");
        assertTrue(content.contains("SELECT DISTINCT"));

        content = FileReaderUtil.getFileContent("sql/codedObsCount.sql", false);
        assertTrue(content.contains("SELECT DISTINCT"));
    }

    @Test
    public void shouldReadFileContentFromAbsoluteFilePath() throws Exception {
        String content = FileReaderUtil.getFileContent("./src/test/sql/test.sql", true);
        assertTrue(content.contains("select * from someTable"));
        assertTrue(content.contains("where a > 10"));
    }
}