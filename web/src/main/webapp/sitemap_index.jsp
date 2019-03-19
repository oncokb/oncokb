<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/xml; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.mskcc.cbio.oncokb.util.PropertiesUtils" %>
<%@ page import="org.json.simple.parser.JSONParser" %>
<%@ page import="org.json.simple.JSONArray" %>

<%
    String url = request.getRequestURL().toString();
    String baseUrl = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();

    pageContext.setAttribute("serverRoot", baseUrl);

    if (!PropertiesUtils.showSiteMaps()) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
        response.setHeader("X-Robots-Tag", "noindex");

%>

<c:import var="dataJson" url="${serverRoot}/api/v1/genes"/>
<%

    String json = (String) pageContext.getAttribute("dataJson");
    Object obj = new JSONParser().parse(json);
    JSONArray ja = (JSONArray) obj;
    pageContext.setAttribute("mylist", ja);

%>
<?xml version="1.0" encoding="UTF-8"?>
<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <sitemap>
        <loc>${serverRoot}/sitemap_general.xml</loc>
    </sitemap>
    <c:forEach items="${mylist}" var="obj">
        <sitemap>
            <loc>${serverRoot}/sitemap_gene.xml?hugoSymbol=<c:out value="${obj.get('hugoSymbol')}"/>&amp;entrezGeneId=<c:out value="${obj.get('entrezGeneId')}"/></loc>
        </sitemap>
    </c:forEach>
</sitemapindex>

<% } %>

