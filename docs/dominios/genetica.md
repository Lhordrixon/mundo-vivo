# 🧬 Genética

- **O que é:** o que uma criatura herda dos pais. O genoma decide
  velocidade, visão, metabolismo e idade máxima.
- **Classes principais:** `sim/genetics/Genome`, `Inheritance`,
  `Phenotype`; o campo `Creature.genome`.
- **Quem depende:** `creature` (ciclo conhecido) e `Simulation`.
  `genetics` depende de `creature` e `util`.
- **Testes que cobrem:** `HeritabilityTest`, `InheritanceTest`,
  `GeneIndependenceTest`, `PopulationGeneticsTest` e a parte de genética
  do `tools/SimSelfTest`.

A espécie, que decide com quem uma criatura pode ter filhos, está em
[criaturas](criaturas.md).

---

## 🧬 O que o genoma guarda?

O genoma são oito blocos de 32 bits. Cada bloco não é um gene: é a semente
de muitos genes.

`Genome.gene(bloco, índice)` transforma um bloco em quantos genes forem
precisos, por uma conta de hash. Assim, 32 bytes viram genes sem limite, e
nenhum ocupa memória.

### Como o filho recebe o genoma?

1. **Cruzamento** (`Inheritance.cross`): cada bloco vem **inteiro** de um
   dos pais, por moeda justa.
2. **Mutação** (`Inheritance.mutate`): cada bloco tem 2% de chance de ter
   um bit invertido.
3. **Traços** (`Phenotype.apply`): o genoma é traduzido em velocidade,
   visão, metabolismo e idade máxima.

Os fundadores recebem um genoma sorteado inteiro. Toda a variação do mundo
sai deles.

### Por que blocos inteiros, e não uma mistura dos pais?

Porque a mistura destrói a herança. A decisão completa está em
[genoma em blocos](../decisoes/0005-genoma-em-blocos.md).

A alternativa óbvia seria `filho = hash(pai, mãe)`. O hash de dois pais dá
um valor sem relação com nenhum dos dois. Dois pais rápidos teriam um filho
lento com a mesma chance de qualquer outro.

Sem essa relação, não há herança. E sem herança, a seleção natural não
funciona: nascer bem-adaptado não aumenta a chance de ter filhos
bem-adaptados.

Herdando blocos inteiros, tudo o que um bloco codifica chega junto ao
filho. De brinde, genes do mesmo bloco passam juntos entre gerações, como na
natureza.

> [!NOTE]
> O contraste é medido, não só argumentado. `HeritabilityTest` roda os dois
> esquemas lado a lado.

| Esquema | Herdabilidade medida |
|---|---|
| Blocos, sem mutação | **1,008** |
| Blocos, mutação de 2% | **1,001** |
| Blocos, mutação de 20% | 0,810 |
| `filho = hash(pai, mãe)` | **0,020** |

A herdabilidade é a inclinação da reta "traço do filho × média dos pais".
Um significa que o filho é a média dos pais. Zero significa que os pais não
dizem nada sobre o filho.

---

## 🧬 Como o genoma vira um traço?

Cada traço é a **média de doze genes**, espalhados por quatro blocos. Nunca
um gene só.

Um traço de gene único seria um degrau: o filho teria o valor do pai ou o
da mãe, nada no meio. Com doze genes somados, o traço fica contínuo, e o
filho cai perto da média dos pais.

A média de muitos valores se concentra no meio. Então a maioria das
criaturas nasce mediana, e os extremos são raros.

Os traços **multiplicam** os valores base do `CreatureConfig`, entre 0,7× e
1,3×. Na prática, quase todas as criaturas ficam entre 0,85× e 1,15×. A
faixa é estreita para não desequilibrar a população, que foi calibrada com
medições.

| Traço | Onde é usado |
|---|---|
| velocidade | ao andar (`Simulation.moveToward`) |
| visão | na busca por comida (`Simulation.visionOf`) |
| metabolismo | na fome que sobe a cada passo |
| idade máxima | na morte por velhice |
| tamanho | **em lugar nenhum ainda** |

O tamanho é calculado, mas nada o usa. Está anotado em
[dívida técnica](../divida-tecnica.md).

<details>
<summary>A seleção natural já está agindo?</summary>

Herança é a condição para haver seleção. Não é a prova de que ela está
acontecendo.

Medindo a média dos traços no começo e depois de 20 minutos, em três
sementes:

| traço | 12345 | 2026 | 777 |
|---|---|---|---|
| metabolismo | −1,4% | +1,4% | −2,2% |
| idade máxima | +2,5% | +0,2% | +3,0% |
| visão | +0,2% | +2,2% | −2,4% |

Metabolismo e visão vão para os dois lados. Em vinte minutos, umas sete
gerações, isso é acaso.

A idade máxima sobe nas três. É o traço mais ligado ao número de filhos:
viver mais é ter mais tempo para reproduzir.

Três sementes é pouco para afirmar. A direção comum pode ser coincidência,
com chance de 1 em 8. Provar seleção pede simulações mais longas.

</details>

<details>
<summary>Por que nada disso aloca memória</summary>

Nenhum `new` acontece por criatura, por nascimento ou por quadro.

- O genoma de cada criatura nasce junto com ela, no construtor do pool.
- `cross`, `mutate` e `random` escrevem no array que recebem.
- `Phenotype` escreve em campos simples da criatura.
- `Simulation` guarda um único genoma de rascunho, criado uma vez.

</details>
