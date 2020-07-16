<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="../template/localInclude.jsp"%>
<%@ include file="../template/localHeader.jsp"%>
<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/form/harmonizeFormList.form" />

<%@ include file="../template/jqueryPage.jsp"%>
<h2>
	<spring:message code="eptsharmonization.form.harmonize" />
</h2>
<br />
<c:if test="${not empty mdsFormsWithoutEncounterReferences}">
	<div id="error_msg" style="padding: 8px; font-size: 16px; font-weight: bold; text-align: left;">
		<b><span> <spring:message
			code="eptsharmonization.form.processAllHarmonizationOfEncounterBeforeYouProceed" /></span> <br />
		</b>
	</div>
</c:if>
<div id="error_msg_invalidEncounterType" hidden="hidden">
	<div id="error_msg">
		<span> <spring:message
			code="eptsharmonization.form.processAllHarmonizationOfEncounterBeforeYouProceed" /></span> <br />
	</div>
</div>
<div id="error_msg_SelectAllToProceed" hidden="hidden">
	<div id="error_msg">
		<span> <spring:message
			code="eptsharmonization.confirmAllHarmonization" /></span> <br />
	</div>
</div>
<c:if test="${not empty errorProcessingManualMapping}">
	<div id="error_msg">
		<span>${errorProcessingManualMapping}</span>
	</div>
</c:if>
<c:if test="${not empty harmonizedFormSummary}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.summay.of.already.harmonized.mapping" /> :
		</b><br />
		<c:forEach var="msg" items="${harmonizedFormSummary}">
			<span> <spring:message code="${msg}" text="${msg}" />
			</span>
			<br />
		</c:forEach>

		<form method="post" action="harmonizeFormExportLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					style="width: 14.10em; padding: 1px; font-size: 8pt;"
					value='<spring:message code="eptsharmonization.form.harmonized.viewLog"/>'
					name="harmonizeAllForms" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<c:if
	test="${isFirstStepFormHarmonizationCompleted && isUUIDsAndIDsFormHarmonized && isNamesFormHarmonized && !hasSecondStepFormHarmonization && hasFirstStepHtmlFormComplete && hasSecondStepHtmlFormComplete}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.form.harmonizationFinish" />
		</b>
	</div>
</c:if>

<%@ include file="formHarmonizationStep-0.jsp"%>

<c:if test="${empty mdsFormsWithoutEncounterReferences}">
	<%@ include file="formHarmonizationStep-1.jsp"%>
	<%@ include file="formHarmonizationStep-2.jsp"%>
	<%@ include file="formHarmonizationStep-3.jsp"%>
	
		<c:if test="${not hasFirstStepHtmlFormComplete}">
			<%@ include file="formHarmonizationStep-5.jsp"%>
		</c:if>
		
		<c:if test="${hasFirstStepHtmlFormComplete && not hasSecondStepHtmlFormComplete}">
			<%@ include file="formHarmonizationStep-6.jsp"%>
		</c:if>
		
	<%@ include file="formHarmonizationStep-4.jsp"%>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>