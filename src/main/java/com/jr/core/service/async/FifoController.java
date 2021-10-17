package com.jr.core.service.async;

import com.jr.core.common.logs.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.jr.core.common.async.CoreAsyncNameSpace.*;


public class FifoController implements Runnable {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    // The time max to wait the last thread
    public static final int TIME_OUT_MAX_FIFO      = 500;

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final BlockingQueue< ? extends Callable<?> >          fifo;
    protected final CopyOnWriteArrayList< ? extends Callable<?> >   toCurrentObject;
    protected final CopyOnWriteArrayList< Future<?> >     resultOrderWaiting = new CopyOnWriteArrayList<>();
    protected final ExecutorService executorService;
    protected final ExecutorService executorServiceCheckout;

    protected volatile boolean running                  = false;
    protected volatile boolean stop                     = false;
    protected long maxTimeMillisByCommand               = 60 * 60 * 1000 ; // 1H00

    protected List<TimerByCommand> timerList;

    // --------------------------------------
    // -        Constructors                -
    // --------------------------------------

    /**
     *
     * @param fifo : The FIFO to read
     * @param toCurrentObject : The list of all the objects that are currently in the FIFO. This avoids duplicating existing ones.
     * @param maxPoolThread : The number of commands to be executed in parallel
     */
    public FifoController( BlockingQueue< ? extends Callable<?> > fifo,
                           CopyOnWriteArrayList< ? extends Callable<?> > toCurrentObject,
                           int maxPoolThread )
    {
        this.fifo               = fifo;
        this.toCurrentObject    = toCurrentObject;
        this.executorService    = Executors.newFixedThreadPool(maxPoolThread);
        this.executorServiceCheckout  = Executors.newFixedThreadPool(maxPoolThread + 1 );
        this.timerList          = new ArrayList<>();
    }

    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------

    @Override
    public void run ()
    {
        running = true;
        try {
            while ( ! stop )
            {
                Future<?> future;
                // Retrieving an order
                Callable<?> command = fifo.poll( TIME_OUT_MAX_FIFO, TimeUnit.MILLISECONDS );
                // Deletion of the order removed from the FIFO
                this.toCurrentObject.remove( command );

                if ( command != null ) {
                    this.getLogger().debug("{}{}{} Execute the order {}",
                            FIFO_CONTROLLER, EXECUTE, RUN, command );
                    // Execute the orders
                    future = executorService.submit( command );

                    for( TimerByCommand  timer : this.timerList ) {
                        // Increments counters
                        timer.setNewCommand();
                    }

                    // Add the list of pending results ( with maximum time )
                    this.resultOrderWaiting.add( future );
                    // Check timeOut order asynchronous
                    this.executorServiceCheckout.execute(() -> checkTimeOutCommand( future, command ));
                }

                // Vérification des compteurs / timers
                for( TimerByCommand  timer : this.timerList ) {
                    // Vérification du compteur et block si nécessaire
                    timer.checkCommandAndValidBlock();
                }

                if( this.fifo.isEmpty() && this.resultOrderWaiting.isEmpty() ) {
                    this.stop = true;
                    // No other task can be performed
                    this.executorService.shutdown();
                    this.getLogger().info("{}{}{} The FIFO is empty : {}. Request to stop the service.",
                            FIFO_CONTROLLER, EXECUTE, RUN, this.toCurrentObject );
                }

            }
        } catch ( InterruptedException e ) {
            int sizeCommand = fifo.size();
            this.getLogger().warn("{}{}{} Le controleur a été interrompu ! {} message(s) n'ont pas été envoyé(s) !",
                    FIFO_CONTROLLER, EXECUTE, STOP, sizeCommand );
        } catch (Exception ex) {
            int sizeCommand = fifo.size();
            this.getLogger().error("{}{}{} Erreur durant l'éxecution du controleur ! {} message(s) n'ont pas été envoyé(s) ! MESSAGE : {} : {}",
                    FIFO_CONTROLLER, EXECUTE, FAIL, sizeCommand, ex.getMessage(), ex.getStackTrace() );
        } finally {

            // Wait for the last threads
            if( ! this.resultOrderWaiting.isEmpty() ) {
                this.getLogger().info(LogUtil.format(FIFO_CONTROLLER, EXECUTE, "Le contrôleur attend les derniers Threads",
                        String.format("The controller wait %s ms que les derniers Threads se terminent correctement",
                                TIME_OUT_MAX_FIFO )));
                try {
                    this.executorService.awaitTermination( TIME_OUT_MAX_FIFO, TimeUnit.MILLISECONDS );
                } catch (InterruptedException e) {
                    this.getLogger().warn(LogUtil.format(FIFO_CONTROLLER, EXECUTE,
                            "Request for a sudden stop of the controller !",
                            e));
                }
            }

            if( ! this.executorService.isTerminated() ){
                this.getLogger().warn( LogUtil.format(FIFO_CONTROLLER, EXECUTE, "Some Threads will be stopped suddenly !"));
            }
            // Stopping all Threads and the service
            this.executorService.shutdownNow();

            running = false;
            stop    = false;

            this.getLogger().info("{}{}{} The controller is down.",
                    FIFO_CONTROLLER, EXECUTE, STOP);
        }
    }

