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

        pageContext.setAttribute("entrezGeneId", request.getParameter("entrezGeneId"));
        pageContext.setAttribute("hugoSymbol", request.getParameter("hugoSymbol"));

%>

<c:import var="alterationsJson" url="${serverRoot}/api/v1/genes/${entrezGeneId}/variants"/>

<%

    String json = (String) pageContext.getAttribute("alterationsJson");
    Object obj = new JSONParser().parse(json);
    JSONArray ja = (JSONArray) obj;
    pageContext.setAttribute("alterationList", ja);

%>

<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <url>
        <loc>${serverRoot}/gene/${hugoSymbol}</loc>
    </url>
    <c:forEach items="${alterationList}" var="alteration">
        <url>
            <loc>${serverRoot}/gene/${hugoSymbol}/<c:out value="${alteration.get('name')}"/></loc>
        </url>
    </c:forEach>
</urlset>

<% } %>
