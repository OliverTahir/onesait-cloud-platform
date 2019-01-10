package com.minsait.onesait.platform.examples.iotclient4springboot.repository;

import java.util.List;

import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDynamicQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDynamicRepository;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerParam;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;
import com.minsait.onesait.platform.examples.iotclient4springboot.model.TicketOntology;

@IoTBrokerRepository
public interface TicketsRepositoryDynamicRepository {

	@IoTBrokerQuery
	List<TicketOntology> getTicketByDynamicQuery(@IoTBrokerDynamicRepository String ontology,
			@IoTBrokerDynamicQuery String query);

	@IoTBrokerQuery
	List<TicketOntology> getTicketByDynamicQuery(@IoTBrokerDynamicRepository String ontology,
			@IoTBrokerDynamicQuery String query, @IoTBrokerParam("$user") String user);

}
