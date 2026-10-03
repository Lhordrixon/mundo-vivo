# Aula 7 de 17 · 1.2 Andar
# Guia da aula: 1.2-andar.md
import math

mapa = ["~.....~~.......~"]   # uma faixa de 16 tiles
speed_tiles_per_second = 2.2      # ✏️ MUDAR (abaixo)


class Creature:
    def __init__(self, x, y):
        self.x = x   # 1.5 é o meio do tile 1
        self.y = y


def move_to(c, x, y):
    # ✏️ MONTAR: se mapa[int(y)][int(x)] for "~",
    # use return antes de mudar c.x e c.y.
    c.x = x
    c.y = y


def move_toward(c, target_x, target_y, dt):
    dx = target_x - c.x
    dy = target_y - c.y
    distance = math.sqrt(dx * dx + dy * dy)
    stride = speed_tiles_per_second * dt   # o passo
    if stride >= distance:
        move_to(c, target_x, target_y)
        return True                     # chegou
    move_to(c, c.x + dx / distance * stride,
            c.y + dy / distance * stride)
    return False


# Desenha a faixa e, embaixo, a criatura.
# " " * 5 são 5 espaços: empurra o "o" até o x.
def desenhar(c):
    print(mapa[0])
    print(" " * int(c.x) + "o", " x =", round(c.x, 1))


# --- Exemplo pronto: 4 passos de 0.5 s ---
c = Creature(1.5, 0.5)
for passo in range(4):
    move_toward(c, 14.5, 0.5, 0.5)
desenhar(c)
print("~ água  . terra  o criatura")

# --- ✏️ MUDAR ---
# Faça a criatura andar o dobro nos mesmos
# 4 passos. Mude o speed_tiles_per_second.
assert 9.5 < c.x < 11.5, (
    "Ela foi até x " + str(round(c.x, 1)) + ". Dobre a "
    "velocidade.")

# --- ✏️ MONTAR ---
# Complete o move_to lá em cima.
d = Creature(4.5, 0.5)
for passo in range(4):
    move_toward(d, 14.5, 0.5, 0.5)
desenhar(d)
assert int(d.x) < 6, (
    "A criatura atravessou a água. No move_to, confira o "
    "tile antes de andar.")
print("✅ Aula concluída!")
