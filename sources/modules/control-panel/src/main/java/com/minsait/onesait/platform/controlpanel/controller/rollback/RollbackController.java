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
package com.minsait.onesait.platform.controlpanel.controller.rollback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.minsait.onesait.platform.config.model.Rollback;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.services.rollback.RollbackService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/roolback")
@Slf4j
public class RollbackController {

	@Autowired
	private RollbackService rollbackService;

	@PostMapping(value = "/saveRollback")
	public Rollback saveRollback(Object entity, Rollback.EntityType entityType) {

		Rollback rollback = null;
		if (entityType.equals(Rollback.EntityType.Viewer)) {
			Viewer viewer = (Viewer) entity;
			rollback = rollbackService.findByEntityId(viewer.getId());
			if (rollback == null) {
				rollback = new Rollback();
			}
			try {
				String result = toString(viewer);

				rollback.setEntityId(viewer.getId());
				rollback.setType(entityType);
				rollback.setSerialization(result);

				rollbackService.save(rollback);
			} catch (IOException e) {
				log.error("Error serializating the entity. {}", e);
				return null;
			}
		}

		return rollback;
	}

	@PostMapping(value = "/serialize")
	public Object getRollback(String entityId) {

		Object result = null;
		try {
			Rollback rollback = rollbackService.findByEntityId(entityId);
			result = fromString(rollback.getSerialization());
		} catch (IOException e) {
			log.error("Error serializating the entity. {}", e);
			return null;
		} catch (ClassNotFoundException e) {
			log.error("Error serializating the entity. {}", e);
			return null;
		}

		return result;
	}

	private static Object fromString(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		InputStream targetStream = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(targetStream);
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	private static String toString(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();

		// return new String(baos.toByteArray());
		return Base64.getEncoder().encodeToString(baos.toByteArray());

	}

}
