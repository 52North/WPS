package org.n52.wps.webapp.web;

import java.util.List;

import org.springframework.validation.FieldError;

public class ValidationResponse {
	private String status;
	private List<FieldError> errorMessageList;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<FieldError> getErrorMessageList() {
		return this.errorMessageList;
	}

	public void setErrorMessageList(List<FieldError> errorMessageList) {
		this.errorMessageList = errorMessageList;
	}
}
