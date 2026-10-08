<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Home"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<section class="hero">
  <div>
    <h1>Report it. Track it. Know who owns it.</h1>
    <p class="lead">Every complaint gets a ticket, an officer from the right department and ward, and a deadline. If the deadline passes, it moves up the ladder on its own.</p>
    <form class="inline" action="${ctx}/track" method="get">
      <input name="ticket" placeholder="Ticket no, e.g. ST-261007-K4M2X" aria-label="Ticket number" required>
      <button type="submit">Track</button>
    </form>
    <p><a class="btn ghost" href="${ctx}/register">Report a new problem</a></p>
  </div>
  <ol class="ladder" aria-label="Escalation ladder">
    <li><b>L1 Ward Officer</b><span>Routed by category, ward, priority and current workload</span></li>
    <li><b>L2 Supervisor</b><span>Takes over when the L1 deadline is missed</span></li>
    <li><b>L3 Department Head</b><span>Final stop. A miss here is flagged as an SLA breach</span></li>
  </ol>
</section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
