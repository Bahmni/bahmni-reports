package org.bahmni.reports.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FileReaderUtilTest {

    @Test
    public void shouldReadFileContent() {
        String content = FileReaderUtil.getFileContent("sql/obsCountByGenderAndAgeGroupQuery.sql");
        assertTrue(content.contains("SELECT\n  reporting_age_group.name"));
    }

}