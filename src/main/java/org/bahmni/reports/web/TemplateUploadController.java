package org.bahmni.reports.web;

import org.bahmni.reports.BahmniReportsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
public class TemplateUploadController {

    private BahmniReportsProperties bahmniReportsProperties;

    @Autowired
    public TemplateUploadController(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadTemplateFile(@RequestParam(value = "file") MultipartFile file) throws IOException {
        String templateFileName = getTemplateName(file.getOriginalFilename());
        String pathname = bahmniReportsProperties.getMacroTemplatesTempDirectory() + templateFileName;
        file.transferTo(new File(pathname));
        return templateFileName;
    }

    private String getTemplateName(String originalFilename) {
        return UUID.randomUUID().toString() + "-" + originalFilename;
    }
}
