package tallestred.piglinproliferation.common.loot_tables;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;

import java.util.Objects;

public class CompassLocateObject {
    private final boolean isBiome;
    private final ResourceLocation location;
    private int expiryTime = 24000;
    public CompassLocateObject(boolean isBiome, ResourceLocation location) {
        this.isBiome = isBiome;
        this.location = location;
    }


    public BlockPos locateObject(ServerLevel level, BlockPos sourcePos) {
        return this.isBiome
                ? Objects.requireNonNull(level.findClosestBiome3d(holder -> holder.is(location), sourcePos, 64000, 32, 64)).getFirst()
                : level.findNearestMapStructure(TagKey.create(Registries.STRUCTURE, location), sourcePos, 50, false);
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public void decrementExpiryTime() {
        this.expiryTime--;
    }

    public boolean hasExpired() {
        return expiryTime <= 0;
    }

    @Override
    public String toString() {
        return isBiome ? "biome-" : "structure-" + location.toString();
    }
}
