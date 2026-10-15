#!/usr/bin/env python3
"""Apparences des personnages : peaux de style Minecraft, dessinees en Python.

Une peau de personnage, dans Minecraft, est une image de 64 x 64 pixels dont
chaque region correspond a une partie du modele : la tete occupe le carre (0,0),
le corps (16,16), les bras (40,16) et (32,48), les jambes (0,16) et (16,48).
Les regions a partir de u = 32 ou v = 32 sont la **couche exterieure**, portee
0,25 pixel au-dessus de la peau : c'est la que vont les chapeaux, les cheveux
volumineux et les vestes.

Ce module dessine :
  * une apparence par PNJ (`npc/<identifiant>.png`), a partir de sa tenue, de son
    ecole et de sa fonction, lues dans le catalogue du moteur ;
  * les deux textures de la tenue 3D (`wizard/cloth.png` en niveaux de gris, qui
    sera teintee, et `wizard/detail.png` en couleurs), dessinees a partir de la
    table de pieces du modele Java.
"""
import os
import re

from pixelart import blank, put, rect, rgb, shade

MODULE_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(MODULE_DIR, '..')
ASSETS = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'arcanum_realms')
NPCS_JAVA = os.path.join(ROOT, 'src', 'main', 'java', 'com', 'arcanumrealms', 'core',
                         'world', 'Npcs.java')

# --------------------------------------------------------------- palettes

# peaux : du plus clair au plus fonce
SKIN_TONES = {
    'porcelain': (243, 214, 190, 255),
    'fair': (238, 200, 168, 255),
    'rosy': (232, 186, 156, 255),
    'tan': (214, 168, 128, 255),
    'olive': (186, 138, 100, 255),
    'brown': (140, 96, 66, 255),
    'deep': (104, 70, 48, 255),
    'ashen': (196, 200, 208, 255),      # esprits
}

HAIR_COLOURS = {
    'black': (38, 32, 34, 255),
    'dark_brown': (74, 50, 33, 255),
    'brown': (110, 74, 44, 255),
    'chestnut': (140, 84, 44, 255),
    'blonde': (214, 176, 94, 255),
    'ginger': (186, 92, 42, 255),
    'auburn': (150, 62, 44, 255),
    'grey': (166, 166, 170, 255),
    'white': (232, 232, 236, 255),
    'ash': (96, 100, 112, 255),
    'lilac': (200, 172, 226, 255),
    'green': (108, 156, 96, 255),
    'blue': (86, 118, 186, 255),
}

EYE_COLOURS = {
    'brown': (78, 52, 36, 255),
    'hazel': (118, 88, 44, 255),
    'green': (62, 118, 74, 255),
    'blue': (58, 104, 168, 255),
    'grey': (104, 116, 132, 255),
    'amber': (196, 140, 48, 255),
    'violet': (132, 96, 196, 255),
    'ember': (222, 96, 40, 255),
    'frost': (150, 224, 248, 255),
}

BOOT = (58, 44, 32, 255)
LEATHER = (94, 66, 42, 255)
METAL = (176, 182, 194, 255)
METAL_DARK = (116, 122, 136, 255)
GOLD = (206, 168, 76, 255)

# Predicat d'ecriture : une case de la face avant d'un cube.
#   H cheveux   s peau      b sourcil   w blanc de l'oeil   i iris
#   n nez       m bouche    d barbe     . case transparente (couche exterieure)


# Franges : la rangee 0 est le haut du crane, la rangee 1 la ligne de cheveux
# au-dessus du front. Les styles courts degagent le front, les styles longs le
# couvrent davantage.
FRINGES = {
    'short': 'HHssssHH',
    'bun': 'HHssssHH',
    'ponytail': 'HssssssH',
    'long': 'HHHssHHH',
    'wild': 'HHHHHHHH',
    'braid': 'HHHssHHH',
    'bald': 'ssssssss',
}


def _face_rows(hair_style='short', brow=True, mouth='smile', freckles=False, beard=False):
    """Construit la face avant de la tete (8 lignes de 8 cases).

    Les rangees vont du haut du crane (0) a la bouche (7) : cheveux, front,
    sourcils, yeux, joues, nez, bouche. Une coiffe se pose sur la couche
    exterieure et ne doit pas descendre plus bas que les sourcils.
    """
    rows = []
    rows.append(list('HHHHHHHH'))                                    # haut du crane
    rows.append(list(FRINGES.get(hair_style, 'HHssssHH')))           # ligne de cheveux
    rows.append([shade_skin for shade_skin in 'ssssssss'])           # front
    rows.append(['s', 'b', 's', 's', 's', 's', 'b', 's'] if brow else list('ssssssss'))
    rows.append(['s', 'w', 'i', 's', 's', 'i', 'w', 's'])           # yeux
    rows.append(['s', 's', 's', 'f' if freckles else 's',
                 'f' if freckles else 's', 's', 's', 's'])           # joues
    rows.append(['s', 's', 'n', 'n', 's', 's', 's', 's'])           # nez
    if beard:
        rows.append(['d', 'd', 'm', 'm', 'm', 'd', 'd', 'd'])        # barbe + bouche
    elif mouth == 'smile':
        rows.append(['s', 's', 'm', 'n', 'n', 'm', 's', 's'])        # bouche souriante
    elif mouth == 'grim':
        rows.append(['s', 's', 's', 'm', 'm', 's', 's', 's'])
    else:
        rows.append(['s', 's', 's', 's', 's', 's', 's', 's'])
    return rows


