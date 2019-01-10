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
/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2014  SPAIN
 * 
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.streamsets.destination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minsait.onesait.platform.streamsets.Errors;
import com.minsait.onesait.platform.streamsets.GroupsOnesaitplatform;
import com.minsait.onesait.platform.streamsets.connection.DeviceOperations;
import com.minsait.onesait.platform.streamsets.connection.DeviceOperationsREST;
import com.minsait.onesait.platform.streamsets.destination.beans.OntologyProcessInstance;
import com.minsait.onesait.platform.streamsets.destination.beans.TimeseriesConfig;
import com.minsait.onesait.platform.streamsets.destination.beans.TimeseriesTime;
import com.minsait.onesait.platform.streamsets.destination.ontology.OnesaitplatformOntology;
import com.streamsets.pipeline.api.Batch;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseTarget;
import com.streamsets.pipeline.api.impl.Utils;

/**
 * This target is an example and does not actually write to any destination.
 */
public abstract class OnesaitplatformTarget extends BaseTarget {
	
	private static final Logger logger = LoggerFactory.getLogger(OnesaitplatformTarget.class);

	/**
	 * Gives access to the UI configuration of the stage provided by the
	 * {@link OnesaitplatformDTarget} class.
	 */
	public abstract String getProtocol();
	public abstract String getHost();
	public abstract Integer getPort();
	public abstract String getToken();
	public abstract String getDevice();
	public abstract String getOntology();
	public abstract OntologyProcessInstance getOntologyProcessInstance();
	public abstract String getCustomRootNode();
	public abstract Integer getBulk();
	//public abstract Boolean getCreateOntology();
	public abstract Integer getThread();
	public abstract Boolean getTimeseriesOntology();
	public abstract Boolean getTimeseriesMultiupdate();
	public abstract String getTimeseriesFieldOntology();
	public abstract TimeseriesTime getTimeseriesTimeOntology();
	public abstract String getValueTimeseriesField();
	public abstract Boolean getPrecalcSumTimeseries();
	public abstract Boolean getPrecalcCountTimeseries();
	public abstract String getPrecalcSumTimeseriesField();
	public abstract String getPrecalcCountTimeseriesField();
	public abstract LinkedList<String> getUpdateFields();
	public abstract String getOriginTimeseriesValueField();
	public abstract String getDestinationTimeseriesValueField();
	private List<DeviceOperations> deviceOperations;
	private ExecutorService executor;
	private TimeseriesConfig tsConfig;

