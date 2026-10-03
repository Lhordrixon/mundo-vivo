# Aula 9 de 17 · 1.4 Comer
# Guia da aula: 1.4-comer.md

regrowth_per_second = 0.1   # no jogo é 0.003


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
        # Atualizar: tira o que dá, nunca mais do que há.
        available = self.amount[index]
        taken = min(available, requested)  # o menor
        self.amount[index] = available - taken
        return taken

    def regrow(self, delta_seconds):
        # ✏️ Sua vez (MONTAR): apague o pass. Para cada
        # i em range(len(self.amount)), a comida cresce
        # self.capacity[i] * regrowth_per_second *
        # delta_seconds, sem passar de self.capacity[i].
        # Uma conta longa pode ir numa variável antes.
        pass


def desenhar(food):
    # 1. Repetir: linha por linha, tile por tile.
    for y in range(food.height):
        texto = ""
        for x in range(food.width):
            nivel = food.amount[food.index(x, y)]
            texto = texto + str(int(nivel * 9))
        print(texto)
    # 2. Mostrar
    print("9 cheio ... 0 vazio")


# --- Exemplo pronto: comer no tile (2, 1) ---
food = FoodMap(12, 3)
i = food.index(2, 1)
# Rastreio: a comida do tile depois de cada mordida.
print("mordida  comida")
for passo in range(3):
    food.consume(i, 0.45)   # eatingRate por 1 s
    print("  ", passo + 1, "    ", round(food.amount[i], 2))
desenhar(food)

# --- ✏️ MUDAR ---
# ✏️ Sua vez: a criatura foi para o tile (7, 1).
# Troque o None pelo índice desse tile.
indice = None
# Conferir
assert indice == 19, "Use y * width + x com x 7, y 1."
food.consume(indice, 1.0)

# --- ✏️ MONTAR ---
# A sua vez está lá em cima, no regrow.
food.regrow(5)    # 5 segundos de rebrota
# Conferir
assert abs(food.amount[i] - 0.5) < 0.01, (
    "Devia ser 0.5, é " + str(round(food.amount[i], 2)))
assert food.amount[0] <= 1.0, "Passou de 1.0: use min."
desenhar(food)
print("✅ Aula concluída!")
