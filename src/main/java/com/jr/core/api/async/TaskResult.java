package com.jr.core.api.async;

import com.jr.core.common.async.TaskResultStatus;
import lombok.Data;

import java.io.Serializable;

@Data
public class TaskResult< T extends Serializable > implements ITaskResult<T> {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final ITask<T> task;
    private final TaskResultStatus taskResultStatus;
    private final T result;

}
