# 0007 — 240 fundadores

**Problema.** Separar a reprodução em duas espécies derrubou a população
de equilíbrio em cerca de sete vezes: 120 fundadores viravam dois grupos
de uns 60. A cada instante, só algumas
criaturas estão procurando parceiro. Exigir a mesma espécie cortou esse
grupo pela metade.

**Decisão.** Dobrar os fundadores: `CreatureConfig.initialPopulation` foi
de 120 para 240. Assim, cada espécie volta a ter a densidade que a
população inteira tinha antes.

**Alternativas.**
- Alargar o raio de busca por parceiro de 70 para 105, 140 ou 175 tiles:
  não recuperou nada (médias de 70, 55 e 49). As criaturas não estavam
  longe demais; estavam procurando em momentos diferentes.
- Encurtar a espera entre reproduções: passou do ponto (média de 927,
  contra cerca de 470 antes das espécies).

**Consequências.**
- A população voltou à ordem de grandeza de antes das espécies, sem
  extinção nas 36 execuções medidas.
- A comida voltou a ser consumida (66–77% do total, oscilando), e as
  mortes por fome voltaram a existir: 679 e 1086 em quarenta minutos,
  contra 19 com 120 fundadores, quando a comida ficava 99% intocada. O
  mundo voltou a ser limitado por comida, pela natalidade.
- Hoje este número não decide quantos reinos existem
  (`CreatureConfig.initialFactions` decide). Mudar a população só muda
  quantos habitantes cada reino recebe.
- Na época, cada fundador fundava o próprio reino, então o número de
  reinos também dobrou, para 240. Foi conferido antes: nem
  `FactionRegistry` nem `Territory` supõem um número máximo de reinos, e as
  240 facções nasciam com um membro cada, 237 delas com território, sem
  nenhum tile com dono inválido. Isso deixou de valer com a
  [decisão 0006](0006-poucos-reinos-contiguos.md).

<details>
<summary>As medições</summary>

**Isolando a causa.** Mesma semente, mesmo código, mudando só se o filtro
de espécie barra alguém:

| | População média a 20 min | Pior semente |
|---|---|---|
| Uma espécie (filtro inerte) | 470 | 63 |
| Duas espécies, 120 fundadores | 63 | 2 |

**Com a correção**, 12 sementes, 20 minutos simulados, antes do combate:

| Quadros por segundo | 120 fundadores | 240 fundadores |
|---|---|---|
| 30 | média 142, 0 extinções | média 532, 0 extinções (254–1023) |
| 60 | média 63, 0 extinções | média 479, 0 extinções (285–742) |
| 90 | média 66, 0 extinções | média 458, 0 extinções (271–853) |

A tabela de isolamento é de antes de os reinos ganharem nome e cor. Nome
e cor gastam um sorteio por fundação, então cada semente passou a contar
outra história. A conclusão não muda: o filtro de espécie derruba a
população em cerca de uma ordem de grandeza, e os 240 fundadores pagam
essa conta.

Numa medição anterior, três execuções com 120 fundadores se extinguiam
(sementes 99 e 4242). Nesta, nenhuma. Isso não quer dizer que 120 ficou
seguro: quer dizer que ali a extinção sempre foi questão de sorteio.

</details>

**Estado.** Em vigor desde o commit `b4aa668`.
