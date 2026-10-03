# Aula 15 de 25 · 2.3 Primeiro PR pelo PC
# Guia da aula: 2.3-pr-pc.md
# Cada botão do GitHub Desktop faz o mesmo que
# um comando do git. Aqui estão embaralhados.

botoes = {
    1: "Commit to minha-branch",
    2: "Publish branch",
    3: "Clone",
    4: "New branch",
}
comandos = {
    1: "git commit -m \"mensagem\"",
    2: "git push -u origin minha-branch",
    3: "git clone (endereço do projeto)",
    4: "git switch -c minha-branch",
}

# --- Exemplo pronto: cada botão e o seu comando ---
for numero in [1, 2, 3, 4]:
    print(numero, botoes[numero])
    print("    git:", comandos[numero])

# --- ✏️ MUDAR ---
# ✏️ Sua vez: qual número envia a sua branch
# para o GitHub?
envia = None
assert envia == 2, (
    "Enviar para o GitHub é o push. Qual número tem git "
    "push?")

# --- ✏️ MONTAR ---
# ✏️ Sua vez: ponha os 4 números na ordem em que
# você usa, numa lista. Formato: [2, 1, 4, 3]
ordem = None
assert ordem is not None, (
    "Troque o None por uma lista com os 4 números.")
assert sorted(ordem) == [1, 2, 3, 4], (
    "Use os números de 1 a 4, cada um uma vez só.")
assert ordem.index(3) < ordem.index(4), (
    "Primeiro você precisa ter o projeto: Clone.")
assert ordem.index(4) < ordem.index(1), (
    "Crie a branch antes de salvar o commit nela.")
assert ordem.index(1) < ordem.index(2), (
    "Só dá para enviar depois de salvar: commit, depois "
    "push.")

print("Sua ordem:")
for numero in ordem:
    print("-", botoes[numero])
    print("    git:", comandos[numero])
print("✅ Aula concluída!")
