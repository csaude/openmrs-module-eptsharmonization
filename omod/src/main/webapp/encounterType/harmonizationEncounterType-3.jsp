<c:if
	test="${hasSecondStepHarmonization && isFirstStepHarmonizationCompleted}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.onlyOnPServer.inuse" /></b>
	<form method="get" class="box"
		action="harmonizeExportEncounterTypes.form">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.harmonize.encounters" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.harmonize.forms" /></th>
			</tr>
			<c:forEach var="item" items="${productionItemsToExport.items}">
				<tr>
					<td valign="top" align="center">${item.value.encounterType.id}</td>
					<td valign="top">${item.value.encounterType.name}</td>
					<td valign="top">${item.value.encounterType.description}</td>
					<td valign="top">${item.value.encounterType.uuid}</td>
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
</c:if>

<c:if
	test="${hasSecondStepHarmonization && isFirstStepHarmonizationCompleted}">

	<c:if
		test="${not empty swappableEncounterTypesClone && not empty notSwappableEncounterTypesClone}">
		<br />
		<b class="boxHeader"><spring:message
				code="eptsharmonization.encounterType.defineNewMappings" /></b>
		<fieldset>
			<table cellspacing="0" border="0" style="width: 100%">
				<tr>
					<th colspan="3" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.encounterType.from.metadataServer" /></th>
					<th colspan="3" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.encounterType.created.OnProductionServer" /></th>
					<th colspan="2" style="text-align: left; width: 10%;"></th>
				</tr>
				<form method="post" action="addEncounterTypeMapping.form">
					<tr>
						<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
								path="harmonizationItem.key">
								<select name="${status.expression}">
									<option value="">Selecione</option>
									<c:forEach items="${notSwappableEncounterTypes}" var="type">
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
								path="harmonizationItem.value">
								<select name="${status.expression}">
									<option value="">Selecione</option>
									<c:forEach items="${swappableEncounterTypes}" var="type">
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
								<c:when test="${not empty swappableEncounterTypes}">
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
								code="eptsharmonization.encounterType.from.metadataServer" /></th>
						<th colspan="3" style="text-align: center; width: 45%;"><spring:message
								code="eptsharmonization.encounterType.created.OnProductionServer" /></th>
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
					<c:if test="${not empty manualHarmonizeEtypes}">
						<form method="post" action="removeEncounterTypeMapping.form">
							<c:forEach var="item" items="${manualHarmonizeEtypes}"
								varStatus="itemsRow">
								<tr>
									<td valign="top">${item.key.id}</td>
									<td valign="top">${item.key.name}</td>
									<td valign="top">${item.key.description}</td>
									<td valign="top">${item.value.id}</td>
									<td valign="top">${item.value.name}</td>
									<td valign="top">${item.value.description}</td>
									<td colspan="2">
										<div class="submit-btn" align="left">
											<input type="hidden" id="${item.key.uuid}"
												name="metadataServerEncounterTypeUuID"
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
								<form method="post"
									action="processHarmonizeManualMappingEncounterType.form">
									<div class="submit-btn" align="center">
										<input type="submit"
											value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
											name="harmonizeMapping" />
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

	<c:if
		test="${not empty swappableEncounterTypesClone && not empty notSwappableEncounterTypesClone}">
		<br />
		<div class="box">
			<table cellspacing="0" border="0" style="width: 100%">
				<tr>
					<th colspan="4" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.encounterType.from.metadataServer" /></th>
					<th colspan="4" style="text-align: center; width: 45%;"><spring:message
							code="eptsharmonization.encounterType.created.OnProductionServer" /></th>
				</tr>
				<tr>
					<th><spring:message code="general.id" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="general.description" /></th>
					<th><spring:message code="general.uuid" /></th>
					<th><spring:message code="general.id" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="general.description" /></th>
					<th><spring:message code="general.uuid" /></th>
				</tr>
				<c:forEach var="item" items="${notSwappableEncounterTypesClone}"
					varStatus="itemStatus">
					<tr>
						<td valign="top" align="center">${item.id}</td>
						<td valign="top">${item.name}</td>
						<td valign="top">${item.description}</td>
						<td valign="top">${item.uuid}</td>
						<c:choose>
							<c:when
								test="${not empty swappableEncounterTypesClone[itemStatus.index]}">
								<td>${swappableEncounterTypesClone[itemStatus.index].id}</td>
								<td>${swappableEncounterTypesClone[itemStatus.index].name}</td>
								<td>${swappableEncounterTypesClone[itemStatus.index].description}</td>
								<td>${swappableEncounterTypesClone[itemStatus.index].uuid}</td>
							</c:when>
							<c:otherwise>
								<td colspan="4"></td>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
			</table>
		</div>
		<br />
	</c:if>
</c:if>