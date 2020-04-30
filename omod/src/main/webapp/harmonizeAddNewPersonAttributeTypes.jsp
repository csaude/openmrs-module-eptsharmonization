<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeAddNewPersonAttributeTypes.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="eptsharmonization.personattributetype.harmonize" />
</h2>
<br />
<br />
<b class="boxHeader"><spring:message
		code="eptsharmonization.personattributetype.harmonize.onlyOnMDServer" /></b>
<form method="post" class="box"
	action="harmonizePersonAttributeTypesList.form">
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
	<fieldset>
		<input type="submit" value="<spring:message code="general.previous"/>"
			name="previous" /> <input type="submit"
			value='<spring:message code="general.next"/>'
			name="harmonizeNewFromMetadata" />
	</fieldset>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>