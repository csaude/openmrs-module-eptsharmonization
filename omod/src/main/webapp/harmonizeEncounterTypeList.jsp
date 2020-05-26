<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeEncounterTypeList.form" />

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

th {
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
<c:if test="${not empty harmonizedETSummary}">
	<div id="openmrs_msg">
	   <b> <spring:message code="eptsharmonization.summay.of.already.harmonized.mapping" 	/> :</b><br />
		<c:forEach var="msg" items="${harmonizedETSummary}">
			<span> <spring:message code="${msg}" text="${msg}" />
			</span>
			<br />
		</c:forEach>
	</div>
	<br />
</c:if>

<c:if test="${not empty onlyMetadataEncounterTypes}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.onlyOnMDServer" /></b>
	<form method="get" class="box"
		action="harmonizeAddNewEncounterTypes.form">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${onlyMetadataEncounterTypes}">
				<tr>
					<td valign="top" align="center">${item.encounterType.id}</td>
					<td valign="top">${item.encounterType.name}</td>
					<td valign="top">${item.encounterType.description}</td>
					<td valign="top">${item.encounterType.uuid}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="4">
					<div class="submit-btn" align="center">
						<input type="submit" value='<spring:message code="general.next"/>'
							name="harmonizeNewFromMetadata" />
					</div>
				</td>
			</tr>
		</table>
	</form>
	<br />
	<br />
</c:if>

<c:if
	test="${not empty productionItemsToDelete && empty onlyMetadataEncounterTypes}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.onlyOnPServer.unused" /></b>
	<form method="get" class="box"
		action="harmonizeDeleteEncounterTypes.form">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${productionItemsToDelete}">
				<tr>
					<td valign="top" align="center">${item.encounterType.id}</td>
					<td valign="top">${item.encounterType.name}</td>
					<td valign="top">${item.encounterType.description}</td>
					<td valign="top">${item.encounterType.uuid}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="4">
					<div class="submit-btn" align="center">
						<input type="submit" value='<spring:message code="general.next"/>'
							name="exportNewFromProduction" />
					</div>
				</td>
			</tr>
		</table>
	</form>
	<br />
	<br />
</c:if>

<c:if
	test="${not empty productionItemsToExport && empty onlyMetadataEncounterTypes && empty productionItemsToDelete && empty encounterTypesPartialEqual && empty encounterTypesWithDifferentNames}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.onlyOnPServer.inuse" /></b>
	<form method="get" class="box"
		action="harmonizeExportEncounterTypes.form">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${productionItemsToExport}">
				<tr>
					<td valign="top" align="center">${item.encounterType.id}</td>
					<td valign="top">${item.encounterType.name}</td>
					<td valign="top">${item.encounterType.description}</td>
					<td valign="top">${item.encounterType.uuid}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="4">
					<div class="submit-btn" align="center">
						<input type="submit" value='<spring:message code="general.next"/>'
							name="exportNewFromProduction" />
					</div>
				</td>
			</tr>
		</table>
	</form>
	<br />
	<br />
</c:if>

<c:if
	test="${not empty encounterTypesPartialEqual && empty onlyMetadataEncounterTypes && empty productionItemsToDelete && empty encounterTypesWithDifferentNames}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.differentID.andEqualUUID" /></b>
	<form method="get" class="box"
		action="harmonizeUpdateEncounterTypes.form">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.id" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.description" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.id" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${encounterTypesPartialEqual}">
				<tr>
					<td valign="top" align="center">${item.value[0].encounterType.id}</td>
					<td valign="top">${item.value[0].encounterType.name}</td>
					<td valign="top">${item.value[0].encounterType.description}</td>
					<td valign="top" align="center">${item.value[1].encounterType.id}</td>
					<td valign="top">${item.value[1].encounterType.name}</td>
					<td valign="top">${item.value[1].encounterType.description}</td>
					<td valign="top">${item.key}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="7">
					<div class="submit-btn" align="center">
						<input type="submit" value='<spring:message code="general.next"/>'
							name="harmonizeNewFromMetadata" />
					</div>
				</td>
			</tr>
		</table>
	</form>
	<br />
	<br />
</c:if>

<c:if
	test="${not empty encounterTypesWithDifferentNames && empty onlyMetadataEncounterTypes && empty productionItemsToDelete}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.differentNamesAndSameUUIDAndID" /></b>
	<form method="get" class="box"
		action="harmonizeUpdateEncounterTypeNames.form">
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
					<td valign="top">${entry.value[0].encounterType.name}</td>
					<td valign="top">${entry.value[0].encounterType.description}</td>
					<td valign="top">${entry.value[1].encounterType.name}</td>
					<td valign="top">${entry.value[1].encounterType.description}</td>
					<td valign="top" align="center">${entry.value[0].encounterType.id}</td>
					<td valign="top">${entry.key}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="6">
					<div class="submit-btn" align="center">
						<input type="submit" value='<spring:message code="general.next"/>'
							name="updateEncounterTypeNames" />
					</div>
				</td>
			</tr>
		</table>
	</form>
	<br />
	<br />
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>