package com.github.kribblo.fourteenkilobytes;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIUtils;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class FourteenKiloByteProxyServlet extends ProxyServlet {

    private static final int KB = 1024;
    private static final int FOURTEEN_KB = 14 * KB;
    private static final String TARGET_SYSTEM_PROPERTY_KEY = "14kb.target";
    private static final String TEXT_HTML = "text/html";
    private static final Logger log = LoggerFactory.getLogger(FourteenKiloByteProxyServlet.class);

    @Override
    protected void initTarget() throws ServletException {
        targetUri = System.getProperty(TARGET_SYSTEM_PROPERTY_KEY);

        if (targetUri == null) {
            throw new ServletException(TARGET_SYSTEM_PROPERTY_KEY + " is required.");
        }

        try {
            targetUriObj = new URI(targetUri);
        } catch (Exception e) {
            throw new ServletException("Not a a valid URL: " + targetUri, e);
        }

        log.info("Target is set to: {}", targetUri);

        targetHost = URIUtils.extractHost(targetUriObj);
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        String method = servletRequest.getMethod();
        String requestURI = servletRequest.getRequestURI();
        String proxyRequestUri = rewriteUrlFromRequest(servletRequest);
        log.info("{} {} forwarded to {}", method, requestURI, proxyRequestUri);

        super.service(servletRequest, servletResponse);
    }

    @Override
    protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse) throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            Header contentType = entity.getContentType();
            if(contentType.getValue().startsWith(TEXT_HTML)) {
                writeCappedContentToResponse(servletResponse, entity);
            } else {
                OutputStream servletOutputStream = servletResponse.getOutputStream();
                entity.writeTo(servletOutputStream);
            }
        }
    }

    private void writeCappedContentToResponse(HttpServletResponse servletResponse, HttpEntity entity) throws IOException {
        OutputStream servletOutputStream = servletResponse.getOutputStream();

        byte[] buffer = new byte[KB];
        int length;

        int totalLength = 0;
        while ((length = entity.getContent().read(buffer)) != -1) {

            if (length + totalLength > FOURTEEN_KB) {
                length = FOURTEEN_KB - totalLength;
                log.info("Content cut of at 14 kilobytes");
            }

            servletOutputStream.write(buffer, 0, length);

            totalLength += length;

            if (totalLength >= FOURTEEN_KB) {
                break;
            }
        }
    }

    @Override
    protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            String name = header.getName();
            if (!hopByHopHeaders.containsHeader(name)) {
                String value = header.getValue();
                servletResponse.addHeader(name, value);
            }
        }
    }

    @Override
    protected String getRealCookie(String cookieValue) {
        return cookieValue;
    }

    @Override
    protected String getTargetUri(HttpServletRequest servletRequest) {
        return targetUri;
    }
}
