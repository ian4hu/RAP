package com.weimob.rap;

import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ian on 2017/1/6.
 *
 * @author ian
 * @since 2017/01/06 10:10
 */
public class JSON2QueryStringFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            doFilterInteral((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
        }
    }

    @SuppressWarnings("unchecked")
    private void doFilterInteral(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String httpMethod = request.getMethod();
        if (!("POST".equals(httpMethod) || "PUT".equals(httpMethod))) {
            filterChain.doFilter(request, response);
            return;
        }
        List<MediaType> types = MediaType.parseMediaTypes(request.getHeader("Content-Type"));
        MediaType.sortBySpecificityAndQuality(types);

        for (MediaType type : types) {
            if (type.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)) {
                // POST FORM
                Enumeration<String> names = (Enumeration<String>) request.getParameterNames();
                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(request.getRequestURI());
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    builder.replaceQueryParam(name, (Object[]) request.getParameterValues(name));
                }
                URI uri = builder.build().toUri();
                // 重写uri
                filterChain.doFilter(new URIWrappedHttpServletRequest(request, uri), response);
                return;
            }

            if (type.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                // JSON
                // 获取JSON内容
                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }
        }
    }

    public void destroy() {

    }

    public static void main(String[] args) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/aaa/vdvd?bbb=123");
        Map<String, String> param = new HashMap<String, String>();
        param.put("name", "value");
        builder.replaceQueryParam("map", param);
        System.out.println(builder.build().toString());
    }
}
