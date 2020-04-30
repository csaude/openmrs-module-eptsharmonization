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
<c:if test="${not empty encounterTypesWithDifferentNames}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.differentNamesAndSameUUIDAndID" /></b>
	<form method="get" class="box"
		action="harmonizeUpdateEncounterTypeNames.form">
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
		<fieldset>
			<input type="submit" value='<spring:message code="general.next"/>'
				name="updateEncounterTypeNames" />
		</fieldset>
	</form>
	<br />
	<br />
</c:if>

<c:if test="${not empty onlyMetadataEncounterTypes}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.onlyOnMDServer" /></b>
	<form method="post" class="box"
		action="harmonizeAddNewEncounterTypes.form">
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
		<form method="post">
			<fieldset>
				<input type="submit" value='<spring:message code="general.next"/>'
					name="harmonizeNewFromMetadata" />
			</fieldset>
		</form>
	</form>
	<br />
	<br />
</c:if>

<c:if test="${not empty OnlyProductionEncounterTypes}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.onlyOnPServer" /></b>
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
		<form method="post">
			<fieldset>
				<input type="submit"
					value='<spring:message code="eptsharmonization.encountertype.btn.exportNewFromPDS"/>'
					name="exporNewFromProduction" />
			</fieldset>
		</form>
	</form>
	<br />
	<br />
</c:if>

<c:if test="${not empty mdsEncountersPartialEqual}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.differentID.andEqualUUID" /></b>
	<form method="post" class="box"
		action="harmonizeUpdateEncounterTypes.form">
		<fieldset>
			<table>
				<tr>
					<td><b class="boxHeader"><spring:message
								code="eptsharmonization.encountertype.harmonize.mdserver" /></b>
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
						</table></td>
					<td><b class="boxHeader"><spring:message
								code="eptsharmonization.encountertype.harmonize.pdserver" /></b>
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
						</table></td>
				</tr>
			</table>
		</fieldset>
		<form method="post">
			<fieldset>
				<input type="submit" value='<spring:message code="general.next"/>'
					name="harmonizeNewFromMetadata" />
			</fieldset>
		</form>
	</form>
	<br />
	<br />
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>