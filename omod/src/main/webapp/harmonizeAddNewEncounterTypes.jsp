<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeAddNewEncounterTypes.form" />

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
<div id="error_msg" hidden="hidden">
	<br> <span class="error"> <spring:message
			code="eptsharmonization.selectAtLeastOneRowToProceed" /></span>
</div>
<br />
<br />
<b class="boxHeader"><spring:message
		code="eptsharmonization.encountertype.harmonize.onlyOnMDServer" /></b>

<springform:form modelAttribute="harmonizationModel" method="post"
	onsubmit="return validateBeforeNextPage();">
	<fieldset>
		<table id="tableOnlyMDS" cellspacing="0" border="0"
			style="width: 100%">
			<thead>
				<tr>
					<th />
					<th><spring:message code="general.id" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="general.description" /></th>
					<th><spring:message code="general.uuid" /></th>
				</tr>
			</thead>

			<tbody>
				<c:forEach var="item" items="${harmonizationModel.items}"
					varStatus="itemsRow">
					<tr class="oddAssessed">
						<spring:bind path="items[${itemsRow.index}].selected">
							<td align="center"><input type="hidden"
								name="_<c:out value="${status.expression}"/>"> <input
								type="checkbox" class="info-checkBox"
								name="<c:out value="${status.expression}"/>" value="true"
								<c:if test="${status.value}">checked</c:if> /></td>
						</spring:bind>
						<td valign="top" align="center">${item.value.encounterType.id}</td>
						<td valign="top">${item.value.encounterType.name}</td>
						<td valign="top">${item.value.encounterType.description}</td>
						<td valign="top">${item.value.encounterType.uuid}</td>
					</tr>
				</c:forEach>
			</tbody>
			<tr>
				<td colspan="5">
					<div class="submit-btn" align="center">
						<input type="button"
							value="<spring:message code="general.previous"/>"
							onclick="window.location = 'harmonizeEncounterTypeList.form';"
							name="previous" /> <input type="submit"
							value='<spring:message code="general.next"/>'
							name="addEncounterTypes" />
					</div>
				</td>
			</tr>
		</table>
	</fieldset>
</springform:form>
<script type="text/javascript">
	var itemTrs = document.querySelectorAll(".oddAssessed");

	function validateBeforeNextPage() {

		var itemTrs = document.querySelectorAll(".oddAssessed");
		var atLeastOneCkecked = false;
		var validTable = true;

		itemTrs.forEach(function(item) {

			var inputCheckBox = item.querySelector(".info-checkBox");

			if (inputCheckBox.checked) {
				atLeastOneCkecked = true;
			}
		});

		var divErrorMsg = document.querySelector("#error_msg");

		if (!atLeastOneCkecked) {
			divErrorMsg.hidden = "";
		} else {
			divErrorMsg.hidden = "hidden";
		}
		return atLeastOneCkecked;
	}
</script>
<%@ include file="/WEB-INF/template/footer.jsp"%>