/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.webapp.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Handles user configurations URI requests and mapping.
 */
@Controller
public class UsersController {
	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Display the list of all users.
	 * 
	 * @param model
	 * @return Users view
	 */
	@RequestMapping(value = "users", method = RequestMethod.GET)
	public String getUsers(Model model) {
		model.addAttribute("users", configurationManager.getUserServices().getAllUsers());
		return "users";
	}

	/**
	 * Display the change password page.
	 * 
	 * @return Change password view
	 */
	@RequestMapping(value = "change_password", method = RequestMethod.GET)
	public String getChangePasswordForm() {
		return "change_password";
	}

	/**
	 * Process password change request. The method will decode the password and check with the user's supplied current
	 * password before changing the password.
	 * 
	 * @param model
	 * @param principal
	 * @param currentPassword
	 * @param newPassword
	 * @return change password view if there is an error, or homepage if the change is successful.
	 */
	@RequestMapping(value = "change_password", method = RequestMethod.POST)
	public String processChangePasswordForm(Model model, Principal principal,
			@RequestParam("currentPassword") String currentPassword, @RequestParam("newPassword") String newPassword) {
		User user = configurationManager.getUserServices().getUser(principal.getName());
		if (passwordEncoder.matches(currentPassword, user.getPassword())) {
			if (newPassword.trim().isEmpty()) {
				model.addAttribute("newPasswordError", "New password cannot be empty.");
				return "change_password";
			}
			user.setPassword(passwordEncoder.encode(newPassword));
			configurationManager.getUserServices().updateUser(user);
			return "redirect:/";
		} else {
			model.addAttribute("error", "Current password does not match.");
			return "change_password";
		}
	}

	/**
	 * Display user edit page.
	 * 
	 * @param model
	 * @param userId
	 *            The id of the user to be edited
	 * @return
	 */
	@RequestMapping(value = "users/{userId}/edit", method = RequestMethod.GET)
	public String getEditUserForm(Model model, @PathVariable("userId") int userId) {
		model.addAttribute("user", configurationManager.getUserServices().getUser(userId));
		return "edit_user";
	}

	/**
	 * Process user edit request.
	 * 
	 * @param user
	 *            The user to be edited
	 * @return The users view.
	 */
	@RequestMapping(value = "users/{userId}/edit", method = RequestMethod.POST)
	public String processEditUserForm(User user) {
		configurationManager.getUserServices().updateUser(user);
		return "redirect:/users";
	}

	/**
	 * Process delete user request.
	 * 
	 * @param userId
	 *            The id of the user to be deleted
	 */
	@RequestMapping(value = "users/{userId}/delete", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteUser(@PathVariable("userId") int userId) {
		configurationManager.getUserServices().deleteUser(userId);
	}

	/**
	 * Display the add user form.
	 * 
	 * @param model
	 * @return Add user view
	 */
	@RequestMapping(value = "users/add_user", method = RequestMethod.GET)
	public String getAddUserForm(Model model) {
		model.addAttribute("user", new User());
		return "add_user";
	}

	/**
	 * Process add user form submission. The method will return an HTTP 200 status code if there are no errors, else, it
	 * will return a 400 status code.
	 * 
	 * @param user
	 *            The user to be added
	 * @param result
	 * @param model
	 * @param response
	 * @return A {@code ValidationResponse} object which contains the list of errors, if any.
	 */
	@RequestMapping(value = "users/add_user", method = RequestMethod.POST)
	@ResponseBody
	public ValidationResponse processAddUserForm(@ModelAttribute("user") @Valid User user, BindingResult result,
			Model model, HttpServletResponse response) {
		ValidationResponse validationResponse = new ValidationResponse();
		if (result.hasErrors()) {
			validationResponse.setErrorMessageList(result.getFieldErrors());
			response.setStatus(400);
		} else {
			String shaPassword = passwordEncoder.encode(user.getPassword());
			user.setPassword(shaPassword);
			configurationManager.getUserServices().insertUser(user);
		}
		return validationResponse;
	}

	/**
	 * Handles {@code DuplicateKeyException} which is thrown when the username already exists when adding a new user.
	 * The method returns a 400 status code along with a JSON object containing the error message.
	 * 
	 * @param e
	 *            The DuplicateKeyException
	 * @return A {@code ValidationResponse} object containing the error .
	 */
	@ExceptionHandler(DuplicateKeyException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ValidationResponse hanleException(DuplicateKeyException e) {
		ValidationResponse validationResponse = new ValidationResponse();
		List<FieldError> listOfErros = new ArrayList<FieldError>();
		FieldError error = new FieldError("User", "username",
				"Username already exist. Please choose a different username.");
		listOfErros.add(error);
		validationResponse.setErrorMessageList(listOfErros);
		return validationResponse;
	}
}
