# Aula 14 de 17 · 2.2 Primeiro PR pelo celular
# Guia da aula: 2.2-pr-celular.md
# Os passos do pull request estão embaralhados.

passos = {
    1: "tocar em Commit changes...",
    2: "tocar em Create pull request",
    3: "abrir o editor pelo link",
    4: "marcar branch nova e Propose changes",
    5: "trocar o texto da linha",
}

# --- Exemplo pronto: os passos, fora de ordem ---
for numero in [1, 2, 3, 4, 5]:
    print(numero, passos[numero])

# --- ✏️ MUDAR ---
# Qual número é o primeiro passo de todos?
primeiro = None
assert primeiro == 3, (
    "Antes de mudar o texto, é preciso abrir o editor.")

# --- ✏️ MONTAR ---
# Ponha os 5 números na ordem certa, numa
# lista. Exemplo de formato: [3, 1, 2, 5, 4]
ordem = None
assert ordem is not None, (
    "Troque o None por uma lista com os 5 números.")
# sorted(lista) devolve a lista em ordem.
assert sorted(ordem) == [1, 2, 3, 4, 5], (
    "Use os números de 1 a 5, cada um uma vez só.")
# ordem.index(n) diz em que posição está o n.
assert ordem.index(3) < ordem.index(5), (
    "Primeiro o editor, depois trocar o texto.")
assert ordem.index(5) < ordem.index(1), (
    "Troque o texto antes de Commit changes.")
assert ordem.index(1) < ordem.index(4), (
    "A branch nova é escolhida depois de Commit changes.")
assert ordem.index(4) < ordem.index(2), (
    "Create pull request é o último passo.")

print("Sua ordem:")
for numero in ordem:
    print("-", passos[numero])
print("✅ Aula concluída!")
