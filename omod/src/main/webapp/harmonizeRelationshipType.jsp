<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Relationship Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeRelationshipTypes.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>
<script>
    $j(document).ready(function(){
        $j('#submit-proceed').click(function(){
            var check = true;
            $j("input:radio").each(function(){
                var name = $j(this).attr("name");
                if($j("input:radio[name=" + name + "]:checked").length == 0){
                    check = false;
                }
            });

            if(!check){
                alert('<spring:message code="eptsharmonization.relationshiptype.harmonize.ensureConfirmation"/>');
                return false;
			}
        });
    });
</script>
<style>
p {
	border: 1px solid black;
}

table {
	border: 3px solid #1aac9b;
	border-collapse: collapse;
}

th {
	background-color: #1aac9b;
}

tr:first-child label {
	padding: 4px !important;
	color: #fff;
	font-weight: bold;
	margin: 4px !important;
	text-shadow: 0 0 .3em black;
	font-size: 12pt;
}

td {
	border: 1px solid #1aac9b;
}

.submit-btn {
	flex: 1;
	margin: 10px 15px;
}

.submit-btn input {
	color: #fff;
	background: #1aac9b;
	padding: 8px;
	width: 12.8em;
	font-weight: bold;
	text-shadow: 0 0 .3em black;
	font-size: 9pt;
	border-radius: 5px 5px;
}
</style>

<h2>
	<spring:message code="eptsharmonization.relationshiptype.harmonize" />
</h2>
<br />
<br />
<c:if test="${not empty harmonizedVTSummary}">
	<div id="openmrs_msg">
	   <b> <spring:message code="eptsharmonization.summay.of.already.harmonized.mapping" 	/> :</b><br />
		<c:forEach var="msg" items="${harmonizedVTSummary}">
			<span> <spring:message code="${msg}" text="${msg}" />
			</span>
			<br />
		</c:forEach>
		<form method="post" action="harmonizeRelationshipTypeListExportLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					   style="width: 8.6em; padding: 6px; font-size: 6pt;"
					   value='<spring:message code="eptsharmonization.encountertype.harmonized.viewLog"/>'
					   name="harmonizeAllRelationshipTypes" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<form id="harmonize-relationship-form" method="POST" class="box" action="mandatoryRelationshipTypeHarmonization.form">
