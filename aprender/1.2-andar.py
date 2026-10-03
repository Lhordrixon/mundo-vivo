# Aula 7 de 25 · 1.2 Andar
# Guia da aula: 1.2-andar.md
import math

# Uma faixa de 16 tiles. mapa[0] é o texto todo, e
# mapa[0][x] é a letra na posição x.
mapa = ["~.....~~.......~"]
# ✏️ Sua vez (MUDAR): a criatura deve andar o
# dobro nos mesmos 4 passos. Mude só este número.
speed_tiles_per_second = 2.2


class Creature:
    def __init__(self, x, y):
        self.x = x   # 1.5 é o meio do tile 1
        self.y = y


def move_to(c, x, y):
    # ✏️ Sua vez (MONTAR): se mapa[int(y)][int(x)]
    # for "~", use return antes de mudar c.x e c.y.
    # (escreva o if aqui)
    # Atualizar
    c.x = x
    c.y = y


def move_toward(c, target_x, target_y, dt):
    # 1. Preparar: quanto falta e o tamanho do passo
    dx = target_x - c.x
    dy = target_y - c.y
    distance = math.sqrt(dx * dx + dy * dy)
    stride = speed_tiles_per_second * dt   # o passo
    # 2. Decidir: chega agora ou só se aproxima?
    if stride >= distance:
        move_to(c, target_x, target_y)
        return True                     # chegou
    # dx / distance é a direção; vezes stride, o passo.
    move_to(c, c.x + dx / distance * stride,
            c.y + dy / distance * stride)
    return False


# Desenha a faixa e, embaixo, a criatura.
# " " * 5 são 5 espaços: empurra o "o" até o x.
def desenhar(c):
    # Mostrar
    print(mapa[0])
    print(" " * int(c.x) + "o", " x =", round(c.x, 1))
    print("~ água  . terra  o criatura")


# --- Exemplo pronto: 4 passos de 0.5 s ---
# Rastreio: o x depois de cada passo.
c = Creature(1.5, 0.5)
print("passo   x")
for passo in range(4):
    move_toward(c, 14.5, 0.5, 0.5)
    print("  ", passo + 1, "  ", round(c.x, 1))
desenhar(c)

# --- ✏️ MUDAR ---
# A sua vez está lá em cima, na velocidade.
# Conferir
assert 9.5 < c.x < 11.5, (
    "x = " + str(round(c.x, 1)) + ": dobre a velocidade.")

# --- ✏️ MONTAR ---
# A sua vez está lá em cima, no move_to.
d = Creature(4.5, 0.5)
for passo in range(4):
    move_toward(d, 14.5, 0.5, 0.5)
desenhar(d)
# Conferir
assert int(d.x) < 6, (
    "Foi a x " + str(round(d.x, 1)) + ": água. Olhe o if.")
print("✅ Aula concluída!")
