# Aula 11 de 25 · 1.6 Reproduzir
# Guia da aula: 1.6-reproduzir.md

adult_age_seconds = 22          # do CreatureConfig
reproduction_hunger_max = 0.22
reproduction_hunger_cost = 0.35
mating_distance_tiles = 1.2


class Creature:
    def __init__(self, name, x, age):
        self.name = name
        self.x = x
        self.age = age
        self.hunger = 0.1
        self.health = 1.0
        self.reproduction_cooldown = 0.0

    def can_reproduce(self):
        # ✏️ Sua vez (MONTAR): devolva True só se as 4
        # valem: self.age >= adult_age_seconds,
        # self.reproduction_cooldown <= 0,
        # self.hunger <= reproduction_hunger_max e
        # self.health > 0.5. Junte as 4 com and. Linha
        # longa? Ponha tudo entre ( ) e quebre antes do and.
        return False

    # Aqui, só o x conta. abs tira o sinal: -3 vira 3.
    def distance_to(self, other):
        return abs(other.x - self.x)


def reproduce(a, b, creatures):
    # 1. Repetir: os dois pais pagam o custo.
    for pai in [a, b]:
        pai.reproduction_cooldown = 55
        novo = pai.hunger + reproduction_hunger_cost
        pai.hunger = min(1.0, novo)
    # 2. Atualizar: o filho entra na lista.
    creatures.append(Creature(a.name + b.name, a.x, 0))


# --- Exemplo pronto: Ana e Bia, lado a lado ---
ana = Creature("A", 2.0, 30)
# ✏️ Sua vez (MUDAR): a Bia ainda é jovem. Mude a
# idade dela (o 10) para ela virar adulta.
bia = Creature("B", 2.8, 10)
print("Distância:", round(ana.distance_to(bia), 1))

# --- ✏️ MUDAR ---
# A sua vez está lá em cima, na idade da Bia.
# Conferir
assert bia.age >= adult_age_seconds, (
    "Bia tem " + str(bia.age) + " s. Adulta: 22 s ou mais.")

# --- ✏️ MONTAR ---
# A sua vez está lá em cima, no can_reproduce.
creatures = [ana, bia]
if ana.can_reproduce() and bia.can_reproduce():
    if ana.distance_to(bia) <= mating_distance_tiles:
        reproduce(ana, bia, creatures)
# Conferir
assert len(creatures) == 3, (
    "Ninguém nasceu: o can_reproduce disse False.")
assert not ana.can_reproduce(), (
    "Ana teve filho agora: espera e fome. Não pode.")
assert not creatures[2].can_reproduce(), (
    "O filho tem 0 s de idade. Filhote não pode.")
for c in creatures:
    print(c.name, c.age, "s, fome", round(c.hunger, 2))
print("✅ Aula concluída!")
