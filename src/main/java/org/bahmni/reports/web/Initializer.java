package org.bahmni.reports.web;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration.Dynamic;

import org.apache.commons.lang3.CharEncoding;
import org.bahmni.reports.web.security.SecurityConfig;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class Initializer implements WebApplicationInitializer {
    private static final long DEFAULT_MAX_UPLOAD_SIZE = 5242880L;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(SecurityConfig.class);
        ctx.setServletContext(servletContext);
        Dynamic dynamic = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
        dynamic.addMapping("/");
        dynamic.setLoadOnStartup(1);
        configureMultipartHandling(servletContext, dynamic);
    }

    /**
     * This method is responsible for handling the multipart configurations such as max upload size and character encoding.
     * By default, it sets the maximum upload size to 5MB and uses UTF-8 encoding for multipart files.
     *
     * @param servletContext the configured ServletContext
     * @param dynamic the Dynamic servlet registration object
     */
    private void configureMultipartHandling(ServletContext servletContext, Dynamic dynamic) {
        // Set max upload size
        // Hardcoding max upload size to 5MB, this needs to be updated if more than 5MB is required in future
        dynamic.setMultipartConfig(new MultipartConfigElement(null, DEFAULT_MAX_UPLOAD_SIZE, -1, -1));

        // Set default encoding for multipart files
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter(CharEncoding.UTF_8);
        characterEncodingFilter.setForceEncoding(true);
        servletContext.addFilter("characterEncodingFilter", characterEncodingFilter);
    }
}