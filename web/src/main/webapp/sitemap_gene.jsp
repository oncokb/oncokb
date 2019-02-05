<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/xml; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.mskcc.cbio.oncokb.util.PropertiesUtils" %>
<%@ page import="org.json.simple.parser.JSONParser" %>
<%@ page import="org.json.simple.JSONArray" %>

<%
if (!PropertiesUtils.showSiteMaps()) {
    response.setStatus(404);
} else {

String protocol = (request.isSecure()) ? "https" : "http";

pageContext.setAttribute("entrezGeneId", request.getParameter("entrezGeneId"));
pageContext.setAttribute("hugoSymbol", request.getParameter("hugoSymbol"));
//pageContext.setAttribute("serverRoot", protocol + "://" + request.getServerName()
pageContext.setAttribute("serverRoot", protocol + "://localhost:8080/oncokb");

%>


<c:if test = "${PropertiesUtils.showSiteMaps()}">
    <c:import var="alterationsJson" url="${serverRoot}/api/v1/genes/${entrezGeneId}/variants"/>
</c:if>

<%

if (PropertiesUtils.showSiteMaps()) {
    String json = (String)pageContext.getAttribute("alterationsJson");
    Object obj = new JSONParser().parse(json);
    JSONArray ja = (JSONArray) obj;
    pageContext.setAttribute("alterationList", ja);
} else {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
}

%>

<c:if test = "${PropertiesUtils.showSiteMaps()}">
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <c:forEach items="${alterationList}" var="alteration">
        <url>
            <loc><%=protocol%>://<%=request.getServerName()%>/gene/${hugoSymbol}/alteration/<c:out value="${alteration.get('name')}"/></loc>
          </url>
    </c:forEach>
</urlset>
</c:if>

<% } %>
