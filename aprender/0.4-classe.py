# Aula 5 de 17 · 0.4 Muitas criaturas: por que classe
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
        pass   # ✏️ MONTAR: troque este pass


criaturas = [Creature(1, 1), Creature(3, 1)]
criaturas[0].passar_tempo()
print("Fome da 1ª:", criaturas[0].hunger)

# --- ✏️ MUDAR ---
# Crie uma terceira criatura em x 5, y 1 e
# ponha na lista com append. Escreva abaixo:


assert len(criaturas) == 3 and criaturas[2].x == 5, (
    "Falta a 3ª criatura, em x 5: criaturas.append(...)")

# --- ✏️ MONTAR ---
# Na classe lá em cima, escreva comer: a
# fome cai 3, mas nunca fica menor que 0.
# É a função da aula 0.3, agora com self.
t = Creature(0, 0)
t.hunger = 5
t.comer()
assert t.hunger == 2, (
    "Após comer: " + str(t.hunger) + ", não 2. Tirou 3?")
t.hunger = 1
t.comer()
assert t.hunger == 0, (
    "Ficou " + str(t.hunger) + ": fome não é negativa.")

# --- Final: as criaturas no mapa ---
mapa = [
    ["~", "~", "~", "~", "~", "~", "~"],
    ["~", ".", ".", ".", ".", ".", "~"],
    ["~", "~", "~", "~", "~", "~", "~"],
]
for c in criaturas:
    mapa[c.y][c.x] = "o"
for linha in mapa:
    texto = ""
    for tile in linha:
        texto = texto + tile + " "
    print(texto)
print("~ água  . terra  o criatura")
print("✅ Aula concluída!")
