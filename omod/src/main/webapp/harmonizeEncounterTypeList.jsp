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
<b class="boxHeader"><spring:message code="eptsharmonization.encountertype.harmonize.onlyOnMDServer" /></b>
<form method="post" class="box" action="harmonizeAddNewEncounterTypes.form">
	<fieldset>
		<table>
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
		</table>
	</fieldset>
	<c:if test="${not empty onlyMetadataEncounterTypes}">
		<form method="post">
			<fieldset>
				<input type="submit"
					value='<spring:message code="general.next"/>'
					name="harmonizeNewFromMetadata" />
			</fieldset>
		</form>
	</c:if>
</form>
<br />
<br />
<b class="boxHeader"><spring:message code="eptsharmonization.encountertype.harmonize.onlyOnPServer" /></b>
<form method="post" class="box">
	<fieldset>
		<table>
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${OnlyProductionEncounterTypes}">
				<tr>
					<td valign="top" align="center">${item.encounterType.id}</td>
					<td valign="top">${item.encounterType.name}</td>
					<td valign="top">${item.encounterType.description}</td>
					<td valign="top">${item.encounterType.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<c:if test="${not empty OnlyProductionEncounterTypes}">
		<form method="post">
			<fieldset>
				<input type="submit"
					value='<spring:message code="eptsharmonization.encountertype.btn.exportNewFromPDS"/>'
					name="exporNewFromProduction" />
			</fieldset>
		</form>
	</c:if>
</form>
<br />
<br />
<b class="boxHeader"><spring:message code="eptsharmonization.encountertype.harmonize.differentID.andEqualUUID" /></b>
<form method="post" class="box" action="harmonizeUpdateEncounterTypes.form">
	<fieldset>
		<table><tr>
			<td>
				<b class="boxHeader"><spring:message code="eptsharmonization.encountertype.harmonize.mdserver" /></b>
				<table>
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
					</tr>
					<c:forEach var="item" items="${mdsEncountersPartialEqual}">
						<tr>
							<td valign="top" align="center">${item.encounterType.id}</td>
							<td valign="top">${item.encounterType.name}</td>
							<td valign="top">${item.encounterType.description}</td>
						</tr>
					</c:forEach>
				</table>
			</td>
			<td>
				<b class="boxHeader"><spring:message code="eptsharmonization.encountertype.harmonize.pdserver" /></b>
				<table>
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${pdsEncountersPartialEqual}">
						<tr>
							<td valign="top" align="center">${item.encounterType.id}</td>
							<td valign="top">${item.encounterType.name}</td>
							<td valign="top">${item.encounterType.description}</td>
							<td valign="top">${item.encounterType.uuid}</td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr></table>
	</fieldset>
	<c:if test="${not empty mdsEncountersPartialEqual}">
		<form method="post">
			<fieldset>
				<input type="submit"
					value='<spring:message code="general.next"/>'
					name="harmonizeNewFromMetadata" />
			</fieldset>
		</form>
	</c:if>
</form>


<%@ include file="/WEB-INF/template/footer.jsp"%>