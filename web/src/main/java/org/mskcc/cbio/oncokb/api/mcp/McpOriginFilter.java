package org.mskcc.cbio.oncokb.api.mcp;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

/**
 * Origin validation and CORS for the MCP endpoint ({@value #MCP_PATH}).
 * <p>
 * Must be mapped in web.xml <em>before</em> Tomcat's {@code CorsFilter}, which allows every origin
 * for the REST API. For the MCP path this filter:
 * <ul>
 *     <li>passes requests without an {@code Origin} header (server-to-server clients) unchanged;</li>
 *     <li>rejects any {@code Origin} not listed in {@value McpSettings#ALLOWED_ORIGINS} with 403, as
 *     the MCP Streamable HTTP transport requires (DNS-rebinding protection);</li>
 *     <li>for an allowlisted origin, answers the CORS preflight itself and echoes only that origin,
 *     never {@code *};</li>
 *     <li>hides the {@code Origin} header from the rest of the chain, so the wildcard
 *     {@code CorsFilter} does not add {@code Access-Control-Allow-Origin: *} to MCP responses.</li>
 * </ul>
 * Other paths, and every path when MCP is disabled, pass through untouched.
 */
public class McpOriginFilter implements Filter {
    static final String MCP_PATH = "/api/v1/mcp";
    private static final String ORIGIN = "Origin";
    static final String FORBIDDEN_BODY = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":" + JsonRpc.INVALID_REQUEST
        + ",\"message\":\"Forbidden: Origin is not allowed.\"}}";

    private boolean enabled;
    private Set<String> allowedOrigins;

    public McpOriginFilter() {
    }

    McpOriginFilter(boolean enabled, Set<String> allowedOrigins) {
        this.enabled = enabled;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        if (allowedOrigins == null) {
            enabled = McpSettings.isEnabled();
            allowedOrigins = McpSettings.allowedOrigins();
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (!enabled || !(req instanceof HttpServletRequest) || !isMcpPath((HttpServletRequest) req)) {
            chain.doFilter(req, res);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String origin = request.getHeader(ORIGIN);
        if (origin == null) {
            chain.doFilter(request, response);
            return;
        }
        if (!isAllowed(origin)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getOutputStream().write(FORBIDDEN_BODY.getBytes(StandardCharsets.UTF_8));
            return;
        }

        response.setHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Vary", ORIGIN);
        if ("OPTIONS".equals(request.getMethod()) && request.getHeader("Access-Control-Request-Method") != null) {
            response.setHeader("Access-Control-Allow-Methods", "POST");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization, MCP-Protocol-Version");
            response.setHeader("Access-Control-Max-Age", "600");
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        chain.doFilter(new WithoutOrigin(request), response);
    }

    @Override
    public void destroy() {
    }

    boolean isAllowed(String origin) {
        String normalized = origin.trim().toLowerCase();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        // "null" is what browsers send from sandboxed or file:// pages; never allow it.
        return !normalized.isEmpty() && !"null".equals(normalized) && allowedOrigins.contains(normalized);
    }

    static boolean isMcpPath(HttpServletRequest request) {
        return MCP_PATH.equals(pathWithinApplication(request));
    }

    static String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (uri == null) {
            return "";
        }
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri;
    }

    private static final class WithoutOrigin extends HttpServletRequestWrapper {
        WithoutOrigin(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            return ORIGIN.equalsIgnoreCase(name) ? null : super.getHeader(name);
        }

        @Override
        public Enumeration getHeaders(String name) {
            return ORIGIN.equalsIgnoreCase(name) ? Collections.enumeration(Collections.emptyList()) : super.getHeaders(name);
        }

        @Override
        public Enumeration getHeaderNames() {
            List<String> names = new ArrayList<>();
            Enumeration all = super.getHeaderNames();
            while (all != null && all.hasMoreElements()) {
                String name = String.valueOf(all.nextElement());
                if (!ORIGIN.equalsIgnoreCase(name)) {
                    names.add(name);
                }
            }
            return Collections.enumeration(names);
        }
    }
}
