package tallestred.piglinproliferation.capablities;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import tallestred.piglinproliferation.PiglinProliferation;

import java.util.function.Supplier;

public class PPCapabilities {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PiglinProliferation.MODID);
    public static final Supplier<AttachmentType<String>> TRANSFORMATION_TRACKER = ATTACHMENT_TYPES.register(
            "transformation_type", () -> AttachmentType.builder(() -> "").serialize(Codec.STRING).build());
    public static final Supplier<AttachmentType<Boolean>> CRITICAL = ATTACHMENT_TYPES.register(
            "critical", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
}
