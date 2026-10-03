# Aula 8 de 17 · 1.3 Fome
# Guia da aula: 1.3-fome.md

hunger_per_second = 0.045      # do CreatureConfig
hunger_seek_threshold = 0.42   # ✏️ MUDAR (abaixo)


class Creature:
    def __init__(self):
        self.hunger = 0.0           # 0 cheia, 1 faminta
        self.state = "WANDERING"    # vagando


def step_creature(c, dt):
    c.hunger += hunger_per_second * dt   # += soma
    if c.state == "WANDERING":
        if c.hunger >= hunger_seek_threshold:
            c.state = "SEEKING_FOOD"


def color_for(state):
    # ✏️ MONTAR: a cor de cada estado no jogo.
    # WANDERING branco, SEEKING_FOOD amarelo,
    # EATING verde, SEEKING_MATE rosa.
    return "?"


# --- Exemplo pronto: 14 s, em passos de 2 s ---
c = Creature()
troca = 99
for passo in range(8):
    t = passo * 2
    print("t=" + str(t) + "s", round(c.hunger, 2),
          c.state, color_for(c.state))
    if c.state == "SEEKING_FOOD" and troca == 99:
        troca = t
    step_creature(c, 2)

# --- ✏️ MUDAR ---
# Faça a criatura procurar comida entre 4 s e
# 6 s. Mude o hunger_seek_threshold lá em cima.
assert 4 <= troca <= 6, (
    "Ela começou a procurar comida em t=" + str(troca)
    + "s. Para trocar mais cedo, o limite sobe ou desce?")

# --- ✏️ MONTAR ---
# Complete o color_for lá em cima.
assert color_for("WANDERING") == "branco", (
    "color_for deu \"" + color_for("WANDERING") + "\" "
    "para WANDERING, que no jogo é branco.")
assert color_for("SEEKING_FOOD") == "amarelo", (
    "No jogo, SEEKING_FOOD é amarelo.")
assert color_for("EATING") == "verde", (
    "No jogo, EATING é verde.")
assert color_for("SEEKING_MATE") == "rosa", (
    "No jogo, SEEKING_MATE é rosa.")
print("Cor de quem procura comida:",
      color_for("SEEKING_FOOD"))
print("✅ Aula concluída!")
