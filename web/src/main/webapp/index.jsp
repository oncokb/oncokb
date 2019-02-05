<%@ page import="org.mskcc.cbio.oncokb.util.PropertiesUtils" %>
<%@ page import="org.mskcc.cbio.oncokb.apiModels.CurationPlatformConfigs" %><%--
  User: zhangh2
  Date: 2019-01-28
  Time: 14:28
--%>
<%
    String basePath = request.getContextPath();
%>

<script type="text/javascript">
    window.CurationPlatformConfigString =
    <%=PropertiesUtils.getCurationPlatformConfigs()%>
</script>

<%@ include file="content.jsp" %>
