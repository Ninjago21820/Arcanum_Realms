#!/usr/bin/env python3
"""Apercu des apparences de personnages, sans lancer Minecraft.

Le mod habille ses personnages (PNJ et professeurs) avec des peaux au format
Minecraft habituel : 64 x 64 pixels, memes regions que les peaux de joueur. Ce
script dessine ces peaux sur un modele humanoide et en fait une planche d'apercu,
ce qui permet de verifier une apparence en quelques secondes.

    python3 tools/preview-skins.py                     # -> docs/skins.png (toutes)
    python3 tools/preview-skins.py --check             # verifie le format des peaux
    python3 tools/preview-skins.py --only pierre       # une seule apparence

Le rendu utilise exactement les regions du modele de joueur de Minecraft : une
peau dessinee ici s'affichera pareil dans le jeu, et une peau libre (celle d'un
joueur, par exemple) peut etre previsualisee de la meme facon.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pixelart import blank, paste, put, read_png, rect, scale, write_png  # noqa: E402

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
SKIN_DIR = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'arcanum_realms',
                        'textures', 'entity', 'npc')
ROBE_DIR = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'arcanum_realms',
                        'textures', 'entity', 'wizard')
OUT = os.path.join(ROOT, 'docs', 'skins.png')

# --------------------------------------------------------------- le modele
#
# Boites du modele de joueur, en unites de modele (1 unite = 1 pixel de peau),
# pieds a y = 0. Chaque boite a sa region dans la peau (texOffs), exactement
# comme le modele humanoide standard de Minecraft.

# nom, (x0, y0, z0, x1, y1, z1), region (u, v), taille (l, h, p)
PARTS = [
    ('jambe droite', (-4, 0, -2, 0, 12, 2), (0, 16), (4, 12, 4)),
    ('jambe gauche', (0, 0, -2, 4, 12, 2), (16, 48), (4, 12, 4)),
    ('corps', (-4, 12, -2, 4, 24, 2), (16, 16), (8, 12, 4)),
    ('bras droit', (-8, 12, -2, -4, 24, 2), (40, 16), (4, 12, 4)),
    ('bras gauche', (4, 12, -2, 8, 24, 2), (32, 48), (4, 12, 4)),
    ('tete', (-4, 24, -4, 4, 32, 4), (0, 0), (8, 8, 8)),
]

# couches exterieures (chapeau, veste, manches, bas) : meme boite, 0,5 unite plus large
OVERLAYS = [
    ('bas droit', (-4, 0, -2, 0, 12, 2), (0, 32), (4, 12, 4)),
    ('bas gauche', (0, 0, -2, 4, 12, 2), (0, 48), (4, 12, 4)),
    ('veste', (-4, 12, -2, 4, 24, 2), (16, 32), (8, 12, 4)),
    ('manche droite', (-8, 12, -2, -4, 24, 2), (40, 32), (4, 12, 4)),
    ('manche gauche', (4, 12, -2, 8, 24, 2), (48, 48), (4, 12, 4)),
    ('chapeau', (-4, 24, -4, 4, 32, 4), (32, 0), (8, 8, 8)),
]

# Les six faces d'une boite, dans l'ordre de la mise en page des peaux.
# Une face est decrite par ses quatre coins (en 3D) et son rectangle dans la peau.
FACE_ORDER = ('dessus', 'dessous', 'droite', 'avant', 'gauche', 'dos')


def face_regions(u, v, length, height, depth):
    """Les six rectangles d'une boite dans la peau (mise en page de Minecraft)."""
    return {
        'dessus': (u + depth, v, length, depth),
        'dessous': (u + depth + length, v, length, depth),
        'droite': (u, v + depth, depth, height),
        'avant': (u + depth, v + depth, length, height),
        'gauche': (u + depth + length, v + depth, depth, height),
        'dos': (u + depth + length + depth, v + depth, length, height),
    }


def face_corners(box, face):
    """Les quatre coins d'une face, dans l'ordre : haut-gauche, haut-droite,
    bas-droite, bas-gauche (tels qu'on les voit en regardant la face)."""
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
    """Tourne le modele puis projette (orthographique) vers l'ecran."""
    x, y, z = point
    cy, sy = math.cos(yaw), math.sin(yaw)
    x2, z2 = x * cy + z * sy, -x * sy + z * cy
    cp, sp = math.cos(pitch), math.sin(pitch)
    y3, z3 = y * cp - z2 * sp, y * sp + z2 * cp
    return x2 * scale_factor, -y3 * scale_factor, z3


