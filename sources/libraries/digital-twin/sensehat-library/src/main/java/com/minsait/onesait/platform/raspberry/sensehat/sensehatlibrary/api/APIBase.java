package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api;

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.CommandResult;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.Command;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.CommandExecutor;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.CommandExecutorFactory;

/**
 * Created by jcincera on 22/06/2017.
 */
public abstract class APIBase {

	private CommandExecutor commandExecutor;

	protected APIBase() {
		this.commandExecutor = CommandExecutorFactory.get();
	}

	protected CommandResult execute(Command command, String... args) {
		return commandExecutor.execute(command, args);
	}
}
