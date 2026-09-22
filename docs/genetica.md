# Genética e espécies

O que uma criatura herda dos pais, e com quem ela pode ter filhos.

**Em resumo:** cada criatura tem uma espécie e um genoma. A espécie decide
com quem ela pode cruzar. O genoma decide velocidade, visão, metabolismo e
quanto tempo ela vive. Os dois passam de pai para filho.

---

## 🧬 Com quem uma criatura pode ter filhos?

Com quem é da mesma espécie. Existem duas: `ALPHA` e `BETA`.

- Cada fundador sorteia a sua.
- Todo filho herda a espécie dos pais.
- Duas criaturas de espécies diferentes nunca formam par, por mais perto que
  estejam.

Como os pais têm sempre a mesma espécie, o filho herda sem sorteio.

Os nomes são sem graça de propósito. As duas espécies são iguais em tudo,
menos em quem cruza com quem. Um nome como "Élfico" prometeria cultura e
comportamento que o código não tem.

### Por que o mundo nasce com 240 criaturas?

Por causa das espécies. Separar a reprodução em duas espécies derrubou a
população em cerca de sete vezes.

O motivo: a cada instante, só algumas criaturas estão procurando parceiro.
Exigir a mesma espécie corta esse grupo pela metade. Dobrar os fundadores,
de 120 para 240, devolveu a cada espécie a densidade que a população
inteira tinha antes.

<details>
<summary>As medições que levaram aos 240 fundadores</summary>

**Isolando a causa.** Mesma semente, mesmo código, mudando só se o filtro
de espécie barra alguém:

| | população média a 20 min | pior semente |
|---|---|---|
| Uma espécie (filtro desligado) | 470 | 63 |
| Duas espécies, 120 fundadores | 63 | 2 |

**O raio de busca não era a causa.** Alargar a busca por parceiro de 70
para 105, 140 ou 175 tiles não recuperou nada: as médias foram 70, 55 e 49.
As criaturas não estavam longe demais. Estavam procurando em momentos
diferentes.

**Encurtar a espera entre reproduções passava do ponto.** A média ia para
927, contra cerca de 470 antes das espécies.

**Dobrar os fundadores resolveu.** Medido em 12 sementes, 20 minutos
simulados, antes de existir combate:

| quadros por segundo | 120 fundadores | 240 fundadores |
|---|---|---|
| 30 | média 142 | média 532 |
| 60 | média 63 | média 479 |
| 90 | média 66 | média 458 |

Com 240, a comida voltou a ser consumida. Ela caiu para 66–77% do total e
passou a oscilar. Com 120, ficava 99% intocada enquanto a população
definhava.

Esses números são de antes do combate e da genética. Os atuais estão em
[verificação](verificacao.md).

</details>

---

## 🧪 O que o genoma guarda?

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

Porque a mistura destrói a herança.

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

## 📏 Como o genoma vira um traço?

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
[dívida técnica](divida-tecnica.md).

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
