<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="../template/localInclude.jsp"%>
<%@ include file="../template/localHeader.jsp"%>
<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/encounterType/harmonizePersonAttributeTypesList.form" />

<%@ include file="../template/jqueryPage.jsp"%>
<h2>
	<spring:message code="eptsharmonization.personattributetype.harmonize" />
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
<c:if test="${not empty harmonizedPersonAttributeTypesSummary}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.summay.of.already.harmonized.mapping" /> :
		</b><br />
		<c:forEach var="msg" items="${harmonizedPersonAttributeTypesSummary}">
			<span> <spring:message code="${msg}" text="${msg}" />
			</span>
			<br />
		</c:forEach>

		<form method="post" action="harmonizePersonAttributeTypesListExportLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					style="width: 14.10em; padding: 1px; font-size: 8pt;"
					value='<spring:message code="eptsharmonization.encountertype.harmonized.viewLog"/>'
					name="harmonizeAllPersonAttributeTypes" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<c:if
	test="${isFirstStepHarmonizationCompleted && isUUIDsAndIDsHarmonized && isNamesHarmonized && !hasSecondStepHarmonization}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.personattributetype.harmonizationFinish" />
		</b>
	</div>
</c:if>

<%@ include file="personAttributeTypesHarmonizationStep-1.jsp"%>
<%@ include file="personAttributeTypesHarmonizationStep-2.jsp"%>
<%@ include file="personAttributeTypesHarmonizationStep-3.jsp"%>
<%@ include file="personAttributeTypesHarmonizationStep-4.jsp"%>

<%@ include file="/WEB-INF/template/footer.jsp"%>