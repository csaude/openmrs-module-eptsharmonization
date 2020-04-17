<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeEncounterTypeList.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="eptsharmonization.encountertype.harmonize" />
</h2>
<br />
<br />
<form method="post" class="box">
	<fieldset>
		<b class="boxHeader"><spring:message
				code="eptsharmonization.encountertype.harmonize.onlyOnMDServer" /></b>
		<table>
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${onlyMetadataEncounterTypes}">
				<tr>
					<td valign="top">${item.encounterType.id}</td>
					<td valign="top">${item.encounterType.name}</td>
					<td valign="top">${item.encounterType.description}</td>
					<td valign="top">${item.encounterType.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<c:if test="${not empty onlyMetadataEncounterTypes}">
		<form method="post">
			<fieldset>
				<input type="submit"
					value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
					name="harmonizeNewFromMetadata" />
			</fieldset>
		</form>
	</c:if>
</form>
<br />
<br />
<form method="post" class="box">
	<fieldset>
		<b class="boxHeader"><spring:message
				code="eptsharmonization.encountertype.harmonize.onlyOnPServer" /></b>
		<table>
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${OnlyProductionEncounterTypes}">
				<tr>
					<td valign="top">${item.encounterType.id}</td>
					<td valign="top">${item.encounterType.name}</td>
					<td valign="top">${item.encounterType.description}</td>
					<td valign="top">${item.encounterType.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
</form>
<br />
<br />
<form method="post" class="box">
	<fieldset>
		<b class="boxHeader"><spring:message
				code="eptsharmonization.encountertype.harmonize.mdserver.with.differentID.andEqualUUID" /></b>
		<table>
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${mdsEncountersPartialEqual}">
				<tr>
					<td valign="top">${item.encounterType.id}</td>
					<td valign="top">${item.encounterType.name}</td>
					<td valign="top">${item.encounterType.description}</td>
					<td valign="top">${item.encounterType.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<fieldset>
		<b class="boxHeader"><spring:message
				code="eptsharmonization.encountertype.harmonize.pdserver.with.differentID.andEqualUUID" /></b>
		<table>
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${pdsEncountersPartialEqual}">
				<tr>
					<td valign="top">${item.encounterType.id}</td>
					<td valign="top">${item.encounterType.name}</td>
					<td valign="top">${item.encounterType.description}</td>
					<td valign="top">${item.encounterType.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<c:if test="${not empty mdsEncountersPartialEqual}">
		<form method="post">
			<fieldset>
				<input type="submit"
					value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
					name="harmonizeNewFromMetadata" />
			</fieldset>
		</form>
	</c:if>
</form>


<%@ include file="/WEB-INF/template/footer.jsp"%>