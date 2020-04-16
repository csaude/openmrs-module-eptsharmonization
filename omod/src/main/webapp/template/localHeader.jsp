<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>
	<li>
		<a
		href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeEncounterTypeList.form"><spring:message
				code="eptsharmonization.harmonize.encountertype" /></a>
	</li>
		
	<!-- Add further links here -->
</ul>
