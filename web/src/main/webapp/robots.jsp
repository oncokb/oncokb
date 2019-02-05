<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/plain; charset=UTF-8" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.mskcc.cbio.oncokb.util.PropertiesUtils"%>
<%
String protocol = (request.isSecure()) ? "https" : "http";
if (!PropertiesUtils.showSiteMaps()) {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
}
%>
<c:if test = "${PropertiesUtils.showSiteMaps()}">
Sitemap: <%=protocol%>://<%=request.getServerName()%>/sitemap_index.xml
</c:if>

<c:if test = "${!PropertiesUtils.showSiteMaps()}">
User-agent: *
Disallow: /
</c:if>


