<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeEncounterTypeList.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<style>
p {
	border: 1px solid black;
}

table {
	border: 3px solid #1aac9b;
	border-collapse: collapse;
}

th {
	background-color: #1aac9b;
	padding: 5px;
	text-align: left;
}

tr:first-child label {
	padding: 4px !important;
	color: #fff;
	font-weight: bold;
	margin: 4px !important;
	text-shadow: 0 0 .3em black;
	font-size: 12pt;
	border: 1px solid #1aac9b;
}

td {
	border: 1px solid #1aac9b;
	padding: 5px;
	text-align: left;
}

.style1 {
	font-size: 14px;
	font-weight: bold;
}

.obs {
	font-size: 14px;
	font-weight: bold;
}

.submit-btn {
	flex: 1;
	margin: 10px 15px;
}

.submit-btn input {
	color: #fff;
	background: #1aac9b;
	padding: 8px;
	width: 12.8em;
	font-weight: bold;
	text-shadow: 0 0 .3em black;
	font-size: 9pt;
	border-radius: 5px 5px;
}
</style>

<h2>
	<spring:message code="eptsharmonization.encountertype.harmonize" />
</h2>
<br />
<c:if test="${not empty harmonizedETSummary}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.summay.of.already.harmonized.mapping" /> :
		</b><br />
		<c:forEach var="msg" items="${harmonizedETSummary}">
			<span> <spring:message code="${msg}" text="${msg}" />
			</span>
			<br />
		</c:forEach>

		<form method="post" action="harmonizeEncounterTypeListExportLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					style="width: 8.6em; padding: 6px; font-size: 6pt;"
					value='<spring:message code="eptsharmonization.encountertype.harmonized.viewLog"/>'
					name="harmonizeAllEncounterTypes" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<c:if
	test="${isFirstStepHarmonizationCompleted && !hasSecondStepHarmonization}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.encounterType.harmonizationFinish" />
		</b>
	</div>
</c:if>

<c:if test="${not empty onlyMetadataEncounterTypes.items}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.onlyOnMDServer" /></b>
	<fieldset>
		<table id="tableOnlyMDS" cellspacing="0" border="0"
			style="width: 100%">
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
			<c:forEach var="item" items="${onlyMetadataEncounterTypes.items}">
				<tr>
					<td valign="top" align="center">${item.value.encounterType.id}</td>
					<td valign="top">${item.value.encounterType.name}</td>
					<td valign="top">${item.value.encounterType.description}</td>
					<td valign="top">${item.value.encounterType.uuid}</td>
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
			code="eptsharmonization.encountertype.harmonize.onlyOnPServer.unused" /></b>
	<form class="box">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${productionItemsToDelete}">
				<tr>
					<td valign="top" align="center">${item.encounterType.id}</td>
					<td valign="top">${item.encounterType.name}</td>
					<td valign="top">${item.encounterType.description}</td>
					<td valign="top">${item.encounterType.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	</form>
	<br />
</c:if>

<c:if test="${not empty encounterTypesPartialEqual.items}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.differentID.andEqualUUID" /></b>
	<fieldset>
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th style="text-align: left; width: 5%;"><spring:message
						code="eptsharmonization.encountertype.mdserver.id" /></th>
				<th style="text-align: left; width: 15%;"><spring:message
						code="eptsharmonization.encountertype.mdserver.name" /></th>
				<th style="text-align: left; width: 15%;"><spring:message
						code="eptsharmonization.encountertype.mdserver.description" /></th>
				<th style="text-align: left; width: 5%;"><spring:message
						code="eptsharmonization.encountertype.pdserver.id" /></th>
				<th style="text-align: left; width: 15%;"><spring:message
						code="eptsharmonization.encountertype.pdserver.name" /></th>
				<th style="text-align: left; width: 15%;"><spring:message
						code="eptsharmonization.encountertype.pdserver.description" /></th>
				<th style="text-align: left; width: 20%;"><spring:message
						code="general.uuid" /></th>
				<th style="text-align: left; width: 5%;"><spring:message
						code="eptsharmonization.encountertype.harmonize.encounters" /></th>
				<th style="text-align: left; width: 5%;"><spring:message
						code="eptsharmonization.encountertype.harmonize.forms" /></th>
			</tr>
			<c:forEach var="item" items="${encounterTypesPartialEqual.items}">
				<tr>
					<td valign="top" style="text-align: left; width: 5%;">${item.value[0].encounterType.id}</td>
					<td valign="top" style="text-align: left; width: 15%;">${item.value[0].encounterType.name}</td>
					<td valign="top" style="text-align: left; width: 15%;">${item.value[0].encounterType.description}</td>
					<td valign="top" style="text-align: left; width: 5%;">${item.value[1].encounterType.id}</td>
					<td valign="top" style="text-align: left; width: 15%;">${item.value[1].encounterType.name}</td>
					<td valign="top" style="text-align: left; width: 15%;">${item.value[1].encounterType.description}</td>
					<td valign="top" style="text-align: left; width: 20%;">${item.key}</td>
					<td style="text-align: right; width: 5%;">${item.encountersCount}</td>
					<td style="text-align: right; width: 5%;">${item.formsCount}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<br />
</c:if>

<c:if test="${not empty encounterTypesWithDifferentNames}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.differentNamesAndSameUUIDAndID" /></b>
	<fieldset>
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.description" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.description" /></th>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="entry" items="${encounterTypesWithDifferentNames}">
				<tr>
					<td valign="top">${entry.value[0].encounterType.name}</td>
					<td valign="top">${entry.value[0].encounterType.description}</td>
					<td valign="top">${entry.value[1].encounterType.name}</td>
					<td valign="top">${entry.value[1].encounterType.description}</td>
					<td valign="top">${entry.value[0].encounterType.id}</td>
					<td valign="top">${entry.key}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<br />
</c:if>

<c:if test="${!isFirstStepHarmonizationCompleted}">
	<br />
	<form method="post" class="box"
		action="harmonizeEncounterTypeList.form">
		<div class="submit-btn" align="center">
			<input type="submit"
				value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
				name="harmonizeAllEncounterTypes" />
		</div>
	</form>
	<br />
</c:if>

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
				<form class="box">
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
											<spring:bind path="harmonizationItem.key">
												<input type="hidden" name="mdsID" value="${item.key.uuid}">
											</spring:bind>
											<input type="submit"
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

				</form>
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

<%@ include file="/WEB-INF/template/footer.jsp"%>