package com.jr.core.service.async;

import com.jr.core.common.logs.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static com.jr.core.common.async.CoreAsyncNameSpace.ASYNC;
import static com.jr.core.common.async.CoreAsyncNameSpace.EXECUTOR;


public abstract class AsyncService< T extends Callable<?> > {


    private static final int NUMBER_OF_CONTROLLER       = 1;
    private static final int NUMBER_MAX_ELEMENT_IN_FIFO = 100;
    private final Integer numberMaxPoolThread;

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final Logger logger                                         = LoggerFactory.getLogger(getClass());
    private final ExecutorService executorController                    = Executors.newFixedThreadPool(NUMBER_OF_CONTROLLER);
    private final BlockingQueue<Callable<?>> fifo                       = new ArrayBlockingQueue<>(NUMBER_MAX_ELEMENT_IN_FIFO);
    private final CopyOnWriteArrayList<Callable<?>> toAnalyseTask       = new CopyOnWriteArrayList<>();

    private FifoController fifoController;
    private boolean running;

    private final List<TimerTask> counterList;
    private final TimeOutTask timeOutTask;

    private Future<?> futureOfFifoController;


    // --------------------------------------
    // -        Constructors                -
    // --------------------------------------

    /**
     *
     * @param timerTaskList : The list of timers to respect
     */
    protected AsyncService( List<TimerTask> timerTaskList)
    {
        this( 5, timerTaskList, null);
    }

    /**
     *
     * @param maxThread : The number of threads executed in parallel
     * @param timerTaskList : The list of timers to respect
     * @param timeOutTask : The maximum execution time of an order
     */
    protected AsyncService(int maxThread, List<TimerTask> timerTaskList, TimeOutTask timeOutTask)
    {
        this.numberMaxPoolThread = maxThread;

        if( timeOutTask == null ){
            this.timeOutTask = new TimeOutTask( 5, TimeUnit.MINUTES );
        }
        else {
            this.timeOutTask = timeOutTask;
        }

        if( timerTaskList == null ){
            this.counterList = new ArrayList<>();
        }
        else {
            this.counterList = timerTaskList;
        }
    }

    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------

    /**
     *
     * @param task : The order to add to the FIFO
     * @return False if the order is already in the Fifo otherwise True
     * @throws IllegalStateException : The fifo is full
     */
    public boolean addTask( T task ) throws IllegalStateException
    {
        // Checks if the order is already in the FIFO
        if( this.isInFifo( task ) )
        {
            this.logger.debug("{}{} The task ({}) is already in the FIFO (total : {}) : {}",
                    ASYNC, EXECUTOR, task, this.toAnalyseTask.size(), this.toAnalyseTask);
            return false;
        }

        // Adding the identifier only
        this.fifo.add( task );
        // The list is used to view the orders already in the FIFO
        this.toAnalyseTask.add( task );

        this.logger.debug("The order list in the FIFO (total : {}) : {}",
                this.toAnalyseTask.size(), this.toAnalyseTask);
        return true;
    }

    /**
     * Start the order analysis controller
     */
    //@Async
    public Thread executorAsynchronously()
    {
        Thread functionAsync = new Thread(() -> {
            if( ! this.isStopped() || this.running )
            {
                return;
            }

            this.running = true ;

            this.logger.info("{}{} Starting the scheduler",
                    ASYNC, EXECUTOR);

            this.fifoController = new FifoController( this.fifo, this.toAnalyseTask, numberMaxPoolThread);

            // Adding Timers
            for( TimerTask timerCommand : this.counterList )
            {
                this.fifoController.addCounter(timerCommand);
            }

            // Maximum time of a command execution
            this.fifoController.setMaxTimerCommand( this.timeOutTask);

            futureOfFifoController = this.executorController.submit( this.fifoController );

            try {
                futureOfFifoController.get();
            }
            catch(InterruptedException | ExecutionException ie)
            {
                this.logger.warn("{}{} Controller interrupt ! Exception : {} : {} ",
                        ASYNC, EXECUTOR, ie, ie.getStackTrace() );
            }
            finally
            {
                if( ! futureOfFifoController.isDone() ){
                    this.logger.warn(LogUtil.format(ASYNC, EXECUTOR,
                            "The FIFO controller will be shut down !"));
                    futureOfFifoController.cancel( true );
                }

                this.running = false;
                this.logger.info("{}{} Stopping the scheduler",
                        ASYNC, EXECUTOR);
            }
        });
        functionAsync.start();
        return functionAsync;
    }

    /**
     * Request the controller to stop
     */
    public void stop()
    {
        if( this.fifoController == null ) {
            return;
        }
        // Ask to stop the controller
        this.fifoController.stopController();
    }

    /**
     * Wait the end of execution controller
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void waitEnd() throws ExecutionException, InterruptedException {
        if( this.futureOfFifoController == null ) return;
        this.futureOfFifoController.get();
    }


    public void waitEnd( int time, TimeUnit timeUnit ) throws ExecutionException, InterruptedException, TimeoutException {
        if( this.futureOfFifoController == null ) return;
        this.futureOfFifoController.get( time, timeUnit );
    }

    /**
     *
     * @return
     */
    public boolean isStopped()
    {
        if( this.fifoController == null ) {
            return true;
        }
        return this.fifoController.isStopped();
    }


    /**
     * Checks if the order has already been put in the FIFO
     *
     * @param command : The command to check
     * @return
     */
    public boolean isInFifo( Callable<?> command )
    {
        for( Callable<?> taskToAnalyse : this.toAnalyseTask)
        {
            if( Objects.equals( taskToAnalyse, command ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return The number of orders in the FIFO
     */
    public int getSizeFifo()
    {
        return this.fifo.size();
    }

    /**
     *
     * @return The maximum number of elements to put in the FIFO
     */
    public int getMaxSizeFifo()
    {
        return NUMBER_MAX_ELEMENT_IN_FIFO;
    }

    public List<TimerTask> getCounterList() {
        return new ArrayList<>(counterList);
    }

    /**
     *
     * @return Displays a snapshot of the commands that will be executed by the controller
     */
    public List<Callable<?>> getAllCommands()
    {
        return new ArrayList<>( this.toAnalyseTask);
    }


}
