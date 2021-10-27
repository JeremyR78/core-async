package com.jr.core.api.async;

import com.jr.core.common.async.TaskResultStatus;

import java.io.Serializable;

public interface ITaskResult<T extends Serializable > extends Serializable {

    ITask<T> getTask();
    TaskResultStatus getTaskResultStatus();
    T getResult();

}
