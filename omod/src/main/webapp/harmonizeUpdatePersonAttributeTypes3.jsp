<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Person Attribute Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeUpdatePersonAttributeTypes3.form" />

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
	<spring:message code="eptsharmonization.personattributetype.harmonize" />
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
		code="eptsharmonization.personattributetype.harmonize.differentID.andEqualUUID" /></b>
<springform:form modelAttribute="harmonizationModel" method="post"
	onsubmit="return validateBeforeNextPage();">
	<fieldset>
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.id" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.description" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.id" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${harmonizationModel.items}"
				varStatus="itemsRow">
				<tr class="oddAssessed">
					<c:if test="${item.selected}">
						<td valign="top" align="center">${item.value[0].personAttributeType.id}</td>
						<td valign="top">${item.value[0].personAttributeType.name}</td>
						<td valign="top">${item.value[0].personAttributeType.description}</td>
						<td valign="top" align="center">${item.value[1].personAttributeType.id}
						<td valign="top">${item.value[1].personAttributeType.name}</td>
						<td valign="top">${item.value[1].personAttributeType.description}</td>
						<td valign="top">${item.key}</td>
					</c:if>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="7">
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