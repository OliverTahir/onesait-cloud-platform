/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.controlpanel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Controller
public class DefaultController {

	@Autowired
	private AppWebUtils utils;

	@GetMapping("/")
	public String base() {
		if (utils.isAuthenticated()) {
			if (utils.isUser()) {
				return "redirect:/marketasset/list";
			} else if (utils.isDataViewer()) {
				return "redirect:/dashboards/viewerlist";
			}
			return "redirect:/main";
		}
		return "redirect:/login";
	}

	@GetMapping("/home")
	public String home() {
		return "home";
	}

	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("users", new User());
		return "login";
	}

	@GetMapping("/error")
	public String error() {
		return "error/403";
	}

	@GetMapping("/403")
	public String error403(Model model) {
		model.addAttribute("users", new User());
		return "login";
	}

	@GetMapping("/404")
	public String error404() {
		return "error/404";
	}

}
