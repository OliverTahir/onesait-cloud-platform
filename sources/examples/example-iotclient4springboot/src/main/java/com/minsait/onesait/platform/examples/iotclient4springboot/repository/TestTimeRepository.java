package com.minsait.onesait.platform.examples.iotclient4springboot.repository;

import java.util.List;

import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDynamicQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.examples.iotclient4springboot.model.TestTimeOntology;

@IoTBrokerRepository("TestTime")
public interface TestTimeRepository {

	@IoTBrokerQuery
	List<TestTimeOntology> getTestTimeByDynamicQuery(@IoTBrokerDynamicQuery String query);

	@IoTBrokerQuery(queryType = SSAPQueryType.NATIVE)
	List<TestTimeOntology> getTestTimeByDynamicQueryNative(@IoTBrokerDynamicQuery String query);
}