def palette(spec):
    """Couleurs d'ecriture : chaque caractere de la face a sa couleur."""
    skin = SKIN_TONES[spec['skin']]
    hair = HAIR_COLOURS[spec['hair_colour']]
    return {
        's': skin,
        'H': hair,
        'b': shade(hair, -24),
        'w': (246, 246, 250, 255),
        'i': EYE_COLOURS[spec['eyes']],
        'n': shade(skin, -26),
        'f': shade(skin, -14),
        'm': shade(skin, -60),
        'd': shade(hair, 8),
    }


# ---------------------------------------------------------- le modele 3D

def part(img, x0, y0, w, h, d, colour):
    """Remplit les six faces d'un cube de la peau (dessus, dessous, cotes, avant, dos)."""
    rect(img, x0 + d, y0, w, d, shade(colour, 14))               # dessus
    rect(img, x0 + d + w, y0, w, d, shade(colour, -20))          # dessous
    rect(img, x0, y0 + d, d, h, shade(colour, -8))               # cote droit
    rect(img, x0 + d, y0 + d, w, h, colour)                      # avant
    rect(img, x0 + d + w, y0 + d, d, h, shade(colour, -12))      # cote gauche
    rect(img, x0 + d + w + d, y0 + d, w, h, shade(colour, -16))  # dos


def face_front(img, x0, y0, colour):
    """Peint la face avant d'un cube d'une couleur unie (sans toucher aux cotes)."""
    rect(img, x0, y0, len(colour), 1, colour)


def draw_rows(img, x0, y0, rows, colours):
    """Peint une grille de cases (la face avant d'un cube)."""
    for dy, row in enumerate(rows):
        for dx, cell in enumerate(row):
            if cell == '.':
                continue
            colour = colours.get(cell)
            if colour is not None:
                put(img, x0 + dx, y0 + dy, colour)


# ------------------------------------------------------- parties du corps

HEAD_FRONT = (8, 8)          # coin de la face avant de la tete (8 x 8)
HEAD_CUBE = (0, 0, 8, 8, 8)  # region du cube de la tete (origine, puis taille)
HAT_REGION = (32, 0, 8, 8, 8)
BODY = (16, 16, 8, 12, 4)
JACKET = (16, 32, 8, 12, 4)
ARM_RIGHT = (40, 16, 4, 12, 4)
ARM_LEFT = (32, 48, 4, 12, 4)
SLEEVE_RIGHT = (40, 32, 4, 12, 4)
SLEEVE_LEFT = (48, 48, 4, 12, 4)
LEG_RIGHT = (0, 16, 4, 12, 4)
LEG_LEFT = (16, 48, 4, 12, 4)
TROUSER_RIGHT = (0, 32, 4, 12, 4)
TROUSER_LEFT = (0, 48, 4, 12, 4)


def draw_head(img, spec, colours):
    """Tete : crane, visage, cheveux et barbe."""
    skin = colours['s']
    hair = colours['H']
    part(img, *HEAD_CUBE, skin)

    # cheveux : dessus du crane et arriere
    rect(img, 8, 0, 8, 8, hair)
    rect(img, 24, 8, 8, 8, hair)
    style = spec['hair_style']
    # hauteur des cheveux sur les cotes : le reste du cote reste de la peau, il ne
    # doit jamais etre transparent
    side_height = {'long': 8, 'wild': 8, 'braid': 8, 'short': 5, 'bun': 5,
                   'ponytail': 6}.get(style, 0)
    if side_height:
        rect(img, 0, 8, 8, side_height, hair)      # cote droit
        rect(img, 16, 8, 8, side_height, hair)     # cote gauche
        rect(img, 0, 8 + side_height, 8, 8 - side_height, shade(skin, -12))
        rect(img, 16, 8 + side_height, 8, 8 - side_height, shade(skin, -12))
    else:                                          # crane nu : une teinte de peau suffit
        rect(img, 0, 8, 8, 3, shade(skin, -18))
        rect(img, 16, 8, 8, 3, shade(skin, -18))
    if style == 'bald':
        rect(img, 8, 0, 8, 8, shade(skin, 6))     # pas de cheveux sur le crane
    if spec.get('hood'):
        rect(img, 0, 8, 8, 8, spec['hood_colour'])
        rect(img, 16, 8, 8, 8, spec['hood_colour'])
        rect(img, 8, 0, 8, 8, spec['hood_colour'])

    draw_rows(img, *HEAD_FRONT, _face_rows(
        hair_style=spec['hair_style'],
        brow=not spec.get('child'),
        mouth=spec.get('mouth', 'smile'),
        freckles=spec.get('freckles', False),
        beard=spec.get('beard') == 'full'), colours)

    if spec.get('beard') == 'short':
        rect(img, 9, 14, 6, 2, shade(hair, 4))
    if spec.get('scarf'):
        rect(img, 10, 15, 4, 1, spec['scarf'])


