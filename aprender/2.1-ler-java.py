# Aula 13 de 25 · 2.1 Ler Java
# Guia da aula: 2.1-ler-java.md
# Cada bloco traz linhas Java do jogo.
# Você escreve a mesma conta em Python.

# --- Exemplo pronto ---
# Java (Simulation.java, linha 558):
#   float targetX = tile % world.width() + 0.5f;
# % é o resto da divisão: 23 % 8 dá 7.
tile = 23
width = 8
target_x = tile % width + 0.5
print("target_x =", target_x)

# --- ✏️ MUDAR ---
# Java (Simulation.java, linha 559):
#   float targetY = tile / world.width() + 0.5f;
# ✏️ Sua vez: em Java, int / int corta a parte
# quebrada (23 / 8 dá 2). Faça igual em Python.
target_y = None
assert target_y == 2.5, (
    "target_y deu " + str(target_y) + ", mas no Java dá "
    "2.5. Em Python, 23 / 8 dá 2.875. Para cortar, use //.")
print("target_y =", target_y)

# --- ✏️ MONTAR ---
# Java (World.java, linhas 54 a 56):
#   public boolean inBounds(int x, int y) {
#       return x >= 0 && y >= 0
#           && x < width && y < height;
#   }
height = 3


# ✏️ Sua vez: escreva o in_bounds em Python,
# lendo o Java logo acima.
def in_bounds(x, y):
    return None


assert in_bounds(0, 0) is True, (
    "(0, 0) fica dentro do mundo: in_bounds devia dar "
    "True.")
assert in_bounds(-1, 0) is False, (
    "x = -1 fica fora do mundo: in_bounds devia dar False.")
assert in_bounds(7, 3) is False, (
    "y = 3 fica fora: o mundo só tem as linhas 0, 1 e 2.")
assert in_bounds(8, 0) is False, (
    "x = 8 fica fora: com width 8, x vai de 0 a 7. Use <.")

# --- Final: o tile 23 no mundo 8 x 3 ---
for y in range(height):
    linha = ""
    for x in range(width):
        if x == int(target_x) and y == int(target_y):
            linha = linha + "X"
        else:
            linha = linha + "."
    print(linha)
print("X = tile 23   . = outros tiles")
print("✅ Aula concluída!")
