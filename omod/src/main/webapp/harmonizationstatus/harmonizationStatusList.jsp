<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="../template/localInclude.jsp"%>
<%@ include file="../template/localHeader.jsp"%>
<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizationstatus/harmonizationStatusList.form" />

<%@ include file="../template/jqueryPage.jsp"%>

<style>
#harmonizeStatusTable {
	font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
	border-collapse: collapse;
	width: 100%;
}

#harmonizeStatusTable td, #harmonizeStatusTable th {
	border: 1px solid #ddd;
	padding: 8px;
}

#harmonizeStatusTable tr:nth-child(even) {
	background-color: #f2f2f2;
}

#harmonizeStatusTable tr:hover {
	background-color: #ddd;
}

#harmonizeStatusTable th {
	padding-top: 12px;
	padding-bottom: 12px;
	text-align: left;
	background-color: #1aac9b;
	color: white;
}
</style>
<h2>
	<spring:message code="eptsharmonization.harmonizationstatus.title" />
</h2>
<br />
<br />

<b class="boxHeader"></b>
<fieldset>
	<table id="harmonizeStatusTable">
		<tr>
			<th style="text-align: left; width: 70%;"><spring:message
					code="eptsharmonization.harmonizationstatus.metadatatype" /></th>
			<th style="text-align: left; width: 30%;"><spring:message
					code="eptsharmonization.harmonizationstatus.status" /></th>
		</tr>

		<c:forEach var="item" items="${metadataTypes}" varStatus="itemsRow">
			<tr>
				<td style="text-align: left; width: 70%;"><spring:message
						code="${item.key}" /></td>
				<c:choose>
					<c:when test="${item.value}">
						<td
							style="text-align: left; width: 30%; background-color: #4CAF50;">
							<spring:message
								code="eptsharmonization.harmonizationstatus.harmonized" />
						</td>
					</c:when>
					<c:otherwise>
						<td
							style="text-align: left; width: 30%; background-color: #FF5733;">
							<spring:message
								code="eptsharmonization.harmonizationstatus.pending.harmonization" />
						</td>
					</c:otherwise>
				</c:choose>
			</tr>
		</c:forEach>
	</table>
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>