def sample(skin, u, v):
    height = len(skin)
    width = len(skin[0])
    if u < 0 or v < 0 or u >= width or v >= height:
        return (0, 0, 0, 0)
    return skin[v][u]


def fill_triangle(target, points, uvs, texture):
    """Remplit un triangle en echantillonnant la texture (plus proche voisin).

    Les coordonnees de texture sont interpolees par coordonnees barycentriques :
    c'est ce qui evite les rayures diagonales dues a une face mal plaquee.
    """
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
            # poids de B et C ; le point est interieur si les deux sont >= 0 et wA >= 0
            weight_b = ((xx - ax) * (cy - ay) - (cx - ax) * (yy - ay)) * inverse
            weight_c = ((bx - ax) * (yy - ay) - (xx - ax) * (by - ay)) * inverse
            weight_a = 1.0 - weight_b - weight_c
            if weight_a < -1e-6 or weight_b < -1e-6 or weight_c < -1e-6:
                continue
            u = weight_a * uvs[0][0] + weight_b * uvs[1][0] + weight_c * uvs[2][0]
            v = weight_a * uvs[0][1] + weight_b * uvs[1][1] + weight_c * uvs[2][1]
            colour = sample(texture, int(math.floor(u)), int(math.floor(v)))
            if colour[3] > 0:
                target[py][px] = colour


def render(skin, yaw_degrees=28, pitch_degrees=16, pixels_per_unit=4.0, size=None,
           parts=None, margin=6):
    """Dessine la peau sur un personnage humanoide, vu de trois quarts avant.

    `parts` permet de ne dessiner qu'une partie du personnage (la tete, par
    exemple) ; le contenu est alors centre dans l'image.
    """
    yaw = math.radians(yaw_degrees)
    pitch = math.radians(pitch_degrees)

    faces = []
    for boxes, overlay in ((PARTS, False), (OVERLAYS, True)):
        for name, box, (u, v), dims in boxes:
            if parts and name not in parts:
                continue
            length, height, depth = dims
            regions = face_regions(u, v, length, height, depth)
            grown = box
            if overlay:
                # les couches exterieures sont legerement plus larges que le corps
                grown = (box[0] - 0.5, box[1] - 0.5, box[2] - 0.5, box[3] + 0.5, box[4] + 0.5, box[5] + 0.5)
            for face in FACE_ORDER:
                normal = face_normal(face)
                corners = face_corners(grown, face)
                # faces cachees : on ne dessine que celles tournees vers la camera
                cy, sy = math.cos(yaw), math.sin(yaw)
                nx, nz = normal[0] * cy + normal[2] * sy, -normal[0] * sy + normal[2] * cy
                cp, sp = math.cos(pitch), math.sin(pitch)
                ny, nz2 = normal[1] * cp - nz * sp, normal[1] * sp + nz * cp
                if nz2 <= 0:
                    continue
                projected = [project(c, yaw, pitch, pixels_per_unit) for c in corners]
                depth_average = sum(p[2] for p in projected) / 4.0
                ru, rv, rw, rh = regions[face]
                uvs = ((ru, rv), (ru + rw, rv), (ru + rw, rv + rh), (ru, rv + rh))
                faces.append((depth_average, projected, uvs, skin, overlay, name))

    faces.sort(key=lambda entry: entry[0])

    if not faces:
        return blank(8, 8)

    # cadre du contenu : on centre le personnage (ou la partie demandee)
    xs = [p[0] for _d, projected, _u, _s, _o, _n in faces for p in projected]
    ys = [p[1] for _d, projected, _u, _s, _o, _n in faces for p in projected]
    width = (max(xs) - min(xs)) + 2 * margin
    height = (max(ys) - min(ys)) + 2 * margin
    # l'image doit toujours contenir le personnage : sinon les pieds ou le chapeau
    # seraient coupes
    size = int(max(width, height, size or 0))
    canvas = blank(size, size)
    origin_x = size / 2.0 - (min(xs) + max(xs)) / 2.0
    origin_y = size / 2.0 - (min(ys) + max(ys)) / 2.0
    for _depth, projected, uvs, texture, _overlay, _name in faces:
        points = [(origin_x + px, origin_y + py) for px, py, _z in projected]
        uv_points = [(uvs[0][0], uvs[0][1]), (uvs[1][0], uvs[1][1]),
                     (uvs[2][0], uvs[2][1]), (uvs[3][0], uvs[3][1])]
        fill_triangle(canvas, (points[0], points[1], points[2]),
                      (uv_points[0], uv_points[1], uv_points[2]), texture)
        fill_triangle(canvas, (points[0], points[2], points[3]),
                      (uv_points[0], uv_points[2], uv_points[3]), texture)
    return canvas


