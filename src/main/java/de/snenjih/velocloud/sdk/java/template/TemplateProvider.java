package de.snenjih.velocloud.sdk.java.template;

import de.snenjih.velocloud.sdk.java.utils.FutureConverter;
import de.snenjih.velocloud.shared.template.SharedTemplateProvider;
import de.snenjih.velocloud.shared.template.Template;
import de.snenjih.velocloud.v1.templates.TemplateControllerGrpc;
import de.snenjih.velocloud.v1.templates.TemplateFindRequest;
import io.grpc.ManagedChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class TemplateProvider implements SharedTemplateProvider<Template> {

    private final TemplateControllerGrpc.TemplateControllerFutureStub futureStub;
    private final TemplateControllerGrpc.TemplateControllerBlockingStub blockingStub;

    public TemplateProvider(ManagedChannel channel) {
        this.blockingStub = TemplateControllerGrpc.newBlockingStub(channel);
        this.futureStub = TemplateControllerGrpc.newFutureStub(channel);
    }

    @Override
    public @NotNull List<Template> findAll() {
        return blockingStub.find(TemplateFindRequest.getDefaultInstance()).getTemplateList().stream().map(Template.Companion::from).toList();
    }

    @Override
    @Nullable
    public Template find(@NotNull String name) {
        return blockingStub.find(TemplateFindRequest.newBuilder().setName(name).build()).getTemplateList().stream().map(Template.Companion::from).findFirst().orElse(null);
    }

    @Override
    public @NotNull CompletableFuture<List<Template>> findAllAsync() {
        return FutureConverter.completableFromGuava(futureStub.find(TemplateFindRequest.getDefaultInstance()), it -> it.getTemplateList().stream().map(Template.Companion::from).toList());
    }

    @Override
    @NotNull
    public CompletableFuture<Template> findAsync(@NotNull String name) {
        return FutureConverter.completableFromGuava(futureStub.find(TemplateFindRequest.newBuilder().setName(name).build()), it -> it.getTemplateList().stream().map(Template.Companion::from).findFirst().orElse(null));
    }
}
