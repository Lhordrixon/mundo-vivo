# Aula 11 de 17 · 1.6 Reproduzir
# Guia da aula: 1.6-reproduzir.md

adultAgeSeconds = 22          # do CreatureConfig
reproductionHungerMax = 0.22
reproductionHungerCost = 0.35
matingDistanceTiles = 1.2

class Creature:
    def __init__(self, name, x, age):
        self.name = name
        self.x = x
        self.age = age
        self.hunger = 0.1
        self.health = 1.0
        self.reproductionCooldown = 0.0

    def canReproduce(self):
        # ✏️ MONTAR: devolva True só se as 4 valem:
        # age >= adultAgeSeconds, cooldown <= 0,
        # hunger <= reproductionHungerMax e
        # health > 0.5. Junte as 4 com and.
        return False

    # Aqui, só o x conta. abs tira o sinal: -3 vira 3.
    def distanceTo(self, other):
        return abs(other.x - self.x)

def reproduce(a, b, creatures):
    for pai in [a, b]:
        pai.reproductionCooldown = 55
        novo = pai.hunger + reproductionHungerCost
        pai.hunger = min(1.0, novo)
    creatures.append(Creature(a.name + b.name, a.x, 0))

# --- Exemplo pronto: Ana e Bia, lado a lado ---
ana = Creature("A", 2.0, 30)
bia = Creature("B", 2.8, 10)   # ✏️ MUDAR: a idade
print("Distância:", round(ana.distanceTo(bia), 1))

# --- ✏️ MUDAR ---
assert bia.age >= adultAgeSeconds, ("Bia tem "
    + str(bia.age) + " s. Adulta é de 22 s para cima.")

# --- ✏️ MONTAR: complete o canReproduce ---
creatures = [ana, bia]
if ana.canReproduce() and bia.canReproduce():
    if ana.distanceTo(bia) <= matingDistanceTiles:
        reproduce(ana, bia, creatures)
assert len(creatures) == 3, ("Ninguém nasceu. Ana e "
    "Bia podem, mas o canReproduce disse False.")
assert not ana.canReproduce(), ("Ana acabou de ter "
    "filho: espera e fome. Não pode de novo.")
assert not creatures[2].canReproduce(), ("O filho "
    "tem 0 s de idade. Filhote não pode.")
for c in creatures:
    print(c.name, c.age, "s, fome", round(c.hunger, 2))
print("✅ Aula concluída!")
