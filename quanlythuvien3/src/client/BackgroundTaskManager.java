package client;

import javax.swing.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Background Task Manager with SwingWorker
 * Prevents UI freezing and handles long operations properly
 */
public class BackgroundTaskManager {
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 5;
    private static final int KEEP_ALIVE_TIME = 60;
    
    private static BackgroundTaskManager instance;
    private ExecutorService executorService;
    private volatile boolean isShutdown = false;
    
    private BackgroundTaskManager() {
        initializeExecutor();
    }
    
    public static synchronized BackgroundTaskManager getInstance() {
        if (instance == null) {
            instance = new BackgroundTaskManager();
        }
        return instance;
    }
    
    private void initializeExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int counter = 0;
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "BackgroundTask-" + (++counter));
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };
        
        executorService = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            threadFactory
        );
        
        System.out.println("Background task manager initialized");
    }
    
    /**
     * Execute task with loading dialog
     */
    public <T> void executeWithLoading(
            JFrame parent,
            String loadingMessage,
            BackgroundTask<T> task,
            Consumer<T> onSuccess,
            Consumer<Exception> onError) {
        
        if (isShutdown) {
            return;
        }
        
    final JDialog[] loadingDialogRef = new JDialog[1];
    try {
        loadingDialogRef[0] = LoadingUtils.showLoadingDialog(parent, loadingMessage);
    } catch (Throwable t) {
        loadingDialogRef[0] = null;
    }
    final boolean haveDialog = loadingDialogRef[0] != null;
        
        SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.execute();
            }
            
            @Override
            protected void done() {
                try {
                    if (haveDialog) {
                        try {
                            JDialog dlg = loadingDialogRef[0];
                            if (dlg != null) {
                                dlg.setVisible(false);
                                dlg.dispose();
                            }
                        } catch (Exception ignored) {}
                    }

                    if (!isCancelled()) {
                        T result = get();
                        if (onSuccess != null) {
                            onSuccess.accept(result);
                        }
                    }
                } catch (Exception e) {
                    if (onError != null) {
                        onError.accept(e);
                    } else {
                        handleDefaultError(parent, e);
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Execute task with progress dialog
     */
    public <T> void executeWithProgress(
            JFrame parent,
            String loadingMessage,
            ProgressTask<T> task,
            Consumer<T> onSuccess,
            Consumer<Exception> onError) {
        
        if (isShutdown) {
            return;
        }
        
        final JDialog[] loadingDialogRefProg = new JDialog[1];
        try {
            loadingDialogRefProg[0] = LoadingUtils.showLoadingDialog(parent, loadingMessage);
        } catch (Throwable t) {
            loadingDialogRefProg[0] = null;
        }
        final boolean haveDialogProg = loadingDialogRefProg[0] != null;
        
        SwingWorker<T, Integer> worker = new SwingWorker<T, Integer>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.execute(this::setProgress);
            }
            
            @Override
            protected void process(java.util.List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    int progress = chunks.get(chunks.size() - 1);
                    if (haveDialogProg) {
                        // No-op: LoadingUtils returns a simple JDialog with a JProgressBar that isn't exposed.
                        // If a richer LoadingDialog is provided, it will be handled there.
                    }
                }
            }
            
            @Override
            protected void done() {
                try {
                    if (haveDialogProg) {
                        try {
                            JDialog dlg = loadingDialogRefProg[0];
                            if (dlg != null) {
                                dlg.setVisible(false);
                                dlg.dispose();
                            }
                        } catch (Exception ignored) {}
                    }

                    if (!isCancelled()) {
                        T result = get();
                        if (onSuccess != null) {
                            onSuccess.accept(result);
                        }
                    }
                } catch (Exception e) {
                    if (onError != null) {
                        onError.accept(e);
                    } else {
                        handleDefaultError(parent, e);
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Execute simple background task without loading
     */
    public <T> void executeAsync(
            BackgroundTask<T> task,
            Consumer<T> onSuccess,
            Consumer<Exception> onError) {
        
        if (isShutdown) {
            return;
        }
        
        SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.execute();
            }
            
            @Override
            protected void done() {
                try {
                    if (!isCancelled()) {
                        T result = get();
                        if (onSuccess != null) {
                            SwingUtilities.invokeLater(() -> onSuccess.accept(result));
                        }
                    }
                } catch (Exception e) {
                    if (onError != null) {
                        SwingUtilities.invokeLater(() -> onError.accept(e));
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Execute periodic task (like auto-refresh)
     */
    public ScheduledFuture<?> schedulePeriodicTask(
            Runnable task,
            long initialDelay,
            long period,
            TimeUnit timeUnit) {
        
        if (executorService instanceof ScheduledExecutorService) {
            return ((ScheduledExecutorService) executorService)
                    .scheduleAtFixedRate(task, initialDelay, period, timeUnit);
        } else {
            // Fallback using Timer
            Timer timer = new Timer((int) timeUnit.toMillis(period), e -> {
                if (!isShutdown) {
                    SwingUtilities.invokeLater(task);
                }
            });
            
            Timer initialTimer = new Timer((int) timeUnit.toMillis(initialDelay), e -> {
                timer.start();
                ((Timer) e.getSource()).stop();
            });
            initialTimer.setRepeats(false);
            initialTimer.start();
            
            return null; // Cannot return ScheduledFuture for Timer
        }
    }
    
    /**
     * Default error handler
     */
    private void handleDefaultError(JFrame parent, Exception e) {
        SwingUtilities.invokeLater(() -> {
            String message = "Đã xảy ra lỗi: " + e.getMessage();
            JOptionPane.showMessageDialog(
                parent,
                message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
            );
        });
        
        // Log error
        System.err.println("Background task error: " + e.getMessage());
        e.printStackTrace();
    }
    
    /**
     * Shutdown task manager
     */
    public synchronized void shutdown() {
        if (isShutdown) {
            return;
        }
        
        isShutdown = true;
        
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Background task manager shutdown completed");
    }
    
    /**
     * Check if manager is shutdown
     */
    public boolean isShutdown() {
        return isShutdown;
    }
    
    // Functional interfaces
    @FunctionalInterface
    public interface BackgroundTask<T> {
        T execute() throws Exception;
    }
    
    @FunctionalInterface
    public interface ProgressTask<T> {
        T execute(Consumer<Integer> progressCallback) throws Exception;
    }
    
    // Shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.shutdown();
            }
        }));
    }
}