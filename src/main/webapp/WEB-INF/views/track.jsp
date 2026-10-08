<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Track ticket"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<h1>Track a ticket</h1>
<form class="inline" action="${ctx}/track" method="get">
  <input name="ticket" placeholder="ST-261007-K4M2X" value="<c:out value='${ticket}'/>" aria-label="Ticket number" required>
  <button type="submit">Track</button>
</form>
<c:if test="${not empty error}"><p class="errors" role="alert"><c:out value="${error}"/></p></c:if>

<c:if test="${not empty complaint}">
  <c:if test="${justRegistered}">
    <p class="notice">Complaint registered. Keep this ticket number: <b class="ticket"><c:out value="${complaint.ticketNo}"/></b></p>
  </c:if>
  <section class="card">
    <div class="row">
      <h2><c:out value="${complaint.title}"/></h2>
      <span class="badge s-${complaint.status}">${complaint.status}</span>
    </div>
    <p class="meta"><span class="ticket"><c:out value="${complaint.ticketNo}"/></span>
      &middot; ${complaint.categoryName} &middot; ${complaint.wardName} &middot; ${complaint.departmentName}
      &middot; <span class="badge p-${complaint.priority}">${complaint.priority}</span></p>
    <p><c:out value="${complaint.description}"/></p>
    <dl class="facts">
      <div><dt>Filed</dt><dd>${complaint.createdAtText}</dd></div>
      <div><dt>With</dt><dd><c:out value="${empty complaint.assignedOfficerName ? 'Awaiting assignment' : complaint.assignedOfficerName}"/></dd></div>
      <div><dt>Deadline</dt><dd class="${complaint.pastDue ? 'late' : ''}">${complaint.slaDueAtText}<c:if test="${complaint.pastDue}"> (overdue)</c:if></dd></div>
      <c:if test="${not empty complaint.resolvedAt}"><div><dt>Resolved</dt><dd>${complaint.resolvedAtText}</dd></div></c:if>
    </dl>
    <ol class="ladder small" aria-label="Escalation level">
      <li class="${complaint.escalationLevel >= 1 ? 'on' : ''}"><b>L1</b><span>Ward Officer</span></li>
      <li class="${complaint.escalationLevel >= 2 ? 'on' : ''}"><b>L2</b><span>Supervisor</span></li>
      <li class="${complaint.escalationLevel >= 3 ? 'on' : ''} ${complaint.slaBreached ? 'breach' : ''}"><b>L3</b><span>Dept Head<c:if test="${complaint.slaBreached}"> - SLA breached</c:if></span></li>
    </ol>
  </section>

  <h2>History</h2>
  <ol class="timeline">
    <c:forEach var="h" items="${history}">
      <li>
        <span class="when">${h.changedAtText}</span>
        <b><c:if test="${not empty h.oldStatus and h.oldStatus != h.newStatus}">${h.oldStatus} &rarr; </c:if>${h.newStatus}</b>
        <span class="lvl">${h.levelText}</span>
        <c:if test="${not empty h.newOfficerName}"><span> with <c:out value="${h.newOfficerName}"/></span></c:if>
        <small>by <c:out value="${h.actor}"/><c:if test="${not empty h.note}"> &mdash; <c:out value="${h.note}"/></c:if></small>
      </li>
    </c:forEach>
  </ol>
</c:if>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
