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
import java.util.Optional;

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

    public static <L, R> Location elementLocation(Either<Holder<L>, Holder<R>> element) {
        if (element.left().isPresent()) {
            Optional<ResourceKey<L>> optional = element.left().get().unwrapKey();
            if (optional.isPresent())
                return new Location(optional.get().location(), true);
        } else if (element.right().isPresent()) {
            Optional<ResourceKey<R>> optional = element.right().get().unwrapKey();
            if (optional.isPresent())
                return new Location(optional.get().location(), false);
        }
        return null;
    }

    public record Location(ResourceLocation location, boolean isLeft) {
        public String serialise() {
            return (isLeft ? "L-" : "R-") + this.location;
        }

        public static Location deserialise(String serialisedElement) {
            String[] parts = serialisedElement.split("-");
            return new Location(ResourceLocation.parse(parts[1]), "L".equals(parts[0]));
        }
    }
}