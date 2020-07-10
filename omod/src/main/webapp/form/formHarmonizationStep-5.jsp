
<c:if test="${not hasFirstStepHtmlFormComplete}">
	<c:if test="${not empty htmlFormsWithDifferentFormAndEqualUuid.items}">
		<form method="post" action="processHarmonizationStep5.form">
			<br /> <b class="boxHeader"><spring:message
					code="eptsharmonization.form.htmlform.differrentForm.equal.uuid" /></b>
			<fieldset>
				<table id="tableOnlyMDS" cellspacing="0" border="0"
					style="width: 100%">

					<tr>
						<th colspan="3"><spring:message
								code="eptsharmonization.form.htmlfom.mdsDetails" /></th>
						<th colspan="3"><spring:message
								code="eptsharmonization.form.htmlfom.pdsDetails" /></th>
					</tr>
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message
								code="eptsharmonization.form.htmlform.formName" /></th>
						<th><spring:message
								code="eptsharmonization.form.htmlform.mdsHtmlUUid" /></th>
						<th><spring:message code="general.id" /></th>
						<th><spring:message
								code="eptsharmonization.form.htmlform.formName" /></th>
						<th><spring:message
								code="eptsharmonization.form.htmlform.mdsHtmlUUid" /></th>
					</tr>
					<c:forEach var="item"
						items="${htmlFormsWithDifferentFormAndEqualUuid.items}">
						<tr>
							<td valign="top" align="center">${item.value[0].id}</td>
							<td valign="top">${item.value[0].form.name}</td>
							<td valign="top">${item.value[0].uuid}</td>
							<td valign="top" align="center">${item.value[1].id}</td>
							<td valign="top">${item.value[1].form.name}</td>
							<td valign="top">${item.value[1].uuid}</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
			<div class="submit-btn" align="center">
				<input type="submit"
					value='<spring:message code="eptsharmonization.form.btn.harmonizeNewFromMDS"/>' />
			</div>
		</form>
	</c:if>
</c:if>