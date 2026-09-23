# 🧪 Verificação

- **O que é:** como o projeto é testado, as ferramentas de `tools/` e os
  números de desempenho atuais.
- **Onde:** `core/src/test/` (JUnit) e `tools/` (Java puro).

**Em resumo:** a simulação tem 109 métodos de teste JUnit, que rodam na
CI a cada pull request. Há também 95 verificações que rodam só com o JDK,
sem baixar nada. O APK é gerado na CI e já abriu num celular.

---

## 🧪 Como eu rodo os testes?

Há dois jeitos. Os dois conferem a mesma lógica.

### Pelo Gradle (o oficial)

```bash
./gradlew core:test
```

O Gradle é a ferramenta que compila e testa o projeto. Na primeira vez ele
baixa as dependências, então precisa de internet. No fim, deve aparecer:

```
BUILD SUCCESSFUL
```

É esse comando que a CI roda em todo pull request. A CI também gera o
APK com `./gradlew android:assembleDebug`. Ela não compila o módulo
`desktop`. Uma tag `v*` roda os mesmos testes antes de publicar o APK
numa Release.

### Só com o JDK, sem Gradle

Útil quando não há internet, ou para conferir algo rápido:

```bash
javac -d build/sim $(find core/src/main/java/com/emannuel/mundovivo/sim -name '*.java')
javac -d build/tools -cp build/sim tools/*.java
java -cp build/sim:build/tools SimSelfTest
```

Demora cerca de um minuto e meio. No fim, deve aparecer:

```
=== 95 passaram, 0 falharam ===
```

> [!TIP]
> No Windows, rode esses comandos no **Git Bash**, que vem junto com o Git.
> O `$(find ...)` não funciona no Prompt de Comando.

---

## Que ferramentas existem em `tools/`?

Quatro, todas em Java puro. Depois de compilar como no bloco acima:

| Ferramenta | O que faz |
|---|---|
| `WorldPreview` | gera uma imagem PNG do mundo de uma semente |
| `SimulationPreview` | gera uma imagem do mundo com as criaturas em cima |
| `SimulationReport` | roda a simulação e mostra população, nascimentos e mortes |
| `SimSelfTest` | roda as 95 verificações |

```bash
# imagem do mundo da semente 12345, com 3 px por tile
java -cp build/sim:build/tools WorldPreview 12345 3 previa.png

# o mesmo mundo com as criaturas, depois de 2 minutos simulados
java -cp build/sim:build/tools SimulationPreview 12345 2 3 vivo.png

# população ao longo de 5 minutos simulados
java -cp build/sim:build/tools SimulationReport 12345 5
```

O `WorldPreview` serve para ajustar `WorldConfig` em segundos. O
`SimulationReport` faz o mesmo para `CreatureConfig`.

---

## 🧪 O que os testes garantem?

Cada item abaixo tem teste. Os nomes exatos estão em `core/src/test/` e em
`tools/SimSelfTest.java`.

- 🌍 **Mundo:** a mesma semente dá o mesmo mundo, e sementes diferentes
  dão mundos diferentes. Há terra e mar em proporção jogável.
- 🌾 **Comida:** a rebrota respeita o teto, e ninguém come o que não
  existe.
- 🐾 **Criaturas:** o pool reaproveita espaços. Ninguém sai do mapa nem
  pisa na água. Fome e vida ficam sempre entre 0 e 1.
- 💔 **Dano e morte:** toda morte passa pelo mesmo caminho. Nenhuma morte é
  contada duas vezes.
- ⚔️ **Combate:** reinos diferentes se ferem; o mesmo reino, não. Estar em
  menor número mata mais rápido.
- 👆 **Toque:** onde a criatura é desenhada é onde o toque a encontra.
- 🧬 **Genética:** o filho puxa a média dos pais, com herdabilidade de
  1,008. O esquema ingênuo, testado ao lado, dá 0,020.
- 👑 **Reinos:** o mundo funda 4 reinos contíguos. A mesma semente funda
  os mesmos reinos.
- 📈 **População:** três sementes sobrevivem 20 minutos sem extinguir e sem
  lotar o pool.

---

## Quais são os números atuais?

Medidos com o código de hoje: reinos contíguos, combate e genética.

**População**, em 12 sementes, 20 minutos simulados:

| Quadros por segundo | Média | Menor e maior | Extinções |
|---|---|---|---|
| 30 | 262 | 85 – 514 | 0 de 12 |
| 60 | 233 | 84 – 459 | 0 de 12 |
| 90 | 195 | 12 – 550 | 0 de 12 |

**Custo por passo da simulação.** Um quadro tem 16,6 ms.

| Situação | Custo por passo |
|---|---|
| População normal (288 criaturas) | 0,165 ms |
| Pool cheio (2.999 criaturas) | **8,9 ms** |

> [!WARNING]
> Com o pool cheio, o passo usa mais da metade do quadro. A causa é o
> combate: cada criatura procura inimigos entre todas as outras. Em jogo
> normal a população fica bem abaixo disso. Está anotado em
> [dívida técnica](divida-tecnica.md).

