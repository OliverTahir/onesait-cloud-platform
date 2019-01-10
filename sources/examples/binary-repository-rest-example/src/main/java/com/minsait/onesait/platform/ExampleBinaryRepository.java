package com.minsait.onesait.platform;

import java.io.File;
import java.io.IOException;

import org.overviewproject.mime_types.GetBytesException;

import com.minsait.onesait.platform.binaryrepository.BinaryDataFile;
import com.minsait.onesait.platform.binaryrepository.BinaryRepositoryClient;
import com.minsait.onesait.platform.client.exception.BinaryRepositoryException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleBinaryRepository {

	private final static String USERNAME = "developer";
	private final static String PASSWORD = "changeIt!";
	private final static String SERVER = "http://localhost:18000/controlpanel";
	private final static String PATH_TO_FILE = "/tmp/DNI.pdf";

	public static void main(String[] args) throws BinaryRepositoryException, IOException, GetBytesException {

		// Create binary repository RESTFull client
		final BinaryRepositoryClient client = new BinaryRepositoryClient(USERNAME, PASSWORD, SERVER);

		// Add binary file to platform
		final String newFileId = client.addBinaryFile(new File(PATH_TO_FILE), "");
		log.info("New file ID is {}", newFileId);

		// Retrieve binary file from platform
		final BinaryDataFile bfile = client.getBinaryFile(newFileId);
		log.info("Retrieved file with name \"{}\"", bfile.getFileName());

		// Update binary file
		final String metadata = "{\"private\" : true}";
		client.updateBinaryFile(newFileId, new File(PATH_TO_FILE), metadata);
		log.info("Updated binary file {}", newFileId);

		// delete the binary file
		client.removeBinaryFile(newFileId);
		log.info("Deleted binary file {}", newFileId);

	}
}
