# Como contribuir

Toda mudança entra do mesmo jeito: **branch → commit → pull request**.
Esta página mostra cada etapa com um exemplo.

> [!TIP]
> Primeira vez? O [COMECE-AQUI.md](COMECE-AQUI.md) faz o caminho inteiro
> com você, passo a passo. Esta página é a referência rápida.

---

## 🔁 Qual é o caminho de uma mudança?

```bash
git switch main
git pull
git switch -c fix/criatura-sem-reino
# ... faça a mudança ...
./gradlew core:test
git add .
git commit -m "Corrige o nascimento de filho de criatura sem reino"
git push -u origin fix/criatura-sem-reino
```

Depois, abra o link que o `git push` mostra e preencha o formulário do
pull request.

Nunca envie direto para a `main`. A `main` só recebe mudanças por pull
request.

---

## 🌿 Como dou nome à branch?

Use `tipo/o-que-muda`, em minúsculas, com hífens e sem acentos.

- `docs/` para documentação. Exemplo: `docs/corrige-contagem-de-ferramentas`
- `fix/` para conserto de bug. Exemplo: `fix/criatura-sem-reino`
- `feat/` para algo novo no jogo. Exemplo: `feat/desenha-territorio`
- `test/` para testes novos. Exemplo: `test/resize-da-camera`

Uma branch cuida de um assunto só.

---

## ✍️ Como escrevo a mensagem do commit?

Em português, começando por um verbo no presente, como no histórico do
projeto:

```
Corrige o nascimento de filho de criatura sem reino
Adiciona teste para o resize da câmera
Move AUDITORIA.md para docs/auditoria.md
```

A primeira linha diz **o que** muda, em até 70 letras. Se precisar
explicar **por quê**, deixe uma linha em branco e escreva embaixo:

```bash
git commit -m "Corrige o nascimento de filho de criatura sem reino" -m "O filho herdava o reino -1 e o jogo quebrava."
```

---

## 📬 O que o pull request precisa ter?

- **Um assunto só.** Duas mudanças independentes são dois pull requests.
- **Testes passando.** A CI roda `./gradlew core:test` sozinha e mostra ✅
  ou ❌ na página.
- **O formulário preenchido.** Ele pergunta o que muda, por quê e como
  você testou.

Pull request pequeno é revisado mais rápido. Na dúvida, divida.

---

## 📏 Que regras o código segue?

- **Nada em `sim/` usa o libGDX.** A simulação é Java puro, para rodar e
  testar sem tela. O desenho fica em `render/`.
- **A mesma semente dá sempre o mesmo mundo.** Todo sorteio passa pelo
  sorteador do jogo (`Rng`), nunca por `Math.random()`.
- **Nada é copiado do WorldBox.** Nem código, nem arte, nem texto. Ele é
  só inspiração.

Os detalhes estão em [docs/arquitetura.md](docs/arquitetura.md).

---

## 🙋 Como peço ajuda?

- **Travou num erro?** [Abra uma issue](https://github.com/Lhordrixon/mundo-vivo/issues/new/choose)
  com a mensagem de erro copiada inteira.
- **Dúvida sobre a sua mudança?** Pergunte num comentário do seu pull
  request. Pode abrir o pull request antes de terminar, só para perguntar.

Nenhuma pergunta é boba. Um erro relatado já é uma contribuição.

---

## ⚖️ Direitos

Quem contribui aceita que o código enviado passa a fazer parte do projeto sob as mesmas condições do resto do repositório.
