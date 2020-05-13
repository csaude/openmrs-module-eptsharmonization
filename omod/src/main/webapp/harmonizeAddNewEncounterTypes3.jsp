<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeAddNewEncounterTypes3.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />
<openmrs:htmlInclude
	file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<openmrs:htmlInclude
	file="/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css" />

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
<c:if test="${not empty openmrs_msg}">
	<div id="openmrs_msg">
		<span> <spring:message code="${openmrs_msg}"
				text="${openmrs_msg}" />
		</span>
	</div>
	<br />
</c:if>
<b class="boxHeader"><spring:message
		code="eptsharmonization.encountertype.harmonize.onlyOnMDServer" /></b>

<springform:form modelAttribute="harmonizationModel" method="post">
	<fieldset>
		<table id="tableOnlyMDS" cellspacing="0" border="0"
			style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
				<th><spring:message code="eptsharmonization.encountertype.harmonize.encounters" /></th>
				<th><spring:message code="eptsharmonization.encountertype.harmonize.forms" /></th>
			</tr>
	
			<c:forEach var="item" items="${harmonizationModel.items}"
				varStatus="itemsRow">
				<c:if test="${item.selected}">
					<tr class="oddAssessed">
						<td valign="top" align="center">${item.value.encounterType.id}</td>
						<td valign="top">${item.value.encounterType.name}</td>
						<td valign="top">${item.value.encounterType.description}</td>
						<td valign="top">${item.value.encounterType.uuid}</td>
						<td valign="top" align="center">${item.encountersCount}</td>
						<td valign="top" align="center">${item.formsCount}</td>
					</tr>
				</c:if>
			</c:forEach>
			<tr>
				<td colspan="6">
					<div class="submit-btn" align="center">
						<input type="submit"
							value='<spring:message code="eptsharmonization.encountertype.harmonized.exportLog"/>'
							name="exportLogs" />
					</div>
				</td>
			</tr>
		</table>
	</fieldset>
</springform:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>