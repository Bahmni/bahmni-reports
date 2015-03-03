package org.bahmni.reports.api.util;

import org.bahmni.reports.api.FileReaderUtil;
import org.junit.Test;

import java.lang.String;

import static org.junit.Assert.assertTrue;

public class FileReaderUtilTest {

    @Test
    public void shouldReadFileContent() {
        String content = FileReaderUtil.getFileContent("sql/obsCountByGenderAndAgeGroupQuery.sql");
        assertTrue(content.contains("SELECT distinct answer.concept_full_name as concept_name"));
    }

}