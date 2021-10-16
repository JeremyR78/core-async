package com.jr.core.service.async;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TimerCommand implements Serializable {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final Integer   numberOfCommand;
    private final Integer   unitTime;
    private final TimeUnit  timeConvert;

    // --------------------------------------
    // -        Constructors                -
    // --------------------------------------

    /**
     *
     * @param numberOfCommand : Nombre de commande à exécuter dans le temps impartis
     * @param unitTime        : L'unité
     * @param timeConvert     : L'échelle de temps
     */
    public TimerCommand(Integer numberOfCommand, Integer unitTime, TimeUnit timeConvert )
    {
        this.numberOfCommand    = numberOfCommand;
        this.unitTime           = unitTime;
        this.timeConvert        = timeConvert;
    }

    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------


    public Integer getNumberOfCommand() {
        return numberOfCommand;
    }

    public Integer getUnitTime() {
        return unitTime;
    }

    public TimeUnit getTimeConvert() {
        return timeConvert;
    }

    public long getToMillisecond(){
        return this.timeConvert.convert( this.getUnitTime(), TimeUnit.MILLISECONDS );
    }

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
