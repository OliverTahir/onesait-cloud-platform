-- api
update api set api_type = UPPER(api_type);

-- api_query_parameter
update api_query_parameter set query_header_type = UPPER(query_header_type);
update api_query_parameter set query_data_type = UPPER(query_data_type);

-- binary_file
update binary_file set repository = UPPER(repository);

-- category_relation
update category_relation set type = UPPER(type);

-- configuration 
update configuration set type = UPPER(type);

update configuration set type = 'ENDPOINT_MODULES' where type = 'ENDPOINTMODULES';
update configuration set type = 'OPEN_PLATFORM' where type = 'OPENPLATFORM';

-- data_model
update data_model set type = UPPER(type);

update data_model set type = 'SYSTEM_ONTOLOGY' where type = 'SYSTEMONTOLOGY';
update data_model set type = 'SOCIAL_MEDIA' where type = 'SOCIALMEDIA';
update data_model set type = 'SMART_CITIES' where type = 'SMARTCITIES';
update data_model set type = 'SMART_HOME' where type = 'SMARTHOME';
update data_model set type = 'SMART_ENERGY' where type = 'SMARTENERGY';
update data_model set type = 'SMART_RETAIL' where type = 'SMARTRETAIL';
update data_model set type = 'SMART_INDUSTRY' where type = 'SMARTINDUSTRY';
update data_model set type = 'FIRMWARE_DATA_MODEL' where type = 'FIRMWAREDATAMODEL';
update data_model set type = 'SYSTEM_ONTOLOGY' where type = 'SYSTEMONTOLOGY';

-- digital_twin_type
update digital_twin_type set type = UPPER(type);

-- ontology 
update ontology set rtdbclean_lapse = UPPER(rtdbclean_lapse);
update ontology set rtdb_datasource = UPPER(rtdb_datasource);
update ontology set rtdbhdb_storage = UPPER(rtdbhdb_storage);

update ontology set rtdb_datasource = 'DIGITAL_TWIN' where rtdb_datasource = 'DIGITALTWIN';
update ontology set rtdb_datasource = 'ELASTIC_SEARCH' where rtdb_datasource = 'ELASTICSEARCH';
update ontology set rtdbclean_lapse = 'TWO_DAYS' where rtdbclean_lapse = 'TWODAYS';
update ontology set rtdbclean_lapse = 'THREE_DAYS' where rtdbclean_lapse = 'THREEDAYS';
update ontology set rtdbclean_lapse = 'FIVE_DAYS' where rtdbclean_lapse = 'FIVEDAYS';
update ontology set rtdbclean_lapse = 'ONE_WEEK' where rtdbclean_lapse = 'ONEWEEK';
update ontology set rtdbclean_lapse = 'TWO_WEEKS' where rtdbclean_lapse = 'TWOWEEKS';
update ontology set rtdbclean_lapse = 'ONE_MONTH' where rtdbclean_lapse = 'ONEMONTH';
update ontology set rtdbclean_lapse = 'THREE_MONTHS' where rtdbclean_lapse = 'THREEMONTHS';
update ontology set rtdbclean_lapse = 'SIX_MONTHS' where rtdbclean_lapse = 'SIXMONTHS';
update ontology set rtdbclean_lapse = 'ONE_YEAR' where rtdbclean_lapse = 'ONEYEAR';

-- ontology_rest
update ontology_rest set security_type = UPPER(security_type);

update ontology_rest set security_type = 'API_KEY' where security_type = 'APIKEY';

-- project
update project set type = UPPER(type);

-- video_capture

update video_capture set processor = UPPER(processor);
update video_capture set protocol = UPPER(protocol);
update video_capture set state = UPPER(state);

commit;