    /**
     * Check the max time of execution order
     * @param future
     * @param command
     */
    protected void checkTimeOutCommand( Future<?> future, Callable<?> command )  {
        try {
            // Attend l'éxecution de la commande ( avec un temps maximum )
            future.get(this.maxTimeMillisByCommand, TimeUnit.MILLISECONDS);
        }
        catch ( InterruptedException te ){
            boolean isCancelled = future.cancel( true );
            this.getLogger().warn( LogUtil.format(  FIFO_CONTROLLER, EXECUTE, "Interruption de la commande !",
                    String.format("Commande : %s - Durée max : %s %s - Is canceled : %s",
                            command, this.maxTimeMillisByCommand, TimeUnit.MILLISECONDS , isCancelled ),
                    te ));
        }
        catch ( TimeoutException te) {
            // Annulation de la commande si celle-ci est trop longue
            boolean isCancelled = future.cancel( true );
            this.getLogger().warn( LogUtil.format(  FIFO_CONTROLLER, EXECUTE, "Timeout de la commande !",
                    String.format("Commande : %s - Durée max : %s %s - Is canceled : %s ",
                            command, this.maxTimeMillisByCommand, TimeUnit.MILLISECONDS, isCancelled ),
                    te ));
        } catch ( CancellationException ce ) {
            this.getLogger().warn( LogUtil.format(  FIFO_CONTROLLER, EXECUTE, "Annulation de la commande !",
                    String.format("Commande : %s - Durée max : %s %s", command, this.maxTimeMillisByCommand, TimeUnit.MILLISECONDS),
                    ce ));
        } catch ( ExecutionException ex ){
            this.getLogger().error( LogUtil.format(  FIFO_CONTROLLER, EXECUTE, "Erreur en interne de la commande !",
                    String.format("Commande : %s - Durée max : %s %s", command, this.maxTimeMillisByCommand, TimeUnit.MILLISECONDS),
                    ex ));
        }
        catch ( Exception ex ){
            this.getLogger().error( LogUtil.format( FIFO_CONTROLLER, EXECUTE,
                    "Erreur pendant l'execution d'une commande !",
                    String.format("Commande : %s", command ),
                    ex
            ));
        }
        finally {
            if ( ! future.isDone() && ! future.isCancelled() ) {
                // Annulation de la commande si celle-ci est trop longue
                future.cancel( true );
                this.getLogger().warn("{}{}{} Commande non terminée : {} ! Durée max : {} {} - Tâche terminée : {} - Tâche annulée : {}",
                        FIFO_CONTROLLER, EXECUTE, RUN, command, this.maxTimeMillisByCommand, TimeUnit.MILLISECONDS, future.isDone(),
                        future.isCancelled() );
            }
            // Supprime de la liste des commandes en attente
            this.resultOrderWaiting.remove( future );
        }
    }

    /**
     * Stop the controller
     *
     * The controller wiil be stop in (TIME_OUT_MAX_FIFO x 2) ms MAX
     */
    public void stopController()
    {
        this.stop = true;
        this.getLogger().info("{}{}{} Request to stop the controller.",
                FIFO_CONTROLLER, EXECUTE, STOP);
    }

    /**
     *
     * @return
     */
    public boolean isStopped()
    {
        return ! this.running;
    }

    /**
     * Limit the number of orders to be executed
     *
     * @param numberOfCommand : the number of orders
     * @param time            : the time
     * @param timeUnit        : the unit of time
     */
    public void addCounter( int numberOfCommand, long time, TimeUnit timeUnit )
    {
        TimeUnit timeConvert    = TimeUnit.MILLISECONDS;
        long timer              = timeConvert.convert( time, timeUnit );
        this.timerList.add( new TimerByCommand( numberOfCommand, timer ));
    }

    /**
     *
     * @param timerCommand
     */
    public void addCounter( TimerCommand timerCommand)
    {
        this.addCounter( timerCommand.getNumberOfCommand(),
                timerCommand.getUnitTime(),
                timerCommand.getTimeConvert() );
    }

    public void setMaxTimerCommand( long time, TimeUnit timeUnit )
    {
        TimeUnit timeConvert    = TimeUnit.MILLISECONDS;
        this.maxTimeMillisByCommand = timeConvert.convert( time, timeUnit );
    }

    public void setMaxTimerCommand( TimeOutCommand timeOutCommand )
    {
        this.setMaxTimerCommand( timeOutCommand.getUnitTime(),
                timeOutCommand.getTimeConvert() );
    }

    /**
     * Le logger
     * @return
     */
    protected Logger getLogger() {
        return this.logger;
    }

}
