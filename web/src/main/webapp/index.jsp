<%
    String url = request.getRequestURL().toString();
    String baseUrl = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
    baseUrl = baseUrl.replace("https://", "").replace("http://", "");
%>
<!DOCTYPE html>
<html class="cbioportal-frontend">
<head>
    <script>
        window.frontendConfig = {
            apiRoot: '//'+ '<%=baseUrl%>/', 
            baseUrl: '<%=baseUrl%>',
            basePath: '<%=request.getContextPath()%>',  
        };

    </script>
     
    <script type="text/javascript" src="//<%=baseUrl%>/js/src/load-frontend.js?<%=GlobalProperties.getAppVersion()%>"></script> 
    



    <script>
            loadReactApp(window.frontendConfig);
    </script>

</head>
