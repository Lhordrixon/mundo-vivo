# Aula 17 de 17 · 2.5 Mudar um parâmetro e ver no APK
# Guia da aula: 2.5-parametro.md
# No jogo, a velocidade fica no CreatureConfig:
#   public float speedTilesPerSecond = 2.2f;

speedTilesPerSecond = 2.2

def tilesEm(segundos, velocidade):
    return segundos * velocidade

def corrida(velocidade):    # um > a cada 3 tiles
    distancia = tilesEm(10, velocidade)
    print(velocidade, ">" * int(distancia / 3),
          round(distancia))

# --- Exemplo pronto: a velocidade de hoje ---
print("Tiles andados em 10 segundos:")
corrida(speedTilesPerSecond)

# --- ✏️ MUDAR ---
# Escolha uma velocidade 3 vezes maior.
nova = None
assert nova is not None, "Troque o None da nova."
assert abs(nova - 3 * speedTilesPerSecond) < 0.2, (
    "Você escolheu " + str(nova) + ". Quanto é 3 "
    "vezes 2.2?")
corrida(nova)

# --- ✏️ MONTAR ---
# Escreva tempoParaAtravessar: quantos segundos
# para andar `largura` tiles nessa velocidade.
def tempoParaAtravessar(largura, velocidade):
    return None

hoje = tempoParaAtravessar(256, speedTilesPerSecond)
assert hoje is not None and round(hoje) == 116, (
    "Para 256 tiles a 2.2 por segundo, deu "
    + str(hoje) + ". Divida a largura pela velocidade.")
depois = tempoParaAtravessar(256, nova)
print("Atravessar o mundo hoje:", round(hoje), "s")
print("Com a nova velocidade:", round(depois), "s")
print("A linha nova do Java:")
print("public float speedTilesPerSecond = "
      + str(nova) + "f;")
print("✅ Aula concluída!")
