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
package com.minsait.onesait.platform.streamsets.destination;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minsait.onesait.platform.streamsets.Errors;
import com.minsait.onesait.platform.streamsets.connection.DeviceOperations;
import com.minsait.onesait.platform.streamsets.connection.ErrorResponseOriginalRecord;
import com.streamsets.pipeline.api.base.OnRecordErrorException;

public class OnesaitplatformWorker implements Callable<LinkedList<ErrorManager>> {
	
	private static final Logger logger = LoggerFactory.getLogger(OnesaitplatformWorker.class);

	private DeviceOperations deviceOperations;
	private String ontologyName;
	private List<InstancesStt> bulkData;
	
	public OnesaitplatformWorker(DeviceOperations deviceOperations, String ontologyName, List<InstancesStt> bulkData){
		this.deviceOperations=deviceOperations;
		this.ontologyName = ontologyName;
		this.bulkData = bulkData;
	}
	
	 @Override
	 public LinkedList<ErrorManager> call() {
		 LinkedList<ErrorManager> lem = new LinkedList<ErrorManager>();
		 LinkedList<ErrorResponseOriginalRecord> lerrorRecords = new LinkedList<ErrorResponseOriginalRecord>();
		 try{
			 logger.info("the number of bulk messages process by this worker is " + bulkData.size());
			 
			 for (InstancesStt data : bulkData){
				 logger.info("send insert bulk for " + data.getInsertableRest().size() + " messages");
				 lerrorRecords.addAll(this.deviceOperations.insert(data, ontologyName));
			 }
			 if(lerrorRecords.size() != 0) {
				 for(ErrorResponseOriginalRecord eror : lerrorRecords) {
					 lem.add(new ErrorManager(new OnRecordErrorException(Errors.ERROR_01, null, eror.getResponseErrorText()), eror.getOriRecord()));
				 }
			 }
		 }catch (Exception e){
			 lem.add(new ErrorManager(new OnRecordErrorException(Errors.ERROR_01, null, e.getMessage()), null));	
		 }
		 return lem;
	 }
}
