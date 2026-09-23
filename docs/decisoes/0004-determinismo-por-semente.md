# 0004 — Determinismo pela semente

**Problema.** Sem repetir exatamente uma execução, não há teste de
regressão: não dá para saber se uma mudança alterou o comportamento. E um
save teria de guardar o mapa inteiro.

**Decisão.** Tudo sai da semente. O sorteador é um SplitMix64 próprio
(`sim/util/Rng`). O ruído do terreno sai de hash inteiro
(`sim/noise/FractalNoise`), não de tabela sorteada. Nada em `sim/` usa
`java.util.Random`, `Math.random()` ou relógio.

**Alternativas.** `java.util.Random`, que tem implementação fixa, mas
estado difícil de salvar e é mais lento; ou sortear com o relógio, que
tornaria cada execução única.

**Consequências.**
- Mesma semente, mesmo mundo e mesma história, criatura por criatura, em
  qualquer aparelho. Há testes que exigem isso.
- Qualquer mudança que use o sorteador numa ordem diferente muda a
  história de cada semente. Por isso os números de população são
  remedidos a cada mudança, e são ordem de grandeza, não constantes.
- Um sorteio a mais por evento é caro em estabilidade de medição: o nome
  e a cor de um reino saem de um único `nextLong()`.
- O jogo em si sorteia a semente com o relógio (`MundoVivoGame`), fora de
  `sim/`.

**Estado.** Em vigor.
