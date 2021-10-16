package com.jr.core;

import com.jr.core.common.async.CommandResultStatus;
import com.jr.core.mock.AsyncServiceMock;
import com.jr.core.mock.CommandMock;
import com.jr.core.service.async.FifoController;
import com.jr.core.service.async.TimeOutCommand;
import com.jr.core.service.async.TimerCommand;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AsyncServiceTest {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------


    @Test
    @Order(10)
    void addCommand() throws InterruptedException {

        //
        // - PREPARE TEST
        //

        CommandMock commandMock = new CommandMock( 1 );
        List<TimerCommand> timerCommandList = Collections.singletonList(new TimerCommand(1, 200, TimeUnit.MILLISECONDS));

        final AsyncServiceMock asyncServiceMock = new AsyncServiceMock( timerCommandList );
        asyncServiceMock.addCommand( commandMock );

        //
        // - TEST
        //

        boolean isInFifo = asyncServiceMock.isInFifo( commandMock );
        Assertions.assertTrue( isInFifo );

        boolean isStopped = asyncServiceMock.isStopped();
        Assertions.assertTrue( isStopped );

        int size = asyncServiceMock.getSizeFifo();
        Assertions.assertEquals( 1 , size );

        Thread t = asyncServiceMock.executorAsynchronously();

        // La commande (mock) s'éxecute en 500 ms
        Thread.sleep( 100 ); // 100 ms

        int size2 = asyncServiceMock.getSizeFifo();
        Assertions.assertEquals( 0 , size2 );

        boolean isStopped2 = asyncServiceMock.isStopped();
        Assertions.assertFalse( isStopped2 );

        // On attend la fin du thread
        t.join();

        boolean isStopped3 = asyncServiceMock.isStopped();
        Assertions.assertTrue( isStopped3 );

        //
        // Try restart
        //

        asyncServiceMock.addCommand( commandMock );

        Thread t2 = asyncServiceMock.executorAsynchronously();

        // La commande (mock) s'éxecute en 500 ms
        Thread.sleep( 100 ); // 100 ms

        boolean isStopped4 = asyncServiceMock.isStopped();
        Assertions.assertFalse( isStopped4 );

        // On attend la fin du thread
        t2.join();

        boolean isStopped5 = asyncServiceMock.isStopped();
        Assertions.assertTrue( isStopped5 );
    }


    /**
     * Checking for multithreaded execution
     * @throws InterruptedException
     */
    @Test
    @Order(30)
    void multi_thread() throws InterruptedException {
        this.logger.info("Start multi_thread");

        // Init
        int maxThread = 2;

        CommandMock commandMock1 = new CommandMock( 1 );
        CommandMock commandMock2 = new CommandMock( 2 );
        CommandMock commandMock3 = new CommandMock( 3 );
        CommandMock commandMock4 = new CommandMock( 4 );
        CommandMock commandMock5 = new CommandMock( 5 );
        CommandMock commandMock6 = new CommandMock( 6 );

        List<CommandMock> commandMockList = Arrays.asList( commandMock1, commandMock2, commandMock3, commandMock4,
                commandMock5, commandMock6 );

        int waitOrderExecution = commandMockList.stream().map(CommandMock::getWait)
                .reduce(0, Integer::sum) / maxThread ;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, null, null );

        for( CommandMock commandMockItem : commandMockList ){
            asyncServiceMock.addCommand( commandMockItem );
        }

        // Test
        asyncServiceMock.executorAsynchronously();
        // Time for the controller to turn off
        Thread.sleep( 200 );
        try {
            int totalTimeWithController = waitOrderExecution + FifoController.TIME_OUT_MAX_FIFO ;
            this.logger.info("Wait : {} ms", totalTimeWithController );
            asyncServiceMock.waitEnd( totalTimeWithController, TimeUnit.MILLISECONDS);
        } catch ( Exception ex ){
            Assertions.fail("The controller took too long to stop !");
        }

        // Check is the thread have completed correctly
        for( CommandMock commandMockItem : commandMockList ){
            Assertions.assertEquals( CommandResultStatus.OK, commandMockItem.getStatus(),
                    String.format( "The order %s did not complete correctly !",
                            commandMockItem.getNumber() ) );
        }
    }

    /**
     * Checking the execution in monoThread
     *
     * @throws InterruptedException
     */
    @Test
    @Order(20)
    void mono_thread() throws InterruptedException {
        this.logger.info("Start mono_thread ");

        // Init
        int maxThread = 1;

        CommandMock commandMock1 = new CommandMock( 1 );
        CommandMock commandMock2 = new CommandMock( 2 );
        CommandMock commandMock3 = new CommandMock( 3 );
        CommandMock commandMock4 = new CommandMock( 4 );
        CommandMock commandMock5 = new CommandMock( 5 );
        CommandMock commandMock6 = new CommandMock( 6 );

        List<CommandMock> commandMockList = Arrays.asList( commandMock1, commandMock2, commandMock3, commandMock4,
                commandMock5, commandMock6 );

        int waitOrderExecution = commandMockList.stream().map(CommandMock::getWait).reduce(0, Integer::sum) / maxThread ;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, null, null );

        for( CommandMock commandMockItem : commandMockList ){
            asyncServiceMock.addCommand( commandMockItem );
        }

        // Test
        asyncServiceMock.executorAsynchronously();

        // Time for the controller to turn off
        Thread.sleep( 200 );
        try {
            int totalTimeWithController = waitOrderExecution + FifoController.TIME_OUT_MAX_FIFO;
            this.logger.info("Wait : {} ms", totalTimeWithController );
            asyncServiceMock.waitEnd( totalTimeWithController, TimeUnit.MILLISECONDS);
        } catch ( Exception ex ){
            Assertions.fail("The controller took too long to stop !");
        }

        // Check is the thread have completed correctly
        for( CommandMock commandMockItem : commandMockList ){
            Assertions.assertEquals( CommandResultStatus.OK, commandMockItem.getStatus(),
                    String.format("The order %s did not complete correctly !",
                            commandMockItem.getNumber() )
                    );
        }
    }

    /**
     * Checking that the timeout of one command does not impact the following ones
     *
     * @throws InterruptedException
     */
    @Test
    @Order(50)
    void time_out_multi_thread()  throws InterruptedException  {
        this.logger.info("Start time_out_multi_thread ");

        // Init
        int maxThread = 2;

        CommandMock commandMock1 = new CommandMock( 1, 100 );
        CommandMock commandMock2 = new CommandMock( 2, 3500 );
        CommandMock commandMock3 = new CommandMock( 3, 100 );
        CommandMock commandMock4 = new CommandMock( 4, 100 );
        CommandMock commandMock5 = new CommandMock( 5, 100 );
        CommandMock commandMock6 = new CommandMock( 6, 100 );
        CommandMock commandMock7 = new CommandMock( 7, 100 );

        List<CommandMock> commandMockList = Arrays.asList( commandMock1, commandMock2,  commandMock3, commandMock4,
                commandMock5, commandMock6, commandMock7);

        TimeOutCommand timeOutCommand = new TimeOutCommand(2000, TimeUnit.MILLISECONDS);

        int waitOrderExecution = commandMockList.stream().map(CommandMock::getWait)
                .reduce(0, Integer::sum) / maxThread;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, null, timeOutCommand );

        for( CommandMock commandMockItem : commandMockList ){
            asyncServiceMock.addCommand( commandMockItem );
        }

        // Test
        asyncServiceMock.executorAsynchronously();

        // Time for the controller to turn off
        Thread.sleep( 200 );
        try {
            int totalTimeWithController = waitOrderExecution + (FifoController.TIME_OUT_MAX_FIFO * 2);
            this.logger.info("Wait : {} ms", totalTimeWithController );
            asyncServiceMock.waitEnd( totalTimeWithController, TimeUnit.MILLISECONDS);
        } catch ( Exception ex ){
            Assertions.fail("The controller took too long to stop !");
        }

        for( CommandMock commandMockItem : commandMockList ){
            if( commandMockItem.getNumber() == 2 ){
                Assertions.assertNotEquals( CommandResultStatus.OK, commandMockItem.getStatus(),
                        String.format("The order %s should not have completed correctly!", commandMockItem.getNumber() ));
            } else {
                Assertions.assertEquals( CommandResultStatus.OK, commandMockItem.getStatus(),
                        String.format("The order %s did not complete correctly !", commandMockItem.getNumber() ));
            }
        }

    }

    @Test
    @Order(100)
    void limit_order_execution() throws InterruptedException {
        // Init
        int maxThread = 1;
        int waitBetweenOrder = 300; // ms

        CommandMock commandMock1 = new CommandMock( 1, 100 );
        CommandMock commandMock2 = new CommandMock( 2, 100 );
        CommandMock commandMock3 = new CommandMock( 3, 100 );
        CommandMock commandMock4 = new CommandMock( 4, 100 );

        List<CommandMock> commandMockList = Arrays.asList( commandMock1, commandMock2,  commandMock3, commandMock4 );

        TimeOutCommand timeOutCommand = new TimeOutCommand(2000, TimeUnit.MILLISECONDS);

        List<TimerCommand> timerByCommandList = Arrays.asList( new TimerCommand( 1, waitBetweenOrder, TimeUnit.MILLISECONDS ) );

        int waitOrderExecution = ( commandMockList.stream().map(CommandMock::getWait)
                .reduce(0, Integer::sum) + (500 * commandMockList.size()))
                / maxThread;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, timerByCommandList, timeOutCommand );

        for( CommandMock commandMockItem : commandMockList ){
            asyncServiceMock.addCommand( commandMockItem );
        }

        // Test
        asyncServiceMock.executorAsynchronously();

        // Time for the controller to turn off
        Thread.sleep( 200 );
        try {
            int totalTimeWithController = waitOrderExecution + (FifoController.TIME_OUT_MAX_FIFO * 2);
            this.logger.info("Wait : {} ms", totalTimeWithController );
            asyncServiceMock.waitEnd( totalTimeWithController, TimeUnit.MILLISECONDS);
        } catch ( Exception ex ){
            Assertions.fail("The controller took too long to stop !");
        }

        Date first = null;
        for( CommandMock commandMockItem : commandMockList ){
            Date dateNextOrder = commandMockItem.getStart();
            if( first != null ) {
                long timeMillisecondDiff = dateNextOrder.getTime() - first.getTime();
                this.logger.info("The diff between order {} & {} is : {} ms", commandMockItem.getNumber() -1 ,
                        commandMockItem.getNumber(), timeMillisecondDiff );
                Assertions.assertTrue( timeMillisecondDiff >= waitBetweenOrder );
            }
            first = dateNextOrder;
        }
    }

    @Test
    @Order(200)
    void check_stop_service() throws InterruptedException {
        this.logger.info("Start check_stop_service8727" );

        // Init
        int maxThread = 1;

        CommandMock commandMock1 = new CommandMock( 1, FifoController.TIME_OUT_MAX_FIFO );
        CommandMock commandMock2 = new CommandMock( 2, 1000 );

        List<CommandMock> commandMockList = Arrays.asList( commandMock1, commandMock2 );

        int waitOrderExecution = commandMockList.stream().map(CommandMock::getWait)
                .reduce(0, Integer::sum) / maxThread;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, null, null );

        for( CommandMock commandMockItem : commandMockList ){
            asyncServiceMock.addCommand( commandMockItem );
        }

        // Test
        asyncServiceMock.executorAsynchronously();


        Thread.sleep( 200 );

        // Stop the controller after the fist command
        asyncServiceMock.stop();

        // Time for the controller to turn off
        try {
            int totalTimeWithController = waitOrderExecution + FifoController.TIME_OUT_MAX_FIFO;
            this.logger.info("Wait : {} ms", totalTimeWithController );
            asyncServiceMock.waitEnd( totalTimeWithController, TimeUnit.MILLISECONDS);
        } catch ( Exception ex ){
            Assertions.fail("The controller took too long to stop !");
        }

        // Check
        for( CommandMock commandMockItem : commandMockList ){
            if( commandMockItem.getNumber() == 2 ){
                Assertions.assertNotEquals( CommandResultStatus.OK, commandMockItem.getStatus(),
                        String.format("The order %s should not have completed correctly!", commandMockItem.getNumber() ));
            } else {
                Assertions.assertEquals( CommandResultStatus.OK, commandMockItem.getStatus(),
                        String.format("The order %s did not complete correctly !", commandMockItem.getNumber() ));
            }
        }
    }



}
