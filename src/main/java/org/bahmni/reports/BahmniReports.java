package org.bahmni.reports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import(BahmniReportsConfiguration.class)
@ComponentScan({"org.bahmni.reports"})
@SpringBootApplication
public class BahmniReports extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application){
        return application.sources(BahmniReports.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BahmniReports.class, args);
    }
}
