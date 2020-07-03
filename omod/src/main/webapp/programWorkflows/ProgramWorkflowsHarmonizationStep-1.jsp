
<c:if test="${not isFirstStepHarmonizationCompleted}">

	<form method="post" action="processHarmonizationStep1.form">
		<c:if test="${not empty newMDSProgramWorkflows.items}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.programworkflow.harmonize.onlyOnMDServer" /></b>
			<fieldset>
				<table id="tableOnlyMDS" cellspacing="0" border="0"
					style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="eptsharmonization.programworkflow.harmonize.program" /></th>
						<th><spring:message code="eptsharmonization.programworkflow.harmonize.concept" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${newMDSProgramWorkflows.items}">
						<tr>
							<td valign="top" align="center">${item.value.programWorkflow.id}</td>
							<td valign="top">${item.value.program}</td>
							<td valign="top">${item.value.concept}</td>
							<td valign="top">${item.value.programWorkflow.uuid}</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
		</c:if>

		<c:if test="${not empty productionItemsToDelete}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.programworkflow.harmonize.onlyOnPServer.unused" /></b>
			<fieldset>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="eptsharmonization.programworkflow.harmonize.program" /></th>
						<th><spring:message code="eptsharmonization.programworkflow.harmonize.concept" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${productionItemsToDelete}">
						<tr>
							<td valign="top" align="center">${item.programWorkflow.id}</td>
							<td valign="top">${item.program}</td>
							<td valign="top">${item.concept}</td>
							<td valign="top">${item.programWorkflow.uuid}</td>
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