# --------------------------------------------------- verification et planche


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
    strays = 0
    for y in range(64):
        for x in range(64):
            if skin[y][x][3] > 0 and not inside[y][x]:
                strays += 1
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

    # les yeux sont sur la rangee 4 du visage (y = 12) : trois couleurs au moins
    # (peau, blanc de l'oeil, iris) signifient qu'on voit le regard
    eye_colours = {visible(40 + x, 8 + 4) for x in range(8)}
    if len(eye_colours) < 3:
        problems.append(f'les yeux ne sont pas visibles ({len(eye_colours)} couleur(s) sur la rangee des yeux)')

    # le visage entier doit rester lisible : au moins cinq couleurs visibles
    face_colours = {visible(40 + x, 8 + y) for y in range(8) for x in range(8)}
    if len(face_colours) < 5:
        problems.append(f'le visage manque de details ({len(face_colours)} couleurs visibles)')

    # quelqu'un doit coiffer le personnage : cheveux ou coiffe sur le haut du crane
    top_colours = {visible(40 + x, 8 + y) for y in range(4) for x in range(8)}
    head_top = {skin[0 + y][8 + x] for y in range(4) for x in range(8)}
    if len(top_colours) < 2 and len(head_top) < 2:
        problems.append('rien ne couvre le haut du crane (ni cheveux ni coiffe)')

    return name, problems


def planche(names, columns=5, pixels_per_unit=4.0):
    """Assemble les personnages en une planche avec, pour chacun, deux vues."""
    view = int(40 * pixels_per_unit)
    cell_w = view * 2 + 8
    cell_h = view + 14
    rows = (len(names) + columns - 1) // columns
    sheet = blank(columns * cell_w, rows * cell_h)
    rect(sheet, 0, 0, len(sheet[0]), len(sheet), (26, 28, 38, 255))
    for index, name in enumerate(names):
        skin = read_png(os.path.join(SKIN_DIR, name + '.png'))
        front = render(skin, yaw_degrees=0, pitch_degrees=8, pixels_per_unit=pixels_per_unit,
                       size=int(40 * pixels_per_unit))
        three_quarter = render(skin, yaw_degrees=32, pitch_degrees=16,
                               pixels_per_unit=pixels_per_unit, size=int(40 * pixels_per_unit))
        column = index % columns
        row = index // columns
        base_x = column * cell_w
        base_y = row * cell_h
        paste(sheet, front, base_x + 4, base_y + 4)
        paste(sheet, three_quarter, base_x + view + 4, base_y + 4)
        draw_number(sheet, base_x + 6, base_y + cell_h - 9, index + 1)
    return sheet


DIGITS = {
    '0': ('111', '101', '101', '101', '111'), '1': ('010', '110', '010', '010', '111'),
    '2': ('111', '001', '111', '100', '111'), '3': ('111', '001', '111', '001', '111'),
    '4': ('101', '101', '111', '001', '001'), '5': ('111', '100', '111', '001', '111'),
    '6': ('111', '100', '111', '101', '111'), '7': ('111', '001', '010', '010', '010'),
    '8': ('111', '101', '111', '101', '111'), '9': ('111', '101', '111', '001', '111'),
}


def draw_number(img, x, y, number):
    for digit in str(number):
        pattern = DIGITS[digit]
        for row, line in enumerate(pattern):
            for column, pixel in enumerate(line):
                if pixel == '1':
                    put(img, x + column, y + row, (255, 255, 255, 255))
        x += 4


