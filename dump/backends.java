// [START shutdown_1]
LifecycleManager.getInstance().setShutdownHook(new ShutdownHook() {
	public void shutdown() {
		LifecycleManager.getInstance().interruptAllRequests();
	}
});
// [END shutdown_1]

// [START shutdown_2]
while (haveMoreWork() &&
		!LifecycleManager.getInstance().isShuttingDown()) {
	doSomeWork();
	saveState();
		}
// [END shutdown_2]

// [START addressing_backends]
import com.google.appengine.api.backends.BackendService;
import com.google.appengine.api.backends.BackendServiceFactory;

BackendService backendsApi = BackendServiceFactory.getBackendService();

// Get the backend handling the current request.
String currentBackendName = backendsApi.getCurrentBackend();
// Get the backend instance handling the current request.
int currentInstance = backendsApi.getCurrentInstance();
// [END addressing_backends]

// [START background_threads]
import com.google.appengine.api.ThreadManager;
import java.util.concurrent.AtomicLong;

AtomicLong counter = new AtomicLong();

Thread thread = ThreadManager.createBackgroundThread(new Runnable() {
	public void run() {
		try {
			while (true) {
				counter.incrementAndGet();
				Thread.sleep(10);
			}
		} catch (InterruptedException ex) {
			throw new RuntimeException("Interrupted in loop:", ex);
		}
	}
});
thread.start();
// [END background_threads]
