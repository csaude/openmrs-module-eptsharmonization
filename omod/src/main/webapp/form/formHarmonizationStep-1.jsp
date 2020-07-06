
<c:if test="${not isFirstStepFormHarmonizationCompleted}">

	<form method="post" action="processHarmonizationStep1.form">
		<c:if test="${not empty newMDSForms.items}">
			<br />
			<b class="boxHeader"><spring:message
					code="eptsharmonization.form.harmonize.onlyOnMDServer" /></b>
			<fieldset>
				<table id="tableOnlyMDS" cellspacing="0" border="0"
					style="width: 100%">
					<tr>
						<th><spring:message code="general.id" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="general.description" /></th>
						<th><spring:message code="eptsharmonization.encountertype.id" /></th>
						<th><spring:message code="eptsharmonization.encountertype.name" /></th>
						<th><spring:message code="general.uuid" /></th>
					</tr>
					<c:forEach var="item" items="${newMDSForms.items}">
						<tr>
							<td valign="top" align="center">${item.value.form.id}</td>
							<td valign="top">${item.value.form.name}</td>
							<td valign="top">${item.value.form.description}</td>
							<c:choose>
							<c:when
								test="${not empty item.value.form.encounterType}">
								<td valign="top">${item.value.form.encounterType.id}</td>
								<td valign="top">${item.value.form.encounterType.name}</td>
							</c:when>
							<c:otherwise>
								<td colspan="2"></td>
							</c:otherwise>
						</c:choose>
							<td valign="top">${item.value.form.uuid}</td>
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
				value='<spring:message code="eptsharmonization.form.btn.harmonizeNewFromMDS"/>'
				name="processHarmonizationStep1" />
		</div>

	</form>
</c:if>