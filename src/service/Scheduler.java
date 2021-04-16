package service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.quartz.SchedulerException;

import helpers.Config;
import helpers.Log;
import persistence.DB;

public class Scheduler   {
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static org.quartz.Scheduler qScheduler;
	
	public static void exit() {
		scheduler.shutdown();
		Log.logInfo("ScheduledExecutorService shut down", Scheduler.class);
		try {
			if(qScheduler != null) {
				qScheduler.shutdown(true);
				Log.logInfo("Quartz scheduler shut down", Scheduler.class);				
			}

		} catch (SchedulerException e) {
			Log.logWarning("Scheduler - Exception trying to shutdown quartz scheduler - "+e.getMessage()+" - "+e.getCause(), Scheduler.class);			
		}
		
	}

	public static void schedule() throws SchedulerException {
		Runnable task = () -> {
			try {
				Service.doChecks();
			} catch (Exception e) {
				Log.logWarning("Scheduler - Exception doing checks - "+e.getMessage()+" - "+e.getCause(), Scheduler.class);			
			}
		};
		scheduler.scheduleWithFixedDelay(task, 0, 20, TimeUnit.SECONDS);
				
		Runnable db = () -> {
			DB.dumpDatabase();
		};
		scheduler.scheduleWithFixedDelay(db, 0, Config.getDbBackupIntervalInMinutes(), TimeUnit.MINUTES);
	}

}

