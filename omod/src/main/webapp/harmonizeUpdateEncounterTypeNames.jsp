<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeUpdateEncounterTypeNames.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="eptsharmonization.encountertype.harmonize" />
</h2>
<br />
<br />

<b class="boxHeader"><spring:message
		code="eptsharmonization.encountertype.harmonize.differentNamesAndSameUUIDAndID" /></b>
<form method="post" class="box" action="harmonizeEncounterTypeList.form">
	<fieldset>
		<table>
			<tr>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.description" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.description" /></th>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="entry" items="${encounterTypesWithDifferentNames}">
				<tr>
					<td valign="top" align="center">${entry.value[0].encounterType.name}</td>
					<td valign="top" align="center">${entry.value[0].encounterType.description}</td>
					<td valign="top" align="center">${entry.value[1].encounterType.name}</td>
					<td valign="top" align="center">${entry.value[1].encounterType.description}</td>
					<td valign="top" align="center">${entry.value[0].encounterType.id}</td>
					<td valign="top">${entry.key}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<form method="post">
		<fieldset>
			<input type="submit"
				value="<spring:message code="general.previous"/>" name="previous" />
			<input type="submit"
				value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
				name="updateEncounterTypeNames" />
		</fieldset>
	</form>
</form>
<br />
<br />

<%@ include file="/WEB-INF/template/footer.jsp"%>