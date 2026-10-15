#!/usr/bin/env python3
"""Genere les ressources du mod : modeles, blockstates, loot, recettes, textures.

Ce script n'est pas necessaire pour jouer : il sert a regenerer les fichiers
assets/data si l'on veut changer l'apparence des objets. Il est conserve dans
le depot pour que les textures restent reproductibles (pas de fichier binaire
inexplicable).
"""
import json
import os
import struct
import zlib

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
RES = os.path.join(ROOT, 'src', 'main', 'resources')
ASSETS = os.path.join(RES, 'assets', 'arcanum_realms')
DATA = os.path.join(RES, 'data', 'arcanum_realms')
NS = 'arcanum_realms'


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


# --------------------------------------------------------------------- PNG


def write_png(path, pixels, width, height):
    raw = b''.join(b'\x00' + bytes(pixels[y][x]) for y in range(height) for x in range(1)) \
        if False else b''.join(
            b'\x00' + bytes(v for x in range(width) for v in pixels[y][x]) for y in range(height))
    def chunk(kind, data):
        return (struct.pack('>I', len(data)) + kind + data
                + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff))
    png = (b'\x89PNG\r\n\x1a\n'
           + chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
           + chunk(b'IDAT', zlib.compress(raw, 9))
           + chunk(b'IEND', b''))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(png)


def blank(w, h):
    return [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]


def put(img, x, y, color):
    if 0 <= y < len(img) and 0 <= x < len(img[0]):
        img[y][x] = color


def wand_texture():
    """Baguette : manche en bois, virole d'or, pointe de cristal violette."""
    img = blank(16, 16)
    wood_dark = (74, 45, 25, 255)
    wood = (124, 79, 41, 255)
    wood_light = (163, 111, 62, 255)
    gold = (233, 190, 74, 255)
    gold_dark = (168, 128, 32, 255)
    crystal = (150, 92, 226, 255)
    crystal_light = (208, 172, 255, 255)
    glow = (240, 226, 255, 255)
    # manche en diagonale du bas-gauche vers le haut-droit
    for i in range(11):
        x, y = 3 + i, 14 - i
        put(img, x, y, wood)
        put(img, x, y - 1, wood_light)
        put(img, x - 1, y, wood_dark)
    # virole d'or
    put(img, 12, 5, gold)
    put(img, 13, 4, gold)
    put(img, 12, 4, gold_dark)
    put(img, 13, 3, gold)
    # pointe de cristal
    put(img, 14, 3, crystal)
    put(img, 15, 2, crystal)
    put(img, 14, 2, crystal_light)
    put(img, 15, 3, crystal)
    put(img, 13, 2, crystal_light)
    put(img, 14, 1, glow)
    put(img, 15, 1, glow)
    return img


def circle_texture():
    """Cercle arcanique : anneau d'amethyste avec runes et coeur lumineux."""
    img = blank(16, 16)
    ring = (150, 92, 226, 235)
    ring_dark = (92, 54, 148, 220)
    rune = (222, 190, 255, 240)
    core = (196, 150, 255, 200)
    cx = cy = 7.5
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if 5.2 <= d <= 7.0:
                put(img, x, y, ring if (x + y) % 3 else ring_dark)
            elif d <= 3.4:
                alpha = int(30 + 90 * (3.4 - d) / 3.4)
                put(img, x, y, (196, 150, 255, alpha))
    # huit runes reparties sur l'anneau
    for (dx, dy) in [(0, -6), (4, -4), (6, 0), (4, 4), (0, 6), (-4, 4), (-6, 0), (-4, -4)]:
        put(img, 7 + dx, 7 + dy, rune)
        put(img, 8 + dx, 7 + dy, rune)
    # rayons vers le coeur
    for (dx, dy) in [(0, -4), (3, -3), (4, 0), (3, 3), (0, 4), (-3, 3), (-4, 0), (-3, -3)]:
        put(img, 7 + dx, 7 + dy, (222, 190, 255, 150))
    put(img, 7, 7, (255, 250, 255, 255))
    put(img, 8, 7, (255, 250, 255, 255))
    put(img, 7, 8, (255, 250, 255, 255))
    put(img, 8, 8, (255, 250, 255, 255))
    return img


def logo_texture():
    """Logo 128x128 : anneau a sept couleurs (une par ecole) autour d'un losange."""
    schools = [(228, 89, 43), (122, 85, 214), (116, 196, 232), (217, 169, 60),
               (95, 191, 99), (107, 114, 128), (215, 192, 141)]
    size = 128
    img = [[(18, 16, 28, 255) for _ in range(size)] for _ in range(size)]
    cx = cy = 63.5
    for y in range(size):
        for x in range(size):
            dx, dy = x - cx, y - cy
            d = (dx * dx + dy * dy) ** 0.5
            if 48 <= d <= 54:
                import math
                angle = (math.atan2(dy, dx) + math.pi) / (2 * math.pi)
                color = schools[int(angle * 7) % 7]
                put(img, x, y, (color[0], color[1], color[2], 235))
            elif d <= 12:
                put(img, x, y, (60, 48, 96, 255))
    # losange central
    for y in range(size):
        for x in range(size):
            if abs(x - cx) + abs(y - cy) <= 16:
                put(img, x, y, (232, 217, 160, 255))
            if abs(x - cx) + abs(y - cy) <= 8:
                put(img, x, y, (150, 92, 226, 255))
    return img


def pack_icon(dst):
    write_png(dst, logo_texture(), 128, 128)


# --------------------------------------------------------- modeles / data


def models():
    tex = f'{NS}:block/arcane_circle'
    write_json(os.path.join(ASSETS, 'blockstates', 'arcane_circle.json'),
               {"variants": {"": {"model": f'{NS}:block/arcane_circle'}}})
    write_json(os.path.join(ASSETS, 'models', 'block', 'arcane_circle.json'), {
        "parent": "minecraft:block/block",
        "textures": {"particle": tex, "circle": tex},
        "elements": [{
            "from": [0, 0, 0],
            "to": [16, 3, 16],
            "faces": {
                "up": {"texture": "#circle", "cullface": "up"},
                "down": {"texture": "#circle", "cullface": "down"},
                "north": {"texture": "#circle", "cullface": "north"},
                "south": {"texture": "#circle", "cullface": "south"},
                "west": {"texture": "#circle", "cullface": "west"},
                "east": {"texture": "#circle", "cullface": "east"}
            }
        }]
    })
    write_json(os.path.join(ASSETS, 'models', 'item', 'arcane_circle.json'),
               {"parent": f'{NS}:block/arcane_circle'})
    write_json(os.path.join(ASSETS, 'models', 'item', 'duel_wand.json'),
               {"parent": "minecraft:item/handheld",
                "textures": {"layer0": f'{NS}:item/duel_wand'}})

    # table de butin du bloc
    write_json(os.path.join(DATA, 'loot_tables', 'blocks', 'arcane_circle.json'), {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f'{NS}:arcane_circle'}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]
        }]
    })

    # recettes de survie
    write_json(os.path.join(DATA, 'recipes', 'duel_wand.json'), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["  A", " S ", "S  "],
        "key": {
            "A": {"item": "minecraft:amethyst_shard"},
            "S": {"item": "minecraft:stick"}
        },
        "result": {"item": f'{NS}:duel_wand', "count": 1}
    })
    write_json(os.path.join(DATA, 'recipes', 'arcane_circle.json'), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["SSS", "SAS", "SSS"],
        "key": {
            "S": {"item": "minecraft:stone_bricks"},
            "A": {"item": "minecraft:amethyst_shard"}
        },
        "result": {"item": f'{NS}:arcane_circle', "count": 1}
    })

    # le bloc se mine a la pioche
    write_json(os.path.join(RES, 'data', 'minecraft', 'tags', 'blocks', 'mineable', 'pickaxe.json'),
               {"replace": False, "values": [f'{NS}:arcane_circle']})
    write_json(os.path.join(RES, 'data', 'minecraft', 'tags', 'blocks', 'needs_stone_tool.json'),
               {"replace": False, "values": [f'{NS}:arcane_circle']})


def main():
    models()
    write_png(os.path.join(ASSETS, 'textures', 'item', 'duel_wand.png'), wand_texture(), 16, 16)
    write_png(os.path.join(ASSETS, 'textures', 'block', 'arcane_circle.png'), circle_texture(), 16, 16)
    pack_icon(os.path.join(RES, 'logo.png'))
    print('Ressources generees.')


if __name__ == '__main__':
    main()
