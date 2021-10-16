package com.jr.core.mock;


import com.jr.core.api.async.ICommand;
import com.jr.core.service.async.AsyncService;
import com.jr.core.service.async.TimeOutCommand;
import com.jr.core.service.async.TimerCommand;

import java.util.List;

public class AsyncServiceMock extends AsyncService< ICommand<?> > {

    public AsyncServiceMock(List<TimerCommand> timerCommandList) {
        super(timerCommandList);
    }

    public AsyncServiceMock(int maxThread, List<TimerCommand> timerCommandList, TimeOutCommand timeOutCommand) {
        super(maxThread, timerCommandList, timeOutCommand);
    }

}
