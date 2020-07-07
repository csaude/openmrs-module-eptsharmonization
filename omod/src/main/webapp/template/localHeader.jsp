<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>
	<li><a href="${pageContext.request.contextPath}/module/eptsharmonization/encounterType/harmonizeEncounterTypeList.form"><spring:message
				code="eptsharmonization.harmonize.encountertype" /></a></li>
	<li><a href="${pageContext.request.contextPath}/module/eptsharmonization/personAttributeTypes/harmonizePersonAttributeTypesList.form"><spring:message
				code="eptsharmonization.harmonize.personattributetypes" /></a></li>
	<li><a href="${pageContext.request.contextPath}/module/eptsharmonization/programs/harmonizeProgramsList.form"><spring:message
				code="eptsharmonization.harmonize.programs" /></a></li>
	<li><a href="${pageContext.request.contextPath}/module/eptsharmonization/programWorkflows/harmonizeProgramWorkflowsList.form"><spring:message
				code="eptsharmonization.harmonize.programworkflows" /></a></li>
<<<<<<< HEAD
	<li><a href="${pageContext.request.contextPath}/module/eptsharmonization/programWorkflowStates/harmonizeProgramWorkflowStatesList.form"><spring:message
				code="eptsharmonization.harmonize.programworkflowstates" /></a></li>
	<li><a href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeVisitType.form"><spring:message
				code="eptsharmonization.visittype.harmonize" /></a></li>
	<li><a href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeRelationshipType.form"><spring:message
				code="eptsharmonization.relationshiptype.harmonize" /></a></li>
	<li><a href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeLocationAttributeType.form"><spring:message
				code="eptsharmonization.locationattributetype.harmonize" /></a></li>
	<li><a href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeLocationTag.form"><spring:message
				code="eptsharmonization.locationtag.harmonize" /></a></li>
		
=======
	<li>
	<li>
		<a
				href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeVisitType.form"><spring:message
				code="eptsharmonization.visittype.harmonize" /></a>
	</li>
	<li>
		<a
				href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeRelationshipType.form"><spring:message
				code="eptsharmonization.relationshiptype.harmonize" /></a>
	</li>
		<a
				href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeLocationAttributeType.form"><spring:message
				code="eptsharmonization.locationattributetype.harmonize" /></a>
	</li>
	<li>
		<a		href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeLocationTag.form"><spring:message
				code="eptsharmonization.locationtag.harmonize" /></a>
	</li>
	<li>
		<a		href="${pageContext.request.contextPath}/module/eptsharmonization/harmonizeConcept.form"><spring:message
				code="eptsharmonization.concept.harmonize.status" /></a>
	</li>

>>>>>>> master
	<!-- Add further links here -->
</ul>