<c:if test="${not empty missingInPDS}">
	<b class="boxHeader"><spring:message code="eptsharmonization.relationshiptype.harmonize.missingInPDS" /></b>
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="eptsharmonization.relationshiptype.harmonize.aIsToB" /></th>
				<th><spring:message code="eptsharmonization.relationshiptype.harmonize.bIsToA" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${missingInPDS}">
				<tr>
					<td valign="top" align="center">${item.relationshipType.id}</td>
					<td valign="top">${item.relationshipType.aIsToB}</td>
					<td valign="top">${item.relationshipType.bIsToA}</td>
					<td valign="top">${item.relationshipType.description}</td>
					<td valign="top">${item.relationshipType.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	<br />
	<br />
</c:if>

<c:if test="${not empty notInMDSNotUsed}">
	<b class="boxHeader"><spring:message code="eptsharmonization.relationshiptype.harmonize.notInMDSNotUsed" /></b>
		<table class="box" cellspacing="0" border="0" style="width: 100%;">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="eptsharmonization.relationshiptype.harmonize.aIsToB" /></th>
				<th><spring:message code="eptsharmonization.relationshiptype.harmonize.bIsToA" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${notInMDSNotUsed}">
				<tr>
					<td valign="top" align="center">${item.relationshipType.id}</td>
					<td valign="top">${item.relationshipType.aIsToB}</td>
					<td valign="top">${item.relationshipType.bIsToA}</td>
					<td valign="top">${item.relationshipType.description}</td>
					<td valign="top">${item.relationshipType.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	<br />
	<br />
</c:if>

<c:if test="${not empty sameIdAndUuidDifferentNames}">
	<b class="boxHeader"><spring:message code="eptsharmonization.relationshiptype.harmonize.sameIdAndUuidDifferentNames" /></b>
		<table class="box" cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.uuid" /></th>
				<th>PDS <spring:message code="eptsharmonization.relationshiptype.harmonize.aIsToB" />
					:<spring:message code="eptsharmonization.relationshiptype.harmonize.bIsToA" /></th>
				<th>PDS <spring:message code="general.description" /></th>
				<th>PDS <spring:message code="eptsharmonization.relationshiptype.harmonize.inactive" /></th>
				<th>MDS <spring:message code="eptsharmonization.relationshiptype.harmonize.aIsToB" />
					:<spring:message code="eptsharmonization.relationshiptype.harmonize.bIsToA" /></th>
				<th>MDS <spring:message code="general.description" /></th>
				<th>MDS <spring:message code="eptsharmonization.relationshiptype.harmonize.inactive" /></th>
				<th><spring:message code="eptsharmonization.relationshiptype.harmonize.affectedRelationships" /></th>
				<th colspan="2"><spring:message code="eptsharmonization.relationshiptype.harmonize.actionForSimilarUuid" /></th>
			</tr>
			<c:forEach var="entry" items="${sameIdAndUuidDifferentNames}">
				<tr>
					<td valign="top" align="center">${entry.key[0].id}</td>
					<td valign="top" align="center">${entry.key[0].uuid}</td>
					<td valign="top">${entry.key[0].relationshipType.aIsToB}:${entry.key[0].relationshipType.bIsToA}</td>
					<td valign="top">${entry.key[0].relationshipType.description}</td>
					<td valign="top">${entry.key[0].relationshipType.retired}</td>
					<td valign="top">${entry.key[1].relationshipType.aIsToB}:${entry.key[1].relationshipType.bIsToA}</td>
					<td valign="top">${entry.key[1].relationshipType.description}</td>
					<td valign="top">${entry.key[1].relationshipType.retired}</td>
					<td valign="top" align="center">${entry.value}</td>
					<td><input type="radio" value="true" name="${entry.key[0].uuid}"/><spring:message code="general.yes"/></td>
					<td><input type="radio" value="false" name="${entry.key[0].uuid}"/><spring:message code="general.no"/></td>
				</tr>
			</c:forEach>
		</table>
	<br />
	<br />
</c:if>

<c:if test="${not empty sameUuidDifferentIds}">
	<b class="boxHeader"><spring:message code="eptsharmonization.relationshiptype.harmonize.sameUuidDifferentIds" /></b>
		<table class="box" cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.uuid" /></th>
				<th>PDS <spring:message code="general.id" /></th>
				<th>PDS <spring:message code="eptsharmonization.relationshiptype.harmonize.aIsToB" />
					:<spring:message code="eptsharmonization.relationshiptype.harmonize.bIsToA" /></th>
				<th>PDS <spring:message code="general.description" /></th>
				<th>MDS <spring:message code="general.id" /></th>
				<th>MDS <spring:message code="eptsharmonization.relationshiptype.harmonize.aIsToB" />
					:<spring:message code="eptsharmonization.relationshiptype.harmonize.bIsToA" /></th>
				<th>MDS <spring:message code="general.description" /></th>
				<th><spring:message code="eptsharmonization.relationshiptype.harmonize.affectedRelationships" /></th>
				<th colspan="2"><spring:message code="eptsharmonization.relationshiptype.harmonize.actionForSimilarUuid" /></th>
			</tr>
			<c:forEach var="entry" items="${sameUuidDifferentIds}">
				<tr>
					<td valign="top" align="center">${entry.key[0].uuid}</td>
					<td valign="top" align="center">${entry.key[0].id}</td>
					<td valign="top">${entry.key[0].relationshipType.aIsToB}:${entry.key[0].relationshipType.bIsToA}</td>
					<td valign="top">${entry.key[0].relationshipType.description}</td>
					<td valign="top">${entry.key[1].relationshipType.id}</td>
					<td valign="top">${entry.key[1].relationshipType.aIsToB}:${entry.key[1].relationshipType.bIsToA}</td>
					<td valign="top">${entry.key[1].relationshipType.description}</td>
					<td valign="top" align="center">${entry.value}</td>
					<td><input type="radio" value="true" name="${entry.key[0].uuid}"/><spring:message code="general.yes"/></td>
					<td><input type="radio" value="false" name="${entry.key[0].uuid}"/><spring:message code="general.no"/></td>
				</tr>
			</c:forEach>
		</table>
	<br />
	<br />
</c:if>

<table class="box" cellspacing="0" border="0" style="width: 100%">
	<tr>
		<td>
			<div class="submit-btn" align="center">
				<input id="submit-proceed" type="submit" value="<spring:message code="eptsharmonization.encountertype.btn.harmonizeNewFromMDS" />" name="proceed" />
			</div>
		</td>
	</tr>
</table>
</form>


<%@ include file="/WEB-INF/template/footer.jsp"%>