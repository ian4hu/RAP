package com.weimob.rap;

import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
            doFilterInternal((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
        }
    }

    @SuppressWarnings("unchecked")
    private void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String httpMethod = request.getMethod();
        if (request.getAttribute(JSON2QueryStringFilter.class.getName() + ".processed") != null ||
            !("POST".equals(httpMethod) || "PUT".equals(httpMethod))
            ) {
            filterChain.doFilter(request, response);
            return;
        }
        request.setAttribute(JSON2QueryStringFilter.class.getName() + ".processed", true);
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

    @SuppressWarnings("Since15")
    public static void main(String[] args) throws ScriptException, FileNotFoundException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        engine.eval(new FileReader("/Users/ian/git/tmp/RAP_src/src/main/webapp/stat/js/util/mock-min.js"));
        Object val = engine.eval("JSON.stringify(Mock.mock({\"monitorTrackId\":\"ab2571b3-569a-44d2-b36e-6e51826cce63\",\"timestamp\":function(){return new Date().getTime()},\"_merchantId\":1,\"responseVo\":{\"access_token\":\"vGC6kQrvTuSFxT9Ab7FHSuf2hFPP8JIJMjOkhWdRhJM5eK-k5DTdZZVpO2U66elcn2MwxxBuNimmuN84nF_1Lc8WYQZ6U14_8xkO6mWA3SeXn7RaIxs5aLVS8VNJQbSWUOFhAEAXRI\",\"is_bp\":0},\"successForMornitor\":true,\"processResult\":true,\"returnCode\":\"000000\"}))");
        System.out.println(val);
    }
}
