# Aprendiz simulado

Um agente faz o papel do Alan (Python e C++ iniciais, PEP 8, acha Java
complexo). Ele lê só as aulas, segue os passos, tenta cada lacuna sem
abrir as respostas e relata onde travou.

Limite (HIPÓTESE): é filtro de defeitos, não prova de que o Alan aprende.
Não substitui o Alan nem um aparelho real.

## Rodada 1 · 03/10/2026 · Fase A (17 aulas alteradas)

### Nível 0 (0.0 a 0.4)

Chegou a "✅ Aula concluída!" nas 5 aulas, quase sempre na 1ª tentativa.
Defeitos achados e o que foi feito:

- 0.2 aceitava `outra = c` (uma criatura só) → novo `assert outra is
  not c` e conferência de `health`.
- 0.1–0.4 mandavam "abrir o .py no Pydroid", mas a 0.0 ensina o Raw →
  o Rodar agora diz "copie o 📋 Raw e cole no Pydroid, ou abra do ZIP".
- 0.4 citava um `c` que não existe → `criaturas[0]`; explicado que o
  Python põe o `self` sozinho; objetivo reescrito.
- Dicas que enganavam: "fome não é negativa" com fome 1 → "Fome 1
  menos 3 dá 0 (mínimo)"; 0.0 mostrava `A` fixo → mostra a letra do
  aluno; 0.4 passou a perguntar pelo `self.hunger`.
- 0.3: o parâmetro não é cópia (diferente do C++) → dito na resposta.
- Glossário ganhou `None`, `pass`/`...`, parâmetro, `True`/`False`,
  `and`/`or`, `+=`, `append`/`len`/`range`, `min`/`max`.
- Menores: "chamada" em vez de "usada"; contagem do zero na dica da
  0.1; "sem apagar o agua = 0"; legenda do ⬜ ("só cor").

### Nível 1 (1.1 a 1.7)

Chegou ao fim das 7. Tentativas: 1 na maioria; 3 no MONTAR da 1.4 e da
1.6 (faltava `self.` no comentário). Defeitos e correções:

- 1.4 entregava a resposta duas vezes (o exemplo do cinema dava o mesmo
  29 do assert) → o tile pedido virou (7, 1).
- As Rosetas mostram a solução do MONTAR → o título agora diz "abra
  depois de terminar".
- Comentários sem `self.` (1.4, 1.6) e sem `c = creatures[i]` (1.5) →
  nomes reais no comentário, mais como quebrar linha longa (PEP 8).
- 1.6: o Prever não podia ser conferido → agora pergunta se o programa
  para no primeiro `assert`.
- 1.7: `==` com float → `abs(...) < 0.01`; a barra de comida sumia
  abaixo de 1 → o valor aparece ao lado; `TypeError` de
  `for c in self.creatures` entrou na armadilha.
- 1.5: Investigar pergunta por que a `c` não levou o golpe (o sintoma
  do bug).
- Rótulos numerados começavam em "2." → começam em "1.".
- 1.1: armadilha "de baixo para cima" reescrita; diferença 0.015 x 0.05
  explicada; 1.3: faixa certa do MUDAR (0.10 a 0.27).

Ficou de fora (limite de 40 linhas): rastreio da segunda criatura na
1.2 e do `for` da 1.5.

### Nível 2 (2.1 a 2.5)

Concluiu os 5 `.py`; os passos de navegador foram julgados pelo texto.
Defeitos e correções:

- O modelo de PR sugeria `Resolve #12`, que é a issue da trilha (o PR a
  fecharia), e o texto escrito dentro do `<!-- -->` some → exemplo sem
  número e "escreva numa linha nova, depois do `-->`".
- Faltava o caminho quando a CI fica vermelha → item no "⚠️ Não
  funcionou?" da 2.2 e da 2.3; a tarefa da 2.3 avisa que a CI "Aulas"
  confere o tamanho.
- 2.5 imprimia `6.6000000000000005f` → `round(nova, 2)`.
- Faltava o aviso do Chrome ao baixar `.apk` → "Baixar mesmo assim" na
  2.4 e na 2.5.
- 2.4: `TypeError` sem `str()` escapava da dica → o comentário da
  lacuna lembra o `str(cor[0])`; "troque só os três números".
- "Procure o texto" não funciona no editor do celular → "role até o
  número da linha na margem".
- 2.3: tela "Configure Git", Linux sem Desktop oficial e token no
  `git push`.
- 2.1: pergunta falsa ("toda linha tem `;`") reescrita; `.equals` virou o
  item 18 da Roseta; `in_bounds(8, 0)` pega o erro de um.
- 2.5: velocidade de hoje no Prever, base de comparação e Delete branch.
- Issue #8: apontava para uma seção que não existe → agora aponta para a
  aula 2.2.

Não testável daqui: o Gradle com o plugin Android (o `core:test` com 6.6
foi medido antes, 115 testes verdes) e qualquer botão real do GitHub.
