<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Something went wrong"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<h1>That did not work</h1>
<p class="lead">The page you asked for is missing or the server hit a problem. Nothing you submitted was lost if you saw a ticket number.</p>
<p><a class="btn" href="${ctx}/">Back to home</a></p>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
