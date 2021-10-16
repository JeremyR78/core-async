package com.jr.core.service.async;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class TimeOutCommand implements Serializable {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final Integer   unitTime;
    private final TimeUnit  timeConvert;

    // --------------------------------------
    // -        Constructors                -
    // --------------------------------------

    public TimeOutCommand( Integer unitTime, TimeUnit timeConvert ) {
        this.unitTime       = unitTime;
        this.timeConvert    = timeConvert;
    }

    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------

    public Integer getUnitTime() {
        return unitTime;
    }

    public TimeUnit getTimeConvert() {
        return timeConvert;
    }
}
