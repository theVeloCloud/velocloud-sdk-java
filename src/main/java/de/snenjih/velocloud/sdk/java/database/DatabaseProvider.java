package de.snenjih.velocloud.sdk.java.database;

import com.google.protobuf.Empty;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.snenjih.velocloud.v1.database.DatabaseConfigControllerGrpc;
import de.snenjih.velocloud.v1.database.DatabaseConfigResponse;
import io.grpc.ManagedChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;

/**
 * Provides database access for bridge plugins.
 *
 * Use {@link #cloudDataSource()} for the shared cloud DB (null if Agent has none configured),
 * or {@link #createCustomDataSource(DatabaseConfig)} for a plugin-local pool.
 * Call {@link #close()} during plugin disable to release the cloud pool.
 */
public final class DatabaseProvider {

    private final DatabaseConfigControllerGrpc.DatabaseConfigControllerBlockingStub stub;

    @Nullable private volatile HikariDataSource cloudPool;
    private volatile boolean cloudPoolInitialized = false;

    public DatabaseProvider(@NotNull ManagedChannel channel) {
        this.stub = DatabaseConfigControllerGrpc.newBlockingStub(channel);
    }

    /**
     * Returns the shared cloud DataSource (lazy, cached).
     * Returns null if the Agent has no database configured.
     */
    @Nullable
    public DataSource cloudDataSource() {
        if (!cloudPoolInitialized) {
            synchronized (this) {
                if (!cloudPoolInitialized) {
                    DatabaseConfigResponse r =
                        stub.fetchCloudDatabaseConfig(Empty.getDefaultInstance());
                    if (r.getConfigured()) {
                        cloudPool = buildPool(
                            r.getHost(), r.getPort(), r.getDatabase(),
                            r.getUsername(), r.getPassword(),
                            r.getPoolSize(), "VeloCloud-CloudDB-SDK"
                        );
                    }
                    cloudPoolInitialized = true;
                }
            }
        }
        return cloudPool;
    }

    /**
     * Creates an independent plugin-local HikariCP pool from the provided config.
     * The caller is responsible for closing the returned DataSource.
     */
    @NotNull
    public HikariDataSource createCustomDataSource(@NotNull DatabaseConfig config) {
        return buildPool(
            config.getHost(), config.getPort(), config.getDatabase(),
            config.getUsername(), config.getPassword(),
            config.getPoolSize(), "VeloCloud-PluginDB"
        );
    }

    /** Closes the cached cloud pool. Call this during plugin disable. */
    public void close() {
        synchronized (this) {
            if (cloudPool != null && !cloudPool.isClosed()) {
                cloudPool.close();
            }
        }
    }

    private HikariDataSource buildPool(String host, int port, String db,
                                        String user, String pass, int size, String name) {
        HikariConfig c = new HikariConfig();
        c.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db
                     + "?useSSL=false&autoReconnect=true&characterEncoding=utf8");
        c.setUsername(user);
        c.setPassword(pass);
        c.setMaximumPoolSize(size);
        c.setMinimumIdle(1);
        c.setConnectionTimeout(10_000);
        c.setIdleTimeout(60_000);
        c.setMaxLifetime(1_800_000);
        c.setPoolName(name);
        return new HikariDataSource(c);
    }
}
