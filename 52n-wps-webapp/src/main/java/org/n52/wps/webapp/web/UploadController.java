package org.n52.wps.webapp.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class UploadController {

	private final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

	@RequestMapping(value = "upload_process", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ValidationResponse upload(MultipartHttpServletRequest request, HttpServletResponse response, Model model)
			throws WPSConfigurationException {

		ValidationResponse validationResponse = new ValidationResponse();
		List<FieldError> listOfErros = new ArrayList<FieldError>();

		MultipartFile java = null;
		MultipartFile xml = null;
		String savePath = null;

		if ((java = request.getFile("javaFile")) != null) {
			// check if the file is not empty and the format is correct
			if (!java.isEmpty() && checkFormat(java, request, "text/x-java-source")) {
				savePath = saveJava(java, request);

				// save the XML file if it exist
				if ((xml = request.getFile("xmlFile")) != null) {
					// check if the file is not empty and the format is correct
					if (!xml.isEmpty() && checkFormat(xml, request, "application/xml")) {
						saveXml(xml, savePath);
					} else {
						response.setStatus(400);
						FieldError error = new FieldError(java.getOriginalFilename(), java.getName(),
								"Only XML files are accepted.");
						listOfErros.add(error);
						validationResponse.setErrorMessageList(listOfErros);
					}
				}
			} else {
				response.setStatus(400);
				FieldError error = new FieldError(java.getOriginalFilename(), java.getName(),
						"Only Java source files are accepted.");
				listOfErros.add(error);
				validationResponse.setErrorMessageList(listOfErros);
			}
		} else {
			// return an error if no java file is uploaded
			response.setStatus(400);
			FieldError error = new FieldError("javaFile", "javaFile", "Please upload a Java source file.");
			listOfErros.add(error);
			validationResponse.setErrorMessageList(listOfErros);
		}

		return validationResponse;
	}

	private boolean checkFormat(MultipartFile file, HttpServletRequest request, String requiredMimeType) {
		String mimeType = request.getSession().getServletContext().getMimeType(file.getOriginalFilename());
		if (!mimeType.equals(requiredMimeType)) {
			return false;
		}
		return true;
	}

	private String saveJava(MultipartFile java, HttpServletRequest request) {
		StringBuilder directoryPath = new StringBuilder(request.getSession().getServletContext()
				.getRealPath("/WEB-INF/classes/uploaded"));

		File file = new File(java.getOriginalFilename());
		String packageName = null;
		try {
			FileUtils.writeByteArrayToFile(file, java.getBytes());
			List<String> lines = FileUtils.readLines(file);

			// get the package name
			for (String line : lines) {
				if (line.indexOf("package") == 0) {
					packageName = line.replace("package", "").replace(";", "").trim();
					LOGGER.debug("Pacakge name: " + packageName);
					break;
				}
			}

			// create a subdirectory for each package
			if (packageName != null && !packageName.isEmpty()) {
				String[] splitNames = packageName.split("\\.");
				for (String subdirectory : splitNames) {
					directoryPath.append("/" + subdirectory);
				}
			}

			// copy the file to the final directory
			FileUtils.copyFileToDirectory(file, new File(directoryPath.toString()));
		} catch (IOException e) {
			LOGGER.error("Unable to save java file", e);
		}
		return directoryPath.toString();
	}

	// save the xml file in the same directory as the java file
	private void saveXml(MultipartFile xml, String savePath) {
		File file = new File(savePath + "/" + xml.getOriginalFilename());
		try {
			FileUtils.writeByteArrayToFile(file, xml.getBytes());
		} catch (IOException e) {
			LOGGER.error("Unable to save XML file", e);
		}
	}
}
