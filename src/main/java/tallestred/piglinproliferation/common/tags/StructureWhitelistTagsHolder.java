package tallestred.piglinproliferation.common.tags;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class StructureWhitelistTagsHolder {
    public WhitelistOrBlacklistTag<Structure> structureTag;
    public WhitelistOrBlacklistTag<StructureSet> structureSetTag;
    private final List<Holder<Structure>> structures = Collections.synchronizedList(new ArrayList<>());

    public StructureWhitelistTagsHolder(Supplier<Boolean> isWhitelist, String modid, String tagPrefix) {
        structureTag = new WhitelistOrBlacklistTag<>(isWhitelist, Registries.STRUCTURE, modid, tagPrefix);
        structureSetTag = new WhitelistOrBlacklistTag<>(isWhitelist, Registries.STRUCTURE_SET, modid, tagPrefix);
    }

    public List<Holder<Structure>> structures(RegistryAccess registryAccess) {
        if (structures.isEmpty()) {
            registryAccess.registryOrThrow(Registries.STRUCTURE).getTagOrEmpty(structureTag.tag()).forEach(structures::add);
            registryAccess.registryOrThrow(Registries.STRUCTURE_SET).getTagOrEmpty(structureSetTag.tag()).forEach(h -> {
                h.value().structures().forEach(s -> structures.add(s.structure()));
            });
        }
        return structures;
    }

    public void clearCache() {
        structures.clear();
    }

    public boolean shouldUseWhitelist() {
        return structureTag.shouldUseWhitelist().get();
    }

    public boolean contains(Holder<Structure> holder, RegistryAccess registryAccess) {
        return shouldUseWhitelist() == structures(registryAccess).contains(holder);
    }
}