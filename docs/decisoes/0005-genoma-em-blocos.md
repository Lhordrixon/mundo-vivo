# 0005 — Genoma em blocos

**Problema.** Filhos precisam parecer com os pais. Sem essa relação não
há herança, e sem herança a seleção natural não seleciona nada: nascer
bem-adaptado deixa de aumentar a chance de ter filhos bem-adaptados.

**Decisão.** O genoma são 8 blocos de 32 bits (`Genome.BLOCKS`). Cada
bloco é a semente de muitos genes, expandidos por hash
(`Genome.gene(bloco, índice)`). O filho recebe cada bloco **inteiro** de
um dos pais, por moeda justa (`Inheritance.cross`). Cada bloco tem 2% de
chance de ter um bit invertido (`Inheritance.mutate`). Cada traço é a
média de doze genes de quatro blocos (`Phenotype`).

**Alternativas.**
- `filho = hash(pai, mãe)`: uma semente só por criatura. Mais simples, e
  errado: o hash de dois pais não tem relação com nenhum dos dois.
- Traço de gene único: vira degrau, o filho tem o valor do pai ou o da
  mãe, e a seleção só anda aos saltos.

**Consequências.**
- A herança é medida, não argumentada. `HeritabilityTest` roda os dois
  esquemas lado a lado, com 5.000 casais cada:

  | Esquema | Herdabilidade medida |
  |---|---|
  | Blocos, sem mutação | 1,008 |
  | Blocos, mutação de 2% | 1,001 |
  | Blocos, mutação de 20% | 0,810 |
  | `filho = hash(pai, mãe)` | 0,020 |

- Genes do mesmo bloco viajam juntos entre gerações (ligação gênica).
- Os traços multiplicam a base do `CreatureConfig` numa faixa estreita
  (0,7× a 1,3×), para não desregular a população calibrada.
- 32 bytes por criatura, alocados uma vez com o pool. Nada aloca no
  nascimento.
- `creature` e `genetics` passaram a depender uma da outra.

**Estado.** Em vigor desde o commit `e35143b`.
