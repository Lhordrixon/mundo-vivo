# 0009 — Nomes secos de espécie

**Problema.** As espécies precisavam de nome no código. Um nome evocativo
("Élfico", "Orc") promete aparência, atributos e cultura.

**Decisão.** As duas espécies se chamam `ALPHA` e `BETA` (`Species`). Os
reinos seguem a mesma regra: nomes de sílabas inventadas, que não remetem
a nenhum povo real (`FactionRegistry`).

**Alternativas.** Nomes de fantasia desde já, prontos para quando as
espécies tiverem diferenças.

**Consequências.**
- O código não promete o que não faz. Hoje as espécies são iguais em
  tudo, menos em quem pode ter filho com quem, e não aparecem na tela.
- Quando uma espécie ganhar aparência ou atributos próprios, ganha também
  um nome que combine com eles. Isso é uma mudança de nome, não de lógica.

**Estado.** Em vigor desde o commit `a95c4d3`.
