<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags"%>

<form:form id="customForm" modelAttribute="serviceIdentification" method="POST" action="service_identification" class="form-horizontal">
	<input:customInput label="Title" field="title" desc="The title of the service" />
	<input:customInput label="Abstract" field="serviceAbstract" desc="A brief description of the service" />
	<input:customInput label="Service Type" field="serviceType" />
	<input:customInput label="Service Type Version" field="serviceTypeVersion" />
	<input:customInput label="Keywords" field="keywords" desc="Separated by a semicolon ';'" required="false" />
	<input:customInput label="Fees" field="fees" />
	<input:customInput label="Access Constraints" field="accessConstraints" />
	<input:customInput label="Save" type="submit" />
</form:form>
<script src="<c:url value="/static/js/custom.module.js" />"></script>