<c:if
	test="${isFirstStepHarmonizationCompleted && isUUIDsAndIDsHarmonized && not isNamesHarmonized}">

	<c:if test="${ not empty differentProgramWorkflowsOrConceptsAndSameUUIDAndID.items}">

		<springform:form modelAttribute="differentProgramWorkflowsOrConceptsAndSameUUIDAndID"
			method="post" action="processHarmonizationStep3.form">
			<b class="boxHeader"><spring:message
					code="eptsharmonization.programworkflowstate.harmonize.differentProgramWorkflowsOrConceptsAndSameUUIDAndID" /></b>
			<fieldset>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th><spring:message
								code="eptsharmonization.programworkflowstate.mdserver.concept" /></th>
						<th><spring:message
								code="eptsharmonization.programworkflowstate.mdserver.flowConcept" /></th>
						<th><spring:message
								code="eptsharmonization.programworkflowstate.mdserver.program" /></th>
						<th><spring:message
								code="eptsharmonization.programworkflowstate.pdserver.concept" /></th>
						<th><spring:message
								code="eptsharmonization.programworkflowstate.pdserver.flowConcept" /></th>
						<th><spring:message
								code="eptsharmonization.programworkflowstate.pdserver.program" /></th>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.uuid" /></th>
						<th style="text-align: center; width: 10%;"><spring:message
								code="eptsharmonization.proceedHarmonization" /></th>
					</tr>
					<c:forEach var="item"
						items="${differentProgramWorkflowsOrConceptsAndSameUUIDAndID.items}"
						varStatus="itemsRow">
						<tr class="confirm-each-row-harmonization">
							<td valign="top">${item.value[0].concept}</td>
							<td valign="top">${item.value[0].flowConcept}</td>
							<td valign="top">${item.value[0].flowProgram}</td>
							<td valign="top">${item.value[1].concept}</td>
							<td valign="top">${item.value[1].flowConcept}</td>
							<td valign="top">${item.value[1].flowProgram}</td>
							<td valign="top">${item.value[0].programWorkflowState.id}</td>
							<td valign="top">${item.key}</td>
							<td style="text-align: center;"><spring:bind
									path="items[${itemsRow.index}].selected">
									<input type="hidden"
										name="_<c:out value="${status.expression}"/>">
									<input type="radio" class="radio-proceed"
										name="<c:out value="${status.expression}"/>" value="true"
										<c:if test="${status.value}">checked</c:if> />
									<span><spring:message code="eptsharmonization.proceed" /></span>

								</spring:bind> <spring:bind path="items[${itemsRow.index}].selected">
									<input type="hidden"
										name="_<c:out value="${status.expression}"/>">
									<input type="radio" class="radio-not-proceed"
										name="<c:out value="${status.expression}"/>" value="false"
										<c:if test="${status.value}">checked</c:if> />
									<span><spring:message
											code="eptsharmonization.notProceed" /></span>
								</spring:bind></td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
			<div class="submit-btn" align="center">
				<input type="submit"
					value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
					name="processHarmonizationStep2" id="btn-partialHarmonization" />
			</div>
		</springform:form>
	</c:if>
</c:if>
<script type="text/javascript">

$(document).ready(function () {
   
   $("#btn-partialHarmonization").click(function (event) {
   
      if(isFormValid()){
      	return true;
      }
      else{
       event.preventDefault();
      }      
   });
            
 function isFormValid() {
		var itemTrs = document.querySelectorAll(".confirm-each-row-harmonization");
		var atLeastOneNotSelected = false;

		itemTrs.forEach(function(item) {
			var inputRadioProceed = item.querySelector(".radio-proceed");
		    var inputRadioNotProceed = item.querySelector(".radio-not-proceed");
			
			if (!inputRadioProceed.checked && !inputRadioNotProceed.checked ) {
				atLeastOneNotSelected = true;
				item.classList.add("errorTD");
			}
			else{
				item.classList.remove("errorTD");
			}
		});
		var divErrorMsg = document.querySelector("#error_msg");
		if (atLeastOneNotSelected) {
			divErrorMsg.hidden = "";
		} else {
			divErrorMsg.hidden = "hidden";
		}
		return !atLeastOneNotSelected;
	}
 });
</script>