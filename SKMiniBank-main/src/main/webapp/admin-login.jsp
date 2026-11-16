<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // Forward admin-login.jsp directly to SKMiniBankadmin-login.jsp to maintain consistent route & UI
    request.getRequestDispatcher("/SKMiniBankadmin-login.jsp").forward(request, response);
%>
