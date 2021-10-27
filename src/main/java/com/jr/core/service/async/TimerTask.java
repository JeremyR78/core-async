package com.jr.core.service.async;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Data
public class TimerTask implements Serializable {

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
        if (!(o instanceof TimerTask)) return false;
        TimerTask timerTask = (TimerTask) o;
        return Objects.equals(getNumberOfCommand(), timerTask.getNumberOfCommand()) &&
                Objects.equals(getUnitTime(), timerTask.getUnitTime()) &&
                getTimeConvert() == timerTask.getTimeConvert();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumberOfCommand(), getUnitTime(), getTimeConvert());
    }
}
