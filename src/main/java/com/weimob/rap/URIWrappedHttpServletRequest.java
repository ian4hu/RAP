package com.weimob.rap;

import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.net.URI;

/**
 * Created by ian on 2017/1/6.
 *
 * @author ian
 * @since 2017/01/06 11:11
 */
public class URIWrappedHttpServletRequest extends HttpServletRequestWrapper {
    private final URI uri;
    private final String url;

    public URIWrappedHttpServletRequest(HttpServletRequest request, URI uri) {
        super(request);
        this.uri = uri;
        url = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString()).replacePath(uri.getPath())
            .replaceQuery(uri.getQuery()).build().toString();
    }

    @Override
    public String getRequestURI() {
        return uri.toString();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(url);
    }

    @Override
    public String getQueryString() {
        return uri.getQuery();
    }
}
