<c:if
	test="${isFirstStepFormHarmonizationCompleted && not isUUIDsAndIDsFormHarmonized}">

	<c:if test="${not empty differentIDsAndEqualUUIDForm.items}">
		<springform:form modelAttribute="differentIDsAndEqualUUIDForm"
			method="post" action="processHarmonizationStep2.form">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.form.harmonize.differentID.andEqualUUID" /></b>
			<fieldset>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th style="text-align: left; width: 5%;"><spring:message
								code="eptsharmonization.form.mdserver.id" /></th>
						<th style="text-align: left; width: 15%;"><spring:message
								code="eptsharmonization.form.mdserver.name" /></th>
						<th style="text-align: left; width: 15%;"><spring:message
								code="eptsharmonization.form.mdserver.description" /></th>
						<th><spring:message
								code="eptsharmonization.encountertype.name" /></th>
						<th style="text-align: left; width: 5%;"><spring:message
								code="eptsharmonization.form.pdserver.id" /></th>
						<th style="text-align: left; width: 15%;"><spring:message
								code="eptsharmonization.form.pdserver.name" /></th>
						<th style="text-align: left; width: 15%;"><spring:message
								code="eptsharmonization.form.pdserver.description" /></th>
						<th><spring:message
								code="eptsharmonization.encountertype.name" /></th>
						<th style="text-align: left; width: 10%;"><spring:message
								code="general.uuid" /></th>
						<th style="text-align: left; width: 5%;"><spring:message
								code="eptsharmonization.form.harmonize.encounters" /></th>
						<th style="text-align: center; width: 10%;"><spring:message
								code="eptsharmonization.proceedHarmonization" /></th>
					</tr>
					<c:forEach var="item" items="${differentIDsAndEqualUUIDForm.items}"
						varStatus="itemsRow">
						<tr class="confirm-each-row-harmonization">
							<td valign="top" style="text-align: left; width: 5%;">${item.value[0].form.id}</td>
							<td valign="top" style="text-align: left; width: 15%;">${item.value[0].form.name}</td>
							<td valign="top" style="text-align: left; width: 15%;">${item.value[0].form.description}</td>
							<c:choose>
								<c:when test="${not empty item.value[0].form.encounterType.name}">
									<td>${item.value[0].form.encounterType.name}</td>
								</c:when>
								<c:otherwise>
									<td>N/A</td>
								</c:otherwise>
							</c:choose>
							<td valign="top" style="text-align: left; width: 5%;">${item.value[1].form.id}</td>
							<td valign="top" style="text-align: left; width: 15%;">${item.value[1].form.name}</td>
							<td valign="top" style="text-align: left; width: 15%;">${item.value[1].form.description}</td>
							<c:choose>
								<c:when test="${not empty item.value[1].form.encounterType.name}">
									<td>${item.value[1].form.encounterType.name}</td>
								</c:when>
								<c:otherwise>
									<td>N/A</td>
								</c:otherwise>
							</c:choose>
							<td valign="top" style="text-align: left; width: 10%;">${item.key}</td>
							<td style="text-align: right; width: 5%;">${item.encountersCount}</td>
							<td style="text-align: center; width: 10%;"><spring:bind
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
					value='<spring:message code="eptsharmonization.form.btn.harmonizeNewFromMDS"/>'
					name="processHarmonizationStep2" id="btn-partialHarmonization-step2" />
			</div>
		</springform:form>
	</c:if>
</c:if>
<script type="text/javascript">

$(document).ready(function () {
   
   $("#btn-partialHarmonization-step2").click(function (event) {
   
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
		var divErrorMsg = document.querySelector("#error_msg_SelectAllToProceed");
		if (atLeastOneNotSelected) {
			divErrorMsg.hidden = "";
		} else {
			divErrorMsg.hidden = "hidden";
		}
		return !atLeastOneNotSelected;
	}
 });
</script>