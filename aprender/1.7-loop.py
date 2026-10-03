# Aula 12 de 17 · 1.7 O loop do mundo
# Guia da aula: 1.7-loop.md

regrowthPerSecond = 1.0   # ✏️ MUDAR (abaixo)
# Aqui a comida é uma só, para o mundo todo.
# No jogo, cada tile tem a sua (aula 1.4).

class Creature:
    def __init__(self):
        self.hunger = 0.3

class Simulation:
    def __init__(self, creatures):
        self.food = 10.0              # comida do mundo
        self.creatures = creatures

    def step(self, dt):
        # ✏️ MONTAR: 1) chame self.regrow(dt).
        # 2) chame self.stepCreature(i, dt) para cada
        # criatura, do fim da lista para o começo.
        pass

    def regrow(self, dt):
        rate = regrowthPerSecond * dt
        self.food = min(10.0, self.food + rate)

    def stepCreature(self, i, dt):
        c = self.creatures[i]
        c.hunger += 0.1 * dt
        if c.hunger >= 1.0:
            self.creatures.pop(i)                 # die
            return
        eaten = min(self.food, 0.1 * dt)          # eat
        self.food -= eaten
        c.hunger = max(0.0, c.hunger - eaten * 1.6)
        if c.hunger < 0.05:                 # reproduce
            c.hunger += 0.7
            self.creatures.append(Creature())

def desenhar(sim, t):
    print(t, "#" * int(sim.food), "o" * len(sim.creatures))

# --- Exemplo pronto: o mundo no segundo 0 ---
sim = Simulation([Creature(), Creature(), Creature()])
desenhar(sim, 0)
print("número = segundo  # comida  o criatura")

# --- ✏️ MUDAR ---
# Corte pela metade a comida que volta por
# segundo: mude o regrowthPerSecond lá em cima.
assert regrowthPerSecond == 0.5, ("A rebrota está em "
    + str(regrowthPerSecond) + ". A metade de 1.0 é?")

# --- O mundo vivo: 40 segundos ---
for bloco in range(1, 9):
    for segundo in range(5):
        sim.step(1.0)
    desenhar(sim, bloco * 5)

# --- ✏️ MONTAR: o step lá em cima ---
assert len(sim.creatures) not in [0, 3], ("O mundo "
    "parou ou morreu. O step chama regrow e depois "
    "stepCreature, do fim para o começo?")
print("✅ Aula concluída!")
