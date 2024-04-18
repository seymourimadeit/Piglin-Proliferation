package tallestred.piglinproliferation.common.tags;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class EitherTag<L, R> {
    public final TagKey<L> leftTag;
    public final TagKey<R> rightTag;
    private final ResourceKey<Registry<L>> leftRegistry;
    private final ResourceKey<Registry<R>> rightRegistry;

    public EitherTag(ResourceKey<Registry<L>> leftRegistry, ResourceKey<Registry<R>> rightRegistry, ResourceLocation name) {
        this.leftRegistry = leftRegistry;
        this.rightRegistry = rightRegistry;
        this.leftTag = TagKey.create(leftRegistry, name);
        this.rightTag = TagKey.create(rightRegistry, name);
    }

    public Iterable<Holder<L>> leftValues(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(leftRegistry).getTagOrEmpty(leftTag);
    }

    public Iterable<Holder<R>> rightValues(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(rightRegistry).getTagOrEmpty(rightTag);
    }

    public List<Either<Holder<L>, Holder<R>>> combinedValues(RegistryAccess registryAccess) {
        List<Either<Holder<L>, Holder<R>>> list = new ArrayList<>();
        for (Holder<L> value : leftValues(registryAccess))
            list.add(Either.left(value));
        for (Holder<R> value : rightValues(registryAccess))
            list.add(Either.right(value));
        return list;
    }

    public static <L, R> ResourceLocation elementLocation(Either<Holder<L>, Holder<R>> element) {
        AtomicReference<ResourceLocation> returnValue = new AtomicReference<>();
        element.ifLeft(e -> e.unwrapKey().ifPresent(r -> returnValue.set(r.location())));
        element.ifRight(e -> e.unwrapKey().ifPresent(r -> returnValue.set(r.location())));
        return returnValue.get();
    }

    public static <L, R> String serialisedElement(Either<Holder<L>, Holder<R>> element) {
        return (element.left().isPresent() ? "L-" : "R-") + elementLocation(element);
    }

    public Either<Holder<L>, Holder<R>> deserialisedElement(String serialisedElement, RegistryAccess registryAccess) {
        String[] parts = serialisedElement.split("-");
        ResourceLocation location = new ResourceLocation(parts[1]);
        if ("L".equals(parts[0]))
            return Either.left(Holder.direct(registryAccess.registryOrThrow(leftRegistry).getOptional(location).orElseThrow()));
        else return Either.right(Holder.direct(registryAccess.registryOrThrow(rightRegistry).getOptional(location).orElseThrow()));
    }
}