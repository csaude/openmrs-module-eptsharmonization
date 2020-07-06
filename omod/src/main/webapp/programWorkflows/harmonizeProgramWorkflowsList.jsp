<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="../template/localInclude.jsp"%>
<%@ include file="../template/localHeader.jsp"%>
<openmrs:require privilege="Manage Program Workflow"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/programWorkflows/harmonizeProgramWorkflowsList.form" />

<%@ include file="../template/jqueryPage.jsp"%>
<h2>
	<spring:message code="eptsharmonization.programworkflow.harmonize" />
</h2>
<br />
<div id="error_msg" hidden="hidden">
	<span> <spring:message
			code="eptsharmonization.confirmAllHarmonization" /></span> <br />
</div>
<c:if test="${not empty harmonizedProgramWorkflowsSummary}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.summay.of.already.harmonized.mapping" /> :
		</b><br />
		<c:forEach var="msg" items="${harmonizedProgramWorkflowsSummary}">
			<span> <spring:message code="${msg}" text="${msg}" />
			</span>
			<br />
		</c:forEach>

		<form method="post" action="harmonizeProgramWorkflowsListExportLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					style="width: 14.10em; padding: 1px; font-size: 8pt;"
					value='<spring:message code="eptsharmonization.encountertype.harmonized.viewLog"/>'
					name="harmonizeAllProgramWorkflows" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<c:if
	test="${isFirstStepHarmonizationCompleted && isUUIDsAndIDsHarmonized && isNamesHarmonized && !hasSecondStepHarmonization}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.programworkflow.harmonizationFinish" />
		</b>
	</div>
</c:if>

<%@ include file="ProgramWorkflowsHarmonizationStep-1.jsp"%>
<%@ include file="ProgramWorkflowsHarmonizationStep-2.jsp"%>
<%@ include file="ProgramWorkflowsHarmonizationStep-3.jsp"%>
<%@ include file="ProgramWorkflowsHarmonizationStep-4.jsp"%>

<%@ include file="/WEB-INF/template/footer.jsp"%>