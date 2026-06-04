package de.snenjih.velocloud.sdk.java.database;

public final class DatabaseConfig {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final int poolSize;

    private DatabaseConfig(Builder b) {
        this.host = b.host;
        this.port = b.port;
        this.database = b.database;
        this.username = b.username;
        this.password = b.password;
        this.poolSize = b.poolSize;
    }

    public String getHost()     { return host; }
    public int    getPort()     { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int    getPoolSize() { return poolSize; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String host = "localhost";
        private int    port = 3306;
        private String database;
        private String username;
        private String password = "";
        private int    poolSize = 5;

        public Builder host(String v)     { this.host = v;     return this; }
        public Builder port(int v)        { this.port = v;     return this; }
        public Builder database(String v) { this.database = v; return this; }
        public Builder username(String v) { this.username = v; return this; }
        public Builder password(String v) { this.password = v; return this; }
        public Builder poolSize(int v)    { this.poolSize = v; return this; }

        public DatabaseConfig build() {
            if (database == null || username == null)
                throw new IllegalStateException("database and username are required");
            return new DatabaseConfig(this);
        }
    }
}
