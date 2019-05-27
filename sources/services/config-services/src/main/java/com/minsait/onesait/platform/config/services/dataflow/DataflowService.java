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
package com.minsait.onesait.platform.config.services.dataflow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.minsait.onesait.platform.config.model.Pipeline;

public interface DataflowService {

	//public Notebook saveDBNotebook(String name, String idzep, User user);

	public Pipeline createPipeline(String name, String description, String userId)
			throws UnsupportedEncodingException;

	//public Notebook importNotebook(String name, String data, String userId);

	//public Notebook cloneNotebook(String name, String idzep, String userId);

	//public ResponseEntity<byte[]> exportNotebook(String id, String ususerIder);

	public void removePipeline(String id, String userId);

	//public String loginOrGetWSToken();

	//public String loginOrGetWSTokenAdmin();

	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, String body, String user)
			throws URISyntaxException, ClientProtocolException, IOException;

	public ResponseEntity<byte[]> sendHttpBinary(HttpServletRequest requestServlet, HttpMethod httpMethod, String body, String user)
			throws URISyntaxException, ClientProtocolException, IOException;
	
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, String user)
			throws URISyntaxException, ClientProtocolException, IOException;

	public ResponseEntity<byte[]> sendHttpBinary(String url, HttpMethod httpMethod, String body, String user)
			throws URISyntaxException, ClientProtocolException, IOException;
	
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, HttpHeaders headers, String user)
			throws URISyntaxException, ClientProtocolException, IOException;

	public ResponseEntity<byte[]> sendHttpBinary(String url, HttpMethod httpMethod, String body, HttpHeaders headers, String user)
			throws URISyntaxException, ClientProtocolException, IOException;
	
	public Pipeline getPipeline(String identification, String userId);

	public List<Pipeline> getPipelines(String userId);

	public boolean hasUserPermissionForPipeline(String pipelineId, String userId);

	//public ResponseEntity<String> runParagraph(String zeppelinId, String paragraphId, String bodyParams)
	//		throws ClientProtocolException, URISyntaxException, IOException;

	//public ResponseEntity<String> runAllParagraphs(String zeppelinId)
	//		throws ClientProtocolException, URISyntaxException, IOException;

	//public ResponseEntity<String> getParagraphResult(String zeppelinId, String paragraphId)
	//		throws ClientProtocolException, URISyntaxException, IOException;
}
