<c:if
	test="${hasSecondStepFormHarmonization && isFirstStepFormHarmonizationCompleted && isUUIDsAndIDsFormHarmonized && isNamesFormHarmonized}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.form.harmonize.onlyOnPServer.inuse" /></b>
	<form method="post" class="box"
		action="harmonizeFormExportForms.form">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
				<th><spring:message code="eptsharmonization.encountertype.id" /></th>
				<th><spring:message
								code="eptsharmonization.encountertype.name" /></th>
				<th><spring:message
						code="eptsharmonization.form.harmonize.encounters" /></th>
			</tr>
			<c:forEach var="item" items="${productionItemsToExportForm.items}">
				<tr>
					<td valign="top" align="center">${item.value.form.id}</td>
					<td valign="top">${item.value.form.name}</td>
					<td valign="top">${item.value.form.description}</td>
					<td valign="top">${item.value.form.uuid}</td>
					<td valign="top">${item.value.form.encounterType.id}</td>
					<td valign="top">${item.value.form.encounterType.name}</td>
					<td style="text-align: right;">${item.encountersCount}</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="7">
					<div class="submit-btn" align="center">
						<input type="submit"
							value='<spring:message code="eptsharmonization.form.btn.exportNewFromPDS"/>'
							name="exportNewFromProduction" />
					</div>
				</td>
			</tr>
		</table>
	</form>
	<br />
		
	<c:if test="${not empty notSwappableFormsClone}">
		<br />

		<c:if test="${not empty mdsFormNotHarmonizedYet}">
			<fieldset>
				<legend>
					<b> <spring:message
							code="eptsharmonization.harmonizeBasedOnMDS" /></b>
				</legend>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th colspan="3" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.form.from.metadataServer" /></th>
						<th colspan="3" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.form.created.OnProductionServer" /></th>
						<th colspan="2" style="text-align: left; width: 10%;"></th>
					</tr>

					<form method="post" action="addFormFromMDSMapping.form">
						<tr>
							<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.value">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${mdsFormNotHarmonizedYet}"
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
										<c:forEach items="${swappableForms}" var="type">
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
									<c:when test="${not empty swappableForms}">
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
		<c:if test="${not empty swappableForms}">
			<fieldset>
				<legend>
					<b><spring:message code="eptsharmonization.harmonizeWithinPDS" /></b>
				</legend>

				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th colspan="3" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.form.copiedFrom.metadataServer" /></th>
						<th colspan="3" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.form.created.OnProductionServer" /></th>
						<th colspan="2" style="text-align: left; width: 10%;"></th>
					</tr>
					<form method="post" action="addFormMapping.form">
						<tr>
							<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
									path="harmonizationItem.value">
									<select name="${status.expression}">
										<option value="">Selecione</option>
										<c:forEach items="${notSwappableForms}" var="type">
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
										<c:forEach items="${swappableForms}" var="type">
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
									<c:when test="${not empty swappableForms}">
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
					<th colspan="5" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.form.from.metadataServer" /></th>
					<th colspan="5" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.form.created.OnProductionServer" /></th>
					<th colspan="2"></th>
				</tr>
				<tr>
					<th><spring:message code="general.id" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="general.description" /></th>
					<th><spring:message code="eptsharmonization.encountertype.id" /></th>
					<th><spring:message
								code="eptsharmonization.encountertype.name" /></th>
					<th><spring:message code="general.id" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="general.description" /></th>
					<th><spring:message code="eptsharmonization.encountertype.id" /></th>
					<th><spring:message
								code="eptsharmonization.encountertype.name" /></th>
					<th colspan="2"></th>
				</tr>
				<c:if test="${not empty manualHarmonizeForms}">
					<form method="post" action="removeFormMapping.form">
						<c:forEach var="item" items="${manualHarmonizeForms}"
							varStatus="itemsRow">
							<tr>
								<td valign="top">${item.value.id}</td>
								<td valign="top">${item.value.name}</td>
								<td valign="top">${item.value.description}</td>
								<td valign="top">${item.value.encounterType.id}</td>
								<c:choose>
								<c:when test="${not empty item.value.encounterType.name}">
									<td valign="top">${item.value.encounterType.name}</td>
								</c:when>
								<c:otherwise>
									<td valign="top">N/A</td>
								</c:otherwise>
							</c:choose>
								
								<td valign="top">${item.key.id}</td>
								<td valign="top">${item.key.name}</td>
								<td valign="top">${item.key.description}</td>
								<td valign="top">${item.key.encounterType.id}</td>
								<td valign="top">${item.key.encounterType.name}</td>
								<td colspan="2">
									<div class="submit-btn" align="left">
										<input type="hidden" id="${item.key.uuid}"
											name="productionServerFormUuID"
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
						<td colspan="12">
							<form method="post" action="processHarmonizationStep4.form">
								<div class="submit-btn" align="center">
									<input type="submit"
										value='<spring:message code="eptsharmonization.form.btn.harmonizeNewFromMDS"/>'
										name="processHarmonizationStep4" />
								</div>
							</form>
						</td>
					</tr>
				</c:if>
				</fieldset>
			</table>
	</c:if>
</c:if>