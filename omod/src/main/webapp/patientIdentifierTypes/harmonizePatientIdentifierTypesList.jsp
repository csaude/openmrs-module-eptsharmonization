<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="../template/localInclude.jsp"%>
<%@ include file="../template/localHeader.jsp"%>
<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/patientIdentifierTypes/harmonizePatientIdentifierTypesList.form" />

<%@ include file="../template/jqueryPage.jsp"%>
<h2>
	<spring:message code="eptsharmonization.patientidentifiertype.harmonize" />
</h2>
<br />
<div id="error_msg" hidden="hidden">
	<span> <spring:message
			code="eptsharmonization.confirmAllHarmonization" /></span> <br />
</div>
<c:if test="${not empty harmonizedPatientIdentifierTypesSummary}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.summay.of.already.harmonized.mapping" /> :
		</b><br />
		<c:forEach var="msg" items="${harmonizedPatientIdentifierTypesSummary}">
			<span> <spring:message code="${msg}" text="${msg}" />
			</span>
			<br />
		</c:forEach>

		<form method="post" action="harmonizePatientIdentifierTypesListExportLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					style="width: 14.10em; padding: 1px; font-size: 8pt;"
					value='<spring:message code="eptsharmonization.encountertype.harmonized.viewLog"/>'
					name="harmonizeAllPatientIdentifierTypes" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<c:if
	test="${isFirstStepHarmonizationCompleted && isUUIDsAndIDsHarmonized && isNamesHarmonized && !hasSecondStepHarmonization}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.patientidentifiertype.harmonizationFinish" />
		</b>
	</div>
</c:if>

<%@ include file="patientIdentifierTypesHarmonizationStep-1.jsp"%>
<%@ include file="patientIdentifierTypesHarmonizationStep-2.jsp"%>
<%@ include file="patientIdentifierTypesHarmonizationStep-3.jsp"%>
<%@ include file="patientIdentifierTypesHarmonizationStep-4.jsp"%>

<%@ include file="/WEB-INF/template/footer.jsp"%>