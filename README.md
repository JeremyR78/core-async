# core-async

## Introduction

The purpose of this library is to simplify the completion of asynchronous tasks within an application.

## Get started

Start by creating a class that inherits from 'AsyncService'
```java
public class AsyncServiceMock extends AsyncService< ITask<?> > {

    public AsyncServiceMock(List<TimerTask> timerTaskList) {
        super(timerTaskList);
    }

    public AsyncServiceMock(int maxThread, List<TimerTask> timerTaskList, TimeOutTask timeOutTask) {
        super(maxThread, timerTaskList, timeOutTask);
    }
}
```

Define the maximum number of threads to be used, the timer between each task completion and the timeout for the task to be executed.
```java
// Number of threads to use
int maxThread = 3;

// The maximum execution time of each task
TimeOutTask timeOutTask = new TimeOutTask(2000, TimeUnit.MILLISECONDS);

// Time to respact between each task
List<TimerTask> timerByTaskList = Arrays.asList( new TimerTask( 1, 1000, TimeUnit.MILLISECONDS ) );

// Create the service
AsyncServiceMock asyncServiceMock = new AsyncServiceMock( maxThread, timerByTaskList, timeOutTask );
```

## How to create a new task

Now we must create a task.
The class must be either inherited from Callable and Serializable interface, or an abstract class named Task with basic functions.

```java
import java.io.Serializable;
import java.util.concurrent.Callable;

public class TaskSimple implements Callable<Integer>, Serializable {

    @Override
    public Integer call() throws Exception {
        // wait 5 sec
        Thread.sleep(5000);
    }
}


public class TaskMock extends Task<Integer> {

    @Override
    public Integer call() throws Exception {
        // wait 5 sec
        Thread.sleep(5000);
    }
}
```

## Execute the controller

```java
// Add task in the FIFO
asyncServiceMock.addTask( taskMock );

// This function starts the controller that will create a thread pool to read into the FIFO.
asyncServiceMock.executorAsynchronously();
```

## Running automatic tests

```shell
mvn clean install
```