package tallestred.piglinproliferation.common.entities.spawns;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.saveddata.SavedData;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.tags.PPTags;

import javax.annotation.Nullable;
import java.util.List;

public class TravelerSpawner {
    public static int INITIAL_DELAY = 12000; //Twice as likely as wandering trader
    public static int SPAWN_CHANCE_PERCENT = 25;
    public static MobSpawnType SPAWN_TYPE = MobSpawnType.NATURAL;
    public static int MAX_DISTANCE_FROM_PLAYER = 48;

    public static void tick(ServerLevel level, SpawnDelay spawnDelay) {
        spawnDelay.timeRemaining--;
        if (spawnDelay.timeRemaining <= 0) {
            spawnDelay.timeRemaining = INITIAL_DELAY;
            if (level.random.nextInt(100) <= SPAWN_CHANCE_PERCENT)
                spawn(level);
        }
        spawnDelay.setDirty();
    }

    private static void spawn(ServerLevel level) {
        Player player = getRandomPlayer(level);
        if (player != null && !level.getBiome(player.blockPosition()).is(PPTags.WITHOUT_TRAVELER_SPAWNS) ) {
            BlockPos pos = findSpawnPositionNear(level, player.blockPosition(), MAX_DISTANCE_FROM_PLAYER);
            if (pos != null && !level.getBiome(pos).is(PPTags.WITHOUT_TRAVELER_SPAWNS)) {
                PiglinTraveler traveler = PPEntityTypes.PIGLIN_TRAVELER.get().spawn(level, pos, SPAWN_TYPE);
                if (traveler != null)
                    traveler.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), SPAWN_TYPE, null, null);
            }
        }
    }

    private static ServerPlayer getRandomPlayer(ServerLevel level) {
        List<ServerPlayer> list = level.getPlayers(player -> player.isAlive() && !player.isSpectator());
        return list.isEmpty() ? null : list.get(level.random.nextInt(list.size()));
    }

    @Nullable
    private static BlockPos findSpawnPositionNear(ServerLevel level, BlockPos pos, int maxDistance) {
        BlockPos outputPos = null;
        SpawnPlacements.Type spawnPlacement = SpawnPlacements.getPlacementType(PPEntityTypes.PIGLIN_TRAVELER.get());
        for (int i = 0; i < 10; i++) {
            int x = pos.getX() + level.random.nextInt(maxDistance * 2) - maxDistance;
            int z = pos.getZ() + level.random.nextInt(maxDistance * 2) - maxDistance;
            BlockPos testPos = NaturalSpawner.getTopNonCollidingPos(level, PPEntityTypes.PIGLIN_TRAVELER.get(), x, z);
            if (spawnPlacement.canSpawnAt(level, testPos, PPEntityTypes.PIGLIN_TRAVELER.get())) {
                outputPos = testPos;
                break;
            }
        }
        return outputPos == null ? null : outputPos.above();
    }

    public static class SpawnDelay extends SavedData {
        private int timeRemaining = INITIAL_DELAY;

        public SpawnDelay() {
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putInt("TravelerSpawnDelay", this.timeRemaining);
            return tag;
        }

        public static SpawnDelay load(CompoundTag tag) {
            SpawnDelay delay = new SpawnDelay();
            delay.timeRemaining = tag.getInt("TravelerSpawnDelay");
            return delay;
        }

        public static Factory<SpawnDelay> factory() {
            return new Factory<>(SpawnDelay::new, (SpawnDelay::load), null);
        }
    }
}
