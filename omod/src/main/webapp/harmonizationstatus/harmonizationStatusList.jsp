<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="../template/localInclude.jsp"%>
<%@ include file="../template/localHeader.jsp"%>
<openmrs:require privilege="Manage Encountery Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizationstatus/harmonizationStatusList.form" />

<%@ include file="../template/jqueryPage.jsp"%>
<h2>
	<spring:message code="eptsharmonization.harmonizationstatus.title" />
</h2>
<br />
<br />

<b class="boxHeader"></b>
<fieldset>
	<table cellspacing="0" border="0" style="width: 100%">
		<tr>
			<th style="text-align: left; width: 70%;"><spring:message code="eptsharmonization.harmonizationstatus.metadatatype" /></th>
			<th style="text-align: left; width: 30%;"><spring:message code="eptsharmonization.harmonizationstatus.status" /></th>
		</tr>
		
		<c:forEach var="item" items="${metadataTypes}" varStatus="itemsRow">
			<tr>
				<td style="text-align: left; width: 70%;"><spring:message code="${item.key}" /></td>
				<c:choose>
					<c:when test="${item.value}">
						<td style="text-align: left; width: 30%; background-color: #4CAF50;">
							<spring:message code="eptsharmonization.harmonizationstatus.harmonized" />
						</td>
					</c:when>
					<c:otherwise>
						<td style="text-align: left; width: 30%; background-color: #FF5733;">
							<spring:message code="eptsharmonization.harmonizationstatus.pending.harmonization" />
						</td>
					</c:otherwise>
				</c:choose>
			</tr>
		</c:forEach>
	</table>
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>