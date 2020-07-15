<c:if
	test="${hasSecondStepHarmonization && isFirstStepHarmonizationCompleted && isUUIDsAndIDsHarmonized && isNamesHarmonized}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.program.harmonize.onlyOnPServer.inuse" /></b>
	<form method="post" class="box"
		action="harmonizeExportPrograms.form">
		<table cellspacing="0" border="0" style="width: 100%">
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
			<c:forEach var="item" items="${productionItemsToExport.items}">
				<tr>
					<td valign="top" align="center">${item.value.program.id}</td>
					<td valign="top">${item.value.program.name}</td>
					<td valign="top">${item.value.program.description}</td>
					<td valign="top">${item.value.program.uuid}</td>
					<td style="text-align: right;">${item.encountersCount}</td>
					<td style="text-align: right;">${item.formsCount}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="6">
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

	<c:if
		test="${not empty swappableProgramsClone && not empty notSwappableProgramsClone}">
		<br />
		<b class="boxHeader"><spring:message
				code="eptsharmonization.program.defineNewMappings" /></b>
		<fieldset>
			<table cellspacing="0" border="0" style="width: 100%">
				<tr>
					<th colspan="3" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.program.from.metadataServer" /></th>
					<th colspan="3" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.program.created.OnProductionServer" /></th>
					<th colspan="2" style="text-align: left; width: 10%;"></th>
				</tr>
				<form method="post" action="addProgramMapping.form">
					<tr>
						<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
								path="harmonizationItem.value">
								<select name="${status.expression}">
									<option value="">Selecione</option>
									<c:forEach items="${notSwappablePrograms}" var="type">
										<option value="${type.uuid}"
											<c:if test="${type.uuid == status.value}">selected</c:if>>${type.name}</option>
									</c:forEach>
								</select>
								<c:if test="${not empty errorRequiredMdsValue}">
									<span class="error"><spring:message
											code="${errorRequiredMdsValue}"
											text="${errorRequiredMdsValue}" /></span>
								</c:if>
							</spring:bind></td>
						<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
								path="harmonizationItem.key">
								<select name="${status.expression}">
									<option value="">Selecione</option>
									<c:forEach items="${swappablePrograms}" var="type">
										<option value="${type.uuid}"
											<c:if test="${type.uuid == status.value}">selected</c:if>>${type.name}</option>
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
								<c:when test="${not empty swappablePrograms}">
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
				<fieldset>
					<tr>
						<th colspan="3" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.program.from.metadataServer" /></th>
						<th colspan="3" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.program.created.OnProductionServer" /></th>
						<th colspan="2"></th>
					</tr>
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th colspan="2"></th>
					</tr>
					<c:if test="${not empty manualHarmonizePrograms}">
						<form method="post" action="removeProgramMapping.form">
							<c:forEach var="item" items="${manualHarmonizePrograms}"
								varStatus="itemsRow">
								<tr>
									<td valign="top">${item.value.id}</td>
									<td valign="top">${item.value.name}</td>
									<td valign="top">${item.value.description}</td>
									<td valign="top">${item.key.id}</td>
									<td valign="top">${item.key.name}</td>
									<td valign="top">${item.key.description}</td>
									<td colspan="2">
										<div class="submit-btn" align="left">
											<input type="hidden" id="${item.key.uuid}"
												name="productionServerProgramUuID"
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
							<td colspan="8">
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
		</fieldset>
		<br />
	</c:if>
</c:if>