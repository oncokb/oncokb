<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/xml; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.mskcc.cbio.oncokb.util.PropertiesUtils" %>
<%@ page import="org.json.simple.parser.JSONParser" %>
<%@ page import="org.json.simple.JSONArray" %>

<%
String protocol = (request.isSecure()) ? "https" : "http";
//pageContext.setAttribute("serverRoot", protocol + "://" + request.getServerName());
pageContext.setAttribute("serverRoot", protocol + "://localhost:8080/oncokb");

if (!PropertiesUtils.showSiteMaps()) {
    response.setStatus(404);
} else {

%>

<c:if test = "${PropertiesUtils.showSiteMaps()}">
    <c:import var="dataJson" url="${serverRoot}/api/v1/genes"/>
</c:if>
<%

if (PropertiesUtils.showSiteMaps()) {
     String json = (String)pageContext.getAttribute("dataJson");
       Object obj = new JSONParser().parse(json);
       JSONArray ja = (JSONArray) obj;
       pageContext.setAttribute("mylist", ja);
} else {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
}

%>
<c:if test = "${PropertiesUtils.showSiteMaps()}">
   <?xml version="1.0" encoding="UTF-8"?>
    <%--<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">--%>
        <%--<url>--%>
            <%--<loc><%=protocol%>://<%=request.getServerName()%>/levels</loc>--%>
            <%--<priority>1.0</priority>--%>
        <%--</url>--%>
    <%--</urlset>--%>
   <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
       <c:forEach items="${mylist}" var="obj">
           <sitemap>
                 <loc><%=protocol%>://<%=request.getServerName()%>/sitemap_gene.xml?hugoSymbol=<c:out value="${obj.get('hugoSymbol')}"/>&amp;entrezGeneId=<c:out value="${obj.get('entrezGeneId')}"/></loc>
            </sitemap>
       </c:forEach>
   </sitemapindex>

</c:if>

<% } %>

