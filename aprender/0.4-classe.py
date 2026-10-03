# Aula 5 de 25 · 0.4 Muitas criaturas: por que classe
# Guia da aula: 0.4-classe.md

# --- Antes: com dicionários ---
a = {"x": 1, "y": 1, "hunger": 3}
b = {"x": 4, "y": 1, "hungre": 3}  # digitei errado!
# O Python não reclama do "hungre" aqui.
# Só quebra depois, quando alguém pedir
# b["hunger"].


# --- Depois: com uma classe ---
# A classe é o molde. Toda criatura sai igual.
class Creature:
    def __init__(self, x, y):   # roda ao nascer
        self.x = x
        self.y = y
        self.hunger = 3

    def passar_tempo(self):
        self.hunger = self.hunger + 1

    def comer(self):
        # ✏️ Sua vez (MONTAR): a fome cai 3, mas nunca
        # fica menor que 0. É o comer da 0.3, mas com
        # self.hunger no lugar de c["hunger"].
        pass


criaturas = [Creature(1, 1), Creature(3, 1)]
criaturas[0].passar_tempo()
print("Fome da 1ª:", criaturas[0].hunger)

# --- ✏️ MUDAR ---
# ✏️ Sua vez: crie uma 3ª criatura em x 5, y 1 e
# ponha na lista com append (põe no fim da lista).
# Troque o ... pela linha.
...

# Conferir
assert len(criaturas) == 3 and criaturas[2].x == 5, (
    "Lista com " + str(len(criaturas)) + ". Faltou a 3ª?")

# --- ✏️ MONTAR ---
# A sua vez está lá em cima, no comer da classe.
# Conferir
t = Creature(0, 0)
t.hunger = 5
t.comer()
assert t.hunger == 2, (
    "Ficou " + str(t.hunger) + ", não 2. Usou self.hunger?")
t.hunger = 1
t.comer()
assert t.hunger == 0, (
    "Fome 1 menos 3 dá 0 (mínimo), não " + str(t.hunger))

# --- Final: as criaturas no mapa ---
# 1. Preparar
mapa = [
    ["~", "~", "~", "~", "~", "~", "~"],
    ["~", ".", ".", ".", ".", ".", "~"],
    ["~", "~", "~", "~", "~", "~", "~"],
]
for c in criaturas:
    mapa[c.y][c.x] = "o"
# 2. Repetir
for linha in mapa:
    texto = ""
    for tile in linha:
        texto = texto + tile + " "
    print(texto)
# 3. Mostrar
print("~ água  . terra  o criatura")
print("✅ Aula concluída!")
