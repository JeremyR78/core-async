package com.jr.core.api.async;

import com.jr.core.common.async.CommandResultStatus;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

public interface ICommand<T> extends Callable<T>, Serializable {

    List<String> getMessage();

    /**
     * The status
     * @return
     */
    CommandResultStatus getStatus();

    /**
     * The result of command
     * @return
     */
    T getResult();

    /**
     * Percent
     * @return
     */
    int getPercent();

}
