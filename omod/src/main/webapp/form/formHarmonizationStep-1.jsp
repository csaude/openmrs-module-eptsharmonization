
<c:if test="${not isFirstStepFormHarmonizationCompleted}">

	<form method="post" action="processHarmonizationStep1.form">
		<c:if test="${not empty newMDSForms.items}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.encountertype.harmonize.onlyOnMDServer" /></b>
			<fieldset>
				<table id="tableOnlyMDS" cellspacing="0" border="0"
					style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="general.uuid" /></th>
						<th><spring:message
								code="eptsharmonization.encountertype.harmonize.encounters" /></th>
						<th><spring:message
								code="eptsharmonization.encountertype.harmonize.forms" /></th>
					</tr>
					<c:forEach var="item" items="${newMDSForms.items}">
						<tr>
							<td valign="top" align="center">${item.value.form.id}</td>
							<td valign="top">${item.value.form.name}</td>
							<td valign="top">${item.value.form.description}</td>
							<td valign="top">${item.value.form.uuid}</td>
							<td style="text-align: right;">${item.encountersCount}</td>
							<td style="text-align: right;">${item.formFieldsCount}</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
		</c:if>

		<c:if test="${not empty productionItemsToDeleteForm}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.encountertype.harmonize.onlyOnPServer.unused" /></b>
			<fieldset>
				<table cellspacing="0" border="0" style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${productionItemsToDeleteForm}">
						<tr>
							<td valign="top" align="center">${item.form.id}</td>
							<td valign="top">${item.form.name}</td>
							<td valign="top">${item.form.description}</td>
							<td valign="top">${item.form.uuid}</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<br />
		</c:if>

		<div class="submit-btn" align="center">
			<input type="submit"
				value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
				name="processHarmonizationStep1" />
		</div>

	</form>
</c:if>