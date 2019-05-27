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
package com.minsait.onesait.platform.controlpanel.services.binaryrepository;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

@RestController
public class BinaryRepositoryRestService {

	@Autowired
	private BinaryRepositoryLogicService binaryRepositoryLogicService;
	@Autowired
	private IntegrationResourcesService resourcesService;

	@PostMapping("/binary-repository")
	public ResponseEntity<?> addBinary(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata,
			@RequestParam(value = "repository", required = false) RepositoryType repository) {
		try {
			if (file.getSize() > getMaxSize().longValue())
				return new ResponseEntity<>("File is larger than max size allowed", HttpStatus.INTERNAL_SERVER_ERROR);
			final String fileId = binaryRepositoryLogicService.addBinary(file, metadata, repository);
			return new ResponseEntity<String>(fileId, HttpStatus.CREATED);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/binary-repository/{id}")
	public ResponseEntity<?> getBinary(@PathVariable("id") String fileId) {
		try {
			final BinaryFileData file = binaryRepositoryLogicService.getBinaryFile(fileId);
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new BinaryFileDTO(file));
		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@PutMapping("/binary-repository/{id}")
	public ResponseEntity<?> updateBinary(@PathVariable("id") String fileId, @RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata) throws IOException {
		try {
			binaryRepositoryLogicService.updateBinary(fileId, file, metadata);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@DeleteMapping("/binary-repository/{id}")
	public ResponseEntity<?> deleteBinary(@PathVariable("id") String fileId) {
		try {
			binaryRepositoryLogicService.removeBinary(fileId);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final BinaryRepositoryException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	private Long getMaxSize() {
		return (Long) resourcesService.getGlobalConfiguration().getEnv().getFiles().get("max-size");
	}
}
