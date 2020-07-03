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
	<spring:message code="eptsharmonization.encountertype.harmonize" />
</h2>
<br />
<div id="error_msg" hidden="hidden">
	<span> <spring:message
			code="eptsharmonization.confirmAllHarmonization" /></span> <br />
</div>
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

		<form method="post" action="harmonizeEncounterTypeListExportLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					style="width: 8.6em; padding: 6px; font-size: 6pt;"
					value='<spring:message code="eptsharmonization.encountertype.harmonized.viewLog"/>'
					name="harmonizeAllEncounterTypes" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<c:if
	test="${isFirstStepFormHarmonizationCompleted && isUUIDsAndIDsFormHarmonized && isNamesFormHarmonized && !hasSecondStepFormHarmonization}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.encounterType.harmonizationFinish" />
		</b>
	</div>
</c:if>

<%@ include file="formHarmonizationStep-1.jsp"%>

<%@ include file="/WEB-INF/template/footer.jsp"%>