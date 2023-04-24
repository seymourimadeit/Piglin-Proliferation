package tallestred.piglinproliferation.common.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.Level;

public class PiglinTraveller extends Piglin {
    public PiglinTraveller(EntityType<? extends AbstractPiglin> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
}
