package org.bahmni.reports.util;

import org.apache.log4j.Logger;
import org.bahmni.reports.template.Templates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReaderUtil {
    private static final Logger logger = Logger.getLogger(FileReaderUtil.class);

    public static String getFileContent(final String fileName) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(Templates.class.getClassLoader().getResourceAsStream(fileName)));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return sb.toString();
        } catch (IOException e) {
            logger.error("File" + fileName + "not found", e);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }
}

