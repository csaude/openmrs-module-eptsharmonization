<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Location Attribute Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeLocationAttributeTypes.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>
<script>
	$j(document)
			.ready(
					function() {
						$j('input[name=remove-mapping-button]')
								.click(
										function(event) {
											var that = this;
											var hiddenUuidValue = $j(
													"<input />")
													.attr("type", "hidden")
													.attr("name",
															"productionServerLocationAttributeTypeUuID")
													.attr("value", that.id);
											$j('#remove-mapping-form').append(
													hiddenUuidValue);
											return true;
										});
					});
</script>
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
}

tr:first-child label {
	padding: 4px !important;
	color: #fff;
	font-weight: bold;
	margin: 4px !important;
	text-shadow: 0 0 .3em black;
	font-size: 12pt;
}

td {
	border: 1px solid #1aac9b;
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
	<spring:message
		code="eptsharmonization.locationattributetype.harmonize" />
</h2>
<br />
<br />
<c:if test="${not empty harmonizedVTSummary}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.summay.of.already.harmonized.mapping" /> :
		</b><br />
		<c:forEach var="msg" items="${harmonizedVTSummary}">
			<span> <spring:message code="${msg}" text="${msg}" />
			</span>
			<br />
		</c:forEach>
		<form method="post"
			action="exportLocationAttributeTypesHarmonizationLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					style="width: 14.10em; padding: 1px; font-size: 8pt;"
					value='<spring:message code="eptsharmonization.encountertype.harmonized.viewLog"/>'
					name="harmonizeAllLocationAttributeTypes" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<c:if test="${not empty productionLocationAttributeTypesToExport}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.locationattributetype.harmonize.onlyOnPServer.inuse" /></b>
	<form method="post" class="box"
		action="exportLocationAttributeTypes.form">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
				<th><spring:message
						code="eptsharmonization.locationattributetype.harmonize.affectedLocationAttributes" /></th>
				<c:forEach var="entry"
					items="${productionLocationAttributeTypesToExport}">
					<tr>
						<td valign="top" align="center">${entry.key.id}</td>
						<td valign="top">${entry.key.locationAttributeType.name}</td>
						<td valign="top">${entry.key.locationAttributeType.description}</td>
						<td valign="top">${entry.key.uuid}</td>
						<td style="text-align: right;">${entry.value}</td>
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

<c:if test="${not empty availableMDSMappingTypes}">
	<fieldset>
		<legend>
			<b><spring:message code="eptsharmonization.harmonizeBasedOnMDS" /></b>
		</legend>
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th colspan="3" style="text-align: center; width: 45%;"><spring:message
						code="eptsharmonization.locationattributetype.from.metadataServer" /></th>
				<th colspan="3" style="text-align: center; width: 45%;"><spring:message
						code="eptsharmonization.locationattributetype.created.OnProductionServer" /></th>
				<th colspan="2" style="text-align: left; width: 10%;"></th>
			</tr>
			<form method="post"
				action="addLocationAttributeTypeFromMDSMapping.form">
				<tr>
					<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
							path="locationAttributeTypeBean.value">
							<select name="${status.expression}">
								<option value=""><spring:message
										code="eptsharmonization.locationattributetype.select" /></option>
								<c:forEach items="${availableMDSMappingTypes}" var="type">
									<option value="${type.uuid}">${type.locationAttributeType.name}</option>
								</c:forEach>
							</select>
							<c:if test="${not empty errorRequiredMdsValue}">
								<span class="error"><spring:message
										code="${errorRequiredMdsValue}"
										text="${errorRequiredMdsValue}" /></span>
							</c:if>
						</spring:bind></td>
					<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
							path="locationAttributeTypeBean.key">
							<select name="${status.expression}">
								<option value=""><spring:message
										code="eptsharmonization.locationattributetype.select" /></option>
								<c:forEach items="${mappablePDSLocationAttributeTypes}" var="type">
									<option value="${type.uuid}">${type.locationAttributeType.name}</option>
								</c:forEach>
							</select>
							<c:if test="${not empty errorRequiredPDSValue}">
								<span class="error"> <spring:message
										code="${errorRequiredPDSValue}"
										text="${errorRequiredPDSValue}" />
								</span>
							</c:if>
						</spring:bind></td>
					<td colspan="2" style="text-align: left; width: 10%;">
						<div class="submit-btn" align="left">
							<input type="submit"
								style="width: 8.6em; padding: 6px; font-size: 6pt; background-color: #4CAF50;"
								value='<spring:message code="general.add"/>'
								name="addNewMapping" />
						</div>
					</td>
				</tr>
			</form>
		</table>
	</fieldset>
</c:if>
<c:if
	test="${not empty manualLocationAttributeTypeMappings || not empty mappablePDSLocationAttributeTypes}">
	<fieldset>
		<legend>
			<b><spring:message code="eptsharmonization.harmonizeWithinPDS" /></b>
		</legend>
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th colspan="3" style="text-align: center; width: 45%;"><spring:message
						code="eptsharmonization.locationattributetype.copiedFrom.metadataServer" /></th>
				<th colspan="3" style="text-align: center; width: 45%;"><spring:message
						code="eptsharmonization.locationattributetype.created.OnProductionServer" /></th>
				<th colspan="2" style="text-align: left; width: 10%;"></th>
			</tr>
			<form method="post" action="addLocationAttributeTypeMapping.form">
				<tr>
					<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
							path="locationAttributeTypeBean.value">
							<select name="${status.expression}">
								<option value=""><spring:message
										code="eptsharmonization.locationattributetype.select" /></option>
								<c:forEach items="${availableMappingTypes}" var="type">
									<option value="${type.uuid}">${type.locationAttributeType.name}</option>
								</c:forEach>
							</select>
							<c:if test="${not empty errorRequiredMdsValue}">
								<span class="error"><spring:message
										code="${errorRequiredMdsValue}"
										text="${errorRequiredMdsValue}" /></span>
							</c:if>
						</spring:bind></td>
					<td colspan="3" style="text-align: center; width: 45%;"><spring:bind
							path="locationAttributeTypeBean.key">
							<select name="${status.expression}">
								<option value=""><spring:message
										code="eptsharmonization.locationattributetype.select" /></option>
								<c:forEach items="${mappablePDSLocationAttributeTypes}" var="type">
									<option value="${type.uuid}">${type.locationAttributeType.name}</option>
								</c:forEach>
							</select>
							<c:if test="${not empty errorRequiredPDSValue}">
								<span class="error"> <spring:message
										code="${errorRequiredPDSValue}"
										text="${errorRequiredPDSValue}" />
								</span>
							</c:if>
						</spring:bind></td>
					<td colspan="2" style="text-align: left; width: 10%;">
						<div class="submit-btn" align="left">
							<input type="submit"
								style="width: 8.6em; padding: 6px; font-size: 6pt; background-color: #4CAF50;"
								value='<spring:message code="general.add"/>'
								name="addNewMapping" />
						</div>
					</td>
				</tr>
			</form>
		</table>
	</fieldset>
	<fieldset id="added-mappings-rows">
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th colspan="3" style="text-align: center; width: 45%;"><spring:message
						code="eptsharmonization.locationattributetype.from.metadataServer" /></th>
				<th colspan="3" style="text-align: center; width: 45%;"><spring:message
						code="eptsharmonization.locationattributetype.created.OnProductionServer" /></th>
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
			<c:if test="${not empty manualLocationAttributeTypeMappings}">
				<form id="remove-mapping-form" method="post"
					action="removeLocationAttributeTypeMapping.form">
					<c:forEach var="item"
						items="${manualLocationAttributeTypeMappings}"
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
									<input type="submit" id="${item.key.uuid}"
										style="width: 8.6em; padding: 6px; font-size: 6pt; background-color: #FF5733;"
										value='<spring:message code="general.remove"/>'
										name="remove-mapping-button" />
								</div>

							</td>
						</tr>
					</c:forEach>
				</form>
				<tr>
					<td colspan="8">
						<form method="post"
							action="manualMappingLocationAttributeTypeHarmonization.form">
							<div class="submit-btn" align="center">
								<input type="submit"
									value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
									name="processManualMaps" />
							</div>
						</form>
					</td>
				</tr>
			</c:if>
		</table>
	</fieldset>
	<br />
</c:if>

<c:if test="${harmonizationCompleted}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.locationattributetype.harmonizationFinish" />
		</b>
	</div>
</c:if>
<%@ include file="/WEB-INF/template/footer.jsp"%>