# Aula 6 de 17 · 1.1 O mapa nasce de uma semente
# Guia da aula: 1.1-semente.md
import random

# --- Exemplo pronto: leia e rode ---
# classify: a mesma ideia do WorldGenerator.
# Recebe uma altura de 0 a 1 e diz o tile.
def classify(elevation, seaLevel):
    if elevation < seaLevel:
        return "~"          # água
    # ✏️ MONTAR: praia logo acima do mar. Se
    # elevation < seaLevel + 0.05, devolva ","
    if elevation > 0.9:
        return "^"          # montanha
    return "."              # campo

def generate(seed, width, height, seaLevel):
    rng = random.Random(seed)   # o sorteador
    tiles = []
    for y in range(height):
        linha = []
        for x in range(width):
            altura = rng.random()   # entre 0 e 1
            linha.append(classify(altura, seaLevel))
        tiles.append(linha)
    return tiles

def desenhar(tiles):
    for linha in tiles:
        texto = ""
        for tile in linha:
            texto = texto + tile
        print(texto)
    print("~ água  , praia  . campo  ^ montanha")

seed = 2026
seaLevel = 0.34   # ✏️ MUDAR (veja abaixo)
mundo = generate(seed, 24, 8, seaLevel)
desenhar(mundo)
outro = generate(seed, 24, 8, seaLevel)
print("Mesma semente, mesmo mapa?", mundo == outro)

# --- ✏️ MUDAR ---
# Deixe o mundo alagado: mais da metade água.
# Mude só o número do seaLevel, lá em cima.
assert seaLevel > 0.5, ("O mapa ainda tem pouca "
    "água. O nível do mar (seaLevel) sobe ou desce?")

# --- ✏️ MONTAR ---
# Complete o classify lá em cima: a praia.
praia = classify(0.36, 0.34)
assert praia == ",", ("classify(0.36, 0.34) deu \""
    + praia + "\", mas 0.36 fica logo acima do mar.")
assert classify(0.30, 0.34) == "~", "0.30 é água."
assert classify(0.50, 0.34) == ".", "0.50 é campo."

print("Agora com praias (seaLevel 0.34):")
desenhar(generate(seed, 24, 8, 0.34))
print("✅ Aula concluída!")
