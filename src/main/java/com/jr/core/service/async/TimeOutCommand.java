package com.jr.core.service.async;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Data
@ToString
public class TimeOutCommand implements Serializable {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final Integer   unitTime;
    private final TimeUnit  timeConvert;


}
