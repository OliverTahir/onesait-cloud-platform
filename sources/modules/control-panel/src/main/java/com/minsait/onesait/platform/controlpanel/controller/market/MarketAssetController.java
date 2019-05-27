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
package com.minsait.onesait.platform.controlpanel.controller.market;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.config.model.MarketAsset;
import com.minsait.onesait.platform.config.services.market.MarketAssetService;
import com.minsait.onesait.platform.controlpanel.helper.market.MarketAssetHelper;
import com.minsait.onesait.platform.controlpanel.multipart.MarketAssetMultipart;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/marketasset")
@Slf4j
public class MarketAssetController {

	@Autowired
	MarketAssetService marketAssetService;
	@Autowired
	MarketAssetHelper marketAssetHelper;
	@Autowired
	private AppWebUtils utils;

	private static final String REDIRECT_MARKETASSET_SHOW = "redirect:/marketasset/show/";
	private static final String CANNOT_UPDATE_USER = "Cannot update user that does not exist";
	private static final String API_UPDATE_ERROR = "api.update.error";

	@GetMapping(value = "/create", produces = "text/html")
	public String createForm(Model model) {

		marketAssetHelper.populateMarketAssetCreateForm(model);

		return "marketasset/create";
	}

	@GetMapping(value = "/update/{id}")
	public String updateForm(@PathVariable("id") String id, Model model) {

		try {
			marketAssetHelper.populateMarketAssetUpdateForm(model, id);
		} catch (final Exception e) {
			marketAssetHelper.populateMarketAssetShowForm(model, id);
			return "marketasset/show";
		}

		return "marketasset/create";
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(@PathVariable("id") String id, Model model) {

		marketAssetHelper.populateMarketAssetShowForm(model, id);

		return "marketasset/show";
	}

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String marketassetId) {

		marketAssetHelper.populateMarketAssetListForm(model);

		model.addAttribute("marketAssets", marketAssetHelper
				.toMarketAssetBean(marketAssetService.loadMarketAssetByFilter(marketassetId, utils.getUserId())));

		return "marketasset/list";
	}

	@PostMapping(value = "/create")
	public String create(MarketAssetMultipart marketAssetMultipart, BindingResult bindingResult,
			MultipartHttpServletRequest request, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some user properties missing");
			utils.addRedirectMessage("resource.create.error", redirect);
			return "redirect:/marketasset/create";
		}

		try {

			final String apiId = marketAssetService
					.createMarketAsset(marketAssetHelper.marketAssetMultipartMap(marketAssetMultipart));

			return REDIRECT_MARKETASSET_SHOW + apiId;
		} catch (final Exception e) {
			log.debug(CANNOT_UPDATE_USER);
			utils.addRedirectMessage("user.create.error", redirect);
			return "redirect:/marketasset/create";
		}
	}

	@PostMapping(value = "/update/{id}")
	public String update(@PathVariable("id") String id, MarketAssetMultipart marketAssetMultipart,
			MultipartHttpServletRequest request, BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage(API_UPDATE_ERROR, redirect);
			return "redirect:/marketasset/update";
		}

		try {
			marketAssetService.updateMarketAsset(id, marketAssetHelper.marketAssetMultipartMap(marketAssetMultipart));

			return REDIRECT_MARKETASSET_SHOW + id;
		} catch (final Exception e) {
			log.debug(CANNOT_UPDATE_USER);
			utils.addRedirectMessage(API_UPDATE_ERROR, redirect);
			return "redirect:/marketasset/update";
		}
	}

	@GetMapping(value = "/delete/{id}", produces = "text/html")
	public String delete(Model model, @PathVariable("id") String id) {

		marketAssetService.delete(id, utils.getUserId());

		return "redirect:/marketasset/list";
	}

	@GetMapping(value = "/rateit/{id}/{rate}", produces = "text/html")
	public String rateit(Model model, @PathVariable("id") String id, @PathVariable("rate") String rate) {

		marketAssetService.rate(id, rate, utils.getUserId());

		return REDIRECT_MARKETASSET_SHOW + id;
	}

	@PostMapping(value = "/comment")
	public String comment(HttpServletRequest request, RedirectAttributes redirect) {
		final String id = request.getParameter("marketAssetId");
		final String title = request.getParameter("commentTitle");
		final String comment = request.getParameter("comment");

		try {
			marketAssetService.createComment(id, utils.getUserId(), title, comment);

			return REDIRECT_MARKETASSET_SHOW + id;
		} catch (final Exception e) {
			log.debug(CANNOT_UPDATE_USER);
			utils.addRedirectMessage(API_UPDATE_ERROR, redirect);
			return REDIRECT_MARKETASSET_SHOW + id;
		}
	}

	@GetMapping(value = "/deletecomment/{marketassetid}/{id}", produces = "text/html")
	public String deletecomment(Model model, @PathVariable("marketassetid") String marketassetid,
			@PathVariable("id") String id) {

		marketAssetService.deleteComment(id);

		return REDIRECT_MARKETASSET_SHOW + marketassetid;
	}

	@RequestMapping(value = "/fragment/{type}")
	public String fragment(Model model, @PathVariable("type") String type) {

		marketAssetHelper.populateMarketAssetFragment(model, type);

		return "marketasset/marketassetfragments :: " + type + "MarketAssetFragment";
	}

	@RequestMapping(value = "/apiversions/{identification}")
	public String apiversions(Model model, @PathVariable("identification") String identification) {

		marketAssetHelper.populateApiVersions(model, identification);

		return "marketasset/marketassetfragments :: #versions";
	}

	@RequestMapping(value = "/apidescription")
	public @ResponseBody String apidescription(@RequestBody String apiData) {
		return (marketAssetHelper.getApiDescription(apiData));
	}

	@RequestMapping(value = "/urlwebproject")
	public @ResponseBody String urlwebproject(@RequestBody String webProjectData) {
		return (marketAssetHelper.getUrlWebProjectData(webProjectData));
	}

	@RequestMapping(value = "/validateId")
	public @ResponseBody String validateId(@RequestBody String marketAssetId) {
		return (marketAssetHelper.validateId(marketAssetId));
	}

	@RequestMapping(value = "/{id}/getImage")
	public void showImg(@PathVariable("id") String id, HttpServletResponse response) {
		final byte[] buffer = marketAssetService.getImgBytes(id);
		if (buffer.length > 0) {
			OutputStream output = null;
			try {
				output = response.getOutputStream();
				response.setContentLength(buffer.length);
				output.write(buffer);
			} catch (final Exception e) {
			} finally {
				try {
					output.close();
				} catch (final IOException e) {
				}
			}
		}
	}

	@RequestMapping(value = "/{id}/downloadContent")
	public ResponseEntity<?> download(@PathVariable("id") String id) {
		final MarketAsset asset = marketAssetService.getMarketAssetById(id);
		final ByteArrayResource resource = new ByteArrayResource(asset.getContent());
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(asset.getContentId()))
				.contentLength(resource.contentLength())
				.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);

	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/updateState/{id}/{state}")
	public @ResponseBody String updateState(@PathVariable("id") String id, @PathVariable("state") String state,
			@RequestBody String reasonData) {
		return (marketAssetService.updateState(id, state, reasonData));
	}

}
