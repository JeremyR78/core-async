package com.jr.core;


import com.jr.core.service.async.TimerByCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;


class TimerByCommandTest {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------


    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------


    @Test
    void checkCommandAndValidBlock()
    {
        //
        // - PREPARE TEST
        //
        int tolerance           = 100; // 100 ms
        int numberOfCommand     = 1;
        long timeInMillisecond  = 500; // 500 ms
        TimerByCommand timerByCommand = new TimerByCommand( numberOfCommand, timeInMillisecond );

        // New Command
        timerByCommand.setNewCommand();

        Date firstDate = new Date();

        // Check and Wait
        timerByCommand.checkCommandAndValidBlock();

        Date secondDate = new Date();

        long timeDiff = secondDate.getTime() - firstDate.getTime();

        System.out.println( String.format("First date : %s - Second date : %s => Diff : %s ms ",
                firstDate, secondDate, timeDiff ) );
        Assertions.assertEquals( timeInMillisecond, timeDiff, tolerance );
    }

}
