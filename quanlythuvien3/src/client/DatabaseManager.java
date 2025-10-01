package client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Database Connection Pool Manager
 * Handles connection pooling, timeout prevention, and resource management
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:C:/data/library.db";
    private static final int MAX_CONNECTIONS = 5;
    private static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final int VALIDATION_TIMEOUT = 5; // 5 seconds
    
    private static DatabaseManager instance;
    private BlockingQueue<Connection> connectionPool;
    private volatile boolean isShutdown = false;
    
    private DatabaseManager() {
        initializeConnectionPool();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeConnectionPool() {
        connectionPool = new LinkedBlockingQueue<>(MAX_CONNECTIONS);
        
        // Pre-create connections
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            try {
                Connection conn = createNewConnection();
                connectionPool.offer(conn);
            } catch (SQLException e) {
                System.err.println("Failed to create initial connection: " + e.getMessage());
            }
        }
        
        System.out.println("Database connection pool initialized with " + connectionPool.size() + " connections");
    }
    
    private Connection createNewConnection() throws SQLException {
        String url = DB_URL + "?busy_timeout=" + CONNECTION_TIMEOUT 
                   + "&journal_mode=WAL"
                   + "&synchronous=NORMAL"
                   + "&cache_size=10000"
                   + "&temp_store=memory";
        
        Connection conn = DriverManager.getConnection(url);
        
        // Set connection properties
        conn.setAutoCommit(true);
        
        return conn;
    }
    
    /**
     * Get a connection from the pool
     * Automatically validates and recreates if necessary
     */
    public Connection getConnection() throws SQLException {
        if (isShutdown) {
            throw new SQLException("Database manager is shutdown");
        }
        
        Connection conn = null;
        
        try {
            // Try to get connection from pool with timeout
            conn = connectionPool.poll(5, TimeUnit.SECONDS);
            
            if (conn == null) {
                // Pool is empty, create new connection
                conn = createNewConnection();
            } else {
                // Validate existing connection
                if (!isConnectionValid(conn)) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        // Ignore close errors
                    }
                    conn = createNewConnection();
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
        
        return conn;
    }
    
    /**
     * Return connection to pool
     */
    public void returnConnection(Connection conn) {
        if (conn == null || isShutdown) {
            return;
        }
        
        try {
            if (isConnectionValid(conn)) {
                // Reset connection state
                conn.setAutoCommit(true);
                
                // Return to pool if there's space
                if (!connectionPool.offer(conn)) {
                    // Pool is full, close connection
                    conn.close();
                }
            } else {
                // Invalid connection, close it
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error returning connection to pool: " + e.getMessage());
            try {
                conn.close();
            } catch (SQLException closeError) {
                // Ignore close errors
            }
        }
    }
    
    /**
     * Validate connection health
     */
    private boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && 
                   !conn.isClosed() && 
                   conn.isValid(VALIDATION_TIMEOUT);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Execute operation with automatic connection management
     */
    public <T> T executeWithConnection(DatabaseOperation<T> operation) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            return operation.execute(conn);
        } finally {
            if (conn != null) {
                returnConnection(conn);
            }
        }
    }
    
    /**
     * Execute operation without return value
     */
    public void executeWithConnection(DatabaseVoidOperation operation) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            operation.execute(conn);
        } finally {
            if (conn != null) {
                returnConnection(conn);
            }
        }
    }
    
    /**
     * Shutdown connection pool
     */
    public synchronized void shutdown() {
        if (isShutdown) {
            return;
        }
        
        isShutdown = true;
        
        // Close all connections in pool
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection during shutdown: " + e.getMessage());
            }
        }
        
        System.out.println("Database connection pool shutdown completed");
    }
    
    /**
     * Get connection pool status
     */
    public String getPoolStatus() {
        return String.format("Pool Status - Available: %d, Max: %d, Shutdown: %s", 
                           connectionPool.size(), MAX_CONNECTIONS, isShutdown);
    }
    
    // Functional interfaces for database operations
    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute(Connection conn) throws SQLException;
    }
    
    @FunctionalInterface
    public interface DatabaseVoidOperation {
        void execute(Connection conn) throws SQLException;
    }
    
    // Shutdown hook to cleanup resources
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.shutdown();
            }
        }));
    }
}