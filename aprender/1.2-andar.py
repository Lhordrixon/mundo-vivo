# Aula 7 de 17 · 1.2 Andar
# Guia da aula: 1.2-andar.md
import math

mapa = ["~.....~~.......~"]   # uma faixa de 16 tiles
speedTilesPerSecond = 2.2      # ✏️ MUDAR (abaixo)

class Creature:
    def __init__(self, x, y):
        self.x = x   # 1.5 é o meio do tile 1
        self.y = y

def moveTo(c, x, y):
    # ✏️ MONTAR: se mapa[int(y)][int(x)] for "~",
    # use return antes de mudar c.x e c.y.
    c.x = x
    c.y = y

def moveToward(c, targetX, targetY, dt):
    dx = targetX - c.x
    dy = targetY - c.y
    distance = math.sqrt(dx * dx + dy * dy)
    stride = speedTilesPerSecond * dt   # o passo
    if stride >= distance:
        moveTo(c, targetX, targetY)
        return True                     # chegou
    moveTo(c, c.x + dx / distance * stride,
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
    moveToward(c, 14.5, 0.5, 0.5)
desenhar(c)
print("~ água  . terra  o criatura")

# --- ✏️ MUDAR ---
# Faça a criatura andar o dobro nos mesmos
# 4 passos. Mude o speedTilesPerSecond.
assert 9.5 < c.x < 11.5, ("Ela foi até x "
    + str(round(c.x, 1)) + ". Dobre a velocidade.")

# --- ✏️ MONTAR ---
# Complete o moveTo lá em cima.
d = Creature(4.5, 0.5)
for passo in range(4):
    moveToward(d, 14.5, 0.5, 0.5)
desenhar(d)
assert int(d.x) < 6, ("A criatura atravessou a água."
    " No moveTo, confira o tile antes de andar.")
print("✅ Aula concluída!")
