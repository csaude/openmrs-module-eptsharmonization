<c:if test="${not empty mdsFormsWithoutEncounterReferences}">
	<form method="post">
		<br /> <b class="boxHeader"><spring:message
				code="eptsharmonization.form.withoutEncounterTypeReference" /></b>
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
				<c:forEach var="item" items="${mdsFormsWithoutEncounterReferences}">
					<tr>
						<td valign="top">${item.id}</td>
						<td valign="top">${item.name}</td>
						<td valign="top">${item.description}</td>
						<td valign="top">${item.uuid}</td>
						<td valign="top">${item.encounterType.id}</td>
						<c:choose>
							<c:when test="${not empty item.encounterType.name}">
								<td>${item.encounterType.name}</td>
							</c:when>
							<c:otherwise>
								<td>N/A</td>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
			</table>
		</fieldset>
	</form>
</c:if>