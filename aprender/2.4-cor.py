# Aula 16 de 17 · 2.4 Mudar uma cor e ver no APK
# Guia da aula: 2.4-cor.md
# No jogo, uma cor é  new Color(r, g, b, a):
# vermelho, verde e azul, de 0 a 1. O 4º é 1.

cores = {
    "WANDERING": [0.92, 0.92, 0.96],     # branco
    "SEEKING_FOOD": [1.00, 0.78, 0.28],  # amarelo
    "EATING": [0.42, 0.92, 0.45],        # verde
    "SEEKING_MATE": [1.00, 0.45, 0.70],  # rosa
}


def barras(cor):
    nomes = ["R", "G", "B"]
    for i in range(3):
        print(nomes[i], "#" * int(cor[i] * 20), cor[i])


# --- Exemplo pronto: o rosa de SEEKING_MATE ---
print("A cor de hoje:")
barras(cores["SEEKING_MATE"])

# --- ✏️ MUDAR ---
# ✏️ Sua vez: a cor nova de SEEKING_MATE, uma
# lista com 3 números de 0 a 1: [0.1, 0.2, 0.3]
nova = None
assert nova is not None, "Troque o None da nova cor."
for valor in nova:
    assert 0 <= valor <= 1, "Cada número vai de 0 a 1."
for estado in cores:    # cada chave do dicionário
    atual = cores[estado]
    diferenca = (abs(nova[0] - atual[0])
                 + abs(nova[1] - atual[1])
                 + abs(nova[2] - atual[2]))
    assert diferenca > 0.4, (
        "Sua cor ficou parecida demais com a de " + estado
        + ".")
print("A sua cor:")
barras(nova)


# --- ✏️ MONTAR ---
# ✏️ Sua vez: escreva para_java(cor). Ela devolve
# o texto Java: new Color(0.3f, 0.5f, 1.0f, 1f)
# Número com texto precisa de str: str(cor[0])
def para_java(cor):
    return None


texto = para_java([0.3, 0.5, 1.0])
assert texto == "new Color(0.3f, 0.5f, 1.0f, 1f)", (
    "para_java deu " + str(texto) + ". Junte com + e "
    "str(), e ponha f depois de cada número.")
print("Cole isto no CreatureRenderer.java:")
print(para_java(nova))
print("✅ Aula concluída!")
