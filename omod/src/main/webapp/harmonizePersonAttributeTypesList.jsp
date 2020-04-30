<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizePersonAttributeTypesList.form" />

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

tr:first-child {
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
<br />

<c:if test="${not empty onlyMetadataPersonAttributeTypes}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.personattributetype.harmonize.onlyOnMDServer" /></b>
	<form method="post" class="box"
		action="harmonizeAddNewPersonAttributeTypes.form">
		<fieldset>
			<table cellspacing="0" border="0" style="width: 100%">
				<tr>
					<th><spring:message code="general.id" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="general.description" /></th>
					<th><spring:message code="general.uuid" /></th>
				</tr>
				<c:forEach var="item" items="${onlyMetadataPersonAttributeTypes}">
					<tr>
						<td valign="top" align="center">${item.personAttributeType.id}</td>
						<td valign="top">${item.personAttributeType.name}</td>
						<td valign="top">${item.personAttributeType.description}</td>
						<td valign="top">${item.personAttributeType.uuid}</td>
					</tr>
				</c:forEach>
				<tr>
					<td colspan="4">
						<div class="submit-btn" align="center">
							<input type="submit"
								value='<spring:message code="general.next"/>'
								name="harmonizeNewFromMetadata" />
						</div>
					</td>
				</tr>
			</table>
		</fieldset>
	</form>
	<br />
	<br />
</c:if>

<c:if test="${not empty OnlyProductionPersonAttributeTypes}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.personattributetype.harmonize.onlyOnPServer" /></b>
	<form method="post" class="box">
		<fieldset>
			<table cellspacing="0" border="0" style="width: 100%">
				<tr>
					<th><spring:message code="general.id" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="general.description" /></th>
					<th><spring:message code="general.uuid" /></th>
				</tr>
				<c:forEach var="item" items="${OnlyProductionPersonAttributeTypes}">
					<tr>
						<td valign="top" align="center">${item.personAttributeType.id}</td>
						<td valign="top">${item.personAttributeType.name}</td>
						<td valign="top">${item.personAttributeType.description}</td>
						<td valign="top">${item.personAttributeType.uuid}</td>
					</tr>
				</c:forEach>
				<tr>
					<td colspan="4">
						<div class="submit-btn" align="center">
							<input type="submit"
								value='<spring:message code="eptshaeptsharmonization.personattributetypexportNewFromPDS"/>'
								name="exporNewFromProduction" />
						</div>
					</td>
				</tr>
			</table>
		</fieldset>
	</form>
	<br />
	<br />
</c:if>

<c:if test="${not empty mdsPersonAttributeTypesPartialEqual}">
	<b class="boxHeader"><spring:message
			code="eptsharmonization.personattributetype.harmonize.differentID.andEqualUUID" /></b>
	<form method="post" class="box"
		action="harmonizeUpdatePersonAttributeTypes.form">
		<fieldset>
			<table cellspacing="0" border="0" style="width: 100%">
				<tr>
					<td><b class="boxHeader"><spring:message
								code="eptsharmonization.personattributetype.harmonize.mdserver" /></b>
						<table cellspacing="0" border="0" style="width: 100%">
							<tr>
								<th><spring:message code="general.id" /></th>
								<th><spring:message code="general.name" /></th>
								<th><spring:message code="general.description" /></th>
							</tr>
							<c:forEach var="item"
								items="${mdsPersonAttributeTypesPartialEqual}">
								<tr>
									<td valign="top" align="center">${item.personAttributeType.id}</td>
									<td valign="top">${item.personAttributeType.name}</td>
									<td valign="top">${item.personAttributeType.description}</td>
								</tr>
							</c:forEach>
						</table></td>
					<td><b class="boxHeader"><spring:message
								code="eptsharmonization.personattributetype.harmonize.pdserver" /></b>
						<table cellspacing="0" border="0" style="width: 100%">
							<tr>
								<th><spring:message code="general.id" /></th>
								<th><spring:message code="general.name" /></th>
								<th><spring:message code="general.description" /></th>
								<th><spring:message code="general.uuid" /></th>
							</tr>
							<c:forEach var="item"
								items="${pdsPersonAttributeTypesPartialEqual}">
								<tr>
									<td valign="top" align="center">${item.personAttributeType.id}</td>
									<td valign="top">${item.personAttributeType.name}</td>
									<td valign="top">${item.personAttributeType.description}</td>
									<td valign="top">${item.personAttributeType.uuid}</td>
								</tr>
							</c:forEach>
						</table></td>
				</tr>
				<tr>
					<td colspan="4">
						<div class="submit-btn" align="center">
							<input type="submit"
								value='<spring:message code="general.next"/>'
								name="harmonizeNewFromMetadata" />
						</div>
					</td>
				</tr>
			</table>
		</fieldset>
	</form>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>