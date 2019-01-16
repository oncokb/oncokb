<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>

<c:import var = "content" url = "index.html"/>
<c:set var = "baseHref" value="<base href=\"${request.getContextPath()}/\" />" />
<c:set var = "updatedContent" value = "${fn:replace(content, '<base href=\"/\">', baseHref)}" />
"
<c:out value = "${updatedContent}" escapeXml="false"/>
