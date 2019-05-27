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
package com.minsait.onesait.platform.config.services.ontology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.DataModel.MainType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyRest;
import com.minsait.onesait.platform.config.model.OntologyRest.SecurityType;
import com.minsait.onesait.platform.config.model.OntologyRestHeaders;
import com.minsait.onesait.platform.config.model.OntologyRestOperation;
import com.minsait.onesait.platform.config.model.OntologyRestOperation.OperationType;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam.ParamOperationType;
import com.minsait.onesait.platform.config.model.OntologyRestSecurity;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestHeadersRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestOperationParamRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestOperationRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestSecurityRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualOntologyDBRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OntologyServiceImpl implements OntologyService {

	@Autowired
	EntityDeletionService deletionService;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyUserAccessRepository ontologyUserAccessRepository;
	@Autowired
	private OntologyUserAccessTypeRepository ontologyUserAccessTypeRepository;
	@Autowired
	private DataModelRepository dataModelRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private OntologyRestSecurityRepository ontologyRestSecurityRepo;
	@Autowired
	private OntologyRestOperationParamRepository ontologyRestOperationParamRepo;
	@Autowired
	private OntologyRestOperationRepository ontologyRestOperationRepo;
	@Autowired
	private OntologyRestRepository ontologyRestRepo;
	@Autowired
	private OntologyRestHeadersRepository ontologyRestHeadersRepo;
	@Autowired
	private VirtualOntologyDBRepository virtualRepo;
	@Autowired
	private OntologyVirtualDatasourceRepository ontologyVirtualDatasourceRepository;
	@Autowired
	private OntologyVirtualRepository ontologyvirtualRepository;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private OPResourceService resourceService;

	@Value("${onesaitplatform.ontologies.schema.ignore-case-properties:false}")
	private boolean ignoreTitleCaseCheck;

	private static final String USER_UNAUTH_STR = "The user is not authorized";
	private static final String DATOS_STR = "datos";
	private static final String PROP_STR = "properties";
	private static final String OBJ_STR = "object";
	private static final String ARRAY_STR = "array";
	private static final String FORMAT_STR = "format";
	private static final String ITEMS_STR = "items";
	private static final String TYPE_STR = "type";

	@Override
	public List<Ontology> getAllOntologies(String sessionUserId) {

		final User sessionUser = userService.getUser(sessionUserId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return ontologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			return ontologyRepository.findByUserAndOntologyUserAccessAndAllPermissions(sessionUser);
		}
	}

	@Override
	public List<Ontology> getOntologiesByUserId(String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return ontologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			return ontologyRepository.findByUserAndAccess(sessionUser);
		}

	}

	@Override
	public List<Ontology> getOntologiesByUserAndAccess(String sessionUserId, String identification,
			String description) {
		List<Ontology> ontologies;
		final User sessionUser = userService.getUser(sessionUserId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			ontologies = ontologyRepository.findByIdentificationLikeAndDescriptionLike(identification, description);
		} else {
			ontologies = ontologyRepository.findByUserAndPermissionsANDIdentificationAndDescription(sessionUser,
					identification, description);
		}
		return ontologies;

	}

	@Override
	public List<Ontology> getOntologiesWithDescriptionAndIdentification(String sessionUserId, String identification,
			String description) {
		List<Ontology> ontologies;
		final User sessionUser = userService.getUser(sessionUserId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			ontologies = ontologyRepository
					.findByIdentificationContainingAndDescriptionContainingAndActiveTrue(identification, description);
		} else {
			ontologies = ontologyRepository.findByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(
					sessionUser, identification, description);
		}
		return ontologies;
	}

	@Override
	public List<String> getAllIdentificationsByUser(String userId) {
		List<Ontology> ontologies = new ArrayList<Ontology>();
		final User user = userService.getUser(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			ontologies = ontologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			ontologies = ontologyRepository.findByUserOrderByIdentificationAsc(user);
		}

		final List<String> identifications = new ArrayList<String>();
		for (final Ontology ontology : ontologies) {
			identifications.add(ontology.getIdentification());

		}
		return identifications;
	}

	@Override
	public Ontology getOntologyById(String ontologyId, String sessionUserId) {
		final Ontology ontology = ontologyRepository.findById(ontologyId);
		final User sessionUser = userService.getUser(sessionUserId);
		if (ontology != null) {
			if (hasUserPermissionForQuery(sessionUser, ontology)) {
				return ontology;
			} else {
				throw new OntologyServiceException(USER_UNAUTH_STR);
			}
		} else {
			return null;
		}

	}

	@Override
	public OntologyRest getOntologyRestByOntologyId(Ontology ontologyId) {
		return ontologyRestRepo.findByOntologyId(ontologyId);
	}

	@Override
	public OntologyRestSecurity getOntologyRestSecurityByOntologyRest(OntologyRest ontologyRest) {
		return ontologyRestSecurityRepo.findById(ontologyRest.getSecurityId().getId());
	}

	@Override
	public OntologyRestHeaders getOntologyRestHeadersByOntologyRest(OntologyRest ontologyRest) {
		return ontologyRestHeadersRepo.findById(ontologyRest.getHeaderId().getId());
	}

	@Override
	public List<OntologyRestOperation> getOperationsByOntologyRest(OntologyRest ontologyRest) {
		return ontologyRestOperationRepo.findByOntologyRestId(ontologyRest);
	}

	@Override
	public List<OntologyRestOperationParam> getOperationsParamsByOperation(OntologyRestOperation operation) {
		return ontologyRestOperationParamRepo.findByOperationId(operation);
	}

	@Override
	public Ontology getOntologyByIdentification(String identification, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		final Ontology ontology = ontologyRepository.findByIdentification(identification);

		if (ontology != null) {
			if (hasUserPermissionForQuery(sessionUser, ontology)) {
				return ontology;
			} else {
				throw new OntologyServiceException(USER_UNAUTH_STR);
			}
		} else {
			return null;
		}
	}

	@Override
	public List<DataModel> getAllDataModels() {
		return dataModelRepository.findAll();
	}

	@Override
	public List<DataModel> getEmptyBaseDataModel() {
		return dataModelRepository.findByName("EmptyBase");
	}

	@Override
	public List<String> getAllDataModelTypes() {
		final List<MainType> types = Arrays.asList(DataModel.MainType.values());
		final List<String> typesString = new ArrayList<String>();
		for (final MainType type : types) {
			typesString.add(type.toString());
		}
		return typesString;
	}

	@Override
	public boolean hasUserPermissionForQuery(User user, Ontology ontology) {
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else if (ontology.getUser().getUserId().equals(user.getUserId())) {
			return true;
		} else if (ontology.isPublic()) {
			return true;
		} else {
			final OntologyUserAccess userAuthorization = ontologyUserAccessRepository.findByOntologyAndUser(ontology,
					user);
			if (userAuthorization != null) {
				switch (OntologyUserAccessType.Type.valueOf(userAuthorization.getOntologyUserAccessType().getName())) {
				case ALL:
				case INSERT:
				case QUERY:
					return true;
				default:
					return false;
				}
			} else {
				return resourceService.hasAccess(user.getUserId(), ontology.getId(), ResourceAccessType.VIEW);
			}
		}
	}

	@Override
	public boolean hasUserPermissionForQuery(String userId, Ontology ontology) {
		final User user = userService.getUser(userId);
		return hasUserPermissionForQuery(user, ontology);
	}

	@Override
	public boolean hasUserPermissionForQuery(String userId, String ontologyIdentificator) {
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentificator);
		return hasUserPermissionForQuery(userId, ontology);
	}

	@Override
	public boolean hasUserPermissionForInsert(User user, Ontology ontology) {
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else if (ontology.getUser().getUserId().equals(user.getUserId())) {
			return true;
		} else {
			final OntologyUserAccess userAuthorization = ontologyUserAccessRepository.findByOntologyAndUser(ontology,
					user);
			if (userAuthorization != null) {
				switch (OntologyUserAccessType.Type.valueOf(userAuthorization.getOntologyUserAccessType().getName())) {
				case ALL:
				case INSERT:
					return true;
				default:
					return false;
				}
			} else {
				return resourceService.hasAccess(user.getUserId(), ontology.getId(), ResourceAccessType.MANAGE);
			}
		}
	}

	@Override
	public boolean hasUserPermissionForInsert(String userId, String ontologyIdentificator) {
		final User user = userService.getUser(userId);
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentificator);
		return hasUserPermissionForInsert(user, ontology);
	}

	@Override
	public Map<String, String> getOntologyFields(String identification, String sessionUserId)
			throws JsonProcessingException, IOException {
		final Map<String, String> fields = new TreeMap<String, String>();
		final Ontology ontology = getOntologyByIdentification(identification, sessionUserId);
		if (ontology != null) {
			final ObjectMapper mapper = new ObjectMapper();

			JsonNode jsonNode = null;
			try {

				jsonNode = mapper.readTree(ontology.getJsonSchema());

			} catch (final Exception e) {
				if (ontology.getJsonSchema().contains("'"))
					jsonNode = mapper.readTree(ontology.getJsonSchema().replaceAll("'", "\""));
			}
			// Predefine Path to data properties
			if (!jsonNode.path(DATOS_STR).path(PROP_STR).isMissingNode())
				jsonNode = jsonNode.path(DATOS_STR).path(PROP_STR);
			else
				jsonNode = jsonNode.path(PROP_STR);

			final Iterator<String> iterator = jsonNode.fieldNames();
			String property;
			while (iterator.hasNext()) {
				property = iterator.next();

				if (jsonNode.path(property).get(TYPE_STR).asText().equals(OBJ_STR)) {
					extractSubFieldsFromJson(fields, jsonNode, property, property, false, false);
				} else if (jsonNode.path(property).get(TYPE_STR).asText().equals(ARRAY_STR)) {
					extractSubFieldsFromJson(fields, jsonNode, property, property, true, false);
				} else {
					if (jsonNode.path(property).get(FORMAT_STR) != null)
						fields.put(property, "date");
					else
						fields.put(property, jsonNode.path(property).get(TYPE_STR).asText());
				}

			}
		}
		return fields;
	}

	@Override
	public Map<String, String> getOntologyFieldsQueryTool(String identification, String sessionUserId)
			throws JsonProcessingException, IOException {
		Map<String, String> fields = new TreeMap<String, String>();
		String context = "";
		final Ontology ontology = getOntologyByIdentification(identification, sessionUserId);
		if (ontology != null) {
			final ObjectMapper mapper = new ObjectMapper();

			JsonNode jsonNode = null;
			try {

				jsonNode = mapper.readTree(ontology.getJsonSchema());

			} catch (final Exception e) {
				if (ontology.getJsonSchema().contains("'"))
					jsonNode = mapper.readTree(ontology.getJsonSchema().replaceAll("'", "\""));
			}

			// Predefine Path to data properties
			if (!jsonNode.path(DATOS_STR).path(PROP_STR).isMissingNode()) {
				context = jsonNode.path(PROP_STR).fields().next().getKey();
				jsonNode = jsonNode.path(DATOS_STR).path(PROP_STR);

			} else
				jsonNode = jsonNode.path(PROP_STR);

			final Iterator<String> iterator = jsonNode.fieldNames();
			String property;
			while (iterator.hasNext()) {
				property = iterator.next();
				if (jsonNode.path(property).toString().equals("{}")) {
					fields.put(property, OBJ_STR);
				} else if (jsonNode.path(property).get(TYPE_STR).asText().equals(OBJ_STR)) {
					fields.put(property, jsonNode.path(property).get(TYPE_STR).asText());
					extractSubFieldsFromJson(fields, jsonNode, property, property, false, true);
				} else if (jsonNode.path(property).get(TYPE_STR).asText().equals(ARRAY_STR)) {
					extractSubFieldsFromJson(fields, jsonNode, property, property, true, true);
				} else {
					if (jsonNode.path(property).get(FORMAT_STR) != null)
						fields.put(property, "date");
					else
						fields.put(property, jsonNode.path(property).get(TYPE_STR).asText());
				}

			}
		}
		// add Context to fields for query
		if (!context.equals("")) {
			final Map<String, String> fieldsForQuery = new TreeMap<String, String>();
			for (final Map.Entry<String, String> field : fields.entrySet()) {
				final String key = field.getKey();
				final String value = field.getValue();
				fieldsForQuery.put(context + "." + key, value);
			}
			fields = fieldsForQuery;
		}
		return fields;
	}

	@Override
	public void updateOntology(Ontology ontology, String sessionUserId, OntologyConfiguration config,
			boolean hasDocuments) throws OntologyDataJsonProblemException {
		if (hasDocuments) {
			ontologyDataService.checkRequiredFields(ontologyRepository.findById(ontology.getId()).getJsonSchema(),
					ontology.getJsonSchema());
		}
		updateOntology(ontology, sessionUserId, config);

	}

	@Override
	public void updateOntology(Ontology ontology, String sessionUserId, OntologyConfiguration config)
			throws OntologyDataJsonProblemException {
		final Ontology ontologyDb = ontologyRepository.findById(ontology.getId());
		final User sessionUser = userService.getUser(sessionUserId);
		final String objectId = config.getObjectId();

		if (ontologyDb != null) {
			if (hasUserPermisionForChangeOntology(sessionUser, ontologyDb)) {
				checkOntologySchema(ontology.getJsonSchema());
				if (!ignoreTitleCaseCheck)
					ontologyDataService.checkTitleCaseSchema(ontology.getJsonSchema());

				ontology.setUser(ontologyDb.getUser());

				ontology.setOntologyUserAccesses(ontologyDb.getOntologyUserAccesses());

				if (ontology.isRtdbToHdb())
					ontology.setRtdbClean(true);
				else
					ontology.setRtdbToHdbStorage(null);

				if (ontology.isRtdbClean() && ontology.getRtdbCleanLapse().equals(RtdbCleanLapse.NEVER)) {
					ontology.setRtdbCleanLapse(RtdbCleanLapse.ONE_MONTH);
				}

				ontology.setIdentification(ontologyDb.getIdentification());
				ontologyRepository.save(ontology);
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
					createRestOntology(ontologyDb, config);
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					final OntologyVirtual ontologyVirtual = ontologyvirtualRepository.findByOntologyId(ontologyDb);
					ontologyVirtual.setOntologyId(ontology);
					ontologyVirtual.setObjectId(objectId);
					ontologyvirtualRepository.save(ontologyVirtual);
				}
			} else {
				throw new OntologyServiceException(USER_UNAUTH_STR);
			}
		} else
			throw new OntologyServiceException("Ontology does not exist");
	}

	// TODO it should be checked that onotologies are assigned to the session
	// user.
	@Override
	@Transactional
	public void createOntology(Ontology ontology, OntologyConfiguration config)
			throws OntologyServiceException, OntologyDataJsonProblemException {

		if (ontologyRepository.findByIdentification(ontology.getIdentification()) == null) {
			if (ontology.isRtdbClean()
					&& (ontology.getRtdbCleanLapse() == null || ontology.getRtdbCleanLapse().getMilliseconds() == 0)) {
				ontology.setRtdbClean(false);
				ontology.setRtdbCleanLapse(RtdbCleanLapse.NEVER);
			} else if (!ontology.isRtdbClean()) {
				ontology.setRtdbCleanLapse(RtdbCleanLapse.NEVER);
			}
			if (ontology.getDataModel() != null) {
				final DataModel dataModel = dataModelRepository.findById(ontology.getDataModel().getId());
				ontology.setDataModel(dataModel);
			} else {
				final DataModel dataModel = dataModelRepository.findByName("EmptyBase").get(0);
				ontology.setDataModel(dataModel);
			}

			if (!ignoreTitleCaseCheck && !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL))
				ontologyDataService.checkTitleCaseSchema(ontology.getJsonSchema());

			final User user = userService.getUser(ontology.getUser().getUserId());
			if (user != null) {
				ontology.setUser(user);
				ontologyRepository.saveAndFlush(ontology);
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
					createRestOntology(ontology, config);
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					createVirtualOntology(ontology, config.getDatasource(), config.getObjectId());
				}
			} else {
				throw new OntologyServiceException("Invalid user");
			}
		} else {
			throw new OntologyServiceException(
					"Ontology with identification: " + ontology.getIdentification() + " exists");
		}

	}

	private void createVirtualOntology(Ontology ontology, String datasourceName, String objectId) {
		if (objectId == null || objectId == "") {
			objectId = null;
		}
		final OntologyVirtualDatasource datasource = ontologyVirtualDatasourceRepository
				.findByDatasourceName(datasourceName);

		if (datasource != null) {
			final OntologyVirtual ontologyVirtual = new OntologyVirtual();
			ontologyVirtual.setDatasourceId(datasource);
			ontologyVirtual.setOntologyId(ontology);
			ontologyVirtual.setObjectId(objectId);
			ontologyvirtualRepository.save(ontologyVirtual);
		} else {
			throw new OntologyServiceException("Datasource " + datasourceName + " not found.");
		}
	}

	private void createRestOntology(Ontology ontology, OntologyConfiguration config) {
		try {
			final OntologyRest ontologyRestUpdate = ontologyRestRepo.findByOntologyId(ontology);
			OntologyRest ontologyRest = new OntologyRest();

			Boolean isUpdate = false;
			if (ontologyRestUpdate != null) {
				isUpdate = true;
				ontologyRest = ontologyRestUpdate;
			}
			final JSONObject authJson = new JSONObject();

			Boolean isAuth = false;
			if (config.getAuthCheck() != null && config.getAuthCheck().equals("on")) {
				isAuth = true;
			}
			if (isAuth) {

				if (config.getAuthMethod() != null) {
					if (config.getAuthMethod().equalsIgnoreCase("apiKey")) {
						authJson.put("header", config.getHeader());
						authJson.put("token", config.getToken());
						ontologyRest.setSecurityType(SecurityType.API_KEY);
					} else if (config.getAuthMethod().equalsIgnoreCase("oauth")) {
						authJson.put("user", config.getOauthUser());
						authJson.put("password", config.getOauthPass());
						ontologyRest.setSecurityType(SecurityType.OAUTH);
					} else if (config.getAuthMethod().equalsIgnoreCase("basic")) {
						authJson.put("user", config.getBasicUser());
						authJson.put("password", config.getBasicPass());
						ontologyRest.setSecurityType(SecurityType.BASIC);
					}
				} else {
					ontologyRest.setSecurityType(SecurityType.NONE);
				}

			} else {
				ontologyRest.setSecurityType(SecurityType.NONE);
			}

			OntologyRestSecurity security = new OntologyRestSecurity();
			if (isUpdate) {
				security = ontologyRestUpdate.getSecurityId();
			}

			security.setConfig(authJson.toString());
			security = ontologyRestSecurityRepo.save(security);

			Boolean isInfer = false;
			if (config.getInfer() != null && config.getInfer().equals("on")) {
				isInfer = true;
			}

			OntologyRestHeaders ontologyHeaders = new OntologyRestHeaders();
			if (isUpdate) {
				ontologyHeaders = ontologyRestUpdate.getHeaderId();
			}

			if (config.getHeader() != null) {
				final JSONArray jsonHeader = new JSONArray(config.getHeaders()[0]);
				ontologyHeaders.setConfig(jsonHeader.toString());
				ontologyHeaders = ontologyRestHeadersRepo.save(ontologyHeaders);
			}

			final Set<OntologyRestOperation> operationsList = new HashSet<>();
			final Set<OntologyRestOperationParam> ParamsRestOperations = new HashSet<>();

			List<OntologyRestOperation> operationsOld = new ArrayList<OntologyRestOperation>();

			if (config.getOperations() != null) {

				final JSONArray jsonArray = new JSONArray(config.getOperations()[0]);

				for (int i = 0; i < jsonArray.length(); i++) {

					final JSONObject json = jsonArray.getJSONObject(i);
					final String name = json.getString("name");
					final String type = json.getString(TYPE_STR);
					final String description = json.getString("description");
					final String origin = json.getString("origin");

					OntologyRestOperation operation = new OntologyRestOperation();
					if (isUpdate) {
						operationsOld = ontologyRestOperationRepo.findByOntologyRestId(ontologyRest);
						operation = ontologyRestOperationRepo.findByOntologyRestIdAndName(ontologyRest, name);
						if (operation == null) {
							operation = new OntologyRestOperation();
						}
					}

					final JSONArray pathParams = json.getJSONArray("pathParams");
					final JSONArray queryParams = json.getJSONArray("queryParams");

					for (int x = 0; x < pathParams.length(); x++) {

						final JSONObject pathObj = pathParams.getJSONObject(x);
						final Integer index = pathObj.getInt("indexes");
						final String namePath = pathObj.getString("namesPaths");

						OntologyRestOperationParam operationParam = new OntologyRestOperationParam();

						if (isUpdate && operation.getId() != null) {
							operationParam = ontologyRestOperationParamRepo.findByOperationIdAndNameAndType(operation,
									namePath, ParamOperationType.PATH);
							if (operationParam == null) {
								operationParam = new OntologyRestOperationParam();
							}
						}
						operationParam.setIndexParam(index);
						operationParam.setName(namePath);
						operationParam.setType(ParamOperationType.PATH);
						operationParam.setOperationId(operation);

						ParamsRestOperations.add(operationParam);
					}

					for (int x = 0; x < queryParams.length(); x++) {

						final JSONObject queryObj = queryParams.getJSONObject(x);
						final String nameQuery = queryObj.getString("namesQueries");

						OntologyRestOperationParam operationParam = new OntologyRestOperationParam();

						if (isUpdate && operation.getId() != null) {
							operationParam = ontologyRestOperationParamRepo.findByOperationIdAndNameAndType(operation,
									nameQuery, ParamOperationType.QUERY);
							if (operationParam == null) {
								operationParam = new OntologyRestOperationParam();
							}
						}
						operationParam.setName(nameQuery);
						operationParam.setType(ParamOperationType.QUERY);
						operationParam.setOperationId(operation);

						ParamsRestOperations.add(operationParam);
					}

					operation.setName(name);
					operation.setType(OperationType.valueOf(type.toUpperCase()));
					operation.setDescription(description);
					operation.setOntologyRestId(ontologyRest);
					operation.setOrigin(origin);

					operationsList.add(operation);

				}
			}

			ontologyRest.setBaseUrl(config.getBaseUrl());
			ontologyRest.setInferOps(isInfer);
			ontologyRest.setOntologyId(ontology);
			ontologyRest.setSwaggerUrl(config.getSwagger());
			ontologyRest.setWadlUrl(config.getWadl());
			ontologyRest.setSecurityId(security);
			ontologyRest.setHeaderId(ontologyHeaders);
			if (config.getSchema() != null) {
				ontologyRest.setJsonSchema(config.getSchema());
			}
			ontologyRestRepo.save(ontologyRest);

			if (isUpdate) {

				for (final OntologyRestOperation op : operationsOld) {
					if (!operationsList.contains(op)) {
						ontologyRestOperationRepo.delete(op);
					}
				}
			}

			ontologyRestOperationRepo.save(operationsList);

			ontologyRestOperationParamRepo.save(ParamsRestOperations);

		} catch (final Exception e) {
			throw new OntologyServiceException("Problems creating the external rest ontology", e);

		}

	}

	private Map<String, String> extractSubFieldsFromJson(Map<String, String> fields, JsonNode jsonNode, String property,
			String parentField, boolean isPropertyArray, boolean addTypeObject) {
		if (isPropertyArray) {
			if (!jsonNode.path(property).path(ITEMS_STR).path(PROP_STR).isMissingNode())
				jsonNode = jsonNode.path(property).path(ITEMS_STR).path(PROP_STR);
			else if (!jsonNode.path(property).path(PROP_STR).isMissingNode()) {
				jsonNode = jsonNode.path(property).path(PROP_STR);
			} else {
				jsonNode = jsonNode.path(property).path(ITEMS_STR);
				final int size = jsonNode.size();
				try {
					for (int i = 0; i < size; i++) {
						fields.put(parentField + "." + i, jsonNode.path(i).get(TYPE_STR).asText());
					}
				} catch (final Exception e) {
					fields.put(parentField + "." + 0, jsonNode.get(TYPE_STR).asText());
				}
				return fields;

			}
		} else
			jsonNode = jsonNode.path(property).path(PROP_STR);
		final Iterator<String> iterator = jsonNode.fieldNames();
		String subProperty;
		while (iterator.hasNext()) {
			subProperty = iterator.next();

			if (jsonNode.path(subProperty).get(TYPE_STR).asText().equals(OBJ_STR)) {
				if (addTypeObject)
					fields.put(parentField + "." + subProperty, jsonNode.path(subProperty).get(TYPE_STR).asText());
				extractSubFieldsFromJson(fields, jsonNode, subProperty, parentField + "." + subProperty, false,
						addTypeObject);
			} else if (jsonNode.path(subProperty).get(TYPE_STR).asText().equals(ARRAY_STR)) {
				extractSubFieldsFromJson(fields, jsonNode, subProperty, parentField + "." + subProperty, true,
						addTypeObject);

			} else {
				if (subProperty.equals("$date"))
					fields.put(parentField, "date");
				else {
					if (jsonNode.path(subProperty).get(FORMAT_STR) != null)
						fields.put(parentField + "." + subProperty, "date");
					else
						fields.put(parentField + "." + subProperty, jsonNode.path(subProperty).get(TYPE_STR).asText());

				}
			}
		}

		return fields;

	}

	@Override
	public List<Ontology> getOntologiesByClientPlatform(ClientPlatform clientPlatform) {
		final List<Ontology> ontologies = new ArrayList<Ontology>();
		for (final ClientPlatformOntology relation : clientPlatformOntologyRepository
				.findByClientPlatform(clientPlatform)) {
			ontologies.add(relation.getOntology());
		}
		return ontologies;
	}

	@Override
	public boolean hasOntologyUsersAuthorized(String ontologyId) {
		final Ontology ontology = ontologyRepository.findById(ontologyId);
		final List<OntologyUserAccess> authorizations = ontologyUserAccessRepository.findByOntology(ontology);
		return authorizations != null && authorizations.size() > 0;
	}

	@Override
	public List<OntologyUserAccess> getOntologyUserAccesses(String ontologyId, String sessionUserId) {
		final Ontology ontology = getOntologyById(ontologyId, sessionUserId);
		final List<OntologyUserAccess> authorizations = ontologyUserAccessRepository.findByOntology(ontology);
		return authorizations;
	}

	@Override
	public void createUserAccess(String ontologyId, String userId, String typeName, String sessionUserId) {

		final Ontology ontology = ontologyRepository.findById(ontologyId);
		final User sessionUser = userService.getUser(sessionUserId);

		if (hasUserPermisionForChangeOntology(sessionUser, ontology)) {
			final List<OntologyUserAccessType> managedTypes = ontologyUserAccessTypeRepository.findByName(typeName);
			final OntologyUserAccessType managedType = managedTypes != null && managedTypes.size() > 0
					? managedTypes.get(0)
					: null;
			final User userToBeAutorized = userService.getUser(userId);
			if (ontology != null && managedType != null && userToBeAutorized != null) {
				final OntologyUserAccess ontologyUserAccess = new OntologyUserAccess();
				ontologyUserAccess.setOntology(ontology);
				ontologyUserAccess.setUser(userToBeAutorized);
				ontologyUserAccess.setOntologyUserAccessType(managedType);
				ontologyUserAccessRepository.save(ontologyUserAccess);
			} else {
				throw new OntologyServiceException("Problem creating the authorization");
			}
		} else {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
	}

	@Override
	public OntologyUserAccess getOntologyUserAccessByOntologyIdAndUserId(String ontologyId, String userId,
			String sessionUserId) {
		final Ontology ontology = getOntologyById(ontologyId, sessionUserId);
		final User user = userService.getUser(userId);
		final OntologyUserAccess userAccess = ontologyUserAccessRepository.findByOntologyAndUser(ontology, user);
		if (userAccess == null) {
			throw new OntologyServiceException("Problem obtaining user data");
		} else {
			return userAccess;
		}
	}

	@Override
	public OntologyUserAccess getOntologyUserAccessById(String userAccessId, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		final OntologyUserAccess userAccess = ontologyUserAccessRepository.findById(userAccessId);
		if (hasUserPermissionForQuery(sessionUser, userAccess.getOntology())) {
			return userAccess;
		} else {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
	}

	@Override
	@Transactional
	@Modifying
	public void deleteOntologyUserAccess(String userAccessId, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		final OntologyUserAccess userAccess = ontologyUserAccessRepository.findById(userAccessId);

		if (hasUserPermisionForChangeOntology(sessionUser, userAccess.getOntology())) {
			final Set<OntologyUserAccess> accesses = userAccess.getOntology().getOntologyUserAccesses().stream()
					.filter(a -> a.getId() != userAccess.getId()).collect(Collectors.toSet());
			final Ontology ontology = ontologyRepository.findById(userAccess.getOntology().getId());
			ontology.getOntologyUserAccesses().clear();
			ontology.getOntologyUserAccesses().addAll(accesses);
			ontologyRepository.save(ontology);
			// ontologyUserAccessRepository.delete(userAccess);

		} else {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
	}

	@Override
	public void updateOntologyUserAccess(String userAccessId, String typeName, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		final OntologyUserAccess userAccess = ontologyUserAccessRepository.findById(userAccessId);
		final List<OntologyUserAccessType> types = ontologyUserAccessTypeRepository.findByName(typeName);
		if (types != null && types.size() > 0) {
			if (hasUserPermisionForChangeOntology(sessionUser, userAccess.getOntology())) {
				final OntologyUserAccessType typeDB = types.get(0);
				userAccess.setOntologyUserAccessType(typeDB);
				ontologyUserAccessRepository.save(userAccess);
			} else {
				throw new OntologyServiceException(USER_UNAUTH_STR);
			}
		} else {
			throw new IllegalStateException("Incorrect type of access");
		}

	}

	@Override
	public boolean hasUserPermisionForChangeOntology(User user, Ontology ontology) {
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else if (ontology.getUser().getUserId().equals(user.getUserId())) {
			return true;
		} else {
			final OntologyUserAccess userAuthorization = ontologyUserAccessRepository.findByOntologyAndUser(ontology,
					user);

			if (userAuthorization != null) {
				switch (OntologyUserAccessType.Type.valueOf(userAuthorization.getOntologyUserAccessType().getName())) {
				case ALL:
					return true;
				default:
					return false;
				}
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean hasClientPlatformPermisionForInsert(String clientPlatformId, String ontologyId) {
		final ClientPlatformOntology clientPlatformOntology = clientPlatformOntologyRepository
				.findByOntologyAndClientPlatform(ontologyId, clientPlatformId);

		if (clientPlatformOntology != null) {

			switch (ClientPlatformOntology.AccessType.valueOf(clientPlatformOntology.getAccess())) {
			case ALL:
			case INSERT:
				return true;
			default:
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean hasClientPlatformPermisionForQuery(String clientPlatformId, String ontologyId) {

		final ClientPlatformOntology clientPlatformOntology = clientPlatformOntologyRepository
				.findByOntologyAndClientPlatform(ontologyId, clientPlatformId);

		if (clientPlatformOntology != null) {

			switch (ClientPlatformOntology.AccessType.valueOf(clientPlatformOntology.getAccess())) {
			case ALL:
			case INSERT:
			case QUERY:
				return true;
			default:
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean isIdValid(String ontologyId) {

		final String regExp = "^[^\\\\d].*";
		return (ontologyId.matches(regExp));
		// if (ontologyId.matches(regExp)) {
		// return false;
		// } else {
		// return true;
		// }
	}

	@Override
	public List<RtdbDatasource> getDatasources() {
		return Arrays.asList(Ontology.RtdbDatasource.values());
	}

	@Override
	public List<Ontology> getCleanableOntologies() {
		return ontologyRepository.findByRtdbCleanTrueAndRtdbCleanLapseNotNull();
	}

	@Transactional
	@Override
	public void delete(Ontology ontology) {
		ontologyRepository.deleteById(ontology.getId());
		// deletionService.deleteOntology(ontology.getId(),
		// ontology.getUser().getUserId());
	}

	@Override
	public String getRtdbFromOntology(String ontologyIdentification) {
		return ontologyRepository.findByIdentification(ontologyIdentification).getRtdbDatasource().name();
	}

	@Override
	public List<String> getTablesFromDatasource(String datasource) {
		final List<String> lTables = virtualRepo.getTables(datasource);
		return lTables;
	}

	@Override
	public List<String> getDatasourcesRelationals() {
		final List<String> datasources = ontologyVirtualDatasourceRepository
				.findIdentificationsBySgdb(OntologyVirtualDatasource.VirtualDatasourceType.ORACLE);
		return datasources;
	}

	@Override
	public String getInstance(String datasource, String collection) {

		final List<String> result = virtualRepo.getInstanceFromTable(datasource,
				"select * from " + collection + " where rownum=1");
		if (result.size() > 0) {
			return result.get(0);
		} else {
			return "";
		}
	}

	@Override
	public OntologyVirtual getOntologyVirtualByOntologyId(Ontology ontology) {
		return ontologyvirtualRepository.findByOntologyId(ontology);
	}

	@Override
	public void checkOntologySchema(String schema) throws OntologyDataJsonProblemException {
		ProcessingReport report;
		try {
			report = ontologyDataService.reportJsonSchemaValid(schema);
		} catch (final IOException e) {
			log.error("Could not parse json schema {}", e.getMessage());
			throw new OntologyDataJsonProblemException("Could not parse json schema");
		}
		if (!report.isSuccess())
			throw new OntologyDataJsonProblemException("Json schema is not valid: " + report.toString());

	}

	@Override
	public List<Ontology> getAllAuditOntologies() {
		return ontologyRepository.findByIdentificationStartingWith(ServiceUtils.AUDIT_COLLECTION_NAME);
	}

}
