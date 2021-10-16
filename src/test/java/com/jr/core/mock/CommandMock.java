package com.jr.core.mock;

import com.jr.core.api.async.Command;
import com.jr.core.common.async.CommandResultStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class CommandMock extends Command<Integer> {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Getter
    private Integer number;
    @Getter
    public int wait = 200; // ms
    @Getter @Setter(AccessLevel.PROTECTED)
    public Date start;
    @Getter @Setter(AccessLevel.PROTECTED)
    public Date end;

    public CommandMock( Integer number ){
        this.number = number;
    }

    public CommandMock( Integer number, int wait ){
        this.number = number;
        this.wait = wait;
    }

    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------

    @Override
    public Integer call() throws Exception {
        this.setStart( new Date() );
        this.logger.info("Order : {} - START : {} ", number, this.getStart() );
        Thread.sleep( this.wait );
        this.setEnd( new Date() );
        this.logger.info("Order : {} - STOP : {} ", number, this.getEnd() );
        this.setResult( this.number );
        this.setStatus( CommandResultStatus.OK );
        return this.number;
    }

    @Override
    public String toString() {
        return "CommandMock{" +
                "number=" + number +
                ", wait=" + wait +
                '}';
    }
}
