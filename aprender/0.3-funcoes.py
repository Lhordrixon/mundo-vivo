# Aula 4 de 25 · 0.3 Funções que mudam a criatura
# Guia da aula: 0.3-funcoes.md

# --- Exemplo pronto: leia e rode ---
# def cria uma função: um bloco com nome,
# que você usa quantas vezes quiser.
def desenhar(c):
    # 1. Preparar
    mapa = [
        ["~", "~", "~", "~", "~", "~", "~"],
        ["~", ".", ".", ".", ".", ".", "~"],
        ["~", "~", "~", "~", "~", "~", "~"],
    ]
    mapa[c["y"]][c["x"]] = "o"
    # 2. Repetir
    for linha in mapa:
        texto = ""
        for tile in linha:
            texto = texto + tile + " "
        print(texto)
    # 3. Mostrar
    print("~ água  . terra  o criatura")
    print("fome:", c["hunger"], "com fome?", com_fome(c))


def com_fome(c):
    return c["hunger"] >= 5   # devolve True ou False


def andar_direita(c):
    c["x"] = c["x"] + 1


c = {"x": 1, "y": 1, "hunger": 3}
desenhar(c)
andar_direita(c)
desenhar(c)


# --- ✏️ MUDAR ---
# ✏️ Sua vez: o tempo passou, e a fome sobe 1.
# Troque o pass pela linha que faz isso. (pass
# quer dizer "não faça nada": só guarda o lugar.)
def passar_tempo(c):
    pass


passar_tempo(c)
# Conferir
assert c["hunger"] == 4, (
    "Fome " + str(c["hunger"]) + ", não 4. Cada passo soma "
    "1. Olhe o passar_tempo e tente de novo.")


# --- ✏️ MONTAR ---
# ✏️ Sua vez: escreva comer(c). A fome cai 3, mas
# nunca fica menor que 0: use um if.
def comer(c):
    pass


c["hunger"] = 5
comer(c)
# Conferir
assert c["hunger"] == 2, (
    "Após comer: " + str(c["hunger"]) + ", não 2. Tirou 3?")
c["hunger"] = 1
comer(c)
assert c["hunger"] == 0, (
    "Fome 1 menos 3 dá 0 (mínimo), não " + str(c["hunger"]))

desenhar(c)
print("✅ Aula concluída!")
