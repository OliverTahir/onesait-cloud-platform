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
package com.minsait.onesait.platform.config.services.binaryfile;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.model.BinaryFileAccess.Type;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.BinaryFileAccessRepository;
import com.minsait.onesait.platform.config.repository.BinaryFileRepository;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class BinaryFileServiceImpl implements BinaryFileService {

	@Autowired
	private BinaryFileRepository binaryFileRepository;
	@Autowired
	private BinaryFileAccessRepository accessRepository;
	@Autowired
	private UserService userService;

	@Override
	public void createBinaryile(BinaryFile binaryFile) {
		binaryFileRepository.save(binaryFile);
	}

	@Override
	public void updateBinaryFile(String id, String metadata, String mime, String fileName) {
		final BinaryFile file = binaryFileRepository.findByFileId(id);
		if (!StringUtils.isEmpty(metadata))
			file.setMetadata(metadata);
		if (!StringUtils.isEmpty(mime))
			file.setMime(mime);
		if (!StringUtils.isEmpty(fileName))
			file.setFileName(fileName);
		binaryFileRepository.save(file);
	}

	@Override
	public boolean hasUserPermissionWrite(String id, User user) {
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return true;
		else if (binaryFileRepository.findByUserAndFileIdWrite(user, id) != null)
			return true;
		else
			return false;
	}

	@Override
	public boolean hasUserPermissionRead(String id, User user) {
		if (user == null) {
			if (binaryFileRepository.findByFileId(id).isPublic())
				return true;
			else
				return false;
		} else {
			if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
				return true;
			else if (binaryFileRepository.findByUserAndFileId(user, id) != null)
				return true;
			else
				return false;
		}
	}

	@Override
	public void authorizeUser(String id, Type accessType, User user) {
		final BinaryFile file = binaryFileRepository.findByFileId(id);
		if (file != null) {
			BinaryFileAccess access = accessRepository.findByUserAndBinaryFile(user, file);
			if (access == null) {
				access = new BinaryFileAccess();
				access.setAccessType(accessType);
				access.setUser(user);
				access.setBinaryFile(file);
				file.getFileAccesses().add(access);
			} else {
				access.setAccessType(accessType);
				file.getFileAccesses().add(access);
			}
			binaryFileRepository.save(file);
			accessRepository.save(access);
		}
	}

	@Override
	@Transactional
	public void deleteFile(String fileId) {
		binaryFileRepository.deleteByFileId(fileId);
	}

	@Override
	public List<BinaryFile> getAllFiles(User user) {
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return binaryFileRepository.findAll();
		return binaryFileRepository.findByUser(user);
	}

	@Override
	public BinaryFile getFile(String fileId) {
		return binaryFileRepository.findByFileId(fileId);
	}

	@Override
	public void changePublic(String fileId) {
		final BinaryFile file = binaryFileRepository.findByFileId(fileId);
		if (file != null) {
			if (file.isPublic())
				file.setPublic(false);
			else
				file.setPublic(true);
			binaryFileRepository.save(file);
		}

	}

	@Override
	public boolean isUserOwner(String fileId, User user) {
		final BinaryFile file = binaryFileRepository.findByFileId(fileId);
		if (file.getOwner().getUserId().equalsIgnoreCase(user.getUserId())
				|| user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return true;
		return false;
	}

	@Override
	public BinaryFileAccess createBinaryFileAccess(String fileId, String userId, String accessType, User user) {
		if (hasUserPermissionWrite(fileId, user)) {
			final BinaryFileAccess access = new BinaryFileAccess();
			access.setBinaryFile(getFile(fileId));
			access.setUser(userService.getUser(userId));
			access.setAccessType(Type.valueOf(accessType));
			return accessRepository.save(access);
		}
		throw new RuntimeException();
	}

	@Override
	public List<BinaryFileAccess> getAuthorizations(String id, User user) {
		if (hasUserPermissionWrite(id, user)) {
			return accessRepository.findByBinaryFile(getFile(id));
		}
		return new ArrayList<>();
	}

	@Override
	@Transactional
	public void deleteBinaryFileAccess(String id, User user) {
		if (canUserEditAccess(user, id)) {
			if (!accessRepository.findById(id).getUser().getUserId().equals(user.getUserId()))
				accessRepository.deleteById(id);
			else
				throw new RuntimeException();
		} else
			throw new RuntimeException();
	}

	@Override
	public BinaryFileAccess updateBinaryFileAccess(String id, String accesstype, User user) {
		if (canUserEditAccess(user, id)) {
			final BinaryFileAccess access = accessRepository.findById(id);
			access.setAccessType(Type.valueOf(accesstype));
			return accessRepository.save(access);
		}

		throw new RuntimeException();
	}

	@Override
	public boolean canUserEditAccess(User user, String id) {
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return true;
		final BinaryFile file = accessRepository.findById(id).getBinaryFile();
		if (file != null) {
			if (accessRepository.findByUserAndWriteAccess(file, user) != null)
				return true;
		}
		return false;
	}

}
