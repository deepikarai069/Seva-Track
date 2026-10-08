<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Officer desk"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<c:choose>
<c:when test="${empty officer}">
  <h1>Officer desk</h1>
  <p class="lead">Demo build, no login: pick who you are.</p>
  <table class="tbl">
    <thead><tr><th>Officer</th><th>Department</th><th>Level</th><th>Ward</th><th>Open</th></tr></thead>
    <tbody>
    <c:forEach var="o" items="${officers}">
      <tr><td><a href="${ctx}/officer?officerId=${o.id}"><c:out value="${o.name}"/></a></td><td>${o.departmentName}</td>
          <td>${o.levelLabel}</td><td>${empty o.wardName ? 'All wards' : o.wardName}</td><td>${o.openLoad} / ${o.maxLoad}</td></tr>
    </c:forEach>
    </tbody>
  </table>
</c:when>
<c:otherwise>
  <h1><c:out value="${officer.name}"/></h1>
  <p class="meta">${officer.levelLabel} &middot; ${officer.departmentName} &middot; ${empty officer.wardName ? 'All wards' : officer.wardName}
    &middot; open ${officer.openLoad} of ${officer.maxLoad}
    <progress max="${officer.maxLoad}" value="${officer.openLoad}"></progress></p>
  <c:if test="${empty queue}"><p class="card">Nothing in your queue.</p></c:if>
  <c:forEach var="c" items="${queue}">
    <section class="card ${c.pastDue ? 'overdue' : ''}">
      <div class="row">
        <h3><a href="${ctx}/track?ticket=${c.ticketNo}"><c:out value="${c.title}"/></a></h3>
        <span class="badge s-${c.status}">${c.status}</span>
      </div>
      <p class="meta"><span class="ticket">${c.ticketNo}</span> &middot; ${c.categoryName} &middot; ${c.wardName}
        &middot; <span class="badge p-${c.priority}">${c.priority}</span> &middot; ${c.levelLabel}
        &middot; due <span class="${c.pastDue ? 'late' : ''}">${c.slaDueAtText}</span></p>
      <c:if test="${not empty c.nextStatuses}">
        <form class="inline" action="${ctx}/officer" method="post">
          <input type="hidden" name="officerId" value="${officer.id}">
          <input type="hidden" name="complaintId" value="${c.id}">
          <select name="status" aria-label="New status">
            <c:forEach var="s" items="${c.nextStatuses}"><option value="${s}">${s}</option></c:forEach>
          </select>
          <input name="note" placeholder="Note (required to resolve)" maxlength="255">
          <button type="submit">Update</button>
        </form>
      </c:if>
    </section>
  </c:forEach>
  <p><a href="${ctx}/officer">&larr; Switch officer</a></p>
</c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