# Rangees de la face avant de la tete (8 rangees de 8 cases) :
#   0-1 cheveux   2 front   3 sourcils   4 yeux   5 joues   6 nez   7 bouche
# Une coiffe ne doit jamais descendre sur la rangee 4 : sinon le personnage
# n'a plus d'yeux, ce qui donne ces cubes sans visage.

def cone(img, region, profile, colour):
    """Dessine un cone (pyramide en degres) sur les quatre faces d'un cube.

    `profile` donne, rangee par rangee, le nombre de colonnes retirees de chaque
    cote. Comme la meme forme est peinte sur la face avant, les cotes et le dos,
    le chapeau se lit comme un cone sous tous les angles — c'est ainsi que les
    peaux de sorcier de Minecraft dessinent leurs chapeaux pointus.
    """
    u, v, w, h, d = region
    for row, inset in enumerate(profile):
        span = w - 2 * inset
        rect(img, u + d + inset, v + d + row, span, 1, shade(colour, 18 - row * 4))
        rect(img, u + d + w + d + inset, v + d + row, span, 1, shade(colour, -16))
        side = d - 2 * inset
        if side > 0:
            rect(img, u + inset, v + d + row, side, 1, shade(colour, -10))
            rect(img, u + w + d + inset, v + d + row, side, 1, shade(colour, -14))
    # sommet : toute la face du dessus, sinon les cheveux apparaissent au travers
    rect(img, u + d, v, w, d, shade(colour, 22))


def _paint_hat(img, rows):
    """Peint la face avant de la couche chapeau, une liste de (debut, fin, couleur) par rangee."""
    u, v, w, h, d = HAT_REGION
    for row, spans in enumerate(rows):
        for start, stop, colour in spans:
            if stop > start:
                rect(img, u + d + start, v + d + row, stop - start, 1, colour)


def _hat_sides(img, colour, rows=8):
    """Cotes et dos du chapeau : tout le volume, sauf la face avant."""
    u, v, w, h, d = HAT_REGION
    rect(img, u + d, v, w, d, shade(colour, 14))                 # dessus
    rect(img, u, v + d, d, rows, shade(colour, -10))             # cote droit
    rect(img, u + w + d, v + d, d, rows, shade(colour, -14))     # cote gauche
    rect(img, u + d + w + d, v + d, w, rows, shade(colour, -18))  # dos


def draw_head_outer(img, spec, colours):
    """Couche exterieure de la tete : chapeau, capuche ou cheveux volumineux.

    La couche exterieure est un cube un peu plus grand que la tete : les cases
    laissees transparentes dessinent donc des formes qui ne sont pas des cubes
    (un chapeau pointu, une capuche ouverte, un casque a visiere).
    """
    hat = spec['hat']
    trim = spec['trim']

    if hat == 'none':
        if spec['hair_style'] in ('long', 'wild'):
            u, v, w, d = HAT_REGION[0], HAT_REGION[1], HAT_REGION[2], HAT_REGION[4]
            rect(img, u + d, v, w, 3, shade(colours['H'], 10))
        return

    if hat in ('pointed', 'tall'):
        # meme cone que la robe des joueurs : le personnage reste reconnaissable
        # quel que soit l'angle de vue
        # la coiffe descend jusqu'aux sourcils (rangee 3) et pas plus bas :
        # les rangees suivantes restent transparentes, donc le regard est visible
        profile = [3, 3, 2, 1] if hat == 'pointed' else [3, 4, 2, 1]  # rangees 0 a 3
        cone(img, HAT_REGION, profile, trim)
        u, v, w, h, d = HAT_REGION
        rect(img, u + d, v + d + len(profile) - 1, w, 1, GOLD)  # galon dore, en bas de la coiffe
    elif hat == 'hood':
        colour = spec['hood_colour']
        rows = [
            [(0, 8, shade(colour, 8))],
            [(0, 8, colour)],
            [(0, 8, colour)],
            [(0, 2, shade(colour, -6)), (6, 8, shade(colour, -6))],
            [(0, 2, shade(colour, -8)), (6, 8, shade(colour, -8))],
            [(0, 2, shade(colour, -10)), (6, 8, shade(colour, -10))],
            [(0, 2, shade(colour, -12)), (6, 8, shade(colour, -12))],
            [(0, 2, shade(colour, -14)), (6, 8, shade(colour, -14))],
        ]
        _paint_hat(img, rows)
        _hat_sides(img, colour)
    elif hat == 'helm':
        metal, dark = METAL, METAL_DARK
        rows = [
            [(0, 8, shade(metal, 10))],
            [(0, 8, metal)],
            [(0, 8, metal)],
            [(0, 8, dark)],                              # bandeau sur les sourcils
            [(0, 1, dark), (2, 3, dark), (5, 6, dark), (7, 8, dark)],   # montants lateraux
            [(0, 1, dark), (7, 8, dark)],                # protege-joues
            [(0, 1, metal), (7, 8, metal)],
            [(0, 1, dark), (7, 8, dark)],
        ]
        _paint_hat(img, rows)
        _hat_sides(img, metal)
    elif hat in ('cap', 'straw'):
        colour = (214, 186, 108, 255) if hat == 'straw' else trim
        band = (150, 122, 62, 255) if hat == 'straw' else shade(trim, -30)
        # trois rangees de calotte, puis le bord : la coiffe s'arrete au-dessus des
        # yeux (rangee 3 du visage, celle des sourcils)
        rows = [
            [(0, 8, shade(colour, 12))],
            [(0, 8, colour)],
            [(0, 8, shade(colour, -4))],
            [(0, 8, band)],
        ]
        _paint_hat(img, rows)
        _hat_sides(img, colour, rows=4)
        if hat == 'straw':
            # grand bord de paille : sur la rangee des sourcils (jamais sur celle des
            # yeux), et un peu plus bas sur les cotes seulement
            brim = HAT_REGION[1] + HAT_REGION[4] + 3
            rect(img, 32, brim, 28, 1, band)
            rect(img, 32, brim + 1, 8, 2, band)
            rect(img, 44, brim + 1, 16, 2, band)


def draw_torso(img, spec, colours):
    """Robe ou tunique, ceinture et col."""
    robe = spec['robe']
    trim = spec['trim']
    part(img, *BODY, robe)

    # col en V : deux traits de couleur de bord
    put(img, 20, 20, trim)
    put(img, 20, 21, trim)
    put(img, 27, 20, trim)
    put(img, 27, 21, trim)
    rect(img, 22, 20, 4, 2, shade(robe, -22))       # encolure

    if spec['belt']:
        rect(img, 20, 28, 8, 2, shade(spec['belt_colour'], 0))
        rect(img, 23, 28, 2, 2, GOLD)               # boucle
    if spec.get('buttons'):
        for y in (24, 26):
            put(img, 23, y, GOLD)
    if spec.get('apron'):
        rect(img, 22, 22, 4, 8, spec['apron'])
    if spec.get('armour'):
        rect(img, 16 + 4, 16 + 4, 8, 4, METAL)      # plastron
        rect(img, 16 + 4, 16 + 4, 8, 1, shade(METAL, 18))
        rect(img, 16 + 4, 16 + 4 + 3, 8, 1, METAL_DARK)
    if spec.get('emblem'):
        rect(img, 23, 25, 2, 2, spec['emblem'])


def draw_torso_outer(img, spec, colours):
    """Couche exterieure du torse : veste, cape ou plastron."""
    if not spec['sleeves_outer']:
        return
    u, v, w, h, d = JACKET
    rect(img, u + d, v, w, d, shade(spec['robe'], 12))
    rect(img, u, v + d, d, h, shade(spec['robe'], 4))
    rect(img, u + w + d, v + d, d, h, shade(spec['robe'], -4))
    rect(img, u + d, v + d, w, h, shade(spec['robe'], 8))
    rect(img, u + d, v + d + h - 2, w, 2, shade(spec['robe'], -30))      # bas de veste
    rect(img, u + d + w + d, v + d, w, h, shade(spec['robe'], -6))
    rect(img, 22, 21, 4, 1, spec['trim'])                                # col exterieur


def draw_limbs(img, spec, colours):
    """Bras et jambes : manches, mains, pantalon et chaussures."""
    robe = spec['robe']
    trouser = spec['trouser']

    part(img, *ARM_RIGHT, robe)
    part(img, *ARM_LEFT, robe)
    part(img, *LEG_RIGHT, trouser)
    part(img, *LEG_LEFT, trouser)

    if spec['sleeves']:
        # la main depasse de la manche : 4 pixels de peau en bas du bras
        rect(img, 44, 24, 4, 4, colours['s'])
        rect(img, 36, 56, 4, 4, colours['s'])
        rect(img, 44, 16 + 4, 4, 2, spec['trim'])
        rect(img, 36, 48 + 4, 4, 2, spec['trim'])
    else:
        rect(img, 44, 20, 4, 8, colours['s'])
        rect(img, 36, 52, 4, 8, colours['s'])
        rect(img, 44, 16 + 4, 4, 4, robe)
        rect(img, 36, 48 + 4, 4, 4, robe)

    # chaussures : 4 pixels en bas de chaque jambe
    rect(img, 4, 24, 4, 4, BOOT)
    rect(img, 20, 56, 4, 4, BOOT)
    rect(img, 8, 24, 4, 4, shade(BOOT, 10))
    rect(img, 24, 56, 4, 4, shade(BOOT, 10))


def draw_limbs_outer(img, spec, colours):
    """Couche exterieure : manches longues et bords de botte."""
    if not spec['sleeves_outer']:
        return
    for region in (SLEEVE_RIGHT, SLEEVE_LEFT):
        u, v, w, h, d = region
        rect(img, u + d, v, w, d, shade(spec['robe'], 12))
        rect(img, u, v + d, d, h, shade(spec['robe'], 4))
        rect(img, u + w + d, v + d, d, h, shade(spec['robe'], -4))
        rect(img, u + d, v + d, w, h, shade(spec['robe'], 10))
        rect(img, u + d, v + d + h - 2, w, 2, shade(spec['trim'], -10))  # revers de manche
    for region in (TROUSER_RIGHT, TROUSER_LEFT):
        u, v, w, h, d = region
        rect(img, u + d, v, w, d, shade(spec['trouser'], 12))
        rect(img, u, v + d, d, h, shade(spec['trouser'], 4))
        rect(img, u + w + d, v + d, d, h, shade(spec['trouser'], -6))
        rect(img, u + d, v + d, w, h, shade(spec['trouser'], 8))
        rect(img, u + d, v + d + h - 3, w, 3, BOOT)                       # botte


def draw_robes(img, spec, colours):
    """Assemble une apparence complete."""
    draw_head(img, spec, colours)
    draw_torso(img, spec, colours)
    draw_limbs(img, spec, colours)
    draw_head_outer(img, spec, colours)
    draw_torso_outer(img, spec, colours)
    draw_limbs_outer(img, spec, colours)
    return img


def complete(spec):
    """Complete une allure : toute case absente recoit sa valeur par defaut."""
    filled = dict(DEFAULT_LOOK)
    filled.update(spec)
    filled['trim'] = filled.get('trim') or shade(filled['robe'], 46)
    filled['belt'] = filled.get('belt', True)
    filled['belt_colour'] = filled.get('belt_colour', (62, 44, 30, 255))
    filled['sleeves_outer'] = filled.get('sleeves', True)
    filled['trouser'] = filled.get('trouser') or DEFAULT_TROUSER
    filled['hood_colour'] = filled.get('hood_colour') or shade(filled['robe'], -30)
    filled['beard'] = filled.get('beard', 'none')
    filled['hat'] = filled.get('hat', 'none')
    filled['sleeves'] = filled.get('sleeves', False)
    filled['robe'] = filled.get('robe', rgb(0x6C7488))
    return filled


def character(spec):
    """Une image de 64 x 64 contenant l'apparence complete."""
    spec = complete(spec)
    return draw_robes(blank(64, 64), spec, palette(spec))


# ------------------------------------------------- apparences des PNJ

# Traits choisis a la main pour chaque PNJ : c'est ce qui donne a chacun son
# visage. Les couleurs de tenue viennent du catalogue du moteur (Npcs.java),
# donc changer une robe se fait a un seul endroit.
LOOKS = {
    'headmaster_alderwise': dict(skin='fair', hair_colour='white', hair_style='long',
                                 eyes='grey', beard='full', hat='tall', mouth='grim',
                                 emblem=GOLD),
    'shopkeeper_beatrice': dict(skin='rosy', hair_colour='chestnut', hair_style='bun',
                                eyes='hazel', hat='cap', belt=False,
                                apron=(232, 226, 206, 255)),
    'guard_quentin': dict(skin='tan', hair_colour='dark_brown', hair_style='short',
                          eyes='brown', hat='helm', armour=True, sleeves=False, mouth='grim'),
    'lea_nightchant': dict(skin='fair', hair_colour='green', hair_style='long',
                           eyes='green', hat='pointed', mouth='smile'),
    'melisande_whitehorn': dict(skin='porcelain', hair_colour='blonde', hair_style='braid',
                                eyes='blue', hat='none', freckles=True),
    'hanna_emberwright': dict(skin='olive', hair_colour='auburn', hair_style='ponytail',
                              eyes='amber', hat='none', apron=LEATHER),
    'private_quinn': dict(skin='brown', hair_colour='black', hair_style='short',
                          eyes='brown', hat='helm', armour=True, sleeves=False),
    'volcano_spirit': dict(skin='ashen', hair_colour='ash', hair_style='wild',
                           eyes='ember', hat='none', scarf=(226, 104, 42, 255),
                           robe=(120, 62, 44, 255)),
    'captain_marrow': dict(skin='deep', hair_colour='ash', hair_style='short',
                           eyes='frost', hat='hood', hood=True, mouth='grim'),
    'fisherman_pike': dict(skin='tan', hair_colour='brown', hair_style='short',
                           eyes='blue', hat='straw', sleeves=False),
    'warden_ashwind': dict(skin='fair', hair_colour='grey', hair_style='long',
                           eyes='violet', hat='hood', hood=True, hood_colour=(96, 118, 152, 255)),
    'lost_apprentice': dict(skin='porcelain', hair_colour='black', hair_style='wild',
                            eyes='grey', hat='hood', hood=True, mouth='grim',
                            hood_colour=(150, 156, 166, 255)),
    'wizard_of_stone': dict(skin='ashen', hair_colour='grey', hair_style='bald',
                            eyes='grey', hat='none', mouth='grim', freckles=False),
}

# Fonctions du moteur (NpcRole) -> allure par defaut.
ROLE_DEFAULTS = {
    'TRAINER': dict(hat='pointed', sleeves=True, belt=True),
    'QUEST_GIVER': dict(hat='none', sleeves=True, belt=True),
    'SHOPKEEPER': dict(hat='cap', sleeves=True, belt=True),
    'GUARD': dict(hat='helm', sleeves=False, belt=True),
    'VILLAGER': dict(hat='none', sleeves=False, belt=True),
    'BLACKSMITH': dict(hat='none', sleeves=False, belt=True, apron=LEATHER),
}

# Coiffe du modele 3D -> coiffe plate dessinee dans la peau (doublure sous le
# modele). Les deux formes viennent du meme choix : voir Attire.java.
HAT_FOR_ATTIRE = {
    'NONE': 'none',
    'POINTED': 'pointed',
    'HOOD': 'hood',
    'HELM': 'helm',
    'CAP': 'cap',
    'STRAW': 'straw',
}

DEFAULT_LOOK = dict(skin='fair', hair_colour='brown', hair_style='short', eyes='brown',
                    hat='none', beard='none', mouth='smile', sleeves=False)
DEFAULT_TROUSER = (72, 76, 92, 255)


def npc_table():
    """Lit le catalogue des PNJ du moteur : identifiant, role, ecole, couleur de tenue.

    Le fichier Npcs.java est la seule source de verite : changer la couleur d'une
    robe dans le mod change la peau generee, sans rien dupliquer ici.
    """
    with open(NPCS_JAVA, encoding='utf-8') as f:
        source = f.read()

    entries = []
    for block in re.split(r'NpcDef\.builder\("', source)[1:]:
        npc_id = block.split('"')[0]
        outfit = re.search(r'\.outfit\(0x([0-9A-Fa-f]{6})\)', block)
        school = re.search(r'\.school\((\w+)\)', block)
        role = re.search(r'role\(NpcRole\.(\w+)\)', block)
        attire = re.search(r'\.attire\(Attire\.(\w+)\)', block)
        entries.append({
            'id': npc_id,
            'outfit': rgb(int(outfit.group(1), 16)) if outfit else rgb(0x9B59B6),
            'school': school.group(1).lower() if school else None,
            'role': role.group(1) if role else 'VILLAGER',
            'attire': attire.group(1) if attire else 'NONE',
        })

    # les professeurs passent par teacher(...) : ecole et couleur en clair
    schools = ('PYROMANCY', 'TEMPEST', 'FROST', 'RUNOMANCY', 'SYLVAN', 'NECROMANCY',
               'EQUILIBRIUM')
    for block in re.split(r'\n\s*teacher\("', source)[1:]:
        npc_id = block.split('"')[0]
        school = next((name for name in schools
                       if re.search(r',\s*' + name + r',', block)), None)
        outfit = re.search(r',\s*[A-Z_]+,\s*0x([0-9A-Fa-f]{6})', block)
        entries.append({
            'id': npc_id,
            'outfit': rgb(int(outfit.group(1), 16)) if outfit else rgb(0x9B59B6),
            'school': school.lower() if school else None,
            'role': 'TRAINER',
            'attire': 'POINTED',
        })
    return entries


SCHOOL_COLOURS = {
    'pyromancy': rgb(0xC44C28),
    'tempest': rgb(0x7A55D6),
    'frost': rgb(0x74C4E8),
    'runomancy': rgb(0xD9A93C),
    'sylvan': rgb(0x5FBF63),
    'necromancy': rgb(0x6B7280),
    'equilibrium': rgb(0xD7C08D),
}

TROUSER_COLOURS = {
    'GUARD': (66, 70, 84, 255),
    'VILLAGER': (92, 76, 58, 255),
    'SHOPKEEPER': (84, 66, 52, 255),
    'TRAINER': (58, 54, 74, 255),
}

# variation deterministe : deux PNJ sans allure declaree ne se ressemblent pas
def _hash(text):
    value = 2166136261
    for character in text:
        value = ((value ^ ord(character)) * 16777619) & 0xffffffff
    return value


def spec_for(entry):
    """Allure complete d'un PNJ : traits declares + couleurs du moteur."""
    npc_id = entry['id']
    look = dict(DEFAULT_LOOK)
    look.update(ROLE_DEFAULTS.get(entry['role'], {}))
    look.update(LOOKS.get(npc_id, {}))
    # la coiffe portee est un modele 3D (voir WizardRobeModel) : la peau ne la
    # dessine donc plus, ses bords depasseraient autour du modele
    look['hat'] = 'none'
    look['attire'] = entry['attire']

    school = entry['school']
    if entry['role'] == 'TRAINER':
        look.update(dict(hat='pointed', belt=True, sleeves=True, mouth='grim'))

    # les PNJ non decrits a la main varient selon leur identifiant
    seed = _hash(npc_id)
    if npc_id not in LOOKS:
        tones = sorted(SKIN_TONES)
        hairs = sorted(HAIR_COLOURS)
        styles = ('short', 'long', 'bun', 'ponytail', 'bald')
        looks_list = sorted(EYE_COLOURS)
        look['skin'] = tones[seed % len(tones)]
        look['hair_colour'] = hairs[(seed >> 3) % len(hairs)]
        look['hair_style'] = styles[(seed >> 7) % len(styles)]
        look['eyes'] = looks_list[(seed >> 11) % len(looks_list)]

    if look.get('robe') is None:
        school_colour = SCHOOL_COLOURS.get(school)
        look['robe'] = school_colour if (look['hat'] == 'pointed' and school_colour) else entry['outfit']
    look.setdefault('trouser', TROUSER_COLOURS.get(entry['role'], DEFAULT_TROUSER))
    return complete(look)


def write_npc_skins():
    """Ecrit une apparence par PNJ (et supprime les anciennes, par ecole)."""
    out_dir = os.path.join(ASSETS, 'textures', 'entity', 'npc')
    os.makedirs(out_dir, exist_ok=True)

    written = []
    for entry in npc_table():
        spec = spec_for(entry)
        from pixelart import write_png
        path = os.path.join(out_dir, entry['id'] + '.png')
        write_png(path, character(spec), 64, 64)
        written.append(entry['id'])

    # une apparence neutre, pour un PNJ inconnu ou un personnage sans ecole
    neutral = dict(DEFAULT_LOOK)
    neutral.update(dict(robe=rgb(0x6C7488), belt=True, trim=rgb(0xC4C8D6)))
    from pixelart import write_png  # noqa: F811
    write_png(os.path.join(out_dir, 'neutral.png'), character(neutral), 64, 64)

    # nettoyage : les anciennes peaux par ecole ne servent plus
    for name in os.listdir(out_dir):
        if name.endswith('.png') and name[:-4] not in written and name != 'neutral.png':
            os.remove(os.path.join(out_dir, name))
    return written


# ------------------------------- robes des joueurs (couche exterieure seule)


def read_robe_model():
    """Relit le modele 3D dans le code Java.

    Le fichier Java est la seule source de verite : chaque ligne y decrit une
    piece, comme dans un modele Blockbench :

        piece <partie> <nom> <matiere> <u> <v> <x> <y> <z> <largeur> <hauteur> <profondeur>
        pivot <partie> <x> <y> <z>

    Le generateur dessine les textures de ces pieces et les apercus les dessinent
    en 3D, donc l'image et le modele ne peuvent pas diverger.
    """
    path = os.path.join(ROOT, 'src', 'main', 'java', 'com', 'arcanumrealms', 'client',
                        'model', 'WizardRobeModel.java')
    with open(path, encoding='utf-8') as f:
        source = f.read()
    table = re.search(r'PIECES = """(.*?)""";', source, re.S)
    if not table:
        raise SystemExit('table PIECES introuvable dans WizardRobeModel.java')

    pieces = []
    pivots = {}
    for raw in table.group(1).splitlines():
        line = raw.strip()
        if not line or line.startswith('#'):
            continue
        words = line.split()
        if words[0] == 'pivot':
            pivots[words[1]] = tuple(float(value) for value in words[2:5])
        elif words[0] == 'piece':
            part, name, layer = words[1], words[2], words[3]
            u, v = int(words[4]), int(words[5])
            x, y, z = (float(value) for value in words[6:9])
            w, h, d = (float(value) for value in words[9:12])
            pivot = pivots.get(part, (0.0, 0.0, 0.0))
            pieces.append({
                'part': part, 'name': name, 'layer': layer, 'u': u, 'v': v,
                'box': (x, y, z, x + w, y + h, z + d), 'size': (w, h, d),
                'pivot': pivot,
            })
        else:
            raise SystemExit(f'ligne de modele illisible : {line}')
    if not pieces:
        raise SystemExit('aucune piece trouvee dans WizardRobeModel.java')
    return pieces


# ------------------------------------------------- textures de la tenue
#
# Le tissu est dessine en niveaux de gris : il est teinte en jeu par la couleur de
# l'ecole. Les details gardent leur couleur (metal, cuir, galon, gemme).

CLOTH_TONES = {'cloth': 208, 'trim': 238, 'dark': 150}

DETAIL_COLOURS = {
    'metal': (176, 182, 194, 255),
    'metal_dark': (116, 122, 136, 255),
    'leather': (108, 74, 46, 255),
    'leather_dark': (78, 52, 32, 255),
    'gold': (216, 176, 76, 255),
    'gold_dark': (168, 130, 52, 255),
    'gem': (108, 200, 232, 255),
    'hem': (150, 108, 52, 255),
}


def detail_colour(name):
    """Couleur d'une piece de detail, d'apres son nom."""
    if name.startswith('helm'):
        return DETAIL_COLOURS['metal']
    if 'gem' in name:
        return DETAIL_COLOURS['gem']
    if 'belt' in name:
        return DETAIL_COLOURS['leather']
    if 'band' in name and 'straw' not in name:
        return DETAIL_COLOURS['gold_dark']
    if 'straw_band' in name:
        return DETAIL_COLOURS['leather']
    if 'hem' in name:
        return DETAIL_COLOURS['hem']
    if 'hood_edge' in name:
        return (86, 92, 108, 255)
    if 'placket' in name:
        return (232, 226, 206, 255)
    if 'trim' in name or 'buckle' in name:
        return DETAIL_COLOURS['gold']
    return DETAIL_COLOURS['gold']


def face_regions(u, v, length, height, depth):
    """Les six rectangles d'une piece dans la texture (mise en page de Minecraft)."""
    return {
        'dessus': (u + depth, v, length, depth),
        'dessous': (u + depth + length, v, length, depth),
        'droite': (u, v + depth, depth, height),
        'avant': (u + depth, v + depth, length, height),
        'gauche': (u + depth + length, v + depth, depth, height),
        'dos': (u + depth + length + depth, v + depth, length, height),
    }


def robe_texture(pieces=None, size=256):
    """Dessine les deux textures de la tenue : (tissu, details).

    Le tissu est en niveaux de gris (il sera teinte), les details gardent leurs
    couleurs. Les faces sont ombrees — dessus plus clair, dessous plus sombre —
    et un pli marque le milieu du tissu : c'est ce qui donne du volume a distance.
    """
    cloth = blank(size, size)
    detail = blank(size, size)
    pieces = pieces or read_robe_model()

    for piece in pieces:
        u, v = piece['u'], piece['v']
        width, height, depth = (int(round(value)) for value in piece['size'])
        is_cloth = piece['layer'] == 'cloth'
        base = CLOTH_TONES['cloth']
        colour = detail_colour(piece['name'])

        def tone(delta):
            """Couleur d'une face : gris pour le tissu, couleur du detail sinon."""
            if is_cloth:
                value = max(40, min(255, base + delta))
                return (value, value, value, 255)
            return shade(colour, delta // 2)

        target = cloth if is_cloth else detail
        for face, (rx, ry, rw, rh) in face_regions(u, v, width, height, depth).items():
            shift = {'dessus': 26, 'dessous': -44, 'droite': -12, 'avant': 0,
                     'gauche': -22, 'dos': -30}[face]
            rect(target, rx, ry, rw, rh, tone(shift))

        # plis et liseres : c'est ce qui distingue une piece d'un simple cube
        if is_cloth:
            if height >= 4:
                rect(cloth, u + depth, v + depth + height // 2, width, 1, tone(-24))
            if width >= 5:
                rect(cloth, u + depth + width // 3, v + depth, 1, height, tone(-14))
            if height >= 6:
                rect(cloth, u + depth, v + depth + 1, width, 1, tone(12))
        else:
            if height >= 2:
                rect(detail, u + depth, v + depth, width, 1, tone(30))
    return cloth, detail


def write_wizard_robe():
    """Ecrit les deux textures de la tenue 3D."""
    from pixelart import write_png
    out_dir = os.path.join(ASSETS, 'textures', 'entity', 'wizard')
    os.makedirs(out_dir, exist_ok=True)
    for name in os.listdir(out_dir):
        if name.endswith('.png'):
            os.remove(os.path.join(out_dir, name))
    cloth, detail = robe_texture()
    write_png(os.path.join(out_dir, 'cloth.png'), cloth, 256, 256)
    write_png(os.path.join(out_dir, 'detail.png'), detail, 256, 256)
    return ['cloth', 'detail']


def player_skin():
    """Une apparence de joueur simple, pour les apercus de tenue.

    C'est ce que porte un joueur avant la robe : peau, cheveux, chemise et
    pantalon. La tenue 3D se pose par-dessus, sans la remplacer.
    """
    spec = complete(dict(skin='fair', hair_colour='dark_brown', hair_style='short',
                         eyes='brown', robe=rgb(0x6FA8C8), trim=rgb(0x8FC4DC),
                         sleeves=False, belt=False, trouser=rgb(0x46506E), hat='none'))
    return character(spec)


def main():
    skins = write_npc_skins()
    robes = write_wizard_robe()
    model = read_robe_model()
    cloth = sum(1 for piece in model if piece['layer'] == 'cloth')
    print(f'Apparences : {len(skins)} PNJ, {len(robes)} textures de tenue 3D '
          f'({len(model)} pieces dont {cloth} de tissu).')


if __name__ == '__main__':
    main()
