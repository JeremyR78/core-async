package com.jr.core;

import com.jr.core.common.async.TaskResultStatus;
import com.jr.core.mock.AsyncServiceMock;
import com.jr.core.mock.TaskMock;
import com.jr.core.service.async.FifoController;
import com.jr.core.service.async.TimeOutTask;
import com.jr.core.service.async.TimerTask;
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
    void addTask() throws InterruptedException {

        //
        // - PREPARE TEST
        //

        TaskMock taskMock = new TaskMock( 1 );
        List<TimerTask> timerTaskList = Collections.singletonList(new TimerTask(1, 200, TimeUnit.MILLISECONDS));

        final AsyncServiceMock asyncServiceMock = new AsyncServiceMock(timerTaskList);
        asyncServiceMock.addTask( taskMock );

        //
        // - TEST
        //

        boolean isInFifo = asyncServiceMock.isInFifo( taskMock );
        Assertions.assertTrue( isInFifo );

        boolean isStopped = asyncServiceMock.isStopped();
        Assertions.assertTrue( isStopped );

        int size = asyncServiceMock.getSizeFifo();
        Assertions.assertEquals( 1 , size );

        Thread t = asyncServiceMock.executorAsynchronously();

        // The task (mock) is executed in 500 ms
        Thread.sleep( 100 ); // 100 ms

        int size2 = asyncServiceMock.getSizeFifo();
        Assertions.assertEquals( 0 , size2 );

        boolean isStopped2 = asyncServiceMock.isStopped();
        Assertions.assertFalse( isStopped2 );

        // We wait for the end of the thread
        t.join();

        boolean isStopped3 = asyncServiceMock.isStopped();
        Assertions.assertTrue( isStopped3 );

        //
        // Try restart
        //

        asyncServiceMock.addTask( taskMock );

        Thread t2 = asyncServiceMock.executorAsynchronously();

        // The task (mock) is executed in 500 ms
        Thread.sleep( 100 ); // 100 ms

        boolean isStopped4 = asyncServiceMock.isStopped();
        Assertions.assertFalse( isStopped4 );

        // We wait for the end of the thread
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

        TaskMock commandMock1 = new TaskMock( 1 );
        TaskMock commandMock2 = new TaskMock( 2 );
        TaskMock commandMock3 = new TaskMock( 3 );
        TaskMock commandMock4 = new TaskMock( 4 );
        TaskMock commandMock5 = new TaskMock( 5 );
        TaskMock commandMock6 = new TaskMock( 6 );

        List<TaskMock> commandMockList = Arrays.asList( commandMock1, commandMock2, commandMock3, commandMock4,
                commandMock5, commandMock6 );

        int waitOrderExecution = commandMockList.stream().map(TaskMock::getWait)
                .reduce(0, Integer::sum) / maxThread ;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, null, null );

        for( TaskMock commandMockItem : commandMockList ){
            asyncServiceMock.addTask( commandMockItem );
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
        for( TaskMock commandMockItem : commandMockList ){
            Assertions.assertEquals( TaskResultStatus.OK, commandMockItem.getStatus(),
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

        TaskMock taskMock1 = new TaskMock( 1 );
        TaskMock taskMock2 = new TaskMock( 2 );
        TaskMock taskMock3 = new TaskMock( 3 );
        TaskMock taskMock4 = new TaskMock( 4 );
        TaskMock taskMock5 = new TaskMock( 5 );
        TaskMock taskMock6 = new TaskMock( 6 );

        List<TaskMock> taskMockList = Arrays.asList( taskMock1, taskMock2, taskMock3, taskMock4,
                taskMock5, taskMock6 );

        int waitOrderExecution = taskMockList.stream().map(TaskMock::getWait).reduce(0, Integer::sum) / maxThread ;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, null, null );

        for( TaskMock taskMockItem : taskMockList ){
            asyncServiceMock.addTask( taskMockItem );
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
        for( TaskMock taskMockItem : taskMockList ){
            Assertions.assertEquals( TaskResultStatus.OK, taskMockItem.getStatus(),
                    String.format("The order %s did not complete correctly !",
                            taskMockItem.getNumber() )
                    );
        }
    }

    /**
     * Checking that the timeout of one task does not impact the following ones
     *
     * @throws InterruptedException
     */
    @Test
    @Order(50)
    void time_out_multi_thread()  throws InterruptedException  {
        this.logger.info("Start time_out_multi_thread ");

        // Init
        int maxThread = 2;

        TaskMock taskMock1 = new TaskMock( 1, 100 );
        TaskMock taskMock2 = new TaskMock( 2, 3500 );
        TaskMock taskMock3 = new TaskMock( 3, 100 );
        TaskMock taskMock4 = new TaskMock( 4, 100 );
        TaskMock taskMock5 = new TaskMock( 5, 100 );
        TaskMock taskMock6 = new TaskMock( 6, 100 );
        TaskMock taskMock7 = new TaskMock( 7, 100 );

        List<TaskMock> taskMockList = Arrays.asList( taskMock1, taskMock2,  taskMock3, taskMock4,
                taskMock5, taskMock6, taskMock7);

        TimeOutTask timeOutTask = new TimeOutTask(2000, TimeUnit.MILLISECONDS);

        int waitOrderExecution = taskMockList.stream().map(TaskMock::getWait)
                .reduce(0, Integer::sum) / maxThread;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, null, timeOutTask);

        for( TaskMock taskMockItem : taskMockList ){
            asyncServiceMock.addTask( taskMockItem );
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

        for( TaskMock taskMockItem : taskMockList ){
            if( taskMockItem.getNumber() == 2 ){
                Assertions.assertNotEquals( TaskResultStatus.OK, taskMockItem.getStatus(),
                        String.format("The order %s should not have completed correctly!", taskMockItem.getNumber() ));
            } else {
                Assertions.assertEquals( TaskResultStatus.OK, taskMockItem.getStatus(),
                        String.format("The order %s did not complete correctly !", taskMockItem.getNumber() ));
            }
        }

    }

    @Test
    @Order(100)
    void limit_order_execution() throws InterruptedException {
        // Init
        int maxThread = 1;
        int waitBetweenOrder = 300; // ms

        TaskMock taskMock1 = new TaskMock( 1, 100 );
        TaskMock taskMock2 = new TaskMock( 2, 100 );
        TaskMock taskMock3 = new TaskMock( 3, 100 );
        TaskMock taskMock4 = new TaskMock( 4, 100 );

        List<TaskMock> taskMockList = Arrays.asList( taskMock1, taskMock2,  taskMock3, taskMock4 );

        TimeOutTask timeOutTask = new TimeOutTask(2000, TimeUnit.MILLISECONDS);

        List<TimerTask> timerBytaskList = Arrays.asList( new TimerTask( 1, waitBetweenOrder, TimeUnit.MILLISECONDS ) );

        int waitOrderExecution = ( taskMockList.stream().map(TaskMock::getWait)
                .reduce(0, Integer::sum) + (500 * taskMockList.size()))
                / maxThread;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, timerBytaskList, timeOutTask);

        for( TaskMock taskMockItem : taskMockList ){
            asyncServiceMock.addTask( taskMockItem );
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
        for( TaskMock taskMockItem : taskMockList ){
            Date dateNextOrder = taskMockItem.getStart();
            if( first != null ) {
                long timeMillisecondDiff = dateNextOrder.getTime() - first.getTime();
                this.logger.info("The diff between order {} & {} is : {} ms", taskMockItem.getNumber() -1 ,
                        taskMockItem.getNumber(), timeMillisecondDiff );
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

        TaskMock taskMock1 = new TaskMock( 1, FifoController.TIME_OUT_MAX_FIFO );
        TaskMock taskMock2 = new TaskMock( 2, 1000 );

        List<TaskMock> taskMockList = Arrays.asList( taskMock1, taskMock2 );

        int waitOrderExecution = taskMockList.stream().map(TaskMock::getWait)
                .reduce(0, Integer::sum) / maxThread;

        AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, null, null );

        for( TaskMock taskMockItem : taskMockList ){
            asyncServiceMock.addTask( taskMockItem );
        }

        // Test
        asyncServiceMock.executorAsynchronously();


        Thread.sleep( 200 );

        // Stop the controller after the fist task
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
        for( TaskMock taskMockItem : taskMockList ){
            if( taskMockItem.getNumber() == 2 ){
                Assertions.assertNotEquals( TaskResultStatus.OK, taskMockItem.getStatus(),
                        String.format("The order %s should not have completed correctly!", taskMockItem.getNumber() ));
            } else {
                Assertions.assertEquals( TaskResultStatus.OK, taskMockItem.getStatus(),
                        String.format("The order %s did not complete correctly !", taskMockItem.getNumber() ));
            }
        }
    }



}
