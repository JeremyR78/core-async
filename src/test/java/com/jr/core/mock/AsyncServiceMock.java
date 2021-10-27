package com.jr.core.mock;


import com.jr.core.api.async.ITask;
import com.jr.core.service.async.AsyncService;
import com.jr.core.service.async.TimeOutTask;
import com.jr.core.service.async.TimerTask;

import java.util.List;

public class AsyncServiceMock extends AsyncService<ITask<?>> {

    public AsyncServiceMock(List<TimerTask> timerTaskList) {
        super(timerTaskList);
    }

    public AsyncServiceMock(int maxThread, List<TimerTask> timerTaskList, TimeOutTask timeOutTask) {
        super(maxThread, timerTaskList, timeOutTask);
    }

}
