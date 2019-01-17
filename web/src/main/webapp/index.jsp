<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.IOException" %>

<%!
    private String replaceBaseTag(String content, String newPath) {
        return content.replaceAll("<base[^>]*>", "<base href='" + newPath + "/'/>");
    }

    private String getIndexHtmlContent(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }
%>
<%=replaceBaseTag(getIndexHtmlContent(application.getRealPath("/") + "index.html"), request.getContextPath())%>
