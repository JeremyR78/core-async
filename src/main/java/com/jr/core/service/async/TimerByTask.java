package com.jr.core.service.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.jr.core.common.async.CoreAsyncNameSpace.*;

public class TimerByTask implements Serializable {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private final transient Logger logger  = LoggerFactory.getLogger(getClass());
    private final int         numberOfCommand;
    private final long        timeInMillisecond;
    private final List<Date>  counterDateList;


    // --------------------------------------
    // -        Constructors                -
    // --------------------------------------

    /**
     *
     * @param numberOfCommand
     * @param timeInMillisecond
     */
    public TimerByTask(int numberOfCommand, long timeInMillisecond )
    {
        this.counterDateList    = new ArrayList<>();
        this.numberOfCommand    = numberOfCommand;
        this.timeInMillisecond  = timeInMillisecond;
    }

    public TimerByTask(int numberOfCommand, long time, TimeUnit timeUnit )
    {
        this.counterDateList    = new ArrayList<>();
        this.numberOfCommand    = numberOfCommand;
        this.timeInMillisecond  = timeUnit.convert( time, TimeUnit.MILLISECONDS );
    }

    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------

    /**
     * Add a new order and validate the number
     *
     * @return
     */
    public boolean addNewCommandAndValid()
    {
        this.setNewCommand();
        this.check();
        return this.valid();
    }

    /**
     * Add a new order and validate the number
     * Block the time to wait
     */
    public void checkCommandAndValidBlock()
    {
        this.check();
        if( ! this.valid() )
        {
            this.getWaitTime();
        }
    }

    /**
     * Incrementing the command counter
     */
    public void setNewCommand()
    {
        // Nouvelle commande
        this.counterDateList.add( new Date() );
    }

    /**
     *  Deletion of old orders
     */
    public void check()
    {
        Date obsoleteDate         = this.getObsoleteDate();
        List<Date> dateToRemove   = new ArrayList<>();

        // Checking the list
        for( Date dateCommand : this.counterDateList ) {
            if( obsoleteDate.after( dateCommand ) ) {
                dateToRemove.add( dateCommand );
            }
        }

        this.getLogger().debug("{}{} Obsolescence date : {} -> Upstream order(s) date to be deleted : {} . All other dates : {} ",
                TASK, CHECK, obsoleteDate, dateToRemove, this.counterDateList );

        // Removal of obsolete dates
        this.counterDateList.removeAll( dateToRemove );
    }

    /**
     *
     * @return
     */
    public Date getFutureDate()
    {
        if( ! this.counterDateList.isEmpty() )
        {
            int size        = this.counterDateList.size();
            Date lastDate   = this.counterDateList.get( size - 1 );
            Calendar cal    = new GregorianCalendar();
            cal.setTime( lastDate );
            // Ajout du temps à attendre à la dernière date d'éxécution de commande
            cal.add( Calendar.MILLISECOND, (int) this.timeInMillisecond );
            return cal.getTime();
        }
        return new Date();
    }

    public Date getObsoleteDate()
    {
        Date now        = new Date();
        Calendar cal    = new GregorianCalendar();
        cal.setTime( now );
        // Supprime le temps max du compteur
        cal.add( Calendar.MILLISECOND, - (int) this.timeInMillisecond );
        return cal.getTime();
    }


    /**
     * Order number
     * @return
     */
    public int getSize( )
    {
        return this.counterDateList.size();
    }

    /**
     * The number of orders is less than authorized
     *
     * @return
     */
    public boolean valid()
    {
        if( this.getSize() >= this.numberOfCommand )
        {
            this.getLogger().debug("{}{} The maximum number of commands over a given time is reached ! Order number : {} ! Number of authorized orders {}  ",
                    TASK, VALID, this.getSize(), this.numberOfCommand);
            return false;
        }
        this.getLogger().debug("{}{} Order number : {} ! Number of authorized orders {}  ",
                TASK, VALID, this.getSize(), this.numberOfCommand);
        return true;
    }

    /**
     * Wait for the minimum time between the first order and the deadline
     */
    public void getWaitTime()
    {
        if( ! this.counterDateList.isEmpty() )
        {
            Date dateLimit = this.getFutureDate();
            Date now = this.counterDateList.get(0);

            long diffInMillies = Math.abs(  dateLimit.getTime() - now.getTime() );

            this.getLogger().debug( "{}{} Temps à attendre avant la prochaine commande : {} ms. Date actuelle : {} - Date limite : {}",
                    TASK, WAIT, diffInMillies, now, dateLimit );

            try {
                // Attend
                Thread.sleep( diffInMillies );
            }
            catch ( InterruptedException ex )
            {
                // Cancel
            }
        }
    }


    /**
     * The logger
     * @return Logger
     */
    protected Logger getLogger()
    {
        return this.logger;
    }

}
