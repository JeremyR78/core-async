package com.jr.core.api.async;

import com.jr.core.common.async.CommandResultStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;


import java.io.Serializable;

@Data
public class CommandResult< T extends Serializable > implements ICommandResult<T> {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final ICommand<T> command;
    private final CommandResultStatus commandResultStatus;
    private final T result;

}
