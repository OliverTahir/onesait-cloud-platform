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
package com.minsait.onesait.platform.controlpanel.services.project;

import com.minsait.onesait.platform.controlpanel.services.project.exceptions.GitlabException;

public interface GitOperations {

	public void unzipScaffolding(String directory);

	public void createDirectory(String directory) throws GitlabException;

	public void configureGitlabAndInit(String user, String email, String directory) throws GitlabException;

	public void addOrigin(String url, String directory, boolean fetchAfterOrigin) throws GitlabException;

	public void addAll(String directory) throws GitlabException;

	public void commit(String message, String directory) throws GitlabException;

	public void push(String sshUrl, String username, String password, String branch, String directory)
			throws GitlabException;

	public void sparseCheckoutAddPath(String path, String directory) throws GitlabException;

	public void sparseCheckoutConfig(String directory) throws GitlabException;

	public void checkout(String branch, String directory) throws GitlabException;

	public void deleteDirectory(String directory) throws GitlabException;

}
