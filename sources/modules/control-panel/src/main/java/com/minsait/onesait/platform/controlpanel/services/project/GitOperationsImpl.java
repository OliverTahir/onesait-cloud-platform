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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.controlpanel.services.project.exceptions.GitlabException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitOperationsImpl implements GitOperations {

	private final static String RESOURCE_PATH = "static/gitlab/scaffolding-sb-vue.zip";
	private final static String CONFIG_STR = "config";
	private final static String COULD_NOT_PUSH_TO_ORIGIN_MSSG = "Could not push to origin {}";
	private final static String COULD_NOT_PUSH_TO_ORIGIN = "Could not push to origin";
	

	@Override
	public void createDirectory(String directory) throws GitlabException {
		try {
			final ProcessBuilder pb = new ProcessBuilder("mkdir", directory);
			pb.redirectErrorStream(true);
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error("Could not create directory {}", e.getMessage());
			throw new GitlabException("Could not create directory", e);
		}

	}

	@Override
	public void configureGitlabAndInit(String user, String email, String directory) throws GitlabException {
		try {

			ProcessBuilder pb = new ProcessBuilder("git", "init");
			pb.redirectErrorStream(true);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
			pb = new ProcessBuilder("git", CONFIG_STR, "user.name", user);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
			pb = new ProcessBuilder("git", CONFIG_STR, "user.email", email);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
			pb = new ProcessBuilder("git", CONFIG_STR, "http.sslVerify", "false");
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());

		} catch (IOException | InterruptedException e) {
			log.error("Could not config git {}", e.getMessage());
			throw new GitlabException("Could not config git", e);
		}

	}

	@Override
	public void addOrigin(String url, String directory, boolean fetchAfterOrigin) throws GitlabException {

		try {
			final ProcessBuilder pb;
			if (!fetchAfterOrigin)
				pb = new ProcessBuilder("git", "remote", "add", "origin", url);
			else
				pb = new ProcessBuilder("git", "remote", "add", "-f", "origin", url);
			pb.redirectErrorStream(true);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error("Could not add git origin {}", e.getMessage());
			throw new GitlabException("Could not add git origin", e);
		}

	}

	@Override
	public void addAll(String directory) throws GitlabException {
		try {
			final ProcessBuilder pb = new ProcessBuilder("git", "add", ".");
			pb.redirectErrorStream(true);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error("Could not add file for commit {}", e.getMessage());
			throw new GitlabException("Could not add file for commit", e);
		}
	}

	@Override
	public void commit(String message, String directory) throws GitlabException {
		try {
			final ProcessBuilder pb = new ProcessBuilder("git", "commit", "-m", message);
			pb.redirectErrorStream(true);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error("Could not commit {}", e.getMessage());
			throw new GitlabException("Could not commit", e);
		}

	}

	@Override
	public void push(String sshUrl, String username, String password, String branch, String directory)
			throws GitlabException {
		try {
			final String url = sshUrl.replace("https://", "https://" + username + ":" + password + "@");
			final ProcessBuilder pb = new ProcessBuilder("git", "push", "-u", url, branch);
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitlabException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	private String executeAndReadOutput(Process p) throws IOException, InterruptedException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		final StringBuilder builder = new StringBuilder();
		String line = null;
		p.waitFor();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		return builder.toString();
	}

	@Override
	public void unzipScaffolding(String directory) {

		// String filename =
		// Paths.get(getClass().getClassLoader().getResource(RESOURCE_PATH).toURI()).toFile().getAbsolutePath();
		final ClassPathResource cpr = new ClassPathResource(RESOURCE_PATH);

		final File targetFile = new File(directory + File.separator + "zipfile.zip");
		log.info("Getting zip file containing project scafolding: " + targetFile.getAbsolutePath());

		try (final ZipFile zipFile = new ZipFile(targetFile)) {

			FileUtils.copyInputStreamToFile(cpr.getInputStream(), targetFile);
			final Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				final ZipEntry zipEntry = (ZipEntry) enu.nextElement();

				final String name = zipEntry.getName();

				// Do we need to create a directory ?
				final File file = new File(directory + File.separator + name);
				if (name.endsWith("/")) {
					file.mkdirs();
					continue;
				}

				final File parent = file.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}

				// Extract the file
				final InputStream is = zipFile.getInputStream(zipEntry);

				try (final FileOutputStream fos = new FileOutputStream(file)) {
					final byte[] bytes = new byte[1024];
					int length;
					while ((length = is.read(bytes)) >= 0) {
						fos.write(bytes, 0, length);
					}
					is.close();
				} catch (Exception e) {
					log.error("Could not open FileOutputStream: ", e);
				}

			}
			targetFile.delete();

		} catch (final Exception e) {
			log.error("Could not unzip scaffolding project: ", e);
		}

	}

	@Override
	public void sparseCheckoutAddPath(String path, String directory) throws GitlabException {
		try {
			final ProcessBuilder pb = new ProcessBuilder("echo", "'" + path + "' >> .git/info/sparse-checkout");
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitlabException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	@Override
	public void sparseCheckoutConfig(String directory) throws GitlabException {
		try {
			final ProcessBuilder pb = new ProcessBuilder("git", CONFIG_STR, "core.sparseCheckout", "true");
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitlabException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	@Override
	public void checkout(String branch, String directory) throws GitlabException {
		try {

			final ProcessBuilder pb = new ProcessBuilder("git", "checkout", branch);
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitlabException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	@Override
	public void deleteDirectory(String directory) throws GitlabException {
		try {
			final ProcessBuilder pb = new ProcessBuilder("rm", "-r", directory);
			pb.redirectErrorStream(true);
			executeAndReadOutput(pb.start());
		} catch (IOException | InterruptedException e) {
			log.error("Could not delete directory {}", e.getMessage());
			throw new GitlabException("Could not delete directory", e);
		}

	}

}
