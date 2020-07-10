
<c:if
	test="${hasFirstStepHtmlFormComplete && not hasSecondStepHtmlFormComplete}">

	<c:if test="${ not empty newHtmlFormFromMDS}">
		<form method="post" action="processHarmonizationStep6.form">
			<br /> <b class="boxHeader"><spring:message
					code="eptsharmonization.form.htmlform.differrentForm.equal.uuid" /></b>
			<fieldset>
				<table cellspacing="0" border="0" style="width: 100%">

					<tr>
						<th colspan="3"><spring:message
								code="eptsharmonization.form.htmlfom.mdsDetails" /></th>
					</tr>
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message
								code="eptsharmonization.form.htmlform.formName" /></th>
						<th><spring:message
								code="eptsharmonization.form.htmlform.mdsHtmlUUid" /></th>
					</tr>
					<c:forEach var="item" items="${newHtmlFormFromMDS}">
						<tr>
							<td valign="top" align="center">${item.id}</td>
							<td valign="top">${item.form.name}</td>
							<td valign="top">${item.uuid}</td>
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