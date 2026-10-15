package com.arcanumrealms.command;

import com.arcanumrealms.ArcanumConfig;
import com.arcanumrealms.battle.ServerBattleManager;
import com.arcanumrealms.core.minion.Creatures;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.player.WizardData;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Commandes du mod : {@code /arcanum ...}
 *
 * <p>Toutes les commandes sont accessibles a n'importe quel joueur (usage entre amis) :
 * c'est ce qui permet de tester et de regler les duels facilement.
 */
public class ArcanumCommand {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("arcanum")
                .executes(ctx -> help(ctx.getSource()));

        root.then(Commands.literal("help").executes(ctx -> help(ctx.getSource())));
        root.then(Commands.literal("info").executes(ctx -> info(ctx.getSource())));

        // --- duels ---
        root.then(Commands.literal("duel")
                .executes(ctx -> duel(ctx.getSource())));
        root.then(Commands.literal("arena")
                .executes(ctx -> arena(ctx.getSource())));
        root.then(Commands.literal("resolve")
                .executes(ctx -> resolve(ctx.getSource())));
        root.then(Commands.literal("leave")
                .executes(ctx -> leave(ctx.getSource())));
        root.then(Commands.literal("noauto")
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> noAuto(ctx.getSource(), BoolArgumentType.getBool(ctx, "value")))));

        // --- sorcier ---
        root.then(Commands.literal("school")
                .then(Commands.argument("ecole", StringArgumentType.word())
                        .executes(ctx -> setSchool(ctx.getSource(), StringArgumentType.getString(ctx, "ecole")))));
        root.then(Commands.literal("level")
                .then(Commands.argument("niveau", IntegerArgumentType.integer(1, 100))
                        .executes(ctx -> setLevel(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "niveau")))));
        root.then(Commands.literal("powerpips")
                .then(Commands.argument("pourcent", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> setPowerPips(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "pourcent")))));
        root.then(Commands.literal("crit")
                .then(Commands.argument("score", IntegerArgumentType.integer(0, 10000))
                        .executes(ctx -> setCrit(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "score")))));
        root.then(Commands.literal("grimoire")
                .executes(ctx -> grimoire(ctx.getSource())));
        root.then(Commands.literal("deck")
                .then(Commands.literal("reset").executes(ctx -> resetDeck(ctx.getSource())))
                .then(Commands.literal("info").executes(ctx -> deckInfo(ctx.getSource()))));
        root.then(Commands.literal("spells")
                .executes(ctx -> listSpells(ctx.getSource(), null))
                .then(Commands.argument("ecole", StringArgumentType.word())
                        .executes(ctx -> listSpells(ctx.getSource(), StringArgumentType.getString(ctx, "ecole")))));
        root.then(Commands.literal("creatures")
                .executes(ctx -> listCreatures(ctx.getSource())));

        event.getDispatcher().register(root);
    }

    private static ServerPlayer player(CommandSourceStack source) {
        return source.getPlayer();
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.help_header"), false);
        for (String key : List.of("duel", "arena", "resolve", "leave", "noauto", "school", "level",
                "powerpips", "crit", "grimoire", "deck", "spells", "creatures", "info")) {
            source.sendSuccess(() -> Component.translatable("arcanum_realms.command.help." + key), false);
        }
        return 1;
    }

    private static int info(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("[Arcanum Realms] " + SpellBook.size() + " sorts, "
                + Creatures.all().size() + " creatures, "
                + ServerBattleManager.INSTANCE.sessions().size() + " duel(s) en cours."), false);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.info.help"), false);
        return 1;
    }

    private static int duel(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ServerBattleManager.INSTANCE.startDuelWithNearby(sp, null);
        return 1;
    }

    private static int arena(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ServerBattleManager.INSTANCE.startArenaDuel(sp, sp.blockPosition());
        return 1;
    }

    private static int resolve(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ServerBattleManager.Session session = ServerBattleManager.INSTANCE.sessionOf(sp);
        if (session == null) {
            source.sendFailure(Component.translatable("arcanum_realms.message.no_duel"));
            return 0;
        }
        ServerBattleManager.INSTANCE.forceResolve(session);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.resolve.done"), false);
        return 1;
    }

    private static int leave(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ServerBattleManager.Session session = ServerBattleManager.INSTANCE.sessionOf(sp);
        if (session == null) {
            source.sendFailure(Component.translatable("arcanum_realms.message.no_duel"));
            return 0;
        }
        ServerBattleManager.INSTANCE.leaveDuel(session, sp.getUUID(), true);
        return 1;
    }

    private static int noAuto(CommandSourceStack source, boolean value) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        sp.getPersistentData().putBoolean("arcanum_realms:no_duel", value);
        source.sendSuccess(() -> Component.translatable(value
                ? "arcanum_realms.command.noauto.on" : "arcanum_realms.command.noauto.off"), false);
        return 1;
    }

    private static int setSchool(CommandSourceStack source, String schoolId) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        School school;
        try {
            school = School.byId(schoolId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.translatable("arcanum_realms.command.school.unknown", schoolId));
            return 0;
        }
        WizardData data = WizardData.load(sp);
        data.setSchool(school);
        data.clearDeck();
        List<Spell> learned = data.refreshLearnedSpells();
        data.ensureStarterDeck();
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.school.done",
                Component.translatable(school.langKey()), learned.size()), false);
        return 1;
    }

    private static int setLevel(CommandSourceStack source, int level) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        data.setLevel(level);
        List<Spell> learned = data.refreshLearnedSpells();
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.level.done", level, learned.size()), false);
        return 1;
    }

    private static int setPowerPips(CommandSourceStack source, int percent) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        data.setPowerPipChance(percent);
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.powerpips.done", percent), false);
        return 1;
    }

    private static int setCrit(CommandSourceStack source, int crit) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        data.setCritRating(crit);
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.crit.done", crit), false);
        return 1;
    }

    private static int grimoire(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        NetworkHandler.sendWizardData(sp);
        return 1;
    }

    private static int resetDeck(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        data.clearDeck();
        data.ensureStarterDeck();
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.deck.reset", data.deckSize()), false);
        return 1;
    }

    private static int deckInfo(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.deck.info",
                data.deckSize(), WizardData.deckLimit(), data.learnedIds().size()), false);
        data.deck().forEach((id, count) -> SpellBook.find(id).ifPresent(spell ->
                source.sendSuccess(() -> Component.literal(" - " + spell.nameFr() + " x" + count), false)));
        return 1;
    }

    private static int listSpells(CommandSourceStack source, String schoolId) {
        School school = null;
        if (schoolId != null) {
            try {
                school = School.byId(schoolId);
            } catch (IllegalArgumentException e) {
                source.sendFailure(Component.translatable("arcanum_realms.command.school.unknown", schoolId));
                return 0;
            }
        }
        School filter = school;
        List<Spell> spells = filter == null ? List.copyOf(SpellBook.all()) : SpellBook.bySchool(filter);
        source.sendSuccess(() -> Component.literal("[Arcanum Realms] " + spells.size() + " sorts"), false);
        for (Spell spell : spells) {
            String cost = spell.variablePips() ? ("X+" + spell.minPips()) : String.valueOf(spell.pipCost());
            source.sendSuccess(() -> Component.literal(" - " + spell.nameFr() + " (" + cost + " pips, "
                    + spell.accuracy() + "%, niv. " + spell.minLevel() + ")"), false);
        }
        return 1;
    }

    private static int listCreatures(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("[Arcanum Realms] " + Creatures.all().size() + " creatures"), false);
        Creatures.all().forEach(def -> source.sendSuccess(() -> Component.literal(" - " + def.id()
                + " : " + def.nameFr() + " (niv. " + def.level() + ", " + def.health() + " PV, "
                + def.school().displayFr() + ")"), false));
        return 1;
    }

    /** Utilitaire : borne une valeur dans la configuration. */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static {
        // Rappel des bornes de configuration par defaut, utile en debug.
        assert ArcanumConfig.MAX_ROUNDS.get() > 0;
    }
}
