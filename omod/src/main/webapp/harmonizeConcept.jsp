<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/eptsharmonization/css/eptsharmonization.css"/>

<openmrs:require privilege="Manage Visit Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeVisitTypes.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>
<h2>
	<spring:message code="eptsharmonization.concept.harmonize.status" />
</h2>
<br />
<br />
<c:if test="${not empty summaries}">
	<div id="openmrs_msg">
		<c:forEach var="summary" items="${summaries}">
			<span>${summary}</span>
			<br />
		</c:forEach>
	</div>
	<br />
</c:if>

<c:if test="${not empty missingInMDS}">
	<b class="boxHeader"><spring:message code="eptsharmonization.concept.harmonize.missingInMDS" /></b>
	<form method="post" class="box" action="exportConcepts.form">
		<table class="box" cellspacing="0" border="0" style="width: 100%;">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${missingInMDS}">
				<tr>
					<td valign="top" align="center">${item.conceptId}</td>
					<td valign="top">${item.names[0].name}</td>
					<td valign="top">${item.descriptions[0].description}</td>
					<td valign="top">${item.uuid}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="6">
					<div class="submit-btn" align="center">
						<input type="submit"
							   value='<spring:message code="eptsharmonization.encountertype.btn.exportNewFromPDS"/>'
							   name="exportNewFromProduction" />
					</div>
				</td>
			</tr>
		</table>
	</form>
	<br />
	<br />
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>