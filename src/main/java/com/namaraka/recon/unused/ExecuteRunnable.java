/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.unused;

import com.namaraka.recon.InitApp;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import com.namaraka.recon.exceptiontype.MyCustomException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class ExecuteRunnable {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteRunnable.class);


    public static CountDownLatch globalCountDownLatchToAwait;

    /**
     * Execute a file upload task
     *
     * @param uploadFileTask
     * @return 
     * @throws MyCustomException
     */
    public static Future executeTask(Runnable uploadFileTask) throws MyCustomException { //this is the count down latch this thread will await on, it will also create it's own for the next guy in queue to await

        Future futureTask;
        
        try {
            //InitApp.getFileUploadExecService().execute(uploadFileTask); //blocking method
            futureTask = InitApp.getFileUploadExecService().submit(uploadFileTask);
            
        } catch (RejectedExecutionException | NullPointerException ex) {
            
            throw new MyCustomException("Error processing task", ErrorCode.PROCESSING_ERR, ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);
        }
        
        return futureTask;
    }
    
    
    
    
    

    void uploadFilebckup(String fileDetailsJsonString, HttpServletResponse response, String responseToWrite) throws MyCustomException { //this is the count down latch this thread will await on, it will also create it's own for the next guy in queue to await

        try {
            //put some none blocking processing here like getting number of records in files
            CountDownLatch ownCountDownLatch;

            synchronized (this) {

                if (globalCountDownLatchToAwait == null) {//first task to be executed, else latch is already existent
                    globalCountDownLatchToAwait = new CountDownLatch(1);
                    globalCountDownLatchToAwait.countDown();//count it down immediately, otherwise no other thread is available to count this down
                } else {
                    globalCountDownLatchToAwait.await(); //await on this count down latch till it is released by thread holding it
                }
                ownCountDownLatch = new CountDownLatch(1);//task creates own count down task that next task will await      
                globalCountDownLatchToAwait = ownCountDownLatch; // task passes on own count down to the global count down latch for next task
            }

            logger.info(">>>>>>>> Waiting on the previous task holding on the countDownLatch............. ");

            logger.info(">>>>>>>> Previous task complete, done waiting, going to execute this task now!!!");

            ExecutorService fileUploaderExecutor = Executors.newSingleThreadExecutor();
            //Callable<Boolean> callable = new UploadFileTask<>(fileDetailsJsonString);
            Future<Boolean> future = null;
            //future = fileUploaderExecutor.submit(callable);

            future.get();

            //if future isDone, count down latch
            ownCountDownLatch.countDown();
            //or instead count down the global latch
            //GlobalCountDownLatchToAwait.countDown();

            assert (ownCountDownLatch.getCount() == globalCountDownLatchToAwait.getCount());

        } catch (CancellationException | InterruptedException | ExecutionException ex) {

            logger.error("error executing task: " + ex.getCause().getMessage());
            throw new MyCustomException("Error processing task", ErrorCode.PROCESSING_ERR, ex.getCause().getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        }

    }

}
