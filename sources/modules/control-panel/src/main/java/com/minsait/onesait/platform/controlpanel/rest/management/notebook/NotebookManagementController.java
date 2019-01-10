/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.notebook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.controlpanel.rest.NotebookOpsRestServices;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "Notebook Ops", tags = { "Notebook Ops service" })
@RestController
public class NotebookManagementController extends NotebookOpsRestServices {

	@Autowired
	private NotebookService notebookService;
	@Autowired
	private AppWebUtils utils;

	@ApiOperation(value = "Runs paragraph synchronously")
	@PostMapping(value = "/run/notebook/{notebookZepId}/paragraph/{paragraphId}")
	public ResponseEntity<?> runParagraph(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookZepId") String notebookZepId,
			@ApiParam(value = "Paragraph Id", required = true) @PathVariable(name = "paragraphId") String paragraphId,
			@ApiParam(value = "Input parameters") @RequestBody(required = false) String parameters) {

		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionForNotebook(notebookZepId, userId);

		if (authorized) {
			try {
				return notebookService.runParagraph(notebookZepId, paragraphId, parameters != null ? parameters : "");
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Runs all paragraphs synchronously")
	@PostMapping(value = "/run/notebook/{notebookZepId}")
	public ResponseEntity<?> runAllParagraphs(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookZepId") String notebookZepId) {

		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionForNotebook(notebookZepId, userId);

		if (authorized) {
			try {
				return notebookService.runAllParagraphs(notebookZepId);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Get the results of a paragraph")
	@GetMapping(value = "/result/notebook/{notebookZepId}/paragraph/{paragraphId}")
	public ResponseEntity<?> getParagraphResult(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookZepId") String notebookZepId,
			@ApiParam(value = "Paragraph Id", required = true) @PathVariable(name = "paragraphId") String paragraphId) {

		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionForNotebook(notebookZepId, userId);

		if (authorized) {
			try {
				return notebookService.getParagraphResult(notebookZepId, paragraphId);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Get the status of all paragraphs")
	@GetMapping(value = "/run/notebook/{notebookZepId}")
	public ResponseEntity<?> getAllParagraphStatus(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookZepId") String notebookZepId) {

		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionForNotebook(notebookZepId, userId);

		if (authorized) {
			try {
				return notebookService.getAllParagraphStatus(notebookZepId);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Clone a notebook")
	@GetMapping(value = "/run/notebook/{notebookZepId}/{nameClone}")
	public ResponseEntity<?> cloneNotebook(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookZepId") String notebookZepId,
			@ApiParam(value = "Name for the clone", required = true) @PathVariable("v") String nameClone) {

		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionForNotebook(notebookZepId, userId);

		if (authorized) {
			try {
				String id = notebookService.cloneNotebookOnlyZeppelin(nameClone, notebookZepId, userId);
				return new ResponseEntity<>(id, HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Get a paragraph information")
	@GetMapping(value = "/api/notebook/{notebookZepId}/paragraph/{paragraphId}")
	public ResponseEntity<?> getParagraphInfo(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookZepId") String notebookZepId,
			@ApiParam(value = "Paragraph Id", required = true) @PathVariable(name = "paragraphId") String paragraphId) {

		final String userId = utils.getUserId();
		final boolean authorized = notebookService.hasUserPermissionForNotebook(notebookZepId, userId);

		if (authorized) {
			try {
				return notebookService.getParagraphResult(notebookZepId, paragraphId);
			} catch (final Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}
}
