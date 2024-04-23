package tallestred.piglinproliferation.capablities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public interface TransformationSourceListener extends INBTSerializable<CompoundTag> {
    String getTransformationSource();

    void setTransformationSource(String entityName);

    static TransformationSourceListener from(LivingEntity entity) {
        LazyOptional<TransformationSourceListener> listener = entity.getCapability(PPCapablities.TRANSFORMATION_SOURCE_TRACKER);
        if (listener.isPresent())
            return listener.orElseThrow(() -> new IllegalStateException("Capability not found! Report this to the piglin proliferation github!"));
        return null;
    }


    class TransformationSource implements TransformationSourceListener {
        private String entityName = "";

        @Override
        public String getTransformationSource() {
            return entityName;
        }

        @Override
        public void setTransformationSource(String entityName) {
            this.entityName = entityName;
        }

        @Override
        public CompoundTag serializeNBT() {
            final CompoundTag tag = new CompoundTag();
            tag.putString("entity_transformed_from", this.getTransformationSource());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.setTransformationSource(nbt.getString("entity_transformed_from"));
        }
    }
}
