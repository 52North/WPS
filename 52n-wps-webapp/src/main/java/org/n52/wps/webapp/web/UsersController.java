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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UsersController {
	@Autowired
	ConfigurationManager configurationManager;
	
	@Autowired
	PasswordEncoder passwordEncoder;

	@RequestMapping(value = "users", method = RequestMethod.GET)
	public String getUsers(Model model) {
		model.addAttribute("users", configurationManager.getUserServices().getAllUsers());
		return "users";
	}

	@RequestMapping(value = "change_password", method = RequestMethod.GET)
	public String getChangePasswordForm() {
		return "change_password";
	}

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

	@RequestMapping(value = "users/{userId}/edit", method = RequestMethod.GET)
	public String getEditUserForm(Model model, @PathVariable("userId") int userId) {
		model.addAttribute("user", configurationManager.getUserServices().getUser(userId));
		return "edit_user";
	}

	@RequestMapping(value = "users/{userId}/edit", method = RequestMethod.POST)
	public String processEditUserForm(User user) {
		configurationManager.getUserServices().updateUser(user);
		return "redirect:/users";
	}

	@RequestMapping(value = "users/{userId}/delete", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteUser(@PathVariable("userId") int userId) {
		configurationManager.getUserServices().deleteUser(userId);
	}

	@RequestMapping(value = "users/add_user", method = RequestMethod.GET)
	public String getAddUserForm(Model model) {
		model.addAttribute("user", new User());
		return "add_user";
	}

	@RequestMapping(value = "users/add_user", method = RequestMethod.POST)
	public String processAddUserForm(@ModelAttribute("user") @Valid User user, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("user", user);
			return "add_user";
		} else {
			String shaPassword = passwordEncoder.encode(user.getPassword());
			user.setPassword(shaPassword);
			configurationManager.getUserServices().insertUser(user);
			return "redirect:/users";
		}
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public ModelAndView hanleException(DuplicateKeyException e) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", "Username already exist. Please choose a different username.");
		model.put("user", new User());
		return new ModelAndView("add_user", model);
	}
}
