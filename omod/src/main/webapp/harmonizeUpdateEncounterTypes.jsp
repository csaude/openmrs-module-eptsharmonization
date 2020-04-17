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
<form method="post" class="box" action="harmonizeEncounterTypeList.form">
	<fieldset>
		<table><tr>
			<td>
				<b class="boxHeader"><spring:message code="eptsharmonization.encountertype.harmonize.mdserver" /></b>
				<table>
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
					</tr>
					<c:forEach var="item" items="${mdsEncountersPartialEqual}">
						<tr>
							<td valign="top" align="center">${item.encounterType.id}</td>
							<td valign="top">${item.encounterType.name}</td>
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
						<th><spring:message code="general.uuid" /></th>
						<th><spring:message code="eptsharmonization.encountertype.harmonize.encounters" /></th>
						<th><spring:message code="eptsharmonization.encountertype.harmonize.forms" /></th>
					</tr>
					<c:forEach var="item" items="${pdsEncountersPartialEqual}">
						<tr>
							<td valign="top" align="center">${item.encounterType.id}</td>
							<td valign="top">${item.encounterType.name}</td>
							<td valign="top">${item.encounterType.uuid}</td>
							<td valign="top" align="center">0</td>
							<td valign="top" align="center">0</td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr></table>
	</fieldset>
	<form method="post">
		<fieldset>
			<input type="submit" value="<spring:message code="general.previous"/>" name="previous" />
			<input type="submit"
				value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
				name="harmonizeNewFromMetadata" />
		</fieldset>
	</form>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>