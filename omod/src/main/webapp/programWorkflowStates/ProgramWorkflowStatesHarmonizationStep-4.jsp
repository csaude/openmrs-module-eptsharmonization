<c:if
	test="${hasSecondStepHarmonization && isFirstStepHarmonizationCompleted && isUUIDsAndIDsHarmonized && isNamesHarmonized}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.programworkflowstate.harmonize.onlyOnPServer.inuse" /></b>
	<form method="post" class="box"
		action="harmonizeExportProgramWorkflowStates.form">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.concept" /></th>
				<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.flowConcept" /></th>
				<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.program" /></th>
				<th><spring:message code="general.uuid" /></th>
				<th><spring:message
						code="eptsharmonization.programworkflow.harmonize.conceptStateConversions" /></th>
				<th><spring:message
						code="eptsharmonization.programworkflowstate.harmonize.patientStates" /></th>
			</tr>
			<c:forEach var="item" items="${productionItemsToExport.items}">
				<tr>
					<td valign="top" align="center">${item.value.programWorkflowState.id}</td>
					<td valign="top">${item.value.concept}</td>
					<td valign="top">${item.value.flowConcept}</td>
					<td valign="top">${item.value.flowProgram}</td>
					<td valign="top">${item.value.programWorkflowState.uuid}</td>
					<td style="text-align: right;">${item.encountersCount}</td>
					<td style="text-align: right;">${item.formsCount}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="7">
					<div class="submit-btn" align="center">
						<input type="submit"
							value='<spring:message code="eptsharmonization.encountertype.btn.exportNewFromPDS"/>'
							name="exportNewFromProduction" />
					</div>
				</td>
			</tr>
		</table>
	</form>
	<br />

	<c:if test="${not empty notSwappableProgramWorkflowStatesClone}">
		<br />

		<c:if test="${not empty mdsProgramWorkflowStateNotHarmonizedYet}">
			<fieldset>
				<legend>
					<b> <spring:message
							code="eptsharmonization.harmonizeBasedOnMDS" /></b>
				</legend>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th colspan="4" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.programworkflowstate.from.metadataServer" /></th>
						<th colspan="4" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.programworkflowstate.created.OnProductionServer" /></th>
						<th colspan="2" style="text-align: left; width: 10%;"></th>
					</tr>

					<form method="post" action="addProgramWorkflowStateFromMDSMapping.form">
						<tr>
							<td colspan="4" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.value">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${mdsProgramWorkflowStateNotHarmonizedYet}"
											var="type">
											<option value="${type.uuid}"
												<c:if test="${type.uuid == status.value}">selected</c:if>>${type.concept}/${type.flowConcept}/${type.flowProgram}</option>
										</c:forEach>
									</select>
									<c:if test="${not empty errorRequiredMdsValueFromMDS}">
										<span class="error"><spring:message
												code="${errorRequiredMdsValueFromMDS}"
												text="${errorRequiredMdsValueFromMDS}" /></span>
									</c:if>
								</spring:bind></td>
							<td colspan="4" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.key">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${swappableProgramWorkflowStates}" var="type">
											<option value="${type.uuid}"
												<c:if test="${type.uuid == status.value}">selected</c:if>>${type.concept}/${type.flowConcept}/${type.flowProgram}</option>
										</c:forEach>
									</select>
									<c:if test="${not empty errorRequiredPDSValueFromMDS}">
										<span class="error"> <spring:message
												code="${errorRequiredPDSValueFromMDS}"
												text="${errorRequiredPDSValueFromMDS}" />
										</span>
									</c:if>
								</spring:bind></td>
							<td colspan="2" style="text-align: left; width: 10%;"><c:choose>
									<c:when test="${not empty swappableProgramWorkflowStates}">
										<div class="submit-btn" align="left">
											<input type="submit"
												style="width: 8.6em; padding: 6px; font-size: 6pt; background-color: #4CAF50;"
												value='<spring:message code="general.add"/>'
												name="addNewMDSMapping" />
										</div>
									</c:when>
									<c:otherwise>
									</c:otherwise>
								</c:choose></td>
						</tr>
					</form>
				</table>
			</fieldset>
			<br>
		</c:if>
		<c:if test="${not empty swappableProgramWorkflowStates}">
			<fieldset>
				<legend>
					<b><spring:message code="eptsharmonization.harmonizeWithinPDS" /></b>
				</legend>

				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th colspan="4" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.programworkflowstate.copiedFrom.metadataServer" /></th>
						<th colspan="4" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.programworkflowstate.created.OnProductionServer" /></th>
						<th colspan="2" style="text-align: left; width: 10%;"></th>
					</tr>
					<form method="post" action="addProgramWorkflowStateMapping.form">
						<tr>
							<td colspan="4" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.value">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${notSwappableProgramWorkflowStates}" var="type">
											<option value="${type.uuid}"
												<c:if test="${type.uuid == status.value}">selected</c:if>>${type.concept}/${type.flowConcept}/${type.flowProgram}</option>
										</c:forEach>
									</select>
									<c:if test="${not empty errorRequiredMdsValue}">
										<span class="error"><spring:message
												code="${errorRequiredMdsValue}"
												text="${errorRequiredMdsValue}" /></span>
									</c:if>
								</spring:bind></td>
							<td colspan="4" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.key">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${swappableProgramWorkflowStates}" var="type">
											<option value="${type.uuid}"
												<c:if test="${type.uuid == status.value}">selected</c:if>>${type.concept}/${type.flowConcept}/${type.flowProgram}</option>
										</c:forEach>
									</select>
									<c:if test="${not empty errorRequiredPDSValue}">
										<span class="error"> <spring:message
												code="${errorRequiredPDSValue}"
												text="${errorRequiredPDSValue}" />
										</span>
									</c:if>
								</spring:bind></td>
							<td colspan="2" style="text-align: left; width: 10%;"><c:choose>
									<c:when test="${not empty swappableProgramWorkflowStates}">
										<div class="submit-btn" align="left">
											<input type="submit"
												style="width: 8.6em; padding: 6px; font-size: 6pt; background-color: #4CAF50;"
												value='<spring:message code="general.add"/>'
												name="addNewMapping" />
										</div>
									</c:when>
									<c:otherwise>
									</c:otherwise>
								</c:choose></td>
						</tr>
					</form>
				</table>
			</fieldset>
			<br>
		</c:if>
		<fieldset>
			<table cellspacing="0" border="0" style="width: 100%">
				<tr>
					<th colspan="4" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.programworkflowstate.from.metadataServer" /></th>
					<th colspan="4" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.programworkflowstate.created.OnProductionServer" /></th>
					<th colspan="2"></th>
				</tr>
				<tr>
					<th><spring:message code="general.id" /></th>
				<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.concept" /></th>
				<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.flowConcept" /></th>
				<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.program" /></th>
					<th><spring:message code="general.id" /></th>
				<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.concept" /></th>
				<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.flowConcept" /></th>
				<th><spring:message code="eptsharmonization.programworkflowstate.harmonize.program" /></th>
					<th colspan="2"></th>
				</tr>
				<c:if test="${not empty manualHarmonizeProgramWorkflowStates}">
					<form method="post" action="removeProgramWorkflowStateMapping.form">
						<c:forEach var="item" items="${manualHarmonizeProgramWorkflowStates}"
							varStatus="itemsRow">
							<tr>
								<td valign="top">${item.value.id}</td>
								<td valign="top">${item.value.concept}</td>
								<td valign="top">${item.value.flowConcept}</td>
								<td valign="top">${item.value.flowProgram}</td>
								<td valign="top">${item.key.id}</td>
								<td valign="top">${item.key.concept}</td>
								<td valign="top">${item.key.flowConcept}</td>
								<td valign="top">${item.key.flowProgram}</td>
								<td colspan="2">
									<div class="submit-btn" align="left">
										<input type="hidden" id="${item.key.uuid}"
											name="productionServerProgramWorkflowStateUuID"
											value="${item.key.uuid}" /> <input type="submit"
											id="${item.key.uuid}"
											style="width: 8.6em; padding: 6px; font-size: 6pt; background-color: #FF5733;"
											value='<spring:message code="general.remove"/>'
											name="removeMapping" />
									</div>

								</td>
							</tr>
						</c:forEach>
					</form>
					<tr>
						<td colspan="10">
							<form method="post" action="processHarmonizationStep4.form">
								<div class="submit-btn" align="center">
									<input type="submit"
										value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
										name="processHarmonizationStep3" />
								</div>
							</form>
						</td>
					</tr>
				</c:if>
				</fieldset>
			</table>
			<br />
	</c:if>
</c:if>