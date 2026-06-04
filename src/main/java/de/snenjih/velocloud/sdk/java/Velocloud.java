package de.snenjih.velocloud.sdk.java;

import de.snenjih.velocloud.sdk.java.database.DatabaseProvider;
import de.snenjih.velocloud.sdk.java.events.EventProvider;
import de.snenjih.velocloud.sdk.java.groups.GroupProvider;
import de.snenjih.velocloud.sdk.java.platform.PlatformProvider;
import de.snenjih.velocloud.sdk.java.player.PlayerProvider;
import de.snenjih.velocloud.sdk.java.services.ServiceProvider;
import de.snenjih.velocloud.sdk.java.information.CloudInformationProvider;
import de.snenjih.velocloud.sdk.java.template.TemplateProvider;
import de.snenjih.velocloud.shared.VelocloudShared;
import de.snenjih.velocloud.shared.events.SharedEventProvider;
import de.snenjih.velocloud.shared.groups.Group;
import de.snenjih.velocloud.shared.groups.SharedGroupProvider;
import de.snenjih.velocloud.shared.platform.Platform;
import de.snenjih.velocloud.shared.platform.SharedPlatformProvider;
import de.snenjih.velocloud.shared.player.VelocloudPlayer;
import de.snenjih.velocloud.shared.player.SharedPlayerProvider;
import de.snenjih.velocloud.shared.service.Service;
import de.snenjih.velocloud.shared.service.SharedServiceProvider;
import de.snenjih.velocloud.shared.information.SharedCloudInformationProvider;
import de.snenjih.velocloud.shared.information.CloudInformation;
import de.snenjih.velocloud.shared.template.SharedTemplateProvider;
import de.snenjih.velocloud.shared.template.Template;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel;
import org.jetbrains.annotations.NotNull;

public final class Velocloud extends VelocloudShared {

    private static Velocloud instance;

    private final SharedEventProvider eventProvider;
    private final SharedServiceProvider<Service> serviceProvider;
    private final SharedGroupProvider<Group> groupProvider;
    private final SharedPlayerProvider<VelocloudPlayer> playerProvider;
    private final SharedCloudInformationProvider<CloudInformation> cloudInformationProvider;
    private final SharedPlatformProvider<Platform> platformProvider;
    private final SharedTemplateProvider<Template> templateProvider;
    private final DatabaseProvider databaseProvider;

    private final String serviceName;

    public static Velocloud instance() {
        if (instance == null) {
            new Velocloud();
        }
        return instance;
    }

    Velocloud() {
        this(null,
                System.getenv().containsKey("agent_hostname") ? System.getenv("agent_hostname") : "127.0.0.1",
                System.getenv().containsKey("agent_port") ? Integer.parseInt(System.getenv("agent_port")) : 8932,
                true); // use default port for standalone
        instance = this;
    }

    //for off premise bridges
    public Velocloud(String serviceName, String agentHostname, int agentPort, boolean setShared) {
        super(setShared);
        this.serviceName = serviceName;

        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);

        ManagedChannel channel = NettyChannelBuilder
                .forAddress(agentHostname, agentPort)
                .usePlaintext()
                .channelType(NioSocketChannel.class)
                .eventLoopGroup(eventLoopGroup)
                .build();


        this.eventProvider = new EventProvider(channel, this);
        this.serviceProvider = new ServiceProvider(channel);
        this.groupProvider = new GroupProvider(channel);
        this.playerProvider = new PlayerProvider(channel);
        this.cloudInformationProvider = new CloudInformationProvider(channel);
        this.platformProvider = new PlatformProvider(channel);
        this.templateProvider = new TemplateProvider(channel);
        this.databaseProvider = new DatabaseProvider(channel);
    }

    public DatabaseProvider databaseProvider() {
        return this.databaseProvider;
    }

    public String selfServiceName() {
        if (serviceName != null) {
            return serviceName;
        }
        return System.getenv("service-name");
    }

    @Override
    public @NotNull SharedEventProvider eventProvider() {
        return this.eventProvider;
    }

    @Override
    public @NotNull SharedServiceProvider<Service> serviceProvider() {
        return this.serviceProvider;
    }

    @Override
    @NotNull
    public SharedGroupProvider<Group> groupProvider() {
        return this.groupProvider;
    }

    @Override
    public @NotNull SharedPlayerProvider<?> playerProvider() {
        return this.playerProvider;
    }

    @Override
    public @NotNull SharedCloudInformationProvider<?> cloudInformationProvider() {
        return this.cloudInformationProvider;
    }

    @Override
    @NotNull
    public SharedPlatformProvider<?> platformProvider() {
        return this.platformProvider;
    }

    @Override
    public @NotNull SharedTemplateProvider<?> templateProvider() {
        return this.templateProvider;
    }
}
