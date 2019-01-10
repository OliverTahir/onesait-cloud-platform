package com.minsait.onesait.platform.config.repository;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.model.BinaryFileAccess.Type;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)

public class BinaryFileRepositoryIntegrationTest {

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private BinaryFileRepository binaryRepo;
	@Autowired
	private BinaryFileAccessRepository binaryAccessRepo;

	private BinaryFile file;
	private BinaryFileAccess access;

	@Before
	public void setUp() {
		file = new BinaryFile();
		file.setFileId("1");
		file.setMime("application/pdf");
		file.setFileName("example.pdf");
		file.setPublic(false);
		file.setOwner(userRepo.findByUserId("developer"));

		file.setFileExtension("pdf");

		// file.getFileAccesses().add(access);
		file = binaryRepo.save(file);

		access = new BinaryFileAccess();
		access.setAccessType(Type.WRITE);
		access.setBinaryFile(file);
		access.setUser(userRepo.findByUserId("user"));
		access = binaryAccessRepo.save(access);

	}

	@Test
	@Transactional
	public void getFile_ByAllowedUser() {
		Assert.assertTrue(binaryRepo.findByUser(userRepo.findByUserId("developer")).size() > 0);
	}

	@After
	public void tearOff() {
		binaryAccessRepo.delete(access);
		binaryRepo.delete(file);
	}
}
