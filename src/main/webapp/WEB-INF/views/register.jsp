<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Report a problem"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<h1>Report a problem</h1>
<c:if test="${not empty errors}">
  <ul class="errors" role="alert"><c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach></ul>
</c:if>
<form class="card form" action="${ctx}/register" method="post">
  <div class="grid2">
    <label>Your name<input name="citizenName" maxlength="100" required value="<c:out value='${form.citizenName}'/>"></label>
    <label>Phone<input name="citizenPhone" maxlength="15" inputmode="tel" required value="<c:out value='${form.citizenPhone}'/>"></label>
  </div>
  <label>Email <small>(optional)</small><input name="citizenEmail" type="email" maxlength="120" value="<c:out value='${form.citizenEmail}'/>"></label>
  <div class="grid2">
    <label>What is it about?
      <select name="categoryId" required>
        <option value="">Choose a category</option>
        <c:forEach var="d" items="${departments}">
          <optgroup label="${d.name}">
            <c:forEach var="cat" items="${categories}">
              <c:if test="${cat.departmentId == d.id}">
                <option value="${cat.id}" ${form.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
              </c:if>
            </c:forEach>
          </optgroup>
        </c:forEach>
      </select>
    </label>
    <label>Ward
      <select name="wardId" required>
        <option value="">Choose your ward</option>
        <c:forEach var="w" items="${wards}"><option value="${w.id}" ${form.wardId == w.id ? 'selected' : ''}><c:out value="${w.name}"/></option></c:forEach>
      </select>
    </label>
  </div>
  <label>Urgency
    <select name="priority">
      <option value="">Let SevaTrack decide from the category</option>
      <c:forEach var="p" items="${priorities}"><option value="${p}" ${form.priority == p.name() ? 'selected' : ''}>${p}</option></c:forEach>
    </select>
  </label>
  <label>Short title<input name="title" maxlength="150" required value="<c:out value='${form.title}'/>"></label>
  <label>Details <small>(landmark, since when, how bad)</small><textarea name="description" rows="5" maxlength="2000" required><c:out value="${form.description}"/></textarea></label>
  <button type="submit">Submit complaint</button>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
