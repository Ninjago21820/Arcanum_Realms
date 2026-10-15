package com.arcanumrealms.registry;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.entity.WizardNpcEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Entites du mod : pour l'instant, les PNJ qui donnent les quetes. */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ArcanumRealms.MOD_ID);

    public static final RegistryObject<EntityType<WizardNpcEntity>> WIZARD_NPC =
            ENTITIES.register("wizard_npc", () -> EntityType.Builder
                    .of(WizardNpcEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(10)
                    .build("arcanum_realms:wizard_npc"));

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(ModEntities::attributes);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(WIZARD_NPC.get(), WizardNpcEntity.createAttributes().build());
    }
}
