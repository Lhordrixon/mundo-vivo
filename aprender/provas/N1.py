# Prova N1 · O mundo em Python
# Passar = os 4 itens certos sem abrir as respostas.
# Passou? Pode pular o Nível 1: marque "Prova N1" na
# trilha (issue #12). Não passou? A dica diz qual
# aula rever. Os itens vêm misturados.

print("Prova N1: 4 itens. Faça um por vez.")

# --- 1. Índice plano (aula 1.4) ---
width = 10
# ✏️ Sua vez: o índice do tile x = 3, y = 2.
indice = None
# Conferir
assert indice == 23, (
    "Deu " + str(indice) + ". Trocou x e y? Reveja 1.4.")


# --- 2. Outra cara, mesma ideia (aulas 1.2 e 1.3) ---
# Um robô gasta bateria com o tempo: 0.1 por segundo.
# Com bateria menor que 0.3, ele troca o estado para
# "CARREGANDO".
class Robo:
    def __init__(self):
        self.bateria = 1.0
        self.state = "ANDANDO"

    # ✏️ Sua vez: gaste 0.1 * dt e, se a bateria ficar
    # menor que 0.3, troque o estado.
    def step(self, dt):
        pass


r = Robo()
# Conferir: 4 passos de 2 segundos.
casos = [(0.8, "ANDANDO"), (0.6, "ANDANDO"),
         (0.4, "ANDANDO"), (0.2, "CARREGANDO")]
for bat, estado in casos:
    r.step(2)
    perto = abs(r.bateria - bat) < 0.01
    assert perto and r.state == estado, (
        "Bateria " + str(round(r.bateria, 2)) + " e "
        + r.state + "; devia ser " + str(bat) + " e "
        + estado + ". Reveja 1.2 (dt) e 1.3 (estado).")


# --- 3. A regra muda (aula 1.5) ---
# Comida que estraga: cada item de comida tem uma
# idade em dias. Com mais de 3 dias, ele sai da lista.
# ✏️ Sua vez: tire da própria lista os itens com
# idade > 3. Cuidado com a ordem do for (aula 1.5).
def jogar_fora(idades):
    pass


idades = [1, 5, 3, 6, 2, 4, 7]
jogar_fora(idades)
# Conferir
assert idades == [1, 3, 2], (
    "Ficou " + str(idades) + ", devia ser [1, 3, 2]. Mude "
    "a própria lista com pop, sem criar outra. Reveja 1.5.")

# --- 4. Ordem das linhas (aula 1.7) ---
# O step do mundo, sem o recuo e fora de ordem. A
# comida cresce primeiro; depois, cada criatura anda.
# 1: self.step_creature(i, dt)
# 2: self.regrow(dt)
# 3: for i in range(n - 1, -1, -1):
# ✏️ Sua vez: a ordem, numa lista. Ex.: [1, 2, 3]
ordem = None
# Conferir
assert ordem == [2, 3, 1], (
    "Deu " + str(ordem) + ". O step_creature fica dentro "
    "do for. Reveja 1.7.")

print("Agora, 3 frases na issue #12: o que o")
print("step faz, por que o for vai do fim e")
print("o que daria errado se fosse do começo.")
print("✅ Prova N1 concluída!")
