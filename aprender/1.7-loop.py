# Aula 12 de 25 · 1.7 O loop do mundo
# Guia da aula: 1.7-loop.md

# ✏️ Sua vez (MUDAR): corte pela metade a comida
# que volta por segundo. Mude só este número.
regrowth_per_second = 1.0


class Creature:
    def __init__(self):
        self.hunger = 0.3


class Simulation:
    def __init__(self, creatures):
        # Aqui a comida é uma só, para o mundo todo.
        # No jogo, cada tile tem a sua (aula 1.4).
        self.food = 10.0              # comida do mundo
        self.creatures = creatures

    def step(self, dt):
        # ✏️ Sua vez (MONTAR): 1) chame self.regrow(dt).
        # 2) chame self.step_creature(i, dt) para cada
        # criatura, do fim da lista para o começo.
        # Sem isso, o mundo fica parado: as linhas de
        # 5 s a 40 s saem iguais.
        pass

    def regrow(self, dt):
        rate = regrowth_per_second * dt
        self.food = min(10.0, self.food + rate)

    def step_creature(self, i, dt):
        c = self.creatures[i]
        # 1. Atualizar: a fome sobe.
        c.hunger += 0.1 * dt
        # 2. Decidir: morre de fome?
        if c.hunger >= 1.0:
            self.creatures.pop(i)                 # die
            return
        # 3. Atualizar: come o que der.
        eaten = min(self.food, 0.1 * dt)          # eat
        self.food -= eaten
        c.hunger = max(0.0, c.hunger - eaten * 1.6)
        # 4. Decidir: cheia o bastante para ter filho?
        if c.hunger < 0.05:                 # reproduce
            c.hunger += 0.7
            self.creatures.append(Creature())


def desenhar(sim, t):
    # Mostrar: uma linha do rastreio por chamada.
    print(t, "#" * int(sim.food), round(sim.food, 1),
          "o" * len(sim.creatures))


# --- Exemplo pronto: o mundo no segundo 0 ---
sim = Simulation([Creature(), Creature(), Creature()])
desenhar(sim, 0)
print("número = segundo  # comida  o criatura")

# --- ✏️ MUDAR ---
# A sua vez está lá em cima, na rebrota.
# Conferir
assert abs(regrowth_per_second - 0.5) < 0.01, (
    "A metade de 1.0 é 1.0 dividido por 2.")

# --- O mundo vivo: 40 segundos ---
for bloco in range(1, 9):
    for segundo in range(5):
        sim.step(1.0)
    desenhar(sim, bloco * 5)

# --- ✏️ MONTAR ---
# A sua vez está lá em cima, no step.
# Conferir
assert len(sim.creatures) not in [0, 3], (
    "Parou: regrow e step_creature, do fim ao começo?")
print("✅ Aula concluída!")
