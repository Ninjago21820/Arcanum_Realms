#!/usr/bin/env python3
"""Petit atelier d'images sans dependance externe (PNG en lecture et ecriture).

Utilise par les generateurs de ressources du mod : toutes les textures sont
dessinees en Python et versionnees sous forme de PNG, donc regenerables.
"""
import os
import struct
import zlib


# --------------------------------------------------------------------- PNG


def write_png(path, pixels, width=None, height=None):
    """Ecrit une image RGBA (liste de lignes de quadruplets) en PNG."""
    height = height or len(pixels)
    width = width or len(pixels[0])
    raw = b''.join(
        b'\x00' + bytes(v for x in range(width) for v in pixels[y][x]) for y in range(height))
    png = (b'\x89PNG\r\n\x1a\n'
           + _chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
           + _chunk(b'IDAT', zlib.compress(raw, 9))
           + _chunk(b'IEND', b''))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(png)


def _chunk(kind, data):
    return (struct.pack('>I', len(data)) + kind + data
            + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff))


def read_png(path):
    """Lit un PNG 8 bits (RGBA, RGB, palette ou gris) et renvoie des quadruplets."""
    with open(path, 'rb') as f:
        data = f.read()
    if data[:8] != b'\x89PNG\r\n\x1a\n':
        raise ValueError(f'{path} : ce n\'est pas un PNG')

    pos = 8
    header = None
    palette = None
    transparency = None
    idat = b''
    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos + 4])[0]
        kind = data[pos + 4:pos + 8]
        body = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if kind == b'IHDR':
            header = struct.unpack('>IIBBBBB', body)
        elif kind == b'PLTE':
            palette = [tuple(body[i:i + 3]) for i in range(0, len(body), 3)]
        elif kind == b'tRNS':
            transparency = body
        elif kind == b'IDAT':
            idat += body
        elif kind == b'IEND':
            break

    width, height, depth, colour, _comp, _filter, interlace = header
    if depth != 8:
        raise ValueError(f'{path} : seules les images 8 bits sont lues (profondeur {depth})')
    if interlace:
        raise ValueError(f'{path} : images entrelacees non lues')
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[colour]

    raw = zlib.decompress(idat)
    stride = width * channels
    lines = []
    previous = bytearray(stride)
    index = 0
    for _y in range(height):
        filter_type = raw[index]
        index += 1
        line = bytearray(raw[index:index + stride])
        index += stride
        if filter_type == 1:
            for i in range(channels, stride):
                line[i] = (line[i] + line[i - channels]) & 0xff
        elif filter_type == 2:
            for i in range(stride):
                line[i] = (line[i] + previous[i]) & 0xff
        elif filter_type == 3:
            for i in range(stride):
                left = line[i - channels] if i >= channels else 0
                line[i] = (line[i] + ((left + previous[i]) >> 1)) & 0xff
        elif filter_type == 4:
            for i in range(stride):
                left = line[i - channels] if i >= channels else 0
                up = previous[i]
                upleft = previous[i - channels] if i >= channels else 0
                p = left + up - upleft
                pa, pb, pc = abs(p - left), abs(p - up), abs(p - upleft)
                estimate = left if (pa <= pb and pa <= pc) else (up if pb <= pc else upleft)
                line[i] = (line[i] + estimate) & 0xff
        lines.append(line)
        previous = line

    pixels = []
    for line in lines:
        row = []
        for x in range(width):
            chunk = line[x * channels:(x + 1) * channels]
            if colour == 6:
                row.append(tuple(chunk))
            elif colour == 2:
                row.append((chunk[0], chunk[1], chunk[2], 255))
            elif colour == 0:
                row.append((chunk[0], chunk[0], chunk[0], 255))
            elif colour == 4:
                row.append((chunk[0], chunk[0], chunk[0], chunk[1]))
            else:
                entry = palette[chunk[0]]
                alpha = transparency[chunk[0]] if transparency and chunk[0] < len(transparency) else 255
                row.append((entry[0], entry[1], entry[2], alpha))
        pixels.append(row)
    return pixels


# ------------------------------------------------------------------ dessin


def blank(w, h):
    return [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]


def put(img, x, y, colour):
    if 0 <= y < len(img) and 0 <= x < len(img[0]):
        img[y][x] = colour


def rect(img, x0, y0, w, h, colour):
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            put(img, x, y, colour)


def shade(colour, delta):
    """Eclaircit (delta > 0) ou assombrit une couleur, sans changer l'alpha."""
    return (max(0, min(255, colour[0] + delta)), max(0, min(255, colour[1] + delta)),
            max(0, min(255, colour[2] + delta)), colour[3])


def rgb(value, alpha=255):
    """Convertit 0xRRGGBB (la forme utilisee par le moteur) en quadruplet."""
    return ((value >> 16) & 0xff, (value >> 8) & 0xff, value & 0xff, alpha)


def image(w, h, colour):
    img = blank(w, h)
    rect(img, 0, 0, w, h, colour)
    return img


def paste(target, source, x0, y0):
    for y in range(len(source)):
        for x in range(len(source[0])):
            pixel = source[y][x]
            if pixel[3] > 0:
                put(target, x0 + x, y0 + y, pixel)


def scale(img, factor):
    """Agrandit chaque pixel en carre de `factor` cotes (rendu net)."""
    h = len(img)
    w = len(img[0])
    out = blank(w * factor, h * factor)
    for y in range(h):
        for x in range(w):
            pixel = img[y][x]
            if pixel[3] == 0:
                continue
            rect(out, x * factor, y * factor, factor, factor, pixel)
    return out
