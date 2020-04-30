<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeUpdateEncounterTypeNames.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<style>
p {
	border: 1px solid black;
}

table {
	border: 3px solid #1aac9b;
	border-collapse: collapse;
}

tr:first-child {
	background-color: #1aac9b;
}

tr:first-child label {
	padding: 4px !important;
	color: #fff;
	font-weight: bold;
	margin: 4px !important;
	text-shadow: 0 0 .3em black;
	font-size: 12pt;
}

td {
	border: 1px solid #1aac9b;
}

.style1 {
	font-size: 14px;
	font-weight: bold;
}

.obs {
	font-size: 14px;
	font-weight: bold;
}

.submit-btn {
	flex: 1;
	margin: 10px 15px;
}

.submit-btn input {
	color: #fff;
	background: #1aac9b;
	padding: 8px;
	width: 12.8em;
	font-weight: bold;
	text-shadow: 0 0 .3em black;
	font-size: 9pt;
	border-radius: 5px 5px;
}
</style>

<h2>
	<spring:message code="eptsharmonization.encountertype.harmonize" />
</h2>
<br />
<br />

<b class="boxHeader"><spring:message
		code="eptsharmonization.encountertype.harmonize.differentNamesAndSameUUIDAndID" /></b>
<form method="post" class="box" action="harmonizeEncounterTypeList.form">
	<fieldset>
		<table cellspacing="0" border="0" style="width: 100%">
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
			<tr>
				<td colspan="6">
					<div class="submit-btn" align="center">
						<input type="submit"
							value="<spring:message code="general.previous"/>" name="previous" />
						<input type="submit"
							value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
							name="updateEncounterTypeNames" />
					</div>
				</td>
			</tr>
		</table>
	</fieldset>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>