def planche_robes(names, columns=4, pixels_per_unit=5.0):
    """Les robes de sorcier posees sur une peau de joueur, comme en jeu.

    La robe est une couche exterieure : elle habille un personnage dont
    l'apparence reste celle du joueur. L'apercu utilise une peau neutre pour
    montrer ce que la couche ajoute.
    """
    # base : une apparence de joueur simple, pour voir exactement ce que la robe ajoute
    base_path = os.path.join(ROOT, 'build', 'player-base.png')
    import importlib.util
    spec = importlib.util.spec_from_file_location(
        'character_skins', os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                        'character_skins.py'))
    skins = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(skins)
    write_png(base_path, skins.player_skin(), 64, 64)
    joueur = read_png(base_path)
    cell = int(44 * pixels_per_unit)
    rows = (len(names) + columns - 1) // columns
    sheet = blank(columns * cell, rows * cell)
    rect(sheet, 0, 0, len(sheet[0]), len(sheet), (26, 28, 38, 255))
    for index, name in enumerate(names):
        robe = read_png(os.path.join(ROBE_DIR, name + '.png'))
        # on superpose : la couche exterieure de la robe par-dessus celle du personnage
        habille = joueur.copy()
        paste(habille, robe, 0, 0)
        view = render(habille, yaw_degrees=24, pitch_degrees=12, pixels_per_unit=pixels_per_unit,
                      size=cell - 6)
        paste(sheet, view, (index % columns) * cell + 3, (index // columns) * cell + 3)
        draw_number(sheet, (index % columns) * cell + 5, (index // columns) * cell + 8, index + 1)
    return sheet


def planche_visages(names, columns=7, pixels_per_unit=10.0):
    """Gros plan sur chaque visage : avant et profil."""
    size = int(14 * pixels_per_unit)
    cell_w = size * 2 + 6
    cell_h = size + 12
    rows = (len(names) + columns - 1) // columns
    sheet = blank(columns * cell_w, rows * cell_h)
    rect(sheet, 0, 0, len(sheet[0]), len(sheet), (26, 28, 38, 255))
    for index, name in enumerate(names):
        skin = read_png(os.path.join(SKIN_DIR, name + '.png'))
        front = render(skin, yaw_degrees=0, pitch_degrees=0, pixels_per_unit=pixels_per_unit,
                       size=size, parts=('tete',))
        side = render(skin, yaw_degrees=52, pitch_degrees=6, pixels_per_unit=pixels_per_unit,
                      size=size, parts=('tete',))
        base_x = (index % columns) * cell_w
        base_y = (index // columns) * cell_h
        paste(sheet, front, base_x + 3, base_y + 3)
        paste(sheet, side, base_x + size + 3, base_y + 3)
        draw_number(sheet, base_x + 5, base_y + cell_h - 9, index + 1)
    return sheet


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
        sys.exit('aucune peau trouvee dans ' + SKIN_DIR)

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
        print(f'{len(names) - failures}/{len(names)} apparences conformes.')
        sys.exit(1 if failures else 0)

    sheet = scale(planche(names, columns=5, pixels_per_unit=6.0), 2)
    write_png(OUT, sheet)
    faces = scale(planche_visages(names, columns=7, pixels_per_unit=14.0), 2)
    faces_out = os.path.join(ROOT, 'docs', 'skins-visages.png')
    write_png(faces_out, faces)

    robes = sorted(f[:-4] for f in os.listdir(ROBE_DIR) if f.endswith('.png'))
    robes_out = os.path.join(ROOT, 'docs', 'skins-robes.png')
    write_png(robes_out, scale(planche_robes(robes, columns=4, pixels_per_unit=5.0), 2))

    print(f'{len(names)} apparences rendues :')
    print(f'  {os.path.relpath(OUT, ROOT)} (personnages entiers)')
    print(f'  {os.path.relpath(faces_out, ROOT)} (visages en gros plan)')
    print(f'  {os.path.relpath(robes_out, ROOT)} (robes des joueurs, {len(robes)})')
    for index, name in enumerate(names):
        print(f'  {index + 1:2d}. {name}')


if __name__ == '__main__':
    main()
