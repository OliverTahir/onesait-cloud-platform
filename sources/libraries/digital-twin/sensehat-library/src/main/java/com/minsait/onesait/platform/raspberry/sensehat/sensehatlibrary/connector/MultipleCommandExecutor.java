package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector;

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.CommandResult;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.exception.InvalidSystemArchitectureException;

/**
 * Created by jcincera on 27/06/2017.
 */
public class MultipleCommandExecutor implements CommandExecutor {

	MultipleCommandExecutor() {
		if (!System.getProperty("os.arch").toLowerCase().contains("arm")) {
			throw new InvalidSystemArchitectureException(
					"System architecture is not supported for this command executor");
		}
	}

	@Override
	public CommandResult execute(Command command, String... args) {
		throw new UnsupportedOperationException("Not supported yet");
	}
}
