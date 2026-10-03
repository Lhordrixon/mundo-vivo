# Aula 10 de 17 · 1.5 Morrer
# Guia da aula: 1.5-morrer.md

class Creature:
    def __init__(self, name, health):
        self.name = name
        self.health = health    # vida, de 0 a 1

    def applyDamage(self, amount):
        # A porta única: todo dano passa por aqui.
        if amount <= 0 or self.health <= 0:
            return False
        # max escolhe o maior: a vida não fica < 0.
        self.health = max(0.0, self.health - amount)
        return self.health <= 0   # True: morreu agora

def mostrar(creatures):
    for c in creatures:
        barra = "#" * int(c.health * 10)  # "#" n vezes
        print(c.name, barra, round(c.health, 2))

# --- Exemplo pronto: quatro criaturas ---
creatures = [Creature("a", 0.5), Creature("b", 0.1),
             Creature("c", 0.1), Creature("d", 0.9)]
mostrar(creatures)

# --- ✏️ MUDAR ---
# Que golpe mata o alvo, que tem vida 0.5?
golpe = None
alvo = Creature("alvo", 0.5)
assert golpe is not None, "Troque o None do golpe."
assert alvo.applyDamage(golpe), ("Com golpe "
    + str(golpe) + ", o alvo ficou com "
    + str(round(alvo.health, 2)) + " de vida.")

# --- ✏️ MONTAR ---
def step(creatures, damage):
    # ✏️ Este for pula criaturas quando alguém sai
    # da lista. Troque por um for que vá do fim
    # para o começo, usando índices.
    for c in creatures:
        if c.applyDamage(damage):
            creatures.remove(c)    # die: sai da lista

step(creatures, 0.2)
print("Depois de um golpe de 0.2 em todas:")
mostrar(creatures)
nomes = ""
for c in creatures:
    nomes = nomes + c.name
assert nomes == "ad", ("Sobraram " + nomes + ", mas "
    "só a e d deviam sobrar: o for pulou alguém.")
print("✅ Aula concluída!")
