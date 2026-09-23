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

<details>
<summary>O argumento completo, que antes morava no javadoc</summary>

**Contra `filho = hash(pai, mãe)`.** O hash de dois pais é um valor
descorrelacionado dos dois. Dois pais grandes gerariam um filho pequeno com
a mesma probabilidade de qualquer outro. Sem herdabilidade, o que sobra é
deriva aleatória com aparência de evolução, que é o pior dos mundos:
parece que funciona.

**A favor dos blocos.** O bloco chega intacto, então tudo que ele codifica
chega junto, e a correlação pai-filho sobrevive. Genes do mesmo bloco são
herdados juntos, que é como funciona de verdade, e não uma aproximação
simpática.

**Por que todo traço é aditivo.** Um traço de gene único é degrau: ou o
filho herdou aquele bloco e tem o valor do pai, ou não herdou e tem o da
mãe. A média dos pais não prevê nada, a regressão filho × pais fica em
degraus, e a seleção só consegue mover a população aos saltos. Com doze
genes somados, o traço vira contínuo, o filho cai perto da média dos pais,
e uma pressão seletiva fraca consegue empurrar a distribuição aos poucos.

A média de doze uniformes se concentra no meio da faixa (teorema central
do limite): a maioria nasce mediana e os extremos são raros.
`PopulationGeneticsTest` confere isso. O javadoc antigo citava um
`TraitDistributionTest`, que não existe.

**Por que a faixa é estreita.** O equilíbrio da população foi calibrado
com medição, e genética não é desculpa para desregulá-lo. Uma criatura
geneticamente veloz é 30% mais rápida que a média, não o dobro.

</details>

**Estado.** Em vigor desde o commit `e35143b`.
