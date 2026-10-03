# Aula 17 de 17 · 2.5 Mudar um parâmetro e ver no APK
# Guia da aula: 2.5-parametro.md
# No jogo, a velocidade fica no CreatureConfig:
#   public float speedTilesPerSecond = 2.2f;

speed_tiles_per_second = 2.2


def tiles_em(segundos, velocidade):
    return segundos * velocidade


def corrida(velocidade):    # um > a cada 3 tiles
    distancia = tiles_em(10, velocidade)
    print(velocidade, ">" * int(distancia / 3),
          round(distancia))


# --- Exemplo pronto: a velocidade de hoje ---
print("Tiles andados em 10 segundos:")
corrida(speed_tiles_per_second)

# --- ✏️ MUDAR ---
# Escolha uma velocidade 3 vezes maior.
nova = None
assert nova is not None, "Troque o None da nova."
assert abs(nova - 3 * speed_tiles_per_second) < 0.2, (
    "Você escolheu " + str(nova) + ". Quanto é 3 vezes "
    "2.2?")
corrida(nova)

# --- ✏️ MONTAR ---
# Escreva tempo_para_atravessar: quantos segundos
# para andar `largura` tiles nessa velocidade.


def tempo_para_atravessar(largura, velocidade):
    return None


hoje = tempo_para_atravessar(256, speed_tiles_per_second)
assert hoje is not None and round(hoje) == 116, (
    "Para 256 tiles a 2.2 por segundo, deu " + str(hoje)
    + ". Divida a largura pela velocidade.")
depois = tempo_para_atravessar(256, nova)
print("Atravessar o mundo hoje:", round(hoje), "s")
print("Com a nova velocidade:", round(depois), "s")
print("A linha nova do Java:")
print("public float speedTilesPerSecond = "
      + str(nova) + "f;")
print("✅ Aula concluída!")
