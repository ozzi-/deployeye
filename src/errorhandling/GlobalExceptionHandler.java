package errorhandling;

import helpers.Log;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Log.logException(new Exception(e), this);
		e.printStackTrace();
	}
}
