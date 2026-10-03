# Prova N0 · Python que falta
# Passar = os 4 itens certos sem abrir as respostas.
# Passou? Pode pular o Nível 0: marque "Prova N0" na
# trilha (issue #12). Não passou? A dica diz qual
# aula rever.

print("Prova N0: 4 itens. Faça um por vez.")

# --- 1. Mapa (aula 0.1) ---
mapa = [
    ["~", "~", "~", "~"],
    ["~", ".", ".", "~"],
    ["~", "~", "~", "~"],
]
# ✏️ Sua vez: ponha "o" na linha 1, coluna 2.
...
# Conferir
assert mapa[1][2] == "o", (
    "A linha 1, coluna 2 não tem \"o\". Reveja a aula 0.1.")


# --- 2. Outra cara, mesma ideia (aulas 0.2 e 0.3) ---
# Um celular tem bateria de 0 a 100. Cada uso gasta
# 30, mas a bateria nunca fica menor que 0.
# ✏️ Sua vez: escreva usar(cel).
def usar(cel):
    pass


cel = {"bateria": 100}
# Conferir: 4 usos seguidos.
for esperado in [70, 40, 10, 0]:
    usar(cel)
    assert cel["bateria"] == esperado, (
        "Deu " + str(cel["bateria"]) + ", devia dar "
        + str(esperado) + ". Reveja o comer da aula 0.3.")


# --- 3. A regra muda (aula 0.3) ---
# A fome sobe 1 por passo. Mas, se estiver frio, sobe 2.
# ✏️ Sua vez: escreva passar_tempo(c, frio).
def passar_tempo(c, frio):
    pass


c = {"hunger": 3}
# Conferir: sem frio, com frio, sem frio.
casos = [(False, 4), (True, 6), (False, 7)]
for frio, esperado in casos:
    passar_tempo(c, frio)
    assert c["hunger"] == esperado, (
        "Com frio=" + str(frio) + " deu "
        + str(c["hunger"]) + ", devia dar " + str(esperado)
        + ". Reveja a 0.3.")

# --- 4. Ordem das linhas (aula 0.4) ---
# Estas linhas, sem o recuo, estão fora de ordem.
# Juntas, elas criam uma criatura com fome 3:
# 1: self.hunger = 3
# 2: c = Creature()
# 3: class Creature:
# 4: def __init__(self):
# ✏️ Sua vez: a ordem, numa lista. Ex.: [1, 2, 3, 4]
ordem = None
# Conferir
assert ordem == [3, 4, 1, 2], (
    "Deu " + str(ordem) + ". A classe vem antes do objeto. "
    "Reveja a aula 0.4.")

print("Agora, 3 frases na issue #12: o que")
print("uma classe faz, por que usar e o que")
print("é o self.")
print("✅ Prova N0 concluída!")
