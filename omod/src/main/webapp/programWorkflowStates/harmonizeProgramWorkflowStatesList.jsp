<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="../template/localInclude.jsp"%>
<%@ include file="../template/localHeader.jsp"%>
<openmrs:require privilege="Manage Program Workflow"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/programWorkflowStates/harmonizeProgramWorkflowStatesList.form" />

<%@ include file="../template/jqueryPage.jsp"%>
<h2>
	<spring:message code="eptsharmonization.programworkflowstate.harmonize" />
</h2>
<br />
<div id="error_msg" hidden="hidden">
	<span> <spring:message
			code="eptsharmonization.confirmAllHarmonization" /></span> <br />
</div>

<c:if test="${not empty errorProcessingManualMapping}">
	<div id="error_msg">
		<span>${errorProcessingManualMapping}</span>
	</div>
</c:if>
<c:if test="${not empty harmonizedProgramWorkflowStatesSummary}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.summay.of.already.harmonized.mapping" /> :
		</b><br />
		<c:forEach var="msg" items="${harmonizedProgramWorkflowStatesSummary}">
			<span> <spring:message code="${msg}" text="${msg}" />
			</span>
			<br />
		</c:forEach>

		<form method="post" action="harmonizeProgramWorkflowStatesListExportLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					style="width: 14.10em; padding: 1px; font-size: 8pt;"
					value='<spring:message code="eptsharmonization.encountertype.harmonized.viewLog"/>'
					name="harmonizeAllProgramWorkflowStates" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<c:if
	test="${isFirstStepHarmonizationCompleted && isUUIDsAndIDsHarmonized && isNamesHarmonized && !hasSecondStepHarmonization}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.programworkflowstate.harmonizationFinish" />
		</b>
	</div>
</c:if>

<%@ include file="ProgramWorkflowStatesHarmonizationStep-1.jsp"%>
<%@ include file="ProgramWorkflowStatesHarmonizationStep-2.jsp"%>
<%@ include file="ProgramWorkflowStatesHarmonizationStep-3.jsp"%>
<%@ include file="ProgramWorkflowStatesHarmonizationStep-4.jsp"%>

<%@ include file="/WEB-INF/template/footer.jsp"%>