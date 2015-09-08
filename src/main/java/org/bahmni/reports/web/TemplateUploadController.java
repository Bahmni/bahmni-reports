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
        String pathname = bahmniReportsProperties.getMacroTemplatesTempDirectory() + file.getOriginalFilename();
        file.transferTo(new File(pathname));
        return pathname;

    }
}
