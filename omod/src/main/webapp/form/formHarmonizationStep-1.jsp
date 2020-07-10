
<c:if test="${not isFirstStepFormHarmonizationCompleted}">

	<form method="post" action="processHarmonizationStep1.form">
		<c:if test="${not empty newMDSForms.items}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.form.harmonize.onlyOnMDServer" /></b>
			<fieldset>
				<table id="tableOnlyMDS" cellspacing="0" border="0"
					style="width: 100%" >
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="general.uuid" /></th>
						<th><spring:message code="eptsharmonization.encountertype.id" /></th>
						<th><spring:message
								code="eptsharmonization.encountertype.name" /></th>
					</tr>
					<c:forEach var="item" items="${newMDSForms.items}">
						<tr class="confirm-each-row-harmonization">
							<td valign="top" align="center">${item.value.form.id}</td>
							<td valign="top">${item.value.form.name}</td>
							<td valign="top">${item.value.form.description}</td>
							<td valign="top">${item.value.form.uuid}</td>
							<c:choose>
								<c:when test="${not empty item.value.form.encounterType.id}">
									<td valign="top">${item.value.form.encounterType.id}</td>
								</c:when>
								<c:otherwise>
									<td>N/A</td>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${not empty item.value.form.encounterType.name}">
									<td class="td-not-proceed">${item.value.form.encounterType.name}</td>
								</c:when>
								<c:otherwise>
									<td class="td-not-proceed">N/A</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
		</c:if>

		<c:if test="${not empty productionItemsToDeleteForm}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.form.harmonize.onlyOnPServer.unused" /></b>
			<fieldset>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="general.uuid" /></th>
						<th><spring:message code="eptsharmonization.encountertype.id" /></th>
						<th><spring:message
								code="eptsharmonization.encountertype.name" /></th>
					</tr>
					<c:forEach var="item" items="${productionItemsToDeleteForm}">
						<tr>
							<td valign="top" align="center">${item.form.id}</td>
							<td valign="top">${item.form.name}</td>
							<td valign="top">${item.form.description}</td>
							<td valign="top">${item.form.uuid}</td>
							<c:choose>
								<c:when test="${not empty item.form.encounterType.id}">
									<td valign="top">${item.form.encounterType.id}</td>
								</c:when>
								<c:otherwise>
									<td>N/A</td>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${not empty item.form.encounterType.name}">
									<td class="td-not-proceed">${item.form.encounterType.name}</td>
								</c:when>
								<c:otherwise>
									<td class="td-not-proceed">N/A</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
		</c:if>
			<div class="submit-btn" align="center">
				<input type="submit"
					value='<spring:message code="eptsharmonization.form.btn.harmonizeNewFromMDS"/>'
					name="processHarmonizationStep1"  id="btn-first-step-harmonization" />
			</div>
	</form>
</c:if>

<script type="text/javascript">
$(document).ready(function () {
	   
	   $("#btn-first-step-harmonization").click(function (event) {
	      if(isFormValid()){
	    	  return true;
	      }
	      else{
	       event.preventDefault();
	      }      
	   });
	            
	 function isFormValid() {
		 var itemTrs = document.querySelectorAll(".confirm-each-row-harmonization");
			var atLeastOneError = false;
			itemTrs.forEach(function(item) {
				var elem = item.querySelector(".td-not-proceed");				
				if (elem  && elem.innerText == 'N/A') {
					atLeastOneError = true;
					item.classList.add("errorTD");
				}
			});
		
	 	var divErrorMsg = document.querySelector("#error_msg_invalidEncounterType");
		if (atLeastOneError) {
			divErrorMsg.hidden = "";
		} else {
			divErrorMsg.hidden = "hidden";
		}
		return !atLeastOneError;
		}
	 });
</script>