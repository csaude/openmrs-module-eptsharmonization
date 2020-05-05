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
<br />
<b class="boxHeader"><spring:message
		code="eptsharmonization.encountertype.harmonize.differentID.andEqualUUID" /></b>
<form method="post" class="box" action="harmonizeEncounterTypeList.form">
	<fieldset>
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<td><b class="boxHeader"><spring:message
							code="eptsharmonization.encountertype.harmonize.mdserver" /></b>
					<table cellspacing="0" border="0" style="width: 100%">
						<tr>
							<th><spring:message code="general.id" /></th>
							<th><spring:message code="general.name" /></th>
						</tr>
						<c:forEach var="item" items="${mdsEncountersPartialEqual}">
							<tr>
								<td valign="top" align="center">${item.encounterType.id}</td>
								<td valign="top">${item.encounterType.name}</td>
							</tr>
						</c:forEach>
					</table></td>
				<td><b class="boxHeader"><spring:message
							code="eptsharmonization.encountertype.harmonize.pdserver" /></b>
					<table cellspacing="0" border="0" style="width: 100%">
						<tr>
							<th><spring:message code="general.id" /></th>
							<th><spring:message code="general.name" /></th>
							<th><spring:message code="general.uuid" /></th>
							<th><spring:message
									code="eptsharmonization.encountertype.harmonize.encounters" /></th>
							<th><spring:message
									code="eptsharmonization.encountertype.harmonize.forms" /></th>
						</tr>
						<c:forEach var="item" items="${pdsEncountersPartialEqual}">
							<tr>
								<td valign="top" align="center">${item.encounterType.id}</td>
								<td valign="top">${item.encounterType.name}</td>
								<td valign="top">${item.encounterType.uuid}</td>
								<td valign="top" align="center">0</td>
								<td valign="top" align="center">0</td>
							</tr>
						</c:forEach>
					</table></td>
			</tr>
			<tr>
				<td colspan="5">
					<div class="submit-btn" align="center">
						<input type="submit"
							value="<spring:message code="general.previous"/>" name="previous" />
						<input type="submit"
							value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
							name="harmonizeNewFromMetadata" />
					</div>
				</td>
			</tr>
		</table>
	</fieldset>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>