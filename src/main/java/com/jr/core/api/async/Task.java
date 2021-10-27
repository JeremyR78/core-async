package com.jr.core.api.async;

import com.jr.core.common.async.TaskResultStatus;
import com.jr.core.service.async.Observable;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Task<T> extends Observable implements ITask<T>, Serializable {

    // --------------------------------------
    // -        Attributes                  -
    // --------------------------------------

    private static final SimpleDateFormat FORMAT_DATE_UUID = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
    private final String id = String.format("%s_#_%s", FORMAT_DATE_UUID.format( new Date() ), UUID.randomUUID() );

    @Getter(AccessLevel.PROTECTED)
    private final transient Logger logger   = LoggerFactory.getLogger(getClass());
    private final List<String> message      = new ArrayList<>();
    @Setter(AccessLevel.PROTECTED)
    private TaskResultStatus status;
    @Setter(AccessLevel.PROTECTED)
    private T result;
    private int percent = 0;
    @Setter(AccessLevel.PROTECTED)
    private Date started;
    @Setter(AccessLevel.PROTECTED)
    private Date finished;

    // --------------------------------------
    // -        Constructor                 -
    // --------------------------------------


    // --------------------------------------
    // -        Methods                     -
    // --------------------------------------

    protected void setPercent( int percent ){
        int oldPercent = this.percent;
        this.percent = percent;
        this.observers.firePropertyChange("percent", oldPercent, percent );
    }

    /**
     *
     * @return
     */
    public Long getDateDiff(){
        Date started = this.getStarted();
        Date finished = this.getFinished();
        long startedMilli = started != null ? started.getTime() : 0;
        long finishedMilli = finished != null ? finished.getTime() : 0;
        return finished != null ? finishedMilli - startedMilli : 0;
    }

}