**Território:** um recálculo custa 0,284 ms com 403 criaturas e 0,264 ms
com 3.000, num mundo de 49 mil tiles, uma vez por segundo. O custo quase
não depende da população, porque a busca varre os tiles uma vez, não uma
vez por criatura.

<details>
<summary>Por que esses números mudaram tantas vezes</summary>

Os números de população são uma referência de ordem de grandeza. Não são
constantes a defender.

Qualquer mudança que use o sorteador numa ordem diferente muda a história
de cada semente. Por isso eles já foram remedidos várias vezes:

| Momento | 30 / 60 / 90 quadros |
|---|---|
| depois das espécies, com 240 fundadores | 538 / 445 / 403 |
| depois do nome e da cor dos reinos | 532 / 479 / 458 |
| depois dos reinos contíguos e do combate | 218 / 176 / 200 |
| depois da genética (atual) | 262 / 233 / 195 |

A maior queda, entre a segunda e a terceira linha, não é sorteio. É o
combate: a fronteira entre reinos passou a custar vidas.

O custo de 0,090 ms por passo com o pool cheio, citado em versões antigas
deste documento, é de antes do combate.

</details>

---

## O que ainda não foi provado?

- **O APK já abriu num celular Android**, com o mapa e as criaturas na
  tela: são as capturas do README. Isso está provado.
- **`./gradlew desktop:run` não tem registro de execução.** A CI não
  compila o módulo `desktop`.
- **A orientação do mapa não foi comparada com o `WorldPreview`** da
  mesma semente. O jogo sorteia a semente com o relógio, então as capturas
  não servem para isso. Os testes provam que desenho e toque concordam
  entre si.
- **Não há medição de FPS real**, em celular ou no PC.
- **`CameraController.resize()` não tem teste.** A conta de matriz do
  libGDX exige biblioteca nativa, que não carrega nos testes.

<details>
<summary>A lista completa das 95 verificações do <code>SimSelfTest</code></summary>

- Todo o código, incluindo `render/` e os dois launchers, compila com
  `-Xlint:all` sem um único aviso.
- As 95 verificações de `SimSelfTest` passam. Mundo: determinismo por
  semente, sementes diferentes divergindo, todo tile com tipo válido,
  elevação sempre em [0,1], proporção terra/mar jogável em 6 sementes,
  presença de oceano profundo e de montanha, geração em 18 ms. Comida:
  mundo novo na capacidade, rebrota respeitando o teto, consumo limitado ao
  que existe, água sem comida. Pool: contagens coerentes, mil nascimentos
  em dez slots, remoção do meio sem perder ninguém, morte dupla ignorada.
  Facções: ids sequenciais, fundador nasce com um membro, join/leave
  corretos, extinção sem ir negativo, id inexistente lança exceção, a mesma
  semente fundando os mesmos nomes e as mesmas cores, sementes diferentes
  divergindo, nome parecendo nome e não índice, cores opacas e espalhadas
  pelo círculo de matiz, `nameOf`/`colorOf` recusando id inexistente.
  Território: mundo novo sem dono nenhum, uma criatura reivindica tudo que
  alcança, corredor reto divide exatamente na metade, água nunca é
  reivindicada, busca contorna água e resolve empate pela ordem do pool.
  Dano: vida cai e a causa fica registrada, golpe fatal se anuncia e a
  vida para em zero, cadáver não morre duas vezes, dano não positivo
  ignorado, causa não vaza entre duas vidas do mesmo slot, criatura em
  terreno perigoso definha e morre, criatura em terreno normal não, só o
  oceano profundo é perigoso, golpe do jogador fere quem está no tile,
  golpes repetidos matam pelo caminho de morte compartilhado, golpe em
  tile vazio e golpe fora do mundo não fazem nada nem estouram.
  Genética: herdabilidade medida em 5000 casais (inclinação 1,008 sem
  mutação, 1,001 com a mutação padrão), o esquema ingênuo
  `filho = hash(pai, mãe)` medido lado a lado e reprovado (0,020), todo
  bloco do filho vindo inteiro de um dos pais por moeda justa, mesmo par e
  mesmo estado de `Rng` dando o mesmo filho bit a bit, genes do mesmo bloco
  descorrelacionados, e traço aditivo concentrado no meio da faixa.
  Fundação de reinos: o mundo funda o número pedido de facções e não uma
  por fundador, ninguém nasce sem facção, a soma dos membros bate com a
  população, os reinos saem contíguos (mais de 80% com o vizinho mais
  próximo na mesma facção) e a mesma semente funda os mesmos reinos nos
  mesmos lugares.
  Simulação: mesma semente com a mesma história criatura por criatura
  (facção incluída), ninguém saindo do mundo nem pisando na água, fome e
  vida sempre em [0,1], passo gigante cortado, comida caindo com o
  pastoreio, nenhuma facção nova depois da fundação inicial, todo dono de
  território é uma facção que existe, território nunca reivindica água,
  mundo gerado normal sem nenhuma morte por dano de terreno.

- A saída visual do mundo foi conferida no `WorldPreview`: mapas em várias
  sementes, com continentes, cordilheiras com neve no cume, litoral e
  calota polar.

</details>
