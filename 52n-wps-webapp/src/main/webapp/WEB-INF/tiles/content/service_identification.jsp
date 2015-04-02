<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags"%>

<form:form id="customForm" modelAttribute="serviceIdentification" method="POST" action="service_identification" class="form-horizontal">
	<input:customInput label="Title" field="title" desc="The title of the service" required="false" />
	<input:customInput label="Abstract" field="serviceAbstract" desc="A brief description of the service" required="false" />
	<input:customInput label="Service Type" field="serviceType" />
	<input:customInput label="Service Type Versions" field="serviceTypeVersions" desc="Separated by a semicolon ';'" />
	<input:customInput label="Keywords" field="keywords" desc="Separated by a semicolon ';'" required="false" />
	<input:customInput label="Fees" field="fees" required="false" />
	<input:customInput label="Access Constraints" field="accessConstraints" required="false" />
	<input:customInput label="Save" type="submit" />
</form:form>
<script src="<c:url value="/static/js/custom.module.js" />"></script>