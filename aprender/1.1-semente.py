# Aula 6 de 25 · 1.1 O mapa nasce de uma semente
# Guia da aula: 1.1-semente.md
import random


# --- Exemplo pronto: leia e rode ---
# classify: a mesma ideia do WorldGenerator.
# Recebe uma altura de 0 a 1 e diz o tile.
def classify(elevation, sea_level):
    # Decidir: cada if escolhe um tile.
    if elevation < sea_level:
        return "~"          # água
    # ✏️ Sua vez (MONTAR): praia logo acima do mar.
    # Se elevation < sea_level + 0.05, devolva ","
    # (escreva o if aqui)
    if elevation > 0.9:
        return "^"          # montanha
    return "."              # campo


def generate(seed, width, height, sea_level):
    # 1. Preparar
    rng = random.Random(seed)   # o sorteador
    tiles = []
    # 2. Repetir: linha por linha, tile por tile.
    for y in range(height):
        linha = []
        for x in range(width):
            altura = rng.random()   # entre 0 e 1
            linha.append(classify(altura, sea_level))
        tiles.append(linha)
    return tiles


def desenhar(tiles):
    # 1. Repetir
    for linha in tiles:
        texto = ""
        for tile in linha:
            texto = texto + tile
        print(texto)
    # 2. Mostrar
    print("~ água  , praia  . campo  ^ montanha")


seed = 2026
# ✏️ Sua vez (MUDAR): deixe o mundo alagado, com
# mais da metade água. Mude só este número.
sea_level = 0.34
mundo = generate(seed, 24, 8, sea_level)
desenhar(mundo)
outro = generate(seed, 24, 8, sea_level)
print("Mesma semente, mesmo mapa?", mundo == outro)

# --- ✏️ MUDAR ---
# A sua vez está lá em cima, no sea_level.
# Conferir
assert sea_level > 0.5, (
    "Pouca água ainda. O sea_level sobe ou desce?")

# --- ✏️ MONTAR ---
# A sua vez está lá em cima, dentro do classify.
# Conferir
praia = classify(0.36, 0.34)
assert praia == ",", (
    "0.36 é logo acima do mar, mas veio \"" + praia + "\"")
assert classify(0.30, 0.34) == "~", "0.30 é água."
assert classify(0.50, 0.34) == ".", "0.50 é campo."

print("Agora com praias (sea_level 0.34):")
desenhar(generate(seed, 24, 8, 0.34))
print("✅ Aula concluída!")
