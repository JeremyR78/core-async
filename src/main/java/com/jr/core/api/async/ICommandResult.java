package com.jr.core.api.async;

import com.jr.core.common.async.CommandResultStatus;

import java.io.Serializable;

public interface ICommandResult<T extends Serializable > extends Serializable {

    ICommand<T> getCommand();
    CommandResultStatus getCommandResultStatus();
    T getResult();

}
