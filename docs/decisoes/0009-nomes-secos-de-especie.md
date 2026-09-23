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

<details>
<summary>O argumento completo, que antes morava no javadoc</summary>

Hoje a espécie faz exatamente uma coisa: decide quem pode ter filho com
quem. Não muda velocidade, fome, dieta, tamanho nem hostilidade, e não
aparece na tela. Batizar as duas de algo evocativo — lobo e cervo, elfo e
anão — prometeria predação, cultura e guerra que o código não tem. E o
projeto já pagou por promessa não cumprida: a conversão de toque para tile
passou meses escrita e sem chamador. Rótulo seco é mais honesto que
história adiantada.

É por isso que o enum nasce com duas constantes, e não com um bestiário.
Duas bastam para a regra existir e ser testável. As raças de verdade, com
atributos próprios, entram depois, uma de cada vez, cada uma quando tiver
comportamento que a justifique.

</details>

**Estado.** Em vigor desde o commit `a95c4d3`.
