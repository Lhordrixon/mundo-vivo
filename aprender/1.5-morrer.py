# Aula 10 de 25 · 1.5 Morrer
# Guia da aula: 1.5-morrer.md

class Creature:
    def __init__(self, name, health):
        self.name = name
        self.health = health    # vida, de 0 a 1

    def apply_damage(self, amount):
        # A porta única: todo dano passa por aqui.
        # Decidir: dano inválido ou já morto não conta.
        if amount <= 0 or self.health <= 0:
            return False
        # max escolhe o maior: a vida não fica < 0.
        self.health = max(0.0, self.health - amount)
        return self.health <= 0   # True: morreu agora


def mostrar(creatures):
    # Repetir: uma barra de vida por criatura.
    for c in creatures:
        barra = "#" * int(c.health * 10)  # "#" n vezes
        print(c.name, barra, round(c.health, 2))


# --- Exemplo pronto: quatro criaturas ---
creatures = [Creature("a", 0.5), Creature("b", 0.1),
             Creature("c", 0.1), Creature("d", 0.9)]
mostrar(creatures)

# --- ✏️ MUDAR ---
# ✏️ Sua vez: que golpe mata o alvo, que tem vida
# 0.5? Troque o None por esse número.
golpe = None
alvo = Creature("alvo", 0.5)
# Conferir
assert golpe is not None, (
    "golpe ainda é None: o dano é um número. Olhe a "
    "linha golpe e tente um número de 0 a 1.")
assert alvo.apply_damage(golpe), (
    "Com golpe " + str(golpe) + ", o alvo ficou com "
    + str(round(alvo.health, 2)) + " de vida.")


# --- ✏️ MONTAR ---
def step(creatures, damage):
    # ✏️ Sua vez (MONTAR): este for pula criaturas
    # quando alguém sai da lista. Troque por um for
    # do fim para o começo, usando índices:
    # range(len(creatures) - 1, -1, -1) vai do último
    # índice até o 0; creatures.pop(i) tira a do i.
    # Dentro do for, pegue a criatura: c = creatures[i]
    for c in creatures:
        if c.apply_damage(damage):
            creatures.remove(c)    # die: sai da lista


step(creatures, 0.2)
print("Depois de um golpe de 0.2 em todas:")
mostrar(creatures)
nomes = ""
for c in creatures:
    nomes = nomes + c.name
# Conferir
assert nomes == "ad", (
    "Sobraram " + nomes + ", não ad: o for pulou alguém. "
    "Olhe o for do step: vá do fim para o começo.")
print("✅ Aula concluída!")