	/** {@inheritDoc} */
	@Override
	protected List<ConfigIssue> init() {
		// Validate configuration values and open any required resources.
		List<ConfigIssue> issues = super.init();
		
		if (getProtocol().equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMD.name(), "config", Errors.ERROR_00,
					"Protocol required"));
		}

		if (getHost().equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMD.name(), "config", Errors.ERROR_00,
					"Host required"));
		}
		if (getPort().equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMD.name(), "config", Errors.ERROR_00,
					"Port required"));
		}
		if (getToken().equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMD.name(), "config", Errors.ERROR_00,
					"Token required"));
		}
		if (getDevice().equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMD.name(), "config", Errors.ERROR_00,
					"Device required"));
		}
		if (getOntology().equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMD.name(), "config", Errors.ERROR_00,
					"Ontology required"));
		}

		if (getBulk() < 1){
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMD.name(), "config", Errors.ERROR_00,
					"Bulk must be greater than 0"));
		}
		
		if (getThread() < 1) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMD.name(), "config", Errors.ERROR_00,
					"ThreadPool must be greater than 0"));
		}
		
		this.deviceOperations = new ArrayList<>();
		
		for (int i=0 ; i< getThread(); i++){
			logger.info("adding device operation ");
			try{
				if(getTimeseriesOntology()) {
					this.tsConfig = new TimeseriesConfig(getTimeseriesTimeOntology(), getTimeseriesFieldOntology(), getValueTimeseriesField(), getPrecalcSumTimeseries()?getPrecalcSumTimeseriesField():null, getPrecalcCountTimeseries()?getPrecalcCountTimeseriesField():null, getUpdateFields(), getOriginTimeseriesValueField(), getDestinationTimeseriesValueField());
				}
				else {
					this.tsConfig = null;
				}
				this.deviceOperations.add(new DeviceOperationsREST(getProtocol(), getHost(), getPort(), getToken(), getDevice(), this.tsConfig, this.getCustomRootNode(), this.getOntologyProcessInstance()));
			}catch (Exception e) {
				logger.error("error init rest operation ", e);
			}
		}
		
		logger.info("---------------------- init --------------------------");
		
		logger.info("the num of device operation is " + this.deviceOperations.size());
		logger.info("------------------------------------------------");
		
		// If issues is not empty, the UI will inform the user of each
		// configuration issue in the list.
		
		return issues;
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		// Clean up any open resources.
		super.destroy();
		
		logger.info("num of device operations " + this.deviceOperations.size());
		for (DeviceOperations deviceOperation : this.deviceOperations){
			try {
				logger.info("leave device operation");
				deviceOperation.leave();
			} catch (Exception e) {
				logger.error("Error leave ", e);
			}
		}
		
	}

	/** {@inheritDoc} */
	@Override
	public void write(Batch batch) throws StageException {	
		try {
			
			Iterator<Record> batchIterator = batch.getRecords();
			
			List<InstancesStt> recordsPerBulk = separateRecordsInBulkMessages (batchIterator);
			
			if (!recordsPerBulk.isEmpty() && !recordsPerBulk.get(0).getInsertableRest().isEmpty()) {
				
				Map<Integer, List<InstancesStt>> treadData = separateBulkMessagesForThreads(recordsPerBulk);
				
				executeThreads(treadData);
				
			} else {
				logger.debug ("we don´t need to create a worker because the bulk data is empty");
			}
			
		} finally {
			try {
				executor.shutdown();
			}catch (Exception e){
				logger.error("Error shutdown executor ", e);
			}	
		}
	}
	
	private List<InstancesStt> separateRecordsInBulkMessages (Iterator<Record> batchIterator) {
		
		logger.debug("start separateRecordsInBulkMessages");
		
		List<InstancesStt> recordsPerBulk = new LinkedList<>();
		int bulkSize = getBulk();
		int index = 0;
		int itemsInBulk = 0;
		
		if(!getTimeseriesOntology()) {
			recordsPerBulk.add(new InstancesStt(new LinkedList<String>(), new LinkedList<LinkedList<Record>>() ));
			generateInstancesFromRecords(batchIterator,bulkSize,index, itemsInBulk,recordsPerBulk);
		}
		else {
			recordsPerBulk.add(new InstancesStt(new LinkedList<String>(), new LinkedList<LinkedList<Record>>()) );
			if(!getTimeseriesMultiupdate()) {
				generateUpdatesFromRecords(batchIterator,bulkSize,index, itemsInBulk,recordsPerBulk);
			}
			else {
				generateMultiUpdatesFromRecords(batchIterator,bulkSize,index, itemsInBulk,recordsPerBulk);
			}
		}
		logger.debug("the records has been separated into "+ recordsPerBulk.size() + " bulks");
		
		return recordsPerBulk;
	}
	
	private void generateInstancesFromRecords(Iterator<Record> batchIterator, int bulkSize, int index, int itemsInBulk, List<InstancesStt> recordsPerBulk) {
		while (batchIterator.hasNext()) {
			
			InstancesStt recordsInBulkPartition = recordsPerBulk.get(index);
			
			Record record = batchIterator.next();
			
			try {
				String solvedRootNode;
				switch (getOntologyProcessInstance()) {
					case CUSTOMNAME:
						solvedRootNode = getCustomRootNode();
						break;
					case ONTOLOGYNAME:
						solvedRootNode = getOntology();
						break;
					default:
						solvedRootNode = null;
						break;
				}
				recordsInBulkPartition.getInsertableRest().add(OnesaitplatformOntology.constructOntologyInstance(record, getOntologyProcessInstance(), getOntology(), solvedRootNode));
				LinkedList<Record> lrd = new LinkedList<Record>();
				lrd.add(record);
				recordsInBulkPartition.getOriginalValues().add(lrd);
			} catch (Exception e) {
				logger.error("parsing", e);
			}
			
			itemsInBulk++;
			
			if (itemsInBulk >= bulkSize){
				index++;
				recordsPerBulk.add(new InstancesStt(new LinkedList<String>(), new LinkedList<LinkedList<Record>>()) ); 
				itemsInBulk = 0;
			}
			
		}
	}
	
	private void generateUpdatesFromRecords(Iterator<Record> batchIterator, int bulkSize, int index, int itemsInBulk, List<InstancesStt> recordsPerBulk) {
		while (batchIterator.hasNext()) {
			
			InstancesStt recordsInBulkPartition = recordsPerBulk.get(index);
			
			Record record = batchIterator.next();
			
			try {
				recordsInBulkPartition.getInsertableRest().add(OnesaitplatformOntology.constructUpdate(record, getOntologyProcessInstance(), getOntology(), getCustomRootNode(), this.tsConfig));
				LinkedList<Record> lrd = new LinkedList<Record>();
				lrd.add(record);
				recordsInBulkPartition.getOriginalValues().add(lrd);
			} catch (Exception e) {
				logger.error("parsing", e);
			}
			
			itemsInBulk++;
			
			if (itemsInBulk >= bulkSize){
				index++;
				recordsPerBulk.add(new InstancesStt(new LinkedList<String>(), new LinkedList<LinkedList<Record>>()) ); 
				itemsInBulk = 0;
			}
			
		}
	}
	
	private static DateTimeFormatter isoDateDayFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'00:00:00.000'Z'");
	
	private static String formatToIsoDateDay(DateTime date) {
		return isoDateDayFormatter.print(date);
	}
	
	private List<Object> generateKeyOfGroup(Record record, TimeseriesConfig tsConfig) {
		LinkedList<Object> l = new LinkedList<Object>();
		
		Map<String, Field> fieldvalueNew = (Map<String, Field>) record.get().getValue();
		
		DateTime dtNew = new DateTime(fieldvalueNew.get(tsConfig.getTsTimeField()).getValueAsDatetime());
		l.add(formatToIsoDateDay(dtNew));
		
		
		Iterator<String> updIterator = tsConfig.getUpdateFields().iterator();
		while(updIterator.hasNext()) {
			l.add(fieldvalueNew.get(updIterator.next()));
		}		
		return l;
	}
	
	private void generateMultiUpdatesFromRecords(Iterator<Record> batchIterator, int bulkSize, int index, int itemsInBulk, List<InstancesStt> recordsPerBulk) {
		Map<List<Object>,LinkedList<Record>> mapRecordAgg = new HashMap<List<Object>,LinkedList<Record>>();
		
		InstancesStt recordsInBulkPartition = recordsPerBulk.get(index);
		
		int oriRecords  = 0;
		
		while (batchIterator.hasNext()) {
			Record record = batchIterator.next();			
			List<Object> lkey = generateKeyOfGroup(record, tsConfig);
			
			if(mapRecordAgg.containsKey(lkey)) {
				mapRecordAgg.get(lkey).add(record);
			}
			else {
				LinkedList<Record> lrd = new LinkedList<Record>();
				lrd.add(record);
				mapRecordAgg.put(lkey,lrd);
			}
			oriRecords++;
		}
		
		int compressRecords = 0;
		
		for (LinkedList<Record> lrd : mapRecordAgg.values()) {
			try {
				recordsInBulkPartition.getInsertableRest().add(OnesaitplatformOntology.constructMultiUpdate(lrd, getOntologyProcessInstance(), getOntology(), getCustomRootNode(), this.tsConfig));
				recordsInBulkPartition.getOriginalValues().add(lrd);
			} catch (Exception e) {
				logger.error("parsing", e);
			}
			itemsInBulk++;
			
			if (itemsInBulk >= bulkSize){
				index++;
				recordsPerBulk.add(new InstancesStt(new LinkedList<String>(), new LinkedList<LinkedList<Record>>()) );
				recordsInBulkPartition = recordsPerBulk.get(index);
				itemsInBulk = 0;
			}
			compressRecords++;
		}
		
		logger.info("Optimized timeseries update: " + oriRecords + " into " + compressRecords);
	}
	
	private Map<Integer, List<InstancesStt>> separateBulkMessagesForThreads (List<InstancesStt> recordsPerBulk) {
		
		Map<Integer, List<InstancesStt>> treadData = new HashMap<>();
		
		logger.debug("distributing bulk messages");
		
		int threadId = 0;
		for (InstancesStt recordBulk : recordsPerBulk){
			
			if (treadData.get(threadId) == null){
				treadData.put(threadId, new ArrayList<InstancesStt>());
			}
			
			treadData.get(threadId).add(recordBulk);
			
			threadId++;
			
			if (threadId == deviceOperations.size()){
				threadId = 0;
			}
		}	
		
		logger.debug("the bulk messages has been distributed");
		
		return treadData;
	}
	
	private void executeThreads (Map<Integer, List<InstancesStt>> treadData) {
		
		logger.debug("the num of threads to execute is " + getThread());
		
		Set<Future<LinkedList<ErrorManager>>> set = new HashSet<>();
		
		executor = Executors.newFixedThreadPool(getThread());
		
		for (int i = 0; i < getThread() ; i++){		
				logger.debug("create worker " + i);
				Callable<LinkedList<ErrorManager>> worker = new OnesaitplatformWorker(deviceOperations.get(i), getOntology(), treadData.get(i));
				set.add(executor.submit(worker));
		}
		
		logger.debug("threads has been submitted");
		
		for (Future<LinkedList<ErrorManager>> future : set) {
			try{
				List<ErrorManager> errors= future.get();
				logger.error("Found " + errors.size() + " errors in thread operation");
				for(ErrorManager error : errors) {
					try {
						error.getException();
					} catch (Exception e) {
						switch (getContext().getOnErrorRecord()) {
						case DISCARD:
							break;
						case TO_ERROR:
							getContext().toError(error.getRecord(), Errors.ERROR_01, e.toString());
							break;
						case STOP_PIPELINE:
							throw new StageException(Errors.ERROR_01, e.toString());
						default:
							throw new IllegalStateException(Utils.format("Error unknown inserting '{}'",
									getContext().getOnErrorRecord(), e));
						}
					}
				}
			}catch (Exception e){
				logger.error("Error writting in thread target ", e);
			}
		 }
	}
}