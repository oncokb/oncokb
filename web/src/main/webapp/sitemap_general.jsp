<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/xml; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.mskcc.cbio.oncokb.util.PropertiesUtils" %>
<%@ page import="org.json.simple.parser.JSONParser" %>
<%@ page import="org.json.simple.JSONArray" %>

<%
    if (!PropertiesUtils.showSiteMaps()) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
        response.setHeader("X-Robots-Tag", "noindex");

        String url = request.getRequestURL().toString();
        String baseUrl = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();

        pageContext.setAttribute("serverRoot", baseUrl);

%>

<c:if test="${PropertiesUtils.showSiteMaps()}">
    <?xml version="1.0" encoding="UTF-8"?>
    <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
        <url>
            <loc>${serverRoot}/levels</loc>
            <changefreq>monthly</changefreq>
            <priority>1.0</priority>
        </url>
        <url>
            <loc>${serverRoot}/actionable-genes</loc>
            <changefreq>weekly</changefreq>
            <priority>0.9</priority>
        </url>
        <url>
            <loc>${serverRoot}/terms</loc>
            <changefreq>monthly</changefreq>
            <priority>0.8</priority>
        </url>
        <url>
            <loc>${serverRoot}/news</loc>
            <changefreq>weekly</changefreq>
            <priority>0.7</priority>
        </url>
        <url>
            <loc>${serverRoot}/cancer-genes</loc>
            <changefreq>weekly</changefreq>
            <priority>0.6</priority>
        </url>
        <url>
            <loc>${serverRoot}/precision-oncology-therapies</loc>
            <changefreq>weekly</changefreq>
            <priority>0.6</priority>
        </url>
        <url>
            <loc>${serverRoot}/about</loc>
            <changefreq>monthly</changefreq>
            <priority>0.4</priority>
        </url>
        <url>
            <loc>${serverRoot}/team</loc>
            <changefreq>monthly</changefreq>
            <priority>0.3</priority>
        </url>
    </urlset>
</c:if>

<% } %>
