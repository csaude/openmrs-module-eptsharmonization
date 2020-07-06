<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Visit Types"
	otherwise="/login.htm"
	redirect="/module/eptsharmonization/harmonizeLocationTags.form" />

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
                alert('<spring:message code="eptsharmonization.locationtag.harmonize.ensureConfirmation"/>');
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
	<spring:message code="eptsharmonization.locationtag.harmonize" />
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
		<form method="post" action="harmonizeEncounterTypeListExportLog.form">
			<div class="submit-btn" align="right">
				<input type="submit"
					   style="width: 8.6em; padding: 6px; font-size: 6pt;"
					   value='<spring:message code="eptsharmonization.encountertype.harmonized.viewLog"/>'
					   name="harmonizeAllLocationTags" />
			</div>
		</form>
	</div>
	<br />
</c:if>

<form id="harmonize-visit-form" method="POST" class="box" action="mandatoryLocationTagHarmonization.form">
<c:if test="${not empty missingInPDS}">
	<b class="boxHeader"><spring:message code="eptsharmonization.locationtag.harmonize.missingInPDS" /></b>
		<table cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${missingInPDS}">
				<tr>
					<td valign="top" align="center">${item.locationTag.id}</td>
					<td valign="top">${item.locationTag.name}</td>
					<td valign="top">${item.locationTag.description}</td>
					<td valign="top">${item.locationTag.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	<br />
	<br />
</c:if>

<c:if test="${not empty notInMDSNotUsed}">
	<b class="boxHeader"><spring:message code="eptsharmonization.locationtag.harmonize.notInMDSNotUsed" /></b>
		<table class="box" cellspacing="0" border="0" style="width: 100%;">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.uuid" /></th>
			</tr>
			<c:forEach var="item" items="${notInMDSNotUsed}">
				<tr>
					<td valign="top" align="center">${item.locationTag.id}</td>
					<td valign="top">${item.locationTag.name}</td>
					<td valign="top">${item.locationTag.description}</td>
					<td valign="top">${item.locationTag.uuid}</td>
				</tr>
			</c:forEach>
		</table>
	<br />
	<br />
</c:if>

<c:if test="${not empty sameIdAndUuidDifferentNames}">
	<b class="boxHeader"><spring:message code="eptsharmonization.locationtag.harmonize.sameIdAndUuidDifferentNames" /></b>
		<table class="box" cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.id" /></th>
				<th><spring:message code="general.uuid" /></th>
				<th>PDS <spring:message code="general.name" /></th>
				<th>PDS <spring:message code="general.description" /></th>
				<th>MDS <spring:message code="general.name" /></th>
				<th>MDS <spring:message code="general.description" /></th>
				<th><spring:message code="eptsharmonization.locationtag.harmonize.affectedLocations" /></th>
				<th colspan="2"><spring:message code="eptsharmonization.locationtag.harmonize.actionForSimilarUuid" /></th>
			</tr>
			<c:forEach var="entry" items="${sameIdAndUuidDifferentNames}">
				<tr>
					<td valign="top" align="center">${entry.key[0].id}</td>
					<td valign="top" align="center">${entry.key[0].uuid}</td>
					<td valign="top">${entry.key[0].locationTag.name}</td>
					<td valign="top">${entry.key[0].locationTag.description}</td>
					<td valign="top">${entry.key[1].locationTag.name}</td>
					<td valign="top">${entry.key[1].locationTag.description}</td>
					<td valign="top" align="center">${entry.value}</td>
					<td><input type="radio" value="true" name="${entry.key[0].uuid}"/><spring:message code="general.yes"/></td>
					<td><input type="radio" value="false" name="${entry.key[0].uuid}"/><spring:message code="general.no"/></td>
				</tr>
			</c:forEach>
		</table>
	<br />
	<br />
</c:if>

<c:if test="${not empty sameUuidDifferentIdsAndNames}">
	<b class="boxHeader"><spring:message code="eptsharmonization.locationtag.harmonize.sameUuidDifferentIds" /></b>
		<table class="box" cellspacing="0" border="0" style="width: 100%">
			<tr>
				<th><spring:message code="general.uuid" /></th>
				<th>PDS <spring:message code="general.id" /></th>
				<th>PDS <spring:message code="general.name" /></th>
				<th>PDS <spring:message code="general.description" /></th>
				<th>MDS <spring:message code="general.id" /></th>
				<th>MDS <spring:message code="general.name" /></th>
				<th>MDS <spring:message code="general.description" /></th>
				<th><spring:message code="eptsharmonization.locationtag.harmonize.affectedLocations" /></th>
				<th colspan="2"><spring:message code="eptsharmonization.locationtag.harmonize.actionForSimilarUuid" /></th>
			</tr>
			<c:forEach var="entry" items="${sameUuidDifferentIdsAndNames}">
				<tr>
					<td valign="top" align="center">${entry.key[0].uuid}</td>
					<td valign="top" align="center">${entry.key[0].id}</td>
					<td valign="top">${entry.key[0].locationTag.name}</td>
					<td valign="top">${entry.key[0].locationTag.description}</td>
					<td valign="top">${entry.key[1].locationTag.id}</td>
					<td valign="top">${entry.key[1].locationTag.name}</td>
					<td valign="top">${entry.key[1].locationTag.description}</td>
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