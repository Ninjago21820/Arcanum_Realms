#!/usr/bin/env python3
"""Apercu des personnages, sans lancer Minecraft.

Le mod habille ses personnages avec des images au format Minecraft. Ce script
dessine ces images sur un modele humanoide et en fait des planches d'apercu :
c'est ce que le jeu affichera, aux memes regions de texture pres.

    python3 tools/preview-skins.py            # -> docs/skins.png, docs/skins-visages.png, docs/skins-tenues.png
    python3 tools/preview-skins.py --check    # verifie le format des apparences
    python3 tools/preview-skins.py --only lea # une seule apparence

Deux choses sont rendues :
  * les personnages habilles de leur **peau** (visage, cheveux, tunique, bottes) ;
  * la **tenue 3D**, dont le modele est relu dans le code Java
    (`client/model/WizardRobeModel.java`) : chapeau, cape d'epaules, manches,
    ceinture et jupe evasee. La texture de la tenue est en niveaux de gris, donc
    elle est teintee a l'affichage, exactement comme en jeu.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pixelart import blank, paste, put, read_png, rect, scale, write_png  # noqa: E402
import character_skins  # noqa: E402

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
SKIN_DIR = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'arcanum_realms',
                        'textures', 'entity', 'npc')
ROBE_DIR = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'arcanum_realms',
                        'textures', 'entity', 'wizard')
# matieres de la tenue : le tissu est teinte, les details gardent leur couleur
ROBE_TEXTURES = {'cloth': os.path.join(ROBE_DIR, 'cloth.png'),
                 'detail': os.path.join(ROBE_DIR, 'detail.png')}
ROBE_CACHE = {}


def robe_texture(layer):
    if layer not in ROBE_CACHE:
        ROBE_CACHE[layer] = read_png(ROBE_TEXTURES[layer])
    return ROBE_CACHE[layer]

# --------------------------------------------------------------- le modele
#
# Boites du modele de joueur, en unites de modele (1 unite = 1 pixel de peau),
# pieds a y = 0. Chaque boite a sa region dans la peau (texOffs), exactement
# comme le modele humanoide standard de Minecraft.

PARTS = [
    ('jambe droite', (-4, 0, -2, 0, 12, 2), (0, 16), (4, 12, 4)),
    ('jambe gauche', (0, 0, -2, 4, 12, 2), (16, 48), (4, 12, 4)),
    ('corps', (-4, 12, -2, 4, 24, 2), (16, 16), (8, 12, 4)),
    ('bras droit', (-8, 12, -2, -4, 24, 2), (40, 16), (4, 12, 4)),
    ('bras gauche', (4, 12, -2, 8, 24, 2), (32, 48), (4, 12, 4)),
    ('tete', (-4, 24, -4, 4, 32, 4), (0, 0), (8, 8, 8)),
]

# couches exterieures de la peau (chapeau et veste dessines dans la peau elle-meme)
OVERLAYS = [
    ('bas droit', (-4, 0, -2, 0, 12, 2), (0, 32), (4, 12, 4)),
    ('bas gauche', (0, 0, -2, 4, 12, 2), (0, 48), (4, 12, 4)),
    ('veste', (-4, 12, -2, 4, 24, 2), (16, 32), (8, 12, 4)),
    ('manche droite', (-8, 12, -2, -4, 24, 2), (40, 32), (4, 12, 4)),
    ('manche gauche', (4, 12, -2, 8, 24, 2), (48, 48), (4, 12, 4)),
    ('chapeau', (-4, 24, -4, 4, 32, 4), (32, 0), (8, 8, 8)),
]

FACE_ORDER = ('dessus', 'dessous', 'droite', 'avant', 'gauche', 'dos')

# Deplacements des parties du modele de joueur, dans le repere de Minecraft (ou
# l'axe vertical descend depuis les epaules, les pieds etant a y = 24). La tete
# et le torse n'en ont pas ; les bras sont decales sur les cotes.
PART_OFFSETS = {
    'head': (0.0, 0.0, 0.0),
    'body': (0.0, 0.0, 0.0),
    'right_arm': (-5.0, 2.0, 0.0),
    'left_arm': (5.0, 2.0, 0.0),
    # parties d'animation : leur pivot est celui du corps ou de la tete
    'cape': (0.0, 0.0, 0.0),
    'skirt': (0.0, 0.0, 0.0),
}


def face_regions(u, v, length, height, depth):
    """Les six rectangles d'une boite dans la texture (mise en page de Minecraft)."""
    return {
        'dessus': (u + depth, v, length, depth),
        'dessous': (u + depth + length, v, length, depth),
        'droite': (u, v + depth, depth, height),
        'avant': (u + depth, v + depth, length, height),
        'gauche': (u + depth + length, v + depth, depth, height),
        'dos': (u + depth + length + depth, v + depth, length, height),
    }


def face_corners(box, face):
    """Les quatre coins d'une face, de haut-gauche a bas-gauche."""
    x0, y0, z0, x1, y1, z1 = box
    return {
        'dessus': ((x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)),
        'dessous': ((x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)),
        'droite': ((x0, y1, z1), (x0, y1, z0), (x0, y0, z0), (x0, y0, z1)),
        'avant': ((x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)),
        'gauche': ((x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0)),
        'dos': ((x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)),
    }[face]


def face_normal(face):
    return {'dessus': (0, 1, 0), 'dessous': (0, -1, 0), 'droite': (-1, 0, 0),
            'avant': (0, 0, 1), 'gauche': (1, 0, 0), 'dos': (0, 0, -1)}[face]


# ---------------------------------------------------------------- le rendu


def project(point, yaw, pitch, scale_factor):
    """Tourne le modele puis projette (orthographiquement) vers l'ecran."""
    x, y, z = point
    cy, sy = math.cos(yaw), math.sin(yaw)
    x2, z2 = x * cy + z * sy, -x * sy + z * cy
    cp, sp = math.cos(pitch), math.sin(pitch)
    y3, z3 = y * cp - z2 * sp, y * sp + z2 * cp
    return x2 * scale_factor, -y3 * scale_factor, z3


def sample(texture, u, v, tint=(1.0, 1.0, 1.0)):
    height = len(texture)
    width = len(texture[0])
    if u < 0 or v < 0 or u >= width or v >= height:
        return (0, 0, 0, 0)
    pixel = texture[v][u]
    if pixel[3] == 0 or tint == (1.0, 1.0, 1.0):
        return pixel
    return (min(255, int(pixel[0] * tint[0])), min(255, int(pixel[1] * tint[1])),
            min(255, int(pixel[2] * tint[2])), pixel[3])


def fill_triangle(target, points, uvs, texture, tint):
    """Remplit un triangle en echantillonnant la texture (le plus proche voisin)."""
    (ax, ay), (bx, by), (cx, cy) = points
    area = (bx - ax) * (cy - ay) - (cx - ax) * (by - ay)
    if abs(area) < 1e-9:
        return
    inverse = 1.0 / area
    min_x = max(0, int(math.floor(min(ax, bx, cx))))
    max_x = min(len(target[0]) - 1, int(math.ceil(max(ax, bx, cx))))
    min_y = max(0, int(math.floor(min(ay, by, cy))))
    max_y = min(len(target) - 1, int(math.ceil(max(ay, by, cy))))
    for py in range(min_y, max_y + 1):
        yy = py + 0.5
        for px in range(min_x, max_x + 1):
            xx = px + 0.5
            weight_b = ((xx - ax) * (cy - ay) - (cx - ax) * (yy - ay)) * inverse
            weight_c = ((bx - ax) * (yy - ay) - (xx - ax) * (by - ay)) * inverse
            weight_a = 1.0 - weight_b - weight_c
            if weight_a < -1e-6 or weight_b < -1e-6 or weight_c < -1e-6:
                continue
            u = weight_a * uvs[0][0] + weight_b * uvs[1][0] + weight_c * uvs[2][0]
            v = weight_a * uvs[0][1] + weight_b * uvs[1][1] + weight_c * uvs[2][1]
            colour = sample(texture, int(math.floor(u)), int(math.floor(v)), tint)
            if colour[3] > 0:
                target[py][px] = colour


def render(pieces, yaw_degrees=28, pitch_degrees=16, pixels_per_unit=4.0, size=None,
           margin=6, keep=None):
    """Dessine une liste de pieces (personnage ou gros plan) vu de trois quarts avant.

    `keep` permet de ne dessiner qu'une partie du personnage (la tete, par
    exemple) ; le contenu est alors centre dans l'image.
    """
    yaw = math.radians(yaw_degrees)
    pitch = math.radians(pitch_degrees)
    cy, sy = math.cos(yaw), math.sin(yaw)
    cp, sp = math.cos(pitch), math.sin(pitch)

    faces = []
    for piece in pieces:
        if keep and piece['name'] not in keep:
            continue
        for face in FACE_ORDER:
            normal = face_normal(face)
            nx, nz = normal[0] * cy + normal[2] * sy, -normal[0] * sy + normal[2] * cy
            ny, nz2 = normal[1] * cp - nz * sp, normal[1] * sp + nz * cp
            if nz2 <= 0:
                continue
            corners = face_corners(piece['box'], face)
            projected = [project(c, yaw, pitch, pixels_per_unit) for c in corners]
            depth = sum(p[2] for p in projected) / 4.0
            faces.append((depth, projected, piece['regions'][face], piece['texture'],
                          piece['tint']))
    if not faces:
        return blank(8, 8)
    faces.sort(key=lambda entry: entry[0])

    xs = [p[0] for _d, projected, _r, _t, _c in faces for p in projected]
    ys = [p[1] for _d, projected, _r, _t, _c in faces for p in projected]
    width = (max(xs) - min(xs)) + 2 * margin
    height = (max(ys) - min(ys)) + 2 * margin
    size = int(max(width, height, size or 0))
    canvas = blank(size, size)
    origin_x = size / 2.0 - (min(xs) + max(xs)) / 2.0
    origin_y = size / 2.0 - (min(ys) + max(ys)) / 2.0

    for _depth, projected, (ru, rv, rw, rh), texture, tint in faces:
        points = [(origin_x + px, origin_y + py) for px, py, _z in projected]
        uvs = ((ru, rv), (ru + rw, rv), (ru + rw, rv + rh), (ru, rv + rh))
        fill_triangle(canvas, (points[0], points[1], points[2]),
                      (uvs[0], uvs[1], uvs[2]), texture, tint)
        fill_triangle(canvas, (points[0], points[2], points[3]),
                      (uvs[0], uvs[2], uvs[3]), texture, tint)
    return canvas


# ------------------------------------------------------- pieces a dessiner


def skin_pieces(skin):
    """Les pieces du personnage telles qu'elles sont dessinees dans sa peau."""
    pieces = []
    for boxes in (PARTS, OVERLAYS):
        for name, box, (u, v), dims in boxes:
            regions = face_regions(u, v, *dims)
            pieces.append({'name': name, 'box': box, 'regions': regions,
                           'texture': skin, 'tint': (1.0, 1.0, 1.0)})
    return pieces


def robe_pieces(colour, attire, java_pieces=None):
    """La tenue 3D, teintee (tissu) ou en couleurs (details), coiffe choisie.

    Le modele est relu dans le code Java ; seules les pieces de la coiffe portee
    sont dessinees, comme le fait la couche de rendu du mod.
    """
    java_pieces = java_pieces if java_pieces is not None else character_skins.read_robe_model()
    tint = tuple(channel / 255.0 for channel in colour[:3])
    pieces = []
    for piece in java_pieces:
        if not hat_visible(piece['name'], attire):
            continue
        # repere du modele Java (verticale vers le bas, epaules a 0) vers le repere de
        # l'apercu (pieds a 0, verticale vers le haut). Le pivot de la partie compte :
        # les pieces sont ecrites dans son repere local.
        ox, oy, oz = PART_OFFSETS[piece['part']]
        px, py, pz = piece['pivot']
        x0, y0, z0, x1, y1, z1 = piece['box']
        box = (x0 + px + ox, 24 - (y1 + py + oy), z0 + pz + oz,
               x1 + px + ox, 24 - (y0 + py + oy), z1 + pz + oz)
        width, height, depth = (int(round(v)) for v in piece['size'])
        regions = face_regions(piece['u'], piece['v'], width, height, depth)
        is_cloth = piece['layer'] == 'cloth'
        pieces.append({'name': piece['name'], 'box': box, 'regions': regions,
                       'texture': robe_texture(piece['layer']),
                       'tint': tint if is_cloth else (1.0, 1.0, 1.0)})
    return pieces


# Quelles pieces appartiennent a quelle coiffe. Miroir de WizardRobeLayer.wearHat
# cote mod : une seule coiffe est portee a la fois.
HATS = {
    'POINTED': ('hat_brim', 'hat_a', 'hat_b', 'hat_c', 'hat_d', 'hat_e', 'hat_tip',
                'hat_band'),
    'HOOD': ('hood_back', 'hood_top', 'hood_edge', 'hood_cowl'),
    'HELM': ('helm_cap', 'helm_nose', 'helm_cheekL', 'helm_cheekR', 'helm_ridge'),
    'CAP': ('cap_crown', 'cap_brim', 'cap_band'),
    'STRAW': ('straw_crown', 'straw_brim', 'straw_band'),
}
# la cape d'epaules est remplacee par le col de la capuche
REPLACED_BY_HOOD = ('mantle', 'mantle_trim')

# toutes les pieces qui appartiennent a une coiffe
HAT_PIECES = {name for pieces in HATS.values() for name in pieces}


def hat_visible(piece_name, attire):
    """Vrai si cette piece du modele doit etre dessinee pour cette coiffe."""
    if piece_name in HAT_PIECES:
        for name, pieces in HATS.items():
            if piece_name in pieces:
                return name == attire
        return False
    if attire == 'HOOD' and piece_name in REPLACED_BY_HOOD:
        return False
    return True


# ------------------------------------------------------------- planches


DIGITS = {
    '0': ('111', '101', '101', '101', '111'), '1': ('010', '110', '010', '010', '111'),
    '2': ('111', '001', '111', '100', '111'), '3': ('111', '001', '111', '001', '111'),
    '4': ('101', '101', '111', '001', '001'), '5': ('111', '100', '111', '001', '111'),
    '6': ('111', '100', '111', '101', '111'), '7': ('111', '001', '010', '010', '010'),
    '8': ('111', '101', '111', '101', '111'), '9': ('111', '101', '111', '001', '111'),
}


def draw_number(img, x, y, number):
    for digit in str(number):
        for row, line in enumerate(DIGITS[digit]):
            for column, pixel in enumerate(line):
                if pixel == '1':
                    put(img, x + column, y + row, (255, 255, 255, 255))
        x += 4


def attire_of(npc_id):
    """La coiffe d'un PNJ, lue dans le catalogue du moteur."""
    for entry in character_skins.npc_table():
        if entry['id'] == npc_id:
            return entry['attire']
    return 'NONE'


def colour_of(npc_id):
    for entry in character_skins.npc_table():
        if entry['id'] == npc_id:
            return entry['outfit']
    return (0x9B, 0x59, 0xB6)


def dressed(skin, colour, attire):
    """Un personnage complet : sa peau, puis sa tenue 3D par-dessus."""
    return skin_pieces(skin) + robe_pieces(colour, attire)


def npc_character(name):
    """Le personnage habille d'un PNJ, pret a dessiner."""
    skin = read_png(os.path.join(SKIN_DIR, name + '.png'))
    return dressed(skin, colour_of(name), attire_of(name))


def planche(names, columns=5, pixels_per_unit=6.0):
    """Les PNJ habilles, vus de face et de trois quarts."""
    view = int(40 * pixels_per_unit)
    cell_w = view * 2 + 8
    cell_h = view + 16
    rows = (len(names) + columns - 1) // columns
    sheet = blank(columns * cell_w, rows * cell_h)
    rect(sheet, 0, 0, len(sheet[0]), len(sheet), (26, 28, 38, 255))
    for index, name in enumerate(names):
        pieces = npc_character(name)
        front = render(pieces, yaw_degrees=0, pitch_degrees=6, pixels_per_unit=pixels_per_unit,
                       size=view)
        side = render(pieces, yaw_degrees=34, pitch_degrees=14, pixels_per_unit=pixels_per_unit,
                      size=view)
        base_x = (index % columns) * cell_w
        base_y = (index // columns) * cell_h
        paste(sheet, front, base_x + 4, base_y + 4)
        paste(sheet, side, base_x + view + 4, base_y + 4)
        draw_number(sheet, base_x + 6, base_y + cell_h - 9, index + 1)
    return sheet


def planche_visages(names, columns=7, pixels_per_unit=14.0):
    """Gros plan sur chaque visage : avant et profil."""
    size = int(14 * pixels_per_unit)
    cell_w = size * 2 + 6
    cell_h = size + 12
    rows = (len(names) + columns - 1) // columns
    sheet = blank(columns * cell_w, rows * cell_h)
    rect(sheet, 0, 0, len(sheet[0]), len(sheet), (26, 28, 38, 255))
    for index, name in enumerate(names):
        pieces = npc_character(name)
        front = render(pieces, yaw_degrees=0, pitch_degrees=0, pixels_per_unit=pixels_per_unit,
                       size=size, keep=('tete',))
        side = render(pieces, yaw_degrees=52, pitch_degrees=6, pixels_per_unit=pixels_per_unit,
                      size=size, keep=('tete',))
        base_x = (index % columns) * cell_w
        base_y = (index // columns) * cell_h
        paste(sheet, front, base_x + 3, base_y + 3)
        paste(sheet, side, base_x + size + 3, base_y + 3)
        draw_number(sheet, base_x + 5, base_y + cell_h - 9, index + 1)
    return sheet


SCHOOLS = (('pyromancy', 0xE4592B), ('tempest', 0x7A55D6), ('frost', 0x74C4E8),
           ('runomancy', 0xD9A93C), ('sylvan', 0x5FBF63), ('necromancy', 0x6B7280),
           ('equilibrium', 0xD7C08D))


def planche_tenues(pixels_per_unit=6.0):
    """La tenue 3D des joueurs, une par ecole (la robe est teintee par l'ecole)."""
    base = character_skins.player_skin()

    entries = [(name, (rgb >> 16 & 0xff, rgb >> 8 & 0xff, rgb & 0xff)) for name, rgb in SCHOOLS]
    entries.append(('sans ecole', (0x6C, 0x74, 0x88)))

    view = int(40 * pixels_per_unit)
    columns = 4
    cell_w = view * 2 + 8
    cell_h = view + 16
    rows = (len(entries) + columns - 1) // columns
    sheet = blank(columns * cell_w, rows * cell_h)
    rect(sheet, 0, 0, len(sheet[0]), len(sheet), (26, 28, 38, 255))
    for index, (name, colour) in enumerate(entries):
        attire = 'NONE' if name == 'sans ecole' else 'POINTED'
        pieces = skin_pieces(base) + robe_pieces(colour, attire)
        front = render(pieces, yaw_degrees=22, pitch_degrees=10,
                       pixels_per_unit=pixels_per_unit, size=view)
        back = render(pieces, yaw_degrees=200, pitch_degrees=10,
                      pixels_per_unit=pixels_per_unit, size=view)
        base_x = (index % columns) * cell_w
        base_y = (index // columns) * cell_h
        paste(sheet, front, base_x + 4, base_y + 4)
        paste(sheet, back, base_x + view + 4, base_y + 4)
        draw_number(sheet, base_x + 6, base_y + cell_h - 9, index + 1)
    return sheet, [name for name, _colour in entries]


# ----------------------------------------------------- verification


def legal_pixels():
    """L'ensemble des pixels utilisables par une peau 64 x 64 de personnage."""
    inside = [[False] * 64 for _ in range(64)]
    for boxes in (PARTS, OVERLAYS):
        for _name, _box, (u, v), (length, height, depth) in boxes:
            for _face, (rx, ry, rw, rh) in face_regions(u, v, length, height, depth).items():
                for y in range(ry, ry + rh):
                    for x in range(rx, rx + rw):
                        if 0 <= x < 64 and 0 <= y < 64:
                            inside[y][x] = True
    return inside


def check_skin(path):
    """Verifie qu'une peau tient dans la mise en page et n'a pas de trou."""
    name = os.path.basename(path)
    skin = read_png(path)
    problems = []
    height = len(skin)
    width = len(skin[0])
    if (width, height) != (64, 64):
        problems.append(f'taille {width}x{height} au lieu de 64x64')
        return name, problems

    inside = legal_pixels()
    strays = sum(1 for y in range(64) for x in range(64)
                 if skin[y][x][3] > 0 and not inside[y][x])
    if strays:
        problems.append(f'{strays} pixel(s) hors des regions du modele')

    # la couche interieure est le corps du personnage : le moindre trou se verrait
    # a travers lui en jeu
    for part_name, _box, (u, v), dims in PARTS:
        for face, (rx, ry, rw, rh) in face_regions(u, v, *dims).items():
            holes = sum(1 for y in range(ry, ry + rh) for x in range(rx, rx + rw)
                        if skin[y][x][3] == 0)
            if holes:
                problems.append(f'{part_name} ({face}) : {holes} pixel(s) transparent(s)')

    # --- ce qui est reellement visible : la couche exterieure recouvre l'interieure
    def visible(x, y):
        return skin[y][x] if skin[y][x][3] > 0 else skin[y][x - 32]

    eye_colours = {visible(40 + x, 8 + 4) for x in range(8)}
    if len(eye_colours) < 3:
        problems.append(f'les yeux ne sont pas visibles ({len(eye_colours)} couleur(s))')

    face_colours = {visible(40 + x, 8 + y) for y in range(8) for x in range(8)}
    if len(face_colours) < 5:
        problems.append(f'le visage manque de details ({len(face_colours)} couleurs)')

    top_colours = {visible(40 + x, 8 + y) for y in range(4) for x in range(8)}
    head_top = {skin[y][8 + x] for y in range(4) for x in range(8)}
    if len(top_colours) < 2 and len(head_top) < 2:
        problems.append('rien ne couvre le haut du crane')
    return name, problems


def check_robe(path, only_layer=None):
    """Verifie une texture de la tenue 3D : chaque piece doit avoir sa place peinte."""
    name = os.path.basename(path)
    texture = read_png(path)
    problems = []
    height = len(texture)
    width = len(texture[0])
    pieces = [piece for piece in character_skins.read_robe_model()
              if only_layer is None or piece['layer'] == only_layer]
    for piece in pieces:
        u, v = piece['u'], piece['v']
        box_w, box_h, box_d = (int(round(value)) for value in piece['size'])
        regions = face_regions(u, v, box_w, box_h, box_d)
        for face, (rx, ry, rw, rh) in regions.items():
            if rx < 0 or ry < 0 or rx + rw > width or ry + rh > height:
                problems.append(f'{piece["name"]} ({face}) sort de l\'image')
                continue
            holes = sum(1 for y in range(ry, ry + rh) for x in range(rx, rx + rw)
                        if texture[y][x][3] == 0)
            if holes:
                problems.append(f'{piece["name"]} ({face}) : {holes} case(s) vide(s)')

    # la teinte en jeu multiplie la texture : elle doit rester claire pour que la
    # couleur de l'ecole ressorte
    values = [texture[y][x][0] for y in range(0, width, 3) for x in range(0, height, 3)
              if texture[y][x][3] > 0 and x < width and y < height]
    if values and only_layer == 'cloth':
        average = sum(values) / len(values)
        if average < 140:
            problems.append(f'tissu trop sombre pour etre teinte (moyenne {average:.0f}/255)')
    if values and max(values) - min(values) < 20:
        problems.append('texture trop uniforme : les faces ne se distinguent pas')
    return name, problems


# ------------------------------------------------------------------ main


def names_in(only=None):
    names = sorted(f[:-4] for f in os.listdir(SKIN_DIR) if f.endswith('.png'))
    if only:
        names = [n for n in names if only in n]
    return names


def main():
    args = sys.argv[1:]
    only = args[args.index('--only') + 1] if '--only' in args else None
    names = names_in(only)
    if not names:
        sys.exit('aucune apparence trouvee dans ' + SKIN_DIR)

    if '--check' in args:
        failures = 0
        for name in names:
            label, problems = check_skin(os.path.join(SKIN_DIR, name + '.png'))
            if problems:
                failures += 1
                print(f'  [KO]   {label}')
                for problem in problems:
                    print(f'         - {problem}')
            else:
                print(f'  [OK]   {label}')
        for layer, filename in (('cloth', 'cloth.png'), ('detail', 'detail.png')):
            robe_label, robe_problems = check_robe(os.path.join(ROBE_DIR, filename), layer)
            if robe_problems:
                failures += 1
                print(f'  [KO]   tenue 3D ({layer}) : {robe_label}')
                for problem in robe_problems:
                    print(f'         - {problem}')
            else:
                print(f'  [OK]   tenue 3D ({layer}) : {robe_label}')
        total = len(names) + 2
        print(f'{total - failures}/{total} elements conformes.')
        sys.exit(1 if failures else 0)

    write_png(os.path.join(ROOT, 'docs', 'skins.png'),
              scale(planche(names, columns=5, pixels_per_unit=6.0), 2))
    write_png(os.path.join(ROOT, 'docs', 'skins-visages.png'),
              scale(planche_visages(names, columns=7, pixels_per_unit=14.0), 2))
    tenues, labels = planche_tenues(pixels_per_unit=6.0)
    write_png(os.path.join(ROOT, 'docs', 'skins-tenues.png'), scale(tenues, 2))

    print(f'{len(names)} personnages rendus :')
    print('  docs/skins.png (personnages entiers)')
    print('  docs/skins-visages.png (visages en gros plan)')
    print('  docs/skins-tenues.png (tenues 3D des joueurs, une par ecole)')
    for index, name in enumerate(names):
        print(f'  {index + 1:2d}. {name}')
    print('  tenues : ' + ', '.join(labels))


if __name__ == '__main__':
    main()
