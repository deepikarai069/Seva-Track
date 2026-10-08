<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Control room"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="row">
  <h1>Control room</h1>
  <form action="${ctx}/admin" method="post"><input type="hidden" name="action" value="run-sla"><button type="submit">Run SLA check now</button></form>
</div>
<p class="meta">Last 30 days &middot; reports:
  <a href="${ctx}/reports?format=json">JSON</a> &middot; <a href="${ctx}/reports?format=xml">XML</a></p>

<h2>Departments</h2>
<table class="tbl">
  <thead><tr><th>Department</th><th>Total</th><th>Open</th><th>Resolved</th><th>Escalated</th><th>SLA breached</th><th>Avg resolution (h)</th></tr></thead>
  <tbody>
  <c:forEach var="d" items="${departments}">
    <tr><td>${d.departmentName}</td><td>${d.total}</td><td>${d.open}</td><td>${d.resolved}</td><td>${d.escalated}</td>
        <td class="${d.breached > 0 ? 'late' : ''}">${d.breached}</td><td>${d.avgResolutionHours}</td></tr>
  </c:forEach>
  </tbody>
</table>

<h2>Overdue right now <small>(${overdue.size()})</small></h2>
<c:choose>
<c:when test="${empty overdue}"><p class="card">No complaint is past its deadline.</p></c:when>
<c:otherwise>
<table class="tbl">
  <thead><tr><th>Ticket</th><th>Title</th><th>Dept</th><th>Priority</th><th>Level</th><th>With</th><th>Was due</th></tr></thead>
  <tbody>
  <c:forEach var="c" items="${overdue}">
    <tr><td><a class="ticket" href="${ctx}/track?ticket=${c.ticketNo}">${c.ticketNo}</a></td><td><c:out value="${c.title}"/></td><td>${c.departmentName}</td>
        <td><span class="badge p-${c.priority}">${c.priority}</span></td><td>${c.levelLabel}</td>
        <td><c:out value="${empty c.assignedOfficerName ? '-' : c.assignedOfficerName}"/></td><td class="late">${c.slaDueAtText}</td></tr>
  </c:forEach>
  </tbody>
</table>
</c:otherwise>
</c:choose>

<h2>Latest complaints</h2>
<table class="tbl">
  <thead><tr><th>Ticket</th><th>Title</th><th>Ward</th><th>Status</th><th>Level</th><th>With</th><th>Deadline</th></tr></thead>
  <tbody>
  <c:forEach var="c" items="${recent}">
    <tr><td><a class="ticket" href="${ctx}/track?ticket=${c.ticketNo}">${c.ticketNo}</a></td><td><c:out value="${c.title}"/></td><td>${c.wardName}</td>
        <td><span class="badge s-${c.status}">${c.status}</span></td><td>L${c.escalationLevel}${c.slaBreached ? ' (breached)' : ''}</td>
        <td><c:out value="${empty c.assignedOfficerName ? '-' : c.assignedOfficerName}"/></td>
        <td class="${c.pastDue ? 'late' : ''}">${c.slaDueAtText}</td></tr>
  </c:forEach>
  </tbody>
</table>

<h2>Officer workload</h2>
<table class="tbl">
  <thead><tr><th>Officer</th><th>Department</th><th>Level</th><th>Ward</th><th>Load</th></tr></thead>
  <tbody>
  <c:forEach var="o" items="${officers}">
    <tr><td><a href="${ctx}/officer?officerId=${o.id}"><c:out value="${o.name}"/></a></td><td>${o.departmentName}</td><td>${o.levelLabel}</td>
        <td>${empty o.wardName ? 'All wards' : o.wardName}</td>
        <td>${o.openLoad} / ${o.maxLoad} <progress max="${o.maxLoad}" value="${o.openLoad}"></progress></td></tr>
  </c:forEach>
  </tbody>
</table>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
