# Aula 9 de 17 · 1.4 Comer
# Guia da aula: 1.4-comer.md

regrowthPerSecond = 0.1   # no jogo é 0.003

class FoodMap:
    def __init__(self, width, height):
        self.width = width
        self.height = height
        # Uma lista comprida: um número por tile.
        # [1.0] * 3 dá [1.0, 1.0, 1.0].
        self.amount = [1.0] * (width * height)
        self.capacity = [1.0] * (width * height)

    def index(self, x, y):    # World.index no Java
        return y * self.width + x

    def consume(self, index, requested):
        available = self.amount[index]
        taken = min(available, requested)  # o menor
        self.amount[index] = available - taken
        return taken

    def regrow(self, deltaSeconds):
        # ✏️ MONTAR: para cada índice i, a comida
        # cresce capacity[i] * regrowthPerSecond *
        # deltaSeconds, sem passar de capacity[i].
        pass

def desenhar(food):
    for y in range(food.height):
        texto = ""
        for x in range(food.width):
            nivel = food.amount[food.index(x, y)]
            texto = texto + str(int(nivel * 9))
        print(texto)
    print("9 cheio ... 0 vazio")

# --- Exemplo pronto: comer no tile (2, 1) ---
food = FoodMap(12, 3)
i = food.index(2, 1)
for passo in range(3):
    food.consume(i, 0.45)   # eatingRate por 1 s
desenhar(food)

# --- ✏️ MUDAR ---
# A criatura foi para o tile (5, 2). Troque o
# None pelo índice desse tile.
indice = None
assert indice == 29, ("O índice de (5, 2) é y * "
    "width + x, mas está " + str(indice) + ".")
food.consume(indice, 1.0)

# --- ✏️ MONTAR ---
food.regrow(5)    # 5 segundos de rebrota
assert abs(food.amount[i] - 0.5) < 0.01, ("Depois de "
    "regrow(5) o tile comido devia ter 0.5, mas tem "
    + str(round(food.amount[i], 2)))
assert food.amount[0] <= 1.0, "Passou de 1.0: use min."
desenhar(food)
print("✅ Aula concluída!")
