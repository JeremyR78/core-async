package com.jr.core.service.async;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Data
public class TimerCommand implements Serializable {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final Integer   numberOfCommand;
    private final Integer   unitTime;
    private final TimeUnit  timeConvert;


    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimerCommand)) return false;
        TimerCommand timerCommand = (TimerCommand) o;
        return Objects.equals(getNumberOfCommand(), timerCommand.getNumberOfCommand()) &&
                Objects.equals(getUnitTime(), timerCommand.getUnitTime()) &&
                getTimeConvert() == timerCommand.getTimeConvert();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumberOfCommand(), getUnitTime(), getTimeConvert());
    }
}
