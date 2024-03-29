package tallestred.piglinproliferation.capablities;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface TransformationSourceListener extends INBTSerializable<CompoundTag> {
    String getTransformationSource();

    void setTransformationSource(String entityName);


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
