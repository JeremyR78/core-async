package com.jr.core;


import com.jr.core.service.async.TimerByTask;
import com.jr.core.utils.date.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;


class TimerByTaskTest {

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
        TimerByTask timerByTask = new TimerByTask( numberOfCommand, timeInMillisecond );

        // New Command
        timerByTask.setNewCommand();

        Date firstDate = new Date();

        // Check and Wait
        timerByTask.checkCommandAndValidBlock();

        Date secondDate = new Date();

        long timeDiff = DateUtils.getDiffBetweenTwoDate( firstDate, secondDate );

        System.out.println( String.format("First date : %s - Second date : %s => Diff : %s ms ",
                firstDate, secondDate, timeDiff ) );
        Assertions.assertEquals( timeInMillisecond, timeDiff, tolerance );
    }

}
