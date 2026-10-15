package com.arcanumrealms.network;

import com.arcanumrealms.core.world.ContentLine;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Transport des textes traduisibles dans les paquets.
 *
 * <p>Chaque ligne voyage avec sa cle de traduction et son texte par defaut : le
 * client affiche la traduction s'il en a une (celle du mod ou celle d'un pack de
 * ressources), sinon le texte par defaut. C'est ce qui permet de reecrire
 * dialogues, noms de PNJ et noms de sorts sans recompiler quoi que ce soit.
 */
public final class ContentCodec {

    public static final int MAX_LINES = 256;

    private ContentCodec() {}

    public static void writeLine(FriendlyByteBuf buffer, ContentLine line) {
        buffer.writeBoolean(line.translatable());
        if (line.translatable()) buffer.writeUtf(line.key(), 256);
        buffer.writeUtf(line.text(), 1024);
    }

    public static ContentLine readLine(FriendlyByteBuf buffer) {
        String key = buffer.readBoolean() ? buffer.readUtf(256) : null;
        String text = buffer.readUtf(1024);
        return key == null ? ContentLine.literal(text) : ContentLine.of(key, text);
    }

    public static void writeLines(FriendlyByteBuf buffer, List<ContentLine> lines) {
        buffer.writeVarInt(lines.size());
        for (ContentLine line : lines) writeLine(buffer, line);
    }

    public static List<ContentLine> readLines(FriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), MAX_LINES);
        List<ContentLine> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) lines.add(readLine(buffer));
        return lines;
    }

    public static void writeStrings(FriendlyByteBuf buffer, List<String> values) {
        buffer.writeVarInt(values.size());
        for (String value : values) buffer.writeUtf(value, 1024);
    }

    public static List<String> readStrings(FriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), MAX_LINES);
        List<String> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) values.add(buffer.readUtf(1024));
        return values;
    }
}
