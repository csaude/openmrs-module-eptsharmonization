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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeVisitType.form"><spring:message
						code="eptsharmonization.harmonizationstatus.visitType" /></a></td>
			<c:choose>
				<c:when test="${visitTypeStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeLocationTag.form"><spring:message
						code="eptsharmonization.harmonizationstatus.locationTag" /></a></td>
			<c:choose>
				<c:when test="${locationTagStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeLocationAttributeType.form"><spring:message
						code="eptsharmonization.harmonizationstatus.locationAttributeType" /></a></td>
			<c:choose>
				<c:when test="${locationAttributeTypeStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/patientIdentifierTypes/harmonizePatientIdentifierTypesList.form"><spring:message
						code="eptsharmonization.harmonizationstatus.patientIdentifierType" /></a></td>
			<c:choose>
				<c:when test="${patientIdentifierTypeStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/programs/harmonizeProgramsList.form"><spring:message
						code="eptsharmonization.harmonizationstatus.program" /></a></td>
			<c:choose>
				<c:when test="${programStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/programWorkflows/harmonizeProgramWorkflowsList.form"><spring:message
						code="eptsharmonization.harmonizationstatus.programWorkflow" /></a></td>
			<c:choose>
				<c:when test="${programWorkflowStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/programWorkflowStates/harmonizeProgramWorkflowStatesList.form"><spring:message
						code="eptsharmonization.harmonizationstatus.programWorkflowState" /></a></td>
			<c:choose>
				<c:when test="${programWorkflowStateStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/encounterType/harmonizeEncounterTypeList.form"><spring:message
						code="eptsharmonization.harmonizationstatus.encounterType" /></a></td>
			<c:choose>
				<c:when test="${encounterTypeStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/form/harmonizeFormList.form"><spring:message
						code="eptsharmonization.harmonizationstatus.form" /></a></td>
			<c:choose>
				<c:when test="${formStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/personAttributeTypes/harmonizePersonAttributeTypesList.form"><spring:message
						code="eptsharmonization.harmonizationstatus.personAttributeType" /></a></td>
			<c:choose>
				<c:when test="${personAttributeTypeStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeRelationshipType.form"><spring:message
						code="eptsharmonization.harmonizationstatus.relationshipType" /></a></td>
			<c:choose>
				<c:when test="${relationshipTypeStatus}">
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
		<tr>
			<td style="text-align: left; width: 70%;"><a
				href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeConcept.form"><spring:message
						code="eptsharmonization.harmonizationstatus.concept" /></a></td>
			<c:choose>
				<c:when test="${conceptStatus}">
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

	</table>
</fieldset>


<%@ include file="/WEB-INF/template/footer.jsp"%>