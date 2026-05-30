package de.snenjih.velocloud.sdk.java.events;

import de.snenjih.velocloud.sdk.java.Velocloud;
import de.snenjih.velocloud.shared.events.Event;
import de.snenjih.velocloud.shared.events.EventCallback;
import de.snenjih.velocloud.shared.events.SharedEventProvider;
import de.snenjih.velocloud.v1.proto.EventProviderGrpc;
import de.snenjih.velocloud.v1.proto.EventProviderOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jetbrains.annotations.NotNull;

public final class EventProvider extends SharedEventProvider {

    private final EventProviderGrpc.EventProviderStub eventStub;
    private final Velocloud velocloud;

    public EventProvider(ManagedChannel channel, Velocloud velocloud) {
        this.eventStub = EventProviderGrpc.newStub(channel);
        this.velocloud = velocloud;
    }

    @Override
    public void call(@NotNull Event event) {
        EventProviderOuterClass.EventContext request = EventProviderOuterClass.EventContext.newBuilder()
                .setEventName(event.getClass().getSimpleName())
                .setEventData(getGsonSerializer().toJson(event))
                .build();

        eventStub.call(request, new StreamObserver<>() {
            @Override
            public void onNext(EventProviderOuterClass.CallEventResponse response) {
                if (!response.getSuccess()) {
                    System.err.println("Failed to call event: " + response.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                if (!isCancellation(t)) {
                    return;
                }
                System.err.println("Error while calling event: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // No action needed on completion
            }
        });
    }

    @Override
    public <T extends Event> void subscribe(@NotNull Class<T> eventType, @NotNull EventCallback<T> result) {
        EventProviderOuterClass.EventSubscribeRequest request =
                EventProviderOuterClass.EventSubscribeRequest.newBuilder()
                        .setServiceName(velocloud.selfServiceName())
                        .setEventName(eventType.getSimpleName())
                        .build();

        eventStub.withWaitForReady().subscribe(request, new StreamObserver<>() {
            @Override
            public void onNext(EventProviderOuterClass.EventContext context) {
                result.call(getGsonSerializer().fromJson(context.getEventData(), eventType));
            }

            @Override
            public void onError(Throwable t) {
                if (isCancellation(t)) {
                    return;
                }

                System.err.println("Error while subscribing to event: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // No action needed on completion
            }
        });
    }

    private boolean isCancellation(Throwable t) {
        return t instanceof StatusRuntimeException &&
                ((StatusRuntimeException) t).getStatus().getCode() == Status.Code.CANCELLED;
    }
}
