
<springform:form modelAttribute="encounterTypesPartialEqual"
	method="post">
	<c:if test="${not empty encounterTypesPartialEqual.items}">
		<br />
		<b class="boxHeader"><spring:message
				code="eptsharmonization.encountertype.harmonize.differentID.andEqualUUID" /></b>
		<fieldset>
			<table cellspacing="0" border="0" style="width: 100%">
				<tr>
					<th style="text-align: left; width: 5%;"><spring:message
							code="eptsharmonization.encountertype.mdserver.id" /></th>
					<th style="text-align: left; width: 15%;"><spring:message
							code="eptsharmonization.encountertype.mdserver.name" /></th>
					<th style="text-align: left; width: 15%;"><spring:message
							code="eptsharmonization.encountertype.mdserver.description" /></th>
					<th style="text-align: left; width: 5%;"><spring:message
							code="eptsharmonization.encountertype.pdserver.id" /></th>
					<th style="text-align: left; width: 15%;"><spring:message
							code="eptsharmonization.encountertype.pdserver.name" /></th>
					<th style="text-align: left; width: 15%;"><spring:message
							code="eptsharmonization.encountertype.pdserver.description" /></th>
					<th style="text-align: left; width: 20%;"><spring:message
							code="general.uuid" /></th>
					<th style="text-align: left; width: 5%;"><spring:message
							code="eptsharmonization.encountertype.harmonize.encounters" /></th>
					<th style="text-align: left; width: 5%;"><spring:message
							code="eptsharmonization.encountertype.harmonize.forms" /></th>
					<th></th>
				</tr>
				<c:forEach var="item" items="${encounterTypesPartialEqual.items}"
					varStatus="itemsRow">
					<tr>
						<td valign="top" style="text-align: left; width: 5%;">${item.value[0].encounterType.id}</td>
						<td valign="top" style="text-align: left; width: 15%;">${item.value[0].encounterType.name}</td>
						<td valign="top" style="text-align: left; width: 15%;">${item.value[0].encounterType.description}</td>
						<td valign="top" style="text-align: left; width: 5%;">${item.value[1].encounterType.id}</td>
						<td valign="top" style="text-align: left; width: 15%;">${item.value[1].encounterType.name}</td>
						<td valign="top" style="text-align: left; width: 15%;">${item.value[1].encounterType.description}</td>
						<td valign="top" style="text-align: left; width: 20%;">${item.key}</td>
						<td style="text-align: right; width: 5%;">${item.encountersCount}</td>
						<td style="text-align: right; width: 5%;">${item.formsCount}</td>

						<spring:bind path="items[${itemsRow.index}].selected">
							<td><input type="hidden"
								name="_<c:out value="${status.expression}"/>"> <input
								type="checkbox" class="info-checkBox"
								name="<c:out value="${status.expression}"/>" value="true"
								<c:if test="${status.value}">checked</c:if> /></td>
						</spring:bind>

					</tr>
				</c:forEach>
			</table>
		</fieldset>
		<br />
	</c:if>
</springform:form>

<c:if test="${not empty encounterTypesWithDifferentNames}">
	<br />
	<b class="boxHeader"><spring:message
			code="eptsharmonization.encountertype.harmonize.differentNamesAndSameUUIDAndID" /></b>
	<fieldset>
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.mdserver.description" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.name" /></th>
				<th><spring:message
						code="eptsharmonization.encountertype.pdserver.description" /></th>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.uuid" /></th>
				<th></th>
			</tr>
			<c:forEach var="entry" items="${encounterTypesWithDifferentNames}">
				<tr>
					<td valign="top">${entry.value[0].encounterType.name}</td>
					<td valign="top">${entry.value[0].encounterType.description}</td>
					<td valign="top">${entry.value[1].encounterType.name}</td>
					<td valign="top">${entry.value[1].encounterType.description}</td>
					<td valign="top">${entry.value[0].encounterType.id}</td>
					<td valign="top">${entry.key}</td>
					<td style="padding-right: 3em"></td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<br />
</c:if>

<c:if test="${!isFirstStepHarmonizationCompleted}">
	<br />
	<form method="post" class="box"
		action="harmonizeEncounterTypeList.form">
		<div class="submit-btn" align="center">
			<input type="submit"
				value='<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS"/>'
				name="harmonizeAllEncounterTypes" />
		</div>
	</form>
	<br />
</c:if>