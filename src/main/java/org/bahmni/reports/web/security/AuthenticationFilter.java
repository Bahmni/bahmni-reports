package org.bahmni.reports.web.security;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class AuthenticationFilter implements Filter {

    private static final Logger logger = Logger.getLogger(AuthenticationFilter.class);
    public static final String REPORTING_COOKIE_NAME = "reporting_session";
    private OpenMRSAuthenticator authenticator;
    private BahmniReportsProperties properties;
    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public AuthenticationFilter(OpenMRSAuthenticator authenticator,
                                BahmniReportsProperties properties,
                                RequestMappingHandlerMapping handlerMapping) {
        this.authenticator = authenticator;
        this.properties = properties;
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doHttpFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doHttpFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if (!handledByThisApplication(httpServletRequest)) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Reports application cannot handle url " + httpServletRequest.getRequestURI());
            return;
        }

        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            redirectToLogin(httpServletRequest, httpServletResponse);
            return;
        }
        AuthenticationResponse authenticationResponse = AuthenticationResponse.NOT_AUTHENTICATED;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(REPORTING_COOKIE_NAME)) {
                authenticationResponse = authenticator.authenticate(cookie.getValue());
            }
        }

        switch (authenticationResponse) {
            case AUTHORIZED:
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                break;
            case UNAUTHORIZED:
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Privileges is required to access reports");
                break;
            default:
                redirectToLogin(httpServletRequest, httpServletResponse);
                break;
        }
    }

    private boolean handledByThisApplication(HttpServletRequest httpServletRequest) {
        try {
            return handlerMapping.getHandler(httpServletRequest) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void redirectToLogin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        httpServletResponse.getWriter().write("Please login to continue");
        URI redirectUri;
        try {
            redirectUri = new URIBuilder(properties.getBahmniLoginUrl())
                    .addParameter("from", httpServletRequest.getRequestURL() +
                            "?" +
                            httpServletRequest.getQueryString())
                    .build();
        } catch (URISyntaxException e) {
            logger.error("Bad url specified");
            throw new RuntimeException(e);
        }

        httpServletResponse.sendRedirect(redirectUri.toString());
    }

    @Override
    public void destroy() {}
}
