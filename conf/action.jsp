<%--
    Document   : action
    Created on : Feb 18, 2020, 5:01:31 PM
    Author     : Nicola De Nisco

    Questa JSP legge un parametro 'action' dalla request e istanzia la
    corrispettiva action richiedendo l'elaborazione.
    Il contenuto del contex velocity elaborato dall'action viene ritornato
    come oggetto Json.
    Permette di eseguire una action in un contesto Json.
--%>

<%@page language="java"
        contentType="application/json; charset=UTF-8"
        pageEncoding="UTF-8"
        trimDirectiveWhitespaces="true"%>

<jsp:useBean id="actionBean" scope="session" class="org.sirio5.beans.ActionJspBean" />
<jsp:setProperty name="actionBean" property="*" />

<%
  actionBean.runAction(request, response, config, out);
%>
