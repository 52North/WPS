/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * This program is free software; you can redistribute and/or modify it under 
 * the terms of the GNU General Public License version 2 as published by the 
 * Free Software Foundation.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.wps.webapp.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Handles URI requests and mapping for the backup & restore pages.
 */
@Controller
@RequestMapping("backup")
public class BackupController {

	@Autowired
	private ConfigurationManager configurationManager;

	private final Logger LOGGER = LoggerFactory.getLogger(BackupController.class);

	/**
	 * Display the backup page
	 * 
	 * @return the backup view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String displayBackupPage() {
		return "backup";
	}

	/**
	 * Process the backup request. The method will pass the user selection of items to backup to the service.
	 * 
	 * @param request
	 * @return the created Zip archive URL
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String processBackup(HttpServletRequest request, HttpServletResponse response) {
		String[] itemsToBackup = request.getParameterValues("backupSelections");
		String zipUrl = null;
		if (itemsToBackup == null) {
			response.setStatus(400);
			zipUrl = "Please select the items you want to backup.";
		} else {
			try {
				zipUrl = configurationManager.getBackupServices().createBackup(itemsToBackup);
			} catch (IOException e) {
				response.setStatus(500);
				zipUrl = "Backup error: Please ensure that all files are available in the specified paths.";
				LOGGER.error("Backup error:", e);
			}
		}

		return zipUrl;
	}

	/**
	 * Upload the Zip archive and pass it to the service to overwrite configurations.
	 * 
	 * @param request
	 * @param response
	 * @return {@code ValidationResponse}
	 */
	@RequestMapping(value = "restore", method = RequestMethod.POST)
	@ResponseBody
	public ValidationResponse processRestore(MultipartHttpServletRequest request, HttpServletResponse response) {

		ValidationResponse validationResponse = new ValidationResponse();
		List<FieldError> listOfErros = new ArrayList<FieldError>();
		validationResponse.setErrorMessageList(listOfErros);

		MultipartFile zipFile = null;

		if ((zipFile = request.getFile("zipFile")) != null) {
			// check file extension
			String extension = FilenameUtils.getExtension(zipFile.getOriginalFilename());
			if (zipFile.isEmpty() || !extension.equals("zip")) {
				FieldError error = new FieldError(zipFile.getOriginalFilename(), zipFile.getName(),
						"Only Zip archives are accepted.");
				listOfErros.add(error);
				response.setStatus(400);
			} else {
				try {
					configurationManager.getBackupServices().restoreBackup(zipFile.getInputStream());
				} catch (IOException | WPSConfigurationException e) {
					FieldError error = new FieldError(zipFile.getOriginalFilename(), zipFile.getName(),
							"Error in restoring backup. It's possible that the file is not a correct WPSBackup Zip archive.");
					listOfErros.add(error);
					response.setStatus(400);
					LOGGER.error("Error restoring backup:", e);
				}
			}
		} else {
			// return an error if no file is uploaded
			response.setStatus(400);
			FieldError error = new FieldError("zipFile", "zipFile", "Please select a backup zip archive.");
			listOfErros.add(error);
		}

		return validationResponse;
	}

}
