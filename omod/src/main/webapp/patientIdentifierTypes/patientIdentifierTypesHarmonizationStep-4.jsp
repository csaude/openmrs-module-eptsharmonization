<c:if
	test="${hasSecondStepHarmonization && isFirstStepHarmonizationCompleted && isUUIDsAndIDsHarmonized && isNamesHarmonized}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.patientidentifiertype.harmonize.onlyOnPServer.inuse" /></b>
	<form method="post" class="box"
		action="harmonizeExportPatientIdentifierTypes.form">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message
						code="eptsharmonization.patientidentifiertype.format" /></th>
				<th><spring:message
						code="eptsharmonization.patientidentifiertype.checkdigit" /></th>
				<th><spring:message
						code="eptsharmonization.patientidentifiertype.required" /></th>
				<th><spring:message code="general.uuid" /></th>
				<th><spring:message
						code="eptsharmonization.patientidentifiertype.harmonize.patientidentifiers" /></th>
			</tr>
			<c:forEach var="item" items="${productionItemsToExport.items}">
				<tr>
					<td valign="top" align="center">${item.value.patientIdentifierType.id}</td>
					<td valign="top">${item.value.patientIdentifierType.name}</td>
					<td valign="top">${item.value.patientIdentifierType.description}</td>
					<td valign="top">${item.value.patientIdentifierType.format}</td>
					<td valign="top">${item.value.patientIdentifierType.checkDigit}</td>
					<td valign="top">${item.value.patientIdentifierType.required}</td>
					<td valign="top">${item.value.patientIdentifierType.uuid}</td>
					<td style="text-align: right;">${item.encountersCount}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="8">
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

	<c:if test="${not empty notSwappablePatientIdentifierTypesClone}">
		<c:if test="${not empty mdsPatientIdentifierTypeNotHarmonizedYet}">
			<fieldset>
				<legend>
					<b> <spring:message
							code="eptsharmonization.harmonizeBasedOnMDS" /></b>
				</legend>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th colspan="3" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.patientidentifiertype.from.metadataServer" /></th>
						<th colspan="3" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.patientidentifiertype.created.OnProductionServer" /></th>
						<th colspan="2" style="text-align: left; width: 10%;"></th>
					</tr>

					<form method="post"
						action="addPatientIdentifierTypeFromMDSMapping.form">
						<tr>
							<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.value">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${mdsPatientIdentifierTypeNotHarmonizedYet}"
											var="type">
											<option value="${type.uuid}"
												<c:if test="${type.uuid == status.value}">selected</c:if>>${type.name}</option>
										</c:forEach>
									</select>
									<c:if test="${not empty errorRequiredMdsValueFromMDS}">
										<span class="error"><spring:message
												code="${errorRequiredMdsValueFromMDS}"
												text="${errorRequiredMdsValueFromMDS}" /></span>
									</c:if>
								</spring:bind></td>
							<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.key">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${swappablePatientIdentifierTypes}"
											var="type">
											<option value="${type.uuid}"
												<c:if test="${type.uuid == status.value}">selected</c:if>>${type.name}</option>
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
									<c:when test="${not empty swappablePatientIdentifierTypes}">
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

		<c:if test="${not empty swappablePatientIdentifierTypes}">
			<br />
			<fieldset>
			<legend>
					<b> <spring:message
							code="eptsharmonization.harmonizeWithinPDS" /></b>
				</legend>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th colspan="6" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.patientidentifiertype.copiedFrom.metadataServer" /></th>
						<th colspan="6" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.patientidentifiertype.created.OnProductionServer" /></th>
						<th colspan="2" style="text-align: left; width: 10%;"></th>
					</tr>
					<form method="post" action="addPatientIdentifierTypeMapping.form">
						<tr>
							<td colspan="6" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.value">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${notSwappablePatientIdentifierTypes}"
											var="type">
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
							<td colspan="6" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.key">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${swappablePatientIdentifierTypes}"
											var="type">
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
									<c:when test="${not empty swappablePatientIdentifierTypes}">
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
							<th colspan="6" style="text-align: center; width: 45%;"><spring:message
									code="eptsharmonization.patientidentifiertype.from.metadataServer" /></th>
							<th colspan="6" style="text-align: center; width: 45%;"><spring:message
									code="eptsharmonization.patientidentifiertype.created.OnProductionServer" /></th>
							<th colspan="2"></th>
						</tr>
						<tr>
							<th><spring:message code="general.id" /></th>
							<th><spring:message code="general.name" /></th>
							<th><spring:message code="general.description" /></th>
							<th><spring:message
									code="eptsharmonization.patientidentifiertype.format" /></th>
							<th><spring:message
									code="eptsharmonization.patientidentifiertype.checkdigit" /></th>
							<th><spring:message
									code="eptsharmonization.patientidentifiertype.required" /></th>
							<th><spring:message code="general.id" /></th>
							<th><spring:message code="general.name" /></th>
							<th><spring:message code="general.description" /></th>
							<th><spring:message
									code="eptsharmonization.patientidentifiertype.format" /></th>
							<th><spring:message
									code="eptsharmonization.patientidentifiertype.checkdigit" /></th>
							<th><spring:message
									code="eptsharmonization.patientidentifiertype.required" /></th>
							<th colspan="2"></th>
						</tr>
						<c:if test="${not empty manualHarmonizePatientIdentifierTypes}">
							<form method="post"
								action="removePatientIdentifierTypeMapping.form">
								<c:forEach var="item"
									items="${manualHarmonizePatientIdentifierTypes}"
									varStatus="itemsRow">
									<tr>
										<td valign="top">${item.value.id}</td>
										<td valign="top">${item.value.name}</td>
										<td valign="top">${item.value.description}</td>
										<td valign="top">${item.value.format}</td>
										<td valign="top">${item.value.checkDigit}</td>
										<td valign="top">${item.value.required}</td>
										<td valign="top">${item.key.id}</td>
										<td valign="top">${item.key.name}</td>
										<td valign="top">${item.key.description}</td>
										<td valign="top">${item.key.format}</td>
										<td valign="top">${item.key.checkDigit}</td>
										<td valign="top">${item.key.required}</td>
										<td colspan="2">
											<div class="submit-btn" align="left">
												<input type="hidden" id="${item.key.uuid}"
													name="productionServerPatientIdentifierTypeUuID"
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
								<td colspan="14">
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
</c:if>