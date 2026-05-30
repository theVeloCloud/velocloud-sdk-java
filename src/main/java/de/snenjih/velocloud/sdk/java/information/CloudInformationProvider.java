package de.snenjih.velocloud.sdk.java.information;

import de.snenjih.velocloud.shared.information.AggregateCloudInformation;
import de.snenjih.velocloud.shared.information.SharedCloudInformationProvider;
import de.snenjih.velocloud.shared.information.CloudInformation;
import de.snenjih.velocloud.v1.information.*;
import io.grpc.ManagedChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CloudInformationProvider implements SharedCloudInformationProvider<CloudInformation> {

    private final CloudInformationControllerGrpc.CloudInformationControllerBlockingStub blockingStub;

    public CloudInformationProvider(ManagedChannel channel) {
        this.blockingStub = CloudInformationControllerGrpc.newBlockingStub(channel);
    }

    @Override
    public @NotNull CloudInformation find() {
        return CloudInformation.Companion.from(blockingStub.find(CloudInformationFindRequest.getDefaultInstance()).getInformation(0));
    }

    @Override
    public @NotNull List<CloudInformation> find(long from, long to) {
        return blockingStub.find(CloudInformationFindRequest.newBuilder().setFrom(from).setTo(to).build()).getInformationList().stream().map(CloudInformation.Companion::from).toList();
    }

    @Override
    @NotNull
    public List<CloudInformation> findAll() {
        return blockingStub.findAll(CloudInformationFindAllRequest.getDefaultInstance()).getInformationList().stream().map(CloudInformation.Companion::from).toList();
    }

    @Override
    @NotNull
    public List<AggregateCloudInformation> findMinutes(long from, long to) {
        return blockingStub.findMinutes(CloudInformationFindMinutesRequest.newBuilder().setFrom(from).setTo(to).build()).getInformationList().stream().map(AggregateCloudInformation.Companion::from).toList();
    }

    @Override
    @NotNull
    public List<AggregateCloudInformation> findHours(long from, long to) {
        return blockingStub.findHours(CloudInformationFindHoursRequest.newBuilder().setFrom(from).setTo(to).build()).getInformationList().stream().map(AggregateCloudInformation.Companion::from).toList();
    }

    @Override
    @NotNull
    public List<AggregateCloudInformation> findDays(long from, long to) {
        return blockingStub.findDays(CloudInformationFindDaysRequest.newBuilder().setFrom(from).setTo(to).build()).getInformationList().stream().map(AggregateCloudInformation.Companion::from).toList();
    }

    @Override
    @NotNull
    public AggregateCloudInformation findAverage(long from, long to) {
        return AggregateCloudInformation.Companion.from(blockingStub.findAverage(CloudInformationFindAverageRequest.newBuilder().setFrom(from).setTo(to).build()).getInformation());
    }

    @Override
    public void cleanup(long maxAgeMillis) {
        blockingStub.cleanUp(CloudInformationCleanUpRequest.newBuilder().setMaxAgeMillis(maxAgeMillis).build());
    }
}
