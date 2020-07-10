
<c:if test="${not isFirstStepHarmonizationCompleted}">

	<form method="post" action="processHarmonizationStep1.form">
		<c:if test="${not empty newMDSProgramWorkflowStates.items}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.programworkflowstate.harmonize.onlyOnMDServer" /></b>
			<fieldset>
				<table id="tableOnlyMDS" cellspacing="0" border="0"
					style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.concept" /></th>
						<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.flowConcept" /></th>
						<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.program" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${newMDSProgramWorkflowStates.items}">
						<tr>
							<td valign="top" align="center">${item.value.programWorkflowState.id}</td>
							<td valign="top">${item.value.concept}</td>
							<td valign="top">${item.value.flowConcept}</td>
							<td valign="top">${item.value.flowProgram}</td>
							<td valign="top">${item.value.programWorkflowState.uuid}</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
		</c:if>

		<c:if test="${not empty productionItemsToDelete}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.programworkflowstate.harmonize.onlyOnPServer.unused" /></b>
			<fieldset>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.concept" /></th>
						<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.flowConcept" /></th>
						<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.program" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${productionItemsToDelete}">
						<tr>
							<td valign="top" align="center">${item.programWorkflowState.id}</td>
							<td valign="top">${item.concept}</td>
							<td valign="top">${item.flowConcept}</td>
							<td valign="top">${item.flowProgram}</td>
							<td valign="top">${item.programWorkflowState.uuid}</td>
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