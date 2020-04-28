<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizePersonAttributeTypesList.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="eptsharmonization.personattributetype.harmonize" />
</h2>
<br />
<br />
<b class="boxHeader"><spring:message code="eptsharmonization.personattributetype.harmonize.onlyOnMDServer" /></b>
<form method="post" class="box" action="harmonizeAddNewEncounterTypes.form">
	<fieldset>
		<table>
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${onlyMetadataPersonAttributeTypes}">
				<tr>
					<td valign="top" align="center">${item.personAttributeType.id}</td>
					<td valign="top">${item.personAttributeType.name}</td>
					<td valign="top">${item.personAttributeType.description}</td>
					<td valign="top">${item.personAttributeType.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<c:if test="${not empty onlyMetadataPersonAttributeTypes}">
		<form method="post">
			<fieldset>
				<input type="submit"
					value='<spring:message code="general.next"/>'
					name="harmonizeNewFromMetadata" />
			</fieldset>
		</form>
	</c:if>
</form>
<c:if test="${not empty OnlyProductionPersonAttributeTypes}">
<br />
<br />
<b class="boxHeader"><spring:message code="eptsharmonization.personattributetype.harmonize.onlyOnPServer" /></b>
	<form method="post" class="box">
		<fieldset>
			<table>
				<tr>
					<th><spring:message code="general.id" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="general.description" /></th>
					<th><spring:message code="general.uuid" /></th>
				</tr>
				<c:forEach var="item" items="${OnlyProductionPersonAttributeTypes}">
					<tr>
						<td valign="top" align="center">${item.personAttributeType.id}</td>
						<td valign="top">${item.personAttributeType.name}</td>
						<td valign="top">${item.personAttributeType.description}</td>
						<td valign="top">${item.personAttributeType.uuid}</td>
					</tr>
				</c:forEach>
			</table>
		</fieldset>
			<form method="post">
				<fieldset>
					<input type="submit"
						value='<spring:message code="eptshaeptsharmonization.personattributetypexportNewFromPDS"/>'
						name="exporNewFromProduction" />
				</fieldset>
			</form>
	</form>
</c:if>
<br />
<br />
<b class="boxHeader"><spring:message code="eptsharmonization.personattributetype.harmonize.differentID.andEqualUUID" /></b>
<form method="post" class="box" action="harmonizeUpdateEncounterTypes.form">
	<fieldset>
		<table><tr>
			<td>
				<b class="boxHeader"><spring:message code="eptsharmonization.personattributetype.harmonize.mdserver" /></b>
				<table>
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
					</tr>
					<c:forEach var="item" items="${mdsPersonAttributeTypesPartialEqual}">
						<tr>
							<td valign="top" align="center">${item.personAttributeType.id}</td>
							<td valign="top">${item.personAttributeType.name}</td>
							<td valign="top">${item.personAttributeType.description}</td>
						</tr>
					</c:forEach>
				</table>
			</td>
			<td>
				<b class="boxHeader"><spring:message code="eptsharmonization.personattributetype.harmonize.pdserver" /></b>
				<table>
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${pdsPersonAttributeTypesPartialEqual}">
						<tr>
							<td valign="top" align="center">${item.personAttributeType.id}</td>
							<td valign="top">${item.personAttributeType.name}</td>
							<td valign="top">${item.personAttributeType.description}</td>
							<td valign="top">${item.personAttributeType.uuid}</td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr></table>
	</fieldset>
	<c:if test="${not empty mdsPersonAttributeTypesPartialEqual}">
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