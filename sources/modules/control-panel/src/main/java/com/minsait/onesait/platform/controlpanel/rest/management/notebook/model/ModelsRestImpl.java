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
package com.minsait.onesait.platform.controlpanel.rest.management.notebook.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ModelExecution;
import com.minsait.onesait.platform.config.model.ParameterModel;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.ModelExecutionRepository;
import com.minsait.onesait.platform.config.repository.ModelRepository;
import com.minsait.onesait.platform.config.repository.ParameterModelRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.controlpanel.controller.model.ModelController;

@RestController
@EnableAutoConfiguration
public class ModelsRestImpl implements ModelsRest {

	private final static String BEARER_TOKEN = "Bearer";
	private final static String MODEL_NOT_FOUND = "Model is not found.";
	private final static String USER_NOT_FOUND = "User is not found.";
	private final static String ASLFRAME_STR = "?asIframe";
	private final static String NOTEBOOK_STR = "#/notebook/";
	private final static String PARAGRAPH_STR = "/paragraph/";

	@Autowired
	CategoryRelationRepository categoryRelationRepository;

	@Autowired
	ModelRepository modelRepository;

	@Autowired
	ModelExecutionRepository modelExecutionRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	SubcategoryRepository subcategoryRepository;

	@Autowired
	ParameterModelRepository parameterModelRepository;

	@Value("${onesaitplatform.dashboardengine.url}")
	private String dashboardUrl;

	@Value("${onesaitplatform.notebook.url}")
	private String notebookUrl;

	@Autowired
	private ModelController modelController;

	@Autowired
	private JWTService jwtService;

	@Override
	public ResponseEntity<?> getByUserAndHeaderCategoryAndSubcategory(
			@RequestHeader(value = "Authorization") String authorization,
			@RequestParam(value = "Category", required = true) String category,
			@RequestParam(value = "Subcategory", required = true) String subcategory) {

		String jwtToken;
		if (authorization.startsWith(BEARER_TOKEN)) {
			jwtToken = authorization.split(" ")[1];
		} else {
			jwtToken = authorization;
		}
		String loggedUser = jwtService.getAuthentication(jwtToken).getName();

		return this.getByUserAndCategoryAndSubcaegory(loggedUser, category, subcategory);
	}

	@Override
	public ResponseEntity<?> getByUserHeaderAndModelId(@RequestHeader(value = "Authorization") String authorization,
			@RequestParam(value = "Model name", required = true) String modelName) {

		String jwtToken;
		if (authorization.startsWith(BEARER_TOKEN)) {
			jwtToken = authorization.split(" ")[1];
		} else {
			jwtToken = authorization;
		}
		String loggedUser = jwtService.getAuthentication(jwtToken).getName();

		return this.getByUserAndModelId(loggedUser, modelName);
	}

	@Override
	public ResponseEntity<?> getByUserAndCategoryAndSubcaegory(
			@RequestParam(name = "userId", required = true) String userId,
			@RequestParam(name = "category", required = true) String category,
			@RequestParam(name = "subcategory", required = true) String subcategory) {
		try {
			User user = userRepository.findByUserId(userId);

			if (user != null) {
				List<Model> models = new ArrayList<Model>();

				if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
					models = modelRepository.findAll();
				} else {
					models = modelRepository.findByUser(user);
				}

				List<ModelDTO> modelsResult = new ArrayList<ModelDTO>();
				for (Model m : models) {
					CategoryRelation categoryRelation = categoryRelationRepository
							.findByTypeIdAndType(m.getId(), CategoryRelation.Type.MODEL).get(0);
					if (categoryRelation != null) {

						Category c = categoryRepository.findById(categoryRelation.getCategory());
						Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

						if (c != null && subc != null && category.equalsIgnoreCase(c.getIdentification())
								&& subcategory.equalsIgnoreCase(subc.getIdentification())) {
							List<ParameterModelDTO> parameterModelDTOs = new ArrayList<ParameterModelDTO>();
							List<ParameterModel> parameters = parameterModelRepository.findAllByModel(m);
							for (ParameterModel param : parameters) {
								parameterModelDTOs
										.add(new ParameterModelDTO(param.getIdentification(), param.getType().name(),
												param.getRangeFrom(), param.getRangeTo(), param.getEnumerators()));
							}

							if (m.getDashboard() != null) {
								modelsResult.add(new ModelDTO(m.getId(), m.getIdentification(), m.getDescription(),
										m.getNotebook().getIdentification(), m.getDashboard().getIdentification(),
										c.getIdentification(), subc.getIdentification(), null, m.getInputParagraphId(),
										dashboardUrl + m.getDashboard().getId(), parameterModelDTOs,
										m.getCreatedAt().toString()));
							} else {
								modelsResult.add(new ModelDTO(m.getId(), m.getIdentification(), m.getDescription(),
										m.getNotebook().getIdentification(), null, c.getIdentification(),
										subc.getIdentification(), m.getOutputParagraphId(), m.getInputParagraphId(),
										notebookUrl + NOTEBOOK_STR + m.getNotebook().getIdzep() + PARAGRAPH_STR
												+ m.getOutputParagraphId() + ASLFRAME_STR,
										parameterModelDTOs, m.getCreatedAt().toString()));
							}

						}
					}
				}
				return new ResponseEntity<>(modelsResult, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public ResponseEntity<?> getByUserAndModelId(@RequestParam(name = "userId", required = true) String userId,
			@RequestParam(name = "modelName", required = true) String modelName) {
		try {
			User user = userRepository.findByUserId(userId);

			if (user != null) {
				Model m = new Model();

				if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
					m = modelRepository.findByIdentification(modelName).get(0);
				} else {
					m = modelRepository.findByUserAndIdentification(user, modelName);
				}

				List<ModelDTO> modelsResult = new ArrayList<ModelDTO>();
				if (m != null) {
					CategoryRelation categoryRelation = categoryRelationRepository
							.findByTypeIdAndType(m.getId(), CategoryRelation.Type.MODEL).get(0);
					if (categoryRelation != null) {

						Category c = categoryRepository.findById(categoryRelation.getCategory());
						Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

						List<ParameterModelDTO> parameterModelDTOs = new ArrayList<ParameterModelDTO>();
						List<ParameterModel> parameters = parameterModelRepository.findAllByModel(m);
						for (ParameterModel param : parameters) {
							parameterModelDTOs
									.add(new ParameterModelDTO(param.getIdentification(), param.getType().name(),
											param.getRangeFrom(), param.getRangeFrom(), param.getEnumerators()));
						}

						if (m.getDashboard() != null) {
							modelsResult.add(new ModelDTO(m.getId(), m.getIdentification(), m.getDescription(),
									m.getNotebook().getIdentification(), m.getDashboard().getIdentification(),
									c.getIdentification(), subc.getIdentification(), null, m.getInputParagraphId(),
									dashboardUrl + m.getDashboard().getId(), parameterModelDTOs,
									m.getCreatedAt().toString()));
						} else {
							modelsResult.add(new ModelDTO(m.getId(), m.getIdentification(), m.getDescription(),
									m.getNotebook().getIdentification(), null, c.getIdentification(),
									subc.getIdentification(), m.getOutputParagraphId(), m.getInputParagraphId(),
									notebookUrl + NOTEBOOK_STR + m.getNotebook().getIdzep() + PARAGRAPH_STR
											+ m.getOutputParagraphId() + ASLFRAME_STR,
									parameterModelDTOs, m.getCreatedAt().toString()));
						}

					}
				} else {
					return new ResponseEntity<>(MODEL_NOT_FOUND, HttpStatus.NOT_FOUND);
				}
				return new ResponseEntity<>(modelsResult, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> executeModel(String userId, String params, String modelName) {
		try {
			User user = userRepository.findByUserId(userId);
			JSONObject jsonParams = new JSONObject(params);

			if (user != null) {

				Model model = new Model();

				if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
					model = modelRepository.findByIdentification(modelName).get(0);
				} else {
					model = modelRepository.findByUserAndIdentification(user, modelName);
				}

				if (model != null) {
					List<String> listParams = new ArrayList<String>();
					List<ParameterModel> parameters = parameterModelRepository.findAllByModel(model);

					Iterator<String> iterator = jsonParams.keys();
					JSONArray array = new JSONArray();
					while (iterator.hasNext()) {
						String paramName = iterator.next();
						String paramValue = jsonParams.getString(paramName);
						array.put(new JSONObject("{\"param\":\"" + paramName + "\",\"value\":\"" + paramValue + "\"}"));
						listParams.add(paramName);
					}

					for (ParameterModel param : parameters) {

						if (!listParams.contains(param.getIdentification())) {
							return new ResponseEntity<>("There are params missing.", HttpStatus.BAD_REQUEST);
						}
					}

					String result = modelController.execute(model.getId(), array.toString());
					JSONObject jsonResult = new JSONObject(result);
					String modelResult = jsonResult.getString("result");
					if (modelResult.equals("error")) {
						return new ResponseEntity<>("There was an error executing the model.",
								HttpStatus.INTERNAL_SERVER_ERROR);
					} else {
						return new ResponseEntity<>(result, HttpStatus.OK);
					}

				} else {
					return new ResponseEntity<>(MODEL_NOT_FOUND, HttpStatus.NOT_FOUND);
				}

			} else {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> saveExecution(String userId, String params, String modelName, String executionName,
			String executionDescription, String executionId) {
		try {
			User user = userRepository.findByUserId(userId);
			JSONObject jsonParams = new JSONObject(params);

			if (user != null) {

				Model model = new Model();

				if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
					model = modelRepository.findByIdentification(modelName).get(0);
				} else {
					model = modelRepository.findByUserAndIdentification(user, modelName);
				}

				if (model != null) {
					List<String> listParams = new ArrayList<String>();
					List<ParameterModel> parameters = parameterModelRepository.findAllByModel(model);

					Iterator<String> iterator = jsonParams.keys();
					JSONArray array = new JSONArray();
					while (iterator.hasNext()) {
						String paramName = iterator.next();
						String paramValue = jsonParams.getString(paramName);
						array.put(new JSONObject("{\"param\":\"" + paramName + "\",\"value\":\"" + paramValue + "\"}"));
						listParams.add(paramName);
					}

					for (ParameterModel param : parameters) {

						if (!listParams.contains(param.getIdentification())) {
							return new ResponseEntity<>("There are params missing.", HttpStatus.BAD_REQUEST);
						}
					}

					String result = modelController.save(model.getId(), array.toString(), executionId, executionName,
							executionDescription);

					if (result == null) {
						return new ResponseEntity<>("There was an error executing the model.",
								HttpStatus.INTERNAL_SERVER_ERROR);
					} else {
						return new ResponseEntity<>(result, HttpStatus.OK);
					}

				} else {
					return new ResponseEntity<>(MODEL_NOT_FOUND, HttpStatus.NOT_FOUND);
				}

			} else {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> getExecutions(String userId) {
		try {
			User user = userRepository.findByUserId(userId);

			if (user != null) {

				List<ModelExecution> executions = new ArrayList<ModelExecution>();

				if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
					executions = modelExecutionRepository.findByUser(user);
				} else {
					executions = modelExecutionRepository.findAll();
				}

				List<ExecutionDTO> executionsDto = new ArrayList<ExecutionDTO>();

				for (ModelExecution execution : executions) {

					CategoryRelation categoryRelation = categoryRelationRepository
							.findByTypeIdAndType(execution.getModel().getId(), CategoryRelation.Type.MODEL).get(0);
					if (categoryRelation != null) {

						Category c = categoryRepository.findById(categoryRelation.getCategory());
						Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

						ExecutionDTO executionDto = new ExecutionDTO(c.getIdentification(), subc.getIdentification(),
								execution.getIdentification(), execution.getDescription(),
								execution.getModel().getIdentification(), execution.getCreatedAt().toString());

						executionsDto.add(executionDto);
					}

				}

				return new ResponseEntity<>(executionsDto, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> showExecution(String userId, String executionName) {
		try {
			User user = userRepository.findByUserId(userId);

			if (user != null) {

				ModelExecution execution = new ModelExecution();

				if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
					execution = modelExecutionRepository.findByIdentification(executionName);
				} else {
					execution = modelExecutionRepository.findByIdentificationAndUser(executionName, user);
				}

				if (execution != null) {
					String url = null;
					if (execution.getModel().getDashboard() != null) {
						url = dashboardUrl + execution.getModel().getDashboard().getId() + "?idExecution="
								+ execution.getIdEject();

					} else if (execution.getModel().getOutputParagraphId() != null) {
						url = notebookUrl + NOTEBOOK_STR + execution.getModel().getNotebook().getIdzep()
								+ PARAGRAPH_STR + execution.getModel().getOutputParagraphId() + ASLFRAME_STR;

					}

					JSONObject json = new JSONObject();
					json.put("url", url);
					json.put("params", execution.getParameters());

					return new ResponseEntity<>(json.toString(), HttpStatus.OK);
				} else {
					return new ResponseEntity<>("Execution model not found for this user", HttpStatus.NOT_FOUND);
				}

			} else {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
