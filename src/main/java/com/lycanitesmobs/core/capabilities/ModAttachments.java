package com.lycanitesmobs.core.capabilities;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Data attachments replacing the Forge entity/player capabilities.
 * Saved with the entity via the existing readNBT/writeNBT methods.
 * Player data is not copied on death here: ExtendedPlayer restores itself from its own backup on clone.
 */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LycanitesMobs.MODID);

    public static final Supplier<AttachmentType<ExtendedEntity>> EXTENDED_ENTITY = ATTACHMENT_TYPES.register("entity",
            () -> AttachmentType.builder(ExtendedEntity::new).serialize(new IAttachmentSerializer<CompoundTag, ExtendedEntity>() {
                @Override
                public ExtendedEntity read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                    ExtendedEntity extendedEntity = new ExtendedEntity();
                    extendedEntity.readNBT(tag);
                    return extendedEntity;
                }

                @Override
                public CompoundTag write(ExtendedEntity attachment, HolderLookup.Provider provider) {
                    CompoundTag tag = new CompoundTag();
                    attachment.writeNBT(tag);
                    return tag;
                }
            }).build());

    public static final Supplier<AttachmentType<ExtendedPlayer>> EXTENDED_PLAYER = ATTACHMENT_TYPES.register("player",
            () -> AttachmentType.builder(ExtendedPlayer::new).serialize(new IAttachmentSerializer<CompoundTag, ExtendedPlayer>() {
                @Override
                public ExtendedPlayer read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                    ExtendedPlayer extendedPlayer = new ExtendedPlayer();
                    extendedPlayer.readNBT(tag);
                    return extendedPlayer;
                }

                @Override
                public CompoundTag write(ExtendedPlayer attachment, HolderLookup.Provider provider) {
                    CompoundTag tag = new CompoundTag();
                    attachment.writeNBT(tag);
                    return tag;
                }
            }).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
