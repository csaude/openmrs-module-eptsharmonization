<%@ include file="../template/localInclude.jsp"%>
<%@ include file="../template/localHeader.jsp"%>
<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/encounterType/harmonizeEncounterTypeList.form" />

<%@ include file="../template/jqueryPage.jsp"%>
<h2>
	<spring:message code="eptsharmonization.encountertype.harmonize" />
</h2>
<br />
<c:if test="${not empty harmonizedETSummary}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.summay.of.already.harmonized.mapping" /> :
		</b><br />
		<c:forEach var="msg" items="${harmonizedETSummary}">
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
	test="${isFirstStepHarmonizationCompleted && !hasSecondStepHarmonization}">
	<div id="openmrs_msg">
		<b> <spring:message
				code="eptsharmonization.encounterType.harmonizationFinish" />
		</b>
	</div>
</c:if>

<%@ include file="harmonizationEncounterType-1.jsp"%>
<%@ include file="harmonizationEncounterType-2.jsp"%>
<%@ include file="harmonizationEncounterType-3.jsp"%>

<%@ include file="/WEB-INF/template/footer.jsp"%>