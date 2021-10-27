package com.jr.core.common.async;

import static com.jr.core.common.logs.LogUtil.fLog;

public final class CoreAsyncNameSpace {

    public static final String UPDATE_WALLET    = fLog("UPDATE_WALLET");
    public static final String TASK             = fLog("TASK");
    public static final String START            = fLog("START");
    public static final String FINISH           = fLog("FINISH");

    public static final String EXECUTE         = fLog("EXECUTE");
    public static final String CHECK           = fLog("CHECK");
    public static final String VALID           = fLog("VALID");
    public static final String WAIT            = fLog("WAIT");

    public static final String FIFO_CONTROLLER = fLog("FIFO_CONTROLLER");
    public static final String RUN             = fLog("RUN");
    public static final String STOP            = fLog("STOP");
    public static final String FAIL            = fLog("FAIL");

    public static final String ASYNC           = fLog("ASYNC_SERVICE");
    public static final String EXECUTOR        = fLog("EXECUTOR");
    
    private CoreAsyncNameSpace(){}
}
