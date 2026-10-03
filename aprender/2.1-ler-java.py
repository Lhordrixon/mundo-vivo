# Aula 13 de 17 · 2.1 Ler Java
# Guia da aula: 2.1-ler-java.md
# Cada bloco traz linhas Java do jogo.
# Você escreve a mesma conta em Python.

# --- Exemplo pronto ---
# Java (Simulation.java, linha 558):
#   float targetX = tile % world.width() + 0.5f;
# % é o resto da divisão: 23 % 8 dá 7.
tile = 23
width = 8
targetX = tile % width + 0.5
print("targetX =", targetX)

# --- ✏️ MUDAR ---
# Java (Simulation.java, linha 559):
#   float targetY = tile / world.width() + 0.5f;
# Em Java, int / int corta a parte quebrada:
# 23 / 8 dá 2. Faça a mesma conta em Python.
targetY = None
assert targetY == 2.5, ("targetY deu " + str(targetY)
    + ", mas no Java dá 2.5. Em Python, 23 / 8 dá"
    " 2.875. Para cortar, use //.")
print("targetY =", targetY)

# --- ✏️ MONTAR ---
# Java (World.java, linhas 54 a 56):
#   public boolean inBounds(int x, int y) {
#       return x >= 0 && y >= 0
#           && x < width && y < height;
#   }
# Escreva o inBounds em Python.
height = 3
def inBounds(x, y):
    return None

assert inBounds(0, 0) is True, ("(0, 0) fica dentro"
    " do mundo: inBounds devia dar True.")
assert inBounds(-1, 0) is False, ("x = -1 fica fora"
    " do mundo: inBounds devia dar False.")
assert inBounds(7, 3) is False, ("y = 3 fica fora: "
    "o mundo só tem as linhas 0, 1 e 2.")

# --- Final: o tile 23 no mundo 8 x 3 ---
for y in range(height):
    linha = ""
    for x in range(width):
        if x == int(targetX) and y == int(targetY):
            linha = linha + "X"
        else:
            linha = linha + "."
    print(linha)
print("X = tile 23   . = outros tiles")
print("✅ Aula concluída!")
