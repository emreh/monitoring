package ir.myhome.agent.queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Worker ای که از MonitorQueue آیتم می‌گیرد و با استفاده از یک RecordHandler پردازش می‌کند.
 */
public class QueueWorker<T> {

    public interface RecordHandler<T> {
        void handle(T record) throws Exception;
    }

    private final MonitorQueue<T> queue;
    private final RecordHandler<T> handler;
    private final ExecutorService workerPool;
    private final Thread loopThread;
    private volatile boolean running = true;

    public QueueWorker(MonitorQueue<T> queue, RecordHandler<T> handler) {
        this.queue = queue;
        this.handler = handler;
        this.workerPool = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
        this.loopThread = new Thread(this::loop, "monitor-queue-loop");
    }

    public void start() {
        loopThread.start();
    }

    private void loop() {
        while (running && !queue.isShutdown()) {
            try {
                T item = queue.take();
                workerPool.submit(() -> {
                    try {
                        handler.handle(item);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop(long timeoutSeconds) {
        running = false;
        queue.shutdown();
        loopThread.interrupt();
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
