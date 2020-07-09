
<c:if test="${not isFirstStepHarmonizationCompleted}">

	<form method="post" action="processHarmonizationStep1.form">
		<c:if test="${not empty newMDSPatientIdentifierTypes.items}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.patientidentifiertype.harmonize.onlyOnMDServer" /></b>
			<fieldset>
				<table id="tableOnlyMDS" cellspacing="0" border="0"
					style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="eptsharmonization.format" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${newMDSPatientIdentifierTypes.items}">
						<tr>
							<td valign="top" align="center">${item.value.patientIdentifierType.id}</td>
							<td valign="top">${item.value.patientIdentifierType.name}</td>
							<td valign="top">${item.value.patientIdentifierType.description}</td>
							<td valign="top">${item.value.patientIdentifierType.format}</td>
							<td valign="top">${item.value.patientIdentifierType.uuid}</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
		</c:if>

		<c:if test="${not empty productionItemsToDelete}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.patientidentifiertype.harmonize.onlyOnPServer.unused" /></b>
			<fieldset>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="eptsharmonization.format" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${productionItemsToDelete}">
						<tr>
							<td valign="top" align="center">${item.patientIdentifierType.id}</td>
							<td valign="top">${item.patientIdentifierType.name}</td>
							<td valign="top">${item.patientIdentifierType.description}</td>
							<td valign="top">${item.patientIdentifierType.format}</td>
							<td valign="top">${item.patientIdentifierType.uuid}</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
		</c:if>

		<div class="submit-btn" align="center">
			<input type="submit"
				value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
				name="processHarmonizationStep1" />
		</div>

	</form>
</c:if>