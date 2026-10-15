package com.arcanumrealms.network.packet;

import com.arcanumrealms.battle.ServerBattleManager;
import com.arcanumrealms.core.battle.Battle;
import com.arcanumrealms.core.battle.BattleEvent;
import com.arcanumrealms.core.battle.Combatant;
import com.arcanumrealms.core.card.Deck;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.core.status.Status;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Etat complet d'un duel, envoye au client a chaque changement.
 *
 * <p>Le client ne calcule rien : il affiche ce que le serveur lui donne.
 * Cela evite toute desynchronisation et empeche la triche.
 */
public final class BattleStatePacket {

    public static final class Actor {
        public final String id;
        public final String name;
        public final String school;
        public final int health;
        public final int maxHealth;
        public final boolean player;
        public final boolean self;
        public final boolean defeated;
        public final boolean fled;
        public final List<Boolean> pips;
        public final List<String> statuses;

        public Actor(String id, String name, String school, int health, int maxHealth, boolean player,
                     boolean self, boolean defeated, boolean fled, List<Boolean> pips, List<String> statuses) {
            this.id = id;
            this.name = name;
            this.school = school;
            this.health = health;
            this.maxHealth = maxHealth;
            this.player = player;
            this.self = self;
            this.defeated = defeated;
            this.fled = fled;
            this.pips = pips;
            this.statuses = statuses;
        }

        public double healthPercent() {
            return maxHealth <= 0 ? 0 : Math.max(0, Math.min(1, (double) health / maxHealth));
        }
    }

    public static final class Card {
        public final int index;
        public final String spellId;
        public final int cost;
        public final boolean affordable;
        /** Vrai si le sort cible les ennemis. */
        public final boolean offensive;

        public Card(int index, String spellId, int cost, boolean affordable, boolean offensive) {
            this.index = index;
            this.spellId = spellId;
            this.cost = cost;
            this.affordable = affordable;
            this.offensive = offensive;
        }
    }

    public static final class Line {
        public final String fr;
        public final String en;

        public Line(String fr, String en) { this.fr = fr; this.en = en; }
    }

    public final String battleId;
    public final int round;
    public final String phase;
    public final int secondsLeft;
    public final String selfId;
    public final boolean canAct;
    public final boolean canFlee;
    public final List<Actor> actors;
    public final List<Card> hand;
    public final List<Line> events;
    public final String aura;

    public BattleStatePacket(String battleId, int round, String phase, int secondsLeft, String selfId,
                             boolean canAct, boolean canFlee, List<Actor> actors, List<Card> hand,
                             List<Line> events, String aura) {
        this.battleId = battleId;
        this.round = round;
        this.phase = phase;
        this.secondsLeft = secondsLeft;
        this.selfId = selfId;
        this.canAct = canAct;
        this.canFlee = canFlee;
        this.actors = actors;
        this.hand = hand;
        this.events = events;
        this.aura = aura;
    }

    /** Construit le paquet a partir de la session serveur, du point de vue d'un joueur. */
    public static BattleStatePacket of(ServerBattleManager.Session session, ServerPlayer viewer) {
        Battle battle = session.battle;
        String selfId = viewer.getUUID().toString();

        List<Actor> actors = new ArrayList<>();
        for (Combatant combatant : battle.combatants()) {
            List<String> statuses = new ArrayList<>();
            for (Status status : combatant.statuses()) statuses.add(describe(status));
            actors.add(new Actor(combatant.id(), combatant.name(), combatant.school().id(),
                    combatant.health(), combatant.maxHealth(), combatant.isPlayer(),
                    combatant.id().equals(selfId), combatant.defeated(), combatant.fled(),
                    new ArrayList<>(combatant.pips().view()), statuses));
        }

        List<Card> hand = new ArrayList<>();
        Combatant self = battle.get(selfId);
        if (self != null && self.alive()) {
            List<Deck.Card> cards = self.deck().hand();
            for (int i = 0; i < cards.size(); i++) {
                Spell spell = SpellBook.get(cards.get(i).spellId());
                int cost = battle.pipsFor(self, spell, 0);
                boolean affordable = cost >= 0 && self.pips().canPay(cost,
                        self.stats().powerPipsCountDouble(spell.school(), self.school()));
                hand.add(new Card(i, spell.id(), Math.max(0, cost), affordable, spell.targetMode().affectsEnemies()));
            }
        }

        List<Line> events = new ArrayList<>();
        for (BattleEvent event : session.recentEvents) {
            events.add(new Line(event.text(true), event.text(false)));
        }

        String aura = battle.aura() == null ? "" : battle.aura().type().name() + ":" + battle.aura().percent()
                + ":" + battle.aura().rounds();

        String phase = session.state == ServerBattleManager.State.PLANNING ? "PLANNING"
                : session.state == ServerBattleManager.State.RESOLVING ? "RESOLVING" : "FINISHED";

        return new BattleStatePacket(battle.id(), battle.round(), phase, Math.max(0, session.timer / 20),
                selfId, session.state == ServerBattleManager.State.PLANNING && self != null && self.alive(),
                battle.config().allowFlee(), actors, hand, events, aura);
    }

    private static String describe(Status status) {
        return switch (status.type()) {
            case BLADE -> "Lame +" + status.percent() + "%";
            case TRAP -> "Piege +" + status.percent() + "%";
            case SHIELD -> "Bouclier -" + status.percent() + "%";
            case WEAKNESS -> "Faiblesse -" + status.percent() + "%";
            case ACCURACY_CHARM -> "Precision " + (status.percent() >= 0 ? "+" : "") + status.percent() + "%";
            case HEAL_BLADE -> "Soins +" + status.percent() + "%";
            case PIERCE_AURA -> "Percee " + status.percent() + "%";
            case DOT -> "Degats " + status.minValue() + "/round (" + status.rounds() + ")";
            case HOT -> "Soins " + status.minValue() + "/round (" + status.rounds() + ")";
            case ABSORB -> "Absorption " + status.remainingValue();
            case STUN -> "Etourdi (" + status.rounds() + ")";
        };
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(battleId);
        buf.writeVarInt(round);
        buf.writeUtf(phase);
        buf.writeVarInt(secondsLeft);
        buf.writeUtf(selfId);
        buf.writeBoolean(canAct);
        buf.writeBoolean(canFlee);
        buf.writeVarInt(actors.size());
        for (Actor actor : actors) {
            buf.writeUtf(actor.id);
            buf.writeUtf(actor.name);
            buf.writeUtf(actor.school);
            buf.writeVarInt(actor.health);
            buf.writeVarInt(actor.maxHealth);
            buf.writeBoolean(actor.player);
            buf.writeBoolean(actor.self);
            buf.writeBoolean(actor.defeated);
            buf.writeBoolean(actor.fled);
            buf.writeVarInt(actor.pips.size());
            for (Boolean pip : actor.pips) buf.writeBoolean(pip);
            buf.writeVarInt(actor.statuses.size());
            for (String status : actor.statuses) buf.writeUtf(status);
        }
        buf.writeVarInt(hand.size());
        for (Card card : hand) {
            buf.writeVarInt(card.index);
            buf.writeUtf(card.spellId);
            buf.writeVarInt(card.cost);
            buf.writeBoolean(card.affordable);
            buf.writeBoolean(card.offensive);
        }
        buf.writeVarInt(events.size());
        for (Line line : events) {
            buf.writeUtf(line.fr);
            buf.writeUtf(line.en);
        }
        buf.writeUtf(aura);
    }

    public static BattleStatePacket decode(FriendlyByteBuf buf) {
        String battleId = buf.readUtf();
        int round = buf.readVarInt();
        String phase = buf.readUtf();
        int secondsLeft = buf.readVarInt();
        String selfId = buf.readUtf();
        boolean canAct = buf.readBoolean();
        boolean canFlee = buf.readBoolean();
        int actorCount = buf.readVarInt();
        List<Actor> actors = new ArrayList<>(actorCount);
        for (int i = 0; i < actorCount; i++) {
            String id = buf.readUtf();
            String name = buf.readUtf();
            String school = buf.readUtf();
            int health = buf.readVarInt();
            int maxHealth = buf.readVarInt();
            boolean player = buf.readBoolean();
            boolean self = buf.readBoolean();
            boolean defeated = buf.readBoolean();
            boolean fled = buf.readBoolean();
            int pipCount = buf.readVarInt();
            List<Boolean> pips = new ArrayList<>(pipCount);
            for (int p = 0; p < pipCount; p++) pips.add(buf.readBoolean());
            int statusCount = buf.readVarInt();
            List<String> statuses = new ArrayList<>(statusCount);
            for (int s = 0; s < statusCount; s++) statuses.add(buf.readUtf());
            actors.add(new Actor(id, name, school, health, maxHealth, player, self, defeated, fled, pips, statuses));
        }
        int handCount = buf.readVarInt();
        List<Card> hand = new ArrayList<>(handCount);
        for (int i = 0; i < handCount; i++) {
            int index = buf.readVarInt();
            String spellId = buf.readUtf();
            int cost = buf.readVarInt();
            boolean affordable = buf.readBoolean();
            boolean offensive = buf.readBoolean();
            hand.add(new Card(index, spellId, cost, affordable, offensive));
        }
        int eventCount = buf.readVarInt();
        List<Line> events = new ArrayList<>(eventCount);
        for (int i = 0; i < eventCount; i++) {
            String fr = buf.readUtf();
            String en = buf.readUtf();
            events.add(new Line(fr, en));
        }
        String aura = buf.readUtf();
        return new BattleStatePacket(battleId, round, phase, secondsLeft, selfId, canAct, canFlee, actors, hand, events, aura);
    }

    public static void handle(BattleStatePacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                com.arcanumrealms.client.ClientBattleBridge.acceptState(message);
            }
        });
        context.setPacketHandled(true);
    }
}
