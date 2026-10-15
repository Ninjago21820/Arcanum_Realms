package com.arcanumrealms.item;

import com.arcanumrealms.core.world.GearDef;
import com.arcanumrealms.core.world.Gears;
import com.arcanumrealms.player.WizardData;
import com.arcanumrealms.registry.ModItems;
import com.arcanumrealms.util.Language;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Objet d'equipement.
 *
 * <p>Un seul objet Minecraft represente les quarante-sept pieces du catalogue :
 * l'identifiant de l'equipement est stocke dans l'objet lui-meme (tag
 * {@code arcanum_realms:gear}), ce qui evite d'enregistrer des dizaines
 * d'objets presque identiques.
 *
 * <p>Clic droit : la piece est portee et remplace celle du meme emplacement, qui
 * revient dans l'inventaire. Clic droit en s'accroupissant : la piece est retiree.
 */
public class GearItem extends Item {

    public static final String GEAR_TAG = "arcanum_realms:gear";

    public GearItem(Properties properties) {
        super(properties);
    }

    /** Cree une piece d'equipement prete a etre donnee a un joueur. */
    public static ItemStack create(GearDef def, int count) {
        ItemStack stack = new ItemStack(ModItems.WIZARD_GEAR.get(), Math.max(1, count));
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(GEAR_TAG, def.id());
        return stack;
    }

    /** Equipement porte par cet objet (ou null si l'objet n'en designe aucun). */
    public static GearDef gearOf(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof GearItem)) return null;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(GEAR_TAG)) return null;
        return Gears.find(tag.getString(GEAR_TAG)).orElse(null);
    }

    /** Index de modele : 0 pour un objet vide, 1 a 8 pour les emplacements. */
    public static int slotIndex(ItemStack stack) {
        GearDef def = gearOf(stack);
        return def == null ? 0 : def.slot().slotId() + 1;
    }

    @Override
    public Component getName(ItemStack stack) {
        GearDef def = gearOf(stack);
        return def == null ? super.getName(stack)
                : Component.translatable("gear.arcanum_realms." + def.id());
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        GearDef def = gearOf(stack);
        if (def == null) return Rarity.COMMON;
        return switch (def.tier()) {
            case 0 -> Rarity.COMMON;
            case 1 -> Rarity.UNCOMMON;
            case 2 -> Rarity.RARE;
            default -> Rarity.EPIC;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean french = Language.isFrench();
        GearDef def = gearOf(stack);
        if (def == null) {
            tooltip.add(Component.translatable("item.arcanum_realms.wizard_gear.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("gear.arcanum_realms.slot." + def.slot().name().toLowerCase())
                .append(" - ")
                .append(Component.translatable("gear.arcanum_realms.level", def.level()))
                .withStyle(ChatFormatting.GOLD));
        String stats = def.stats(french);
        if (!stats.isEmpty()) tooltip.add(Component.literal(stats).withStyle(ChatFormatting.GRAY));
        if (def.school() != null) {
            tooltip.add(Component.translatable(def.school().langKey()).withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(Component.translatable("item.arcanum_realms.wizard_gear.equip")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    /** Prix d'achat chez le marchand : croit avec le palier et le niveau demande. */
    public static int buyPrice(GearDef def) {
        return 20 + def.level() * 5 + def.tier() * 25;
    }

    /** Texte d'aide affiche par {@code /arcanum equipement}. */
    public static String hint(boolean french) {
        return french
                ? "Clic droit : porter - Clic droit en s'accroupissant : retirer"
                : "Right-click: equip - Shift right-click: unequip";
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        GearDef def = gearOf(stack);
        if (def == null) return InteractionResultHolder.pass(stack);

        if (!level.isClientSide) {
            WizardData data = WizardData.load(player);
            if (player.isShiftKeyDown()) {
                String removed = data.unequip(def.slot());
                data.save(player);
                if (removed != null) {
                    player.displayClientMessage(Component.translatable("arcanum_realms.message.gear_removed",
                            Component.translatable("gear.arcanum_realms." + removed)), false);
                }
            } else {
                String previous = data.equip(def);
                data.save(player);
                stack.shrink(1);
                if (previous != null) {
                    Gears.find(previous).ifPresent(old ->
                            player.getInventory().placeItemBackInInventory(create(old, 1)));
                }
                player.displayClientMessage(Component.translatable("arcanum_realms.message.gear_equipped",
                        Component.translatable("gear.arcanum_realms." + def.id())), false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
