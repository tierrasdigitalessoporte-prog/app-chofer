#!/usr/bin/env python3
"""
Genera los iconos del launcher de Android para "App Chofer", reusando el
mismo diseño (van/autobús visto de lado) que ya se usó para el ícono de la
PWA en transporte-escolar/chofer/icons/. Fondo azul marino oscuro (#111827),
autobús blanco, llantas verdes (#22c55e) — mismo estilo, mismos colores.

Genera:
  - mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher.png       (cuadrado, con fondo)
  - mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher_round.png (mismo arte, recortado en círculo)
  - mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher_foreground.png (solo el autobús, sin fondo, para el ícono "adaptativo")
  - values/ic_launcher_background.xml actualizado a #111827
"""
from PIL import Image, ImageDraw
import os

RES = "/tmp/transporte/app-chofer-nativa/android/app/src/main/res"

BG = (17, 24, 39, 255)       # #111827
BUS = (255, 255, 255, 255)   # blanco
WHEEL = (34, 197, 94, 255)   # #22c55e

# tamaños "legacy" (ic_launcher.png / ic_launcher_round.png)
DENSIDADES_LEGACY = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# tamaños del "foreground" adaptativo (más grandes, con margen de seguridad)
DENSIDADES_FOREGROUND = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}


def dibujar_bus(draw, cx, cy, escala):
    """Dibuja el autobús (visto de lado) centrado en (cx, cy), a un tamaño
    proporcional a `escala` (aprox. el ancho deseado del autobús en px)."""
    ancho = escala
    alto = ancho * 0.52

    x0 = cx - ancho / 2
    y0 = cy - alto / 2
    x1 = cx + ancho / 2
    y1 = cy + alto / 2

    radio = alto * 0.22
    draw.rounded_rectangle([x0, y0, x1, y1], radius=radio, fill=BUS)

    # 3 ventanas laterales + 1 ventana de cabina, como huecos del color de fondo
    margen_v = alto * 0.16
    alto_ventana = alto * 0.38
    y_vent0 = y0 + margen_v
    y_vent1 = y_vent0 + alto_ventana

    ancho_total = x1 - x0
    n_ventanas = 4
    espacio = ancho_total * 0.06
    ancho_ventana = (ancho_total - espacio * (n_ventanas + 1)) / n_ventanas

    xv = x0 + espacio
    for i in range(n_ventanas):
        radv = ancho_ventana * 0.18
        draw.rounded_rectangle(
            [xv, y_vent0, xv + ancho_ventana, y_vent1],
            radius=radv,
            fill=BG,
        )
        xv += ancho_ventana + espacio

    # 2 llantas
    radio_llanta = alto * 0.30
    cy_llanta = y1
    cx_llanta1 = x0 + ancho * 0.24
    cx_llanta2 = x0 + ancho * 0.76
    for cxl in (cx_llanta1, cx_llanta2):
        draw.ellipse(
            [cxl - radio_llanta, cy_llanta - radio_llanta, cxl + radio_llanta, cy_llanta + radio_llanta],
            fill=WHEEL,
        )
        radio_centro = radio_llanta * 0.4
        draw.ellipse(
            [cxl - radio_centro, cy_llanta - radio_centro, cxl + radio_centro, cy_llanta + radio_centro],
            fill=BG,
        )


def generar_cuadrado(tam, redondo=False):
    """Ícono legacy: fondo + autobús, esquinas redondeadas o círculo."""
    img = Image.new("RGBA", (tam, tam), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    if redondo:
        draw.ellipse([0, 0, tam, tam], fill=BG)
    else:
        radio = tam * 0.18
        draw.rounded_rectangle([0, 0, tam, tam], radius=radio, fill=BG)
    dibujar_bus(draw, tam / 2, tam / 2, tam * 0.62)
    return img


def generar_foreground(tam):
    """Ícono adaptativo: SOLO el autobús, sin fondo (el sistema pone su
    propio fondo/máscara), con margen de seguridad como pide Android
    (el contenido debe caber dentro del 66% central)."""
    img = Image.new("RGBA", (tam, tam), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    dibujar_bus(draw, tam / 2, tam / 2, tam * 0.46)
    return img


def main():
    for carpeta, tam in DENSIDADES_LEGACY.items():
        destino = os.path.join(RES, carpeta)
        os.makedirs(destino, exist_ok=True)
        generar_cuadrado(tam, redondo=False).save(os.path.join(destino, "ic_launcher.png"))
        generar_cuadrado(tam, redondo=True).save(os.path.join(destino, "ic_launcher_round.png"))
        print(f"OK {carpeta}/ic_launcher.png y ic_launcher_round.png ({tam}x{tam})")

    for carpeta, tam in DENSIDADES_FOREGROUND.items():
        destino = os.path.join(RES, carpeta)
        os.makedirs(destino, exist_ok=True)
        generar_foreground(tam).save(os.path.join(destino, "ic_launcher_foreground.png"))
        print(f"OK {carpeta}/ic_launcher_foreground.png ({tam}x{tam})")

    # Actualizar el color de fondo del ícono adaptativo a #111827
    ruta_bg = os.path.join(RES, "values", "ic_launcher_background.xml")
    contenido = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<resources>\n"
        '    <color name="ic_launcher_background">#111827</color>\n'
        "</resources>\n"
    )
    with open(ruta_bg, "w") as f:
        f.write(contenido)
    print(f"OK {ruta_bg} actualizado a #111827")


if __name__ == "__main__":
    main()
