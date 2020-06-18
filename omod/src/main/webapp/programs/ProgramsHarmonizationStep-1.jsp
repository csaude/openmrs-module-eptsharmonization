
<c:if test="${not isFirstStepHarmonizationCompleted}">

	<form method="post" action="processHarmonizationStep1.form">
		<c:if test="${not empty newMDSPrograms.items}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.program.harmonize.onlyOnMDServer" /></b>
			<fieldset>
				<table id="tableOnlyMDS" cellspacing="0" border="0"
					style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="general.uuid" /></th>
						<th><spring:message
								code="eptsharmonization.program.harmonize.patientPrograms" /></th>
						<th><spring:message
								code="eptsharmonization.program.harmonize.programWorkflows" /></th>
					</tr>
					<c:forEach var="item" items="${newMDSPrograms.items}">
						<tr>
							<td valign="top" align="center">${item.value.program.id}</td>
							<td valign="top">${item.value.program.name}</td>
							<td valign="top">${item.value.program.description}</td>
							<td valign="top">${item.value.program.uuid}</td>
							<td style="text-align: right;">${item.encountersCount}</td>
							<td style="text-align: right;">${item.formsCount}</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
		</c:if>

		<c:if test="${not empty productionItemsToDelete}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.program.harmonize.onlyOnPServer.unused" /></b>
			<fieldset>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${productionItemsToDelete}">
						<tr>
							<td valign="top" align="center">${item.program.id}</td>
							<td valign="top">${item.program.name}</td>
							<td valign="top">${item.program.description}</td>
							<td valign="top">${item.program.uuid}</td>
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