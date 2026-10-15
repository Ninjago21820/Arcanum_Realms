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


def gear_texture():
    """Icone d'equipement : petite robe pliee sur un fond de parchemin."""
    img = blank(16, 16)
    cloth = (108, 116, 138, 255)
    cloth_light = (144, 152, 174, 255)
    trim = (217, 169, 60, 255)
    for y in range(3, 14):
        for x in range(3, 13):
            put(img, x, y, cloth)
    for x in range(3, 13):
        put(img, x, 3, cloth_light)
    for y in range(3, 8):
        put(img, 8, y, cloth_light)
    for x in range(4, 12):
        put(img, x, 8, trim)
    for y in range(9, 13):
        put(img, 7, y, trim)
    for x in range(5, 11):
        put(img, x, 13, cloth)
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

    # objet d'equipement (une seule icone pour tout le catalogue)
    write_json(os.path.join(ASSETS, 'models', 'item', 'wizard_gear.json'),
               {"parent": "minecraft:item/generated",
                "textures": {"layer0": f'{NS}:item/wizard_gear'}})

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



# ----------------------------------------------------- apparences des PNJ


def wizard_skin(robe, trim, skin, hair):
    """Peau de sorcier 64x64 (mise en page classique, modele humanoide vanilla).

    Une seule peau par ecole : les professeurs portent la robe de leur ecole,
    les habitants une robe neutre. La mise en page est celle des peaux de
    joueur, donc le modele humanoide standard suffit a l'afficher.

    Repere des faces (comme dans la classe CubeListBuilder de Minecraft) :
    dessus (d, 0), dessous (d + w, 0), droite (0, d), face (d, d),
    gauche (d + w, d), dos (d + w + d, d), ou w/h/d sont les dimensions du
    membre et (0,0) le coin de sa region dans la peau.
    """
    img = blank(64, 64)

    def rect(x0, y0, w, h, color):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                put(img, x, y, color)

    def shade(color, delta):
        return (max(0, min(255, color[0] + delta)), max(0, min(255, color[1] + delta)),
                max(0, min(255, color[2] + delta)), color[3])

    def part(x0, y0, w, h, d, color):
        """Remplit les six faces d'un membre de largeur w, hauteur h, profondeur d."""
        rect(x0 + d, y0, w, d, shade(color, 12))            # dessus
        rect(x0 + d + w, y0, w, d, shade(color, -18))       # dessous
        rect(x0, y0 + d, d, h, shade(color, -6))            # droite
        rect(x0 + d, y0 + d, w, h, color)                   # face avant
        rect(x0 + d + w, y0 + d, d, h, shade(color, -10))   # gauche
        rect(x0 + d + w + d, y0 + d, w, h, shade(color, -14))  # dos

    face_skin = shade(skin, 0)
    robe_dark = shade(robe, -30)
    boot = (58, 44, 32, 255)

    # --- tete (8 x 8 x 8, region 0,0) : visage clair, cheveux sur le crane
    part(0, 0, 8, 8, 8, skin)
    rect(8, 0, 8, 8, hair)                 # dessus du crane
    rect(24, 8, 8, 8, hair)                # arriere de la tete
    rect(0, 8, 3, 8, hair)                 # tempes
    rect(8, 8, 8, 3, hair)                 # frange
    rect(9, 11, 2, 2, (46, 34, 34, 255))   # oeil droit
    rect(13, 11, 2, 2, (46, 34, 34, 255))  # oeil gauche
    rect(10, 14, 4, 1, shade(skin, -40))   # bouche

    # --- chapeau pointu (couche exterieure de la tete, region 32,0)
    rect(40, 0, 8, 8, trim)                # sommet
    rect(32, 8, 8, 8, shade(trim, -12))    # cote droit
    rect(48, 8, 8, 8, shade(trim, -12))    # cote gauche
    rect(56, 8, 8, 8, shade(trim, -18))    # dos
    rect(40, 8, 8, 3, trim)                # bord du chapeau devant
    rect(40, 11, 8, 1, shade(trim, -30))   # ombre du bord

    # --- corps (8 x 12 x 4, region 16,16)
    part(16, 16, 8, 12, 4, robe)
    rect(20, 20, 8, 2, trim)               # col
    rect(20, 28, 8, 2, (62, 44, 30, 255))  # ceinture
    rect(23, 30, 2, 2, trim)               # boucle

    # --- bras (4 x 12 x 4) : manches puis mains
    part(40, 16, 4, 12, 4, robe)
    part(32, 48, 4, 12, 4, robe)
    rect(44, 24, 4, 4, face_skin)          # main droite
    rect(36, 56, 4, 4, face_skin)          # main gauche
    rect(40, 20, 4, 12, robe_dark)         # manche droite (exterieur)
    rect(48, 20, 4, 12, robe_dark)         # manche droite (interieur)

    # --- jambes (4 x 12 x 4) : bas de robe puis chaussures
    part(0, 16, 4, 12, 4, robe_dark)
    part(16, 48, 4, 12, 4, robe_dark)
    rect(0, 28, 4, 4, boot)                # pied droit (exterieur)
    rect(4, 28, 4, 4, boot)                # pied droit (avant)
    rect(16, 60, 4, 4, boot)               # pied gauche (exterieur)
    rect(20, 60, 4, 4, boot)               # pied gauche (avant)

    return img


SKINS = {
    'neutral': ((108, 116, 138, 255), (196, 200, 214, 255), (238, 200, 168, 255), (74, 54, 40, 255)),
    'pyromancy': ((196, 76, 40, 255), (250, 190, 90, 255), (238, 200, 168, 255), (166, 62, 36, 255)),
    'tempest': ((122, 85, 214, 255), (222, 206, 255, 255), (236, 198, 168, 255), (58, 44, 96, 255)),
    'frost': ((116, 196, 232, 255), (240, 252, 255, 255), (240, 206, 176, 255), (206, 224, 236, 255)),
    'runomancy': ((217, 169, 60, 255), (255, 240, 200, 255), (234, 196, 160, 255), (140, 108, 40, 255)),
    'sylvan': ((95, 191, 99, 255), (214, 246, 200, 255), (236, 200, 166, 255), (62, 110, 66, 255)),
    'necromancy': ((107, 114, 128, 255), (196, 200, 214, 255), (226, 208, 196, 255), (40, 42, 52, 255)),
    'equilibrium': ((215, 192, 141, 255), (255, 250, 232, 255), (240, 206, 176, 255), (150, 130, 96, 255)),
}


def npc_skins():
    """Ecrit une texture par ecole, plus une tenue neutre pour les habitants."""
    out = 0
    for name, colors in SKINS.items():
        path = os.path.join(ASSETS, 'textures', 'entity', 'npc', name + '.png')
        write_png(path, wizard_skin(*colors), 64, 64)
        out += 1
    return out


def main():
    models()
    write_png(os.path.join(ASSETS, 'textures', 'item', 'duel_wand.png'), wand_texture(), 16, 16)
    write_png(os.path.join(ASSETS, 'textures', 'block', 'arcane_circle.png'), circle_texture(), 16, 16)
    write_png(os.path.join(ASSETS, 'textures', 'item', 'wizard_gear.png'), gear_texture(), 16, 16)
    pack_icon(os.path.join(RES, 'logo.png'))
    skins = npc_skins()
    print(f'Ressources generees ({skins} apparences de PNJ).')


if __name__ == '__main__':
    main()
