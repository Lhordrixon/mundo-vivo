# Mundo Vivo

Jogo de sandbox de deus para Android: um mundo em grade que nasce de uma
semente, com biomas, relevo e — nas etapas seguintes — criaturas e
civilizações autônomas.

Inspirado no gênero que o WorldBox popularizou. **Nenhum código, arte,
som ou texto do WorldBox foi usado**: tudo aqui é implementação original.
Mecânica de jogo não tem proteção de direito autoral; código e arte têm.

---

## Estado atual

| Sistema | Situação |
|---|---|
| 1. Grade de tiles e geração de mundo | pronto e testado |
| 2. Câmera com arraste, pinça e roda do mouse | pronto |
| 3. Criaturas com máquina de estados | pronto e testado |
| 3b. Comida por tile, com rebrota | pronto e testado |
| 3c. Pool de criaturas sem alocação | pronto e testado |
| 4. Facções e território | pronto e testado |
| 5. Save/load | não começou |
| 6. Poderes de deus | não começou |
| 7. IA de guerra | não começou |
| 8. Passo de otimização e medição de FPS | não começou |

O que roda hoje: o app abre, gera um mundo de 256x192 tiles, espalha 120
criaturas pela terra firme e as deixa viver. Elas procuram comida, comem,
procuram parceiro, se reproduzem, envelhecem e morrem — de fome ou de
velhice. A comida cresce de volta conforme a fertilidade do bioma, então a
população cresce onde a terra é boa e míngua onde não é.

A cor de cada criatura mostra o que ela está fazendo: branco vagando,
amarelo procurando comida, verde comendo, rosa procurando parceiro. É o que
torna a simulação legível de relance — dá para ver o amarelo se espalhar
por uma região antes de a população cair ali.

Por baixo, cada criatura também já pertence a uma facção, herdada dos pais,
e o mundo já sabe de quem é cada tile de terra — o território de cada
facção, recalculado algumas vezes por segundo. Isso ainda não aparece na
tela: não há bandeira, nem cor de facção, nem fronteira desenhada. É
território no sentido de dado da simulação, pronto para o sistema de
guerra usar; a parte visual fica para quando `render/` for a vez.

Toque longo descarta o mundo e gera outro — atalho de desenvolvimento, sai
quando a interface de verdade entrar.

---

## Como compilar

Requisitos: JDK 17 ou superior e Android Studio (que já traz o SDK do
Android). Na primeira execução o Gradle baixa o libGDX e o plugin do
Android — precisa de internet.

**Caminho mais curto — rodar no PC, sem celular e sem SDK do Android:**

```bash
./gradlew desktop:run
```

**Gerar o APK de depuração:**

```bash
./gradlew android:assembleDebug
# sai em: android/build/outputs/apk/debug/android-debug.apk
```

**Instalar direto no celular conectado por USB:**

```bash
./gradlew android:installDebug
```

**Rodar os testes:**

```bash
./gradlew core:test
```

Para o APK de release é preciso criar uma chave de assinatura e declará-la
em `android/build.gradle`; sem isso `assembleRelease` gera um APK não
assinado, que o Android recusa instalar.

### Versões fixadas

Ficam todas em `gradle.properties`, em um lugar só:

| Componente | Versão | Por quê |
|---|---|---|
| libGDX | 1.14.2 | versão estável atual |
| Android Gradle Plugin | 8.13.0 | exige Gradle 8.13+ e JDK 17+ |
| Gradle (wrapper) | 8.14.3 | satisfaz o mínimo do AGP |
| compileSdk / targetSdk | 36 | máximo suportado pelo AGP 8.13 |
| minSdk | 26 | Android 8; permite ícone adaptativo em XML puro |
| Java (fonte e destino) | 17 | sem backend HTML no projeto, não há motivo para ficar no 8 |

O Android Studio pode oferecer atualizar o AGP para a série 9.x. Funciona,
mas é uma versão com mudanças incompatíveis no DSL — se aceitar, faça em um
commit isolado para poder voltar atrás.

---

## Arquitetura

```
core/
  sim/         simulação pura — nenhuma linha de libGDX
    Simulation.java  o laço de vida: comida, criaturas, tempo
    noise/     ruído fractal determinístico
    world/     tipos de tile, configuração, mundo, gerador
    creature/  criatura, estados, pool, parâmetros
    ecology/   comida por tile e rebrota
    faction/   registro de facções e território por proximidade
    util/      RNG determinístico
  render/      desenho e câmera — a única parte que conhece libGDX
android/       launcher e empacotamento do APK
desktop/       launcher para rodar no PC
tools/         ferramentas de apoio em Java puro
```

### As quatro decisões que sustentam o resto

**A simulação não conhece a biblioteca gráfica.** Nada em `sim/` importa
libGDX. Consequência prática: dá para gerar e testar um mundo inteiro sem
abrir tela, sem emulador e sem SDK do Android — foi assim que o gerador foi
calibrado. Se um dia o libGDX for trocado, `sim/` não muda.

**O mundo vive em arrays planos, não em objetos Tile.** Um mundo grande
teria 110 mil objetos espalhados pela heap. São dois blocos contíguos,
cerca de 550 KB, percorridos em ordem de memória. É o que permite varrer o
mundo por frame sem pressionar o coletor de lixo — condição para as
unidades autônomas que entram no sistema 3.

**O mundo é desenhado como uma textura, não como 49 mil sprites.** Um
pixel por tile em um `Pixmap`, enviado à GPU, desenhado como um quad só,
com filtro `Nearest`. Uma chamada de desenho por frame em vez de dezenas de
milhares. O preço é que mudar o terreno exige reenviar a textura — por isso
existe o controle de "sujo", que faz a transferência acontecer só nos
frames em que algo mudou de fato.

**Tudo é determinístico a partir da semente.** O RNG é SplitMix64 próprio e
o ruído sai de hash inteiro, não de `java.util.Random` nem de tabela
sorteada em tempo de execução. Mesma semente, mesmo mundo, em qualquer
aparelho. Sem isso não há teste de regressão possível e o save teria que
guardar o mapa inteiro em vez de um número.

### Como o mundo é gerado

Duas passagens sobre a grade:

1. Ruído fractal preenche a elevação e registra o menor e o maior valor.
2. A elevação é reescalada para ocupar [0,1] inteiro, recebe curva de
   contraste, cristas de montanha e afundamento de borda; em seguida
   temperatura (latitude, perturbada por ruído, derrubada pela altitude) e
   umidade (segundo campo de ruído) classificam cada tile.

A normalização entre as duas passagens não é detalhe: o fBm é uma soma de
oitavas e concentra seus valores no meio da faixa, quase nunca chegando
perto de 0 ou de 1. Sem reescalar, nenhum ponto alcança altura de montanha
e o mundo sai inteiramente plano — foi exatamente o que aconteceu na
primeira versão deste gerador.

Separar clima de bioma (em vez de pintar bioma direto da altura) é o que
faz aparecer deserto ao lado de selva na mesma latitude, em vez de faixas
horizontais uniformes.

### Como as criaturas se comportam

Cada criatura tem fome, vida, idade e um estado. A cada passo a fome sobe;
acima de um limiar ela larga o que estiver fazendo e procura comida; com a
fome no máximo começa a perder vida. Saciada, adulta e fora do período de
espera, procura parceiro. A reprodução custa fome aos dois pais — é esse
custo que amarra a população à comida disponível em vez de deixá-la crescer
até bater no teto.

Três números foram descobertos medindo, não escolhendo:

**O raio de busca por parceiro é cinco vezes o da comida.** Com os dois
iguais, uma população que afina deixa de se encontrar e entra em espiral de
extinção mesmo com comida sobrando — um terço das sementes testadas morria
assim. Ampliar o raio de parceiro é de graça, porque essa busca já percorre
a lista de criaturas vivas e o raio só filtra o resultado; ampliar o de
comida custaria varredura de tiles.

**O limiar de "tile comestível" precisa ser baixo.** Com ele alto, a
escassez vira um precipício: um tile com comida logo abaixo do limiar é
invisível, então a comida acaba para todo mundo ao mesmo tempo e a
população colapsa junto. Baixo, a escassez chega devagar e a população
oscila em vez de despencar.

**A espera entre reproduções domina a estabilidade.** Com espera curta a
população multiplica antes de a comida responder, estoura o mapa e morre de
fome inteira. O ciclo de explosão e colapso sumiu ao dobrar a espera.

Uma observação que vale registrar porque contraria a intuição: no equilíbrio
atual as criaturas quase não morrem de fome. Em dez minutos simulados foram
527 mortes de velhice contra 28 de inanição. A comida limita a população
pela **natalidade**, não pela mortalidade — quem está com fome acima do
limiar simplesmente não reproduz. É um regime mais estável que o da fome
matando em massa, e é bom saber disso antes de mexer nos números: baixar a
rebrota não vai matar mais criaturas, vai fazer nascerem menos.

### Como funcionam facções e território

Toda criatura pertence a uma facção. A população inicial funda uma facção
para cada indivíduo — sem pais para herdar de quem, cada fundador começa a
sua própria linhagem. Toda reprodução daí em diante herda: o filho puxa a
facção de um dos dois pais, sorteado com metade de chance cada. Na prática
os dois pais quase sempre já são da mesma facção — a busca por parceiro
tende a achar vizinhos, e vizinhos descendem de gente próxima —, mas nada
impede um casal de facções diferentes, e não existe um jeito óbvio de
"misturar" dois ids em um terceiro. Nenhuma facção nova é fundada depois do
povoamento inicial: `FactionRegistry.factionCount()` fica constante para o
resto do jogo, e todo mundo remonta a um dos fundadores — é isso que o
teste `noNewFactionsAfterInitialFounding` trava.

Território é o conjunto de tiles mais próximos dos membros vivos de cada
facção — mas "mais próximo" aqui é distância percorrida por tiles
caminháveis vizinhos, não linha reta. Duas criaturas nos dois lados de uma
baía não deviam dividir a água ao meio: cada uma levaria muito tempo para
contornar até essa fronteira "mais próxima" corresponder a alguma coisa no
chão. Contar passos pelo grafo de tiles caminháveis resolve isso de graça —
a água nunca é atravessada, então o território de cada lado cresce
contornando a baía, do jeito que uma criatura realmente andaria.

O cálculo é uma busca em largura multi-fonte: todos os membros vivos de
todas as facções entram na fila ao mesmo tempo, e o primeiro a alcançar um
tile decide o dono. Isso é O(tiles), não O(tiles × criaturas) — a diferença
entre varrer o mundo padrão uma vez (49 mil operações) e varrê-lo uma vez
por criatura viva (algumas dezenas de milhões, com a população de
meio-jogo). Os arrays de apoio são pré-alocados no tamanho do mundo e
reaproveitados a cada recálculo, então recalcular não aloca memória nova.
Tiles de água nunca são alcançados e ficam sem dono, e um empate exato de
distância (acontece nas pontas de uma baía contornada dos dois lados) vai
para quem apareceu primeiro na lista de criaturas vivas — arbitrário, mas
determinístico, que é o que importa.

Recalcular o mundo inteiro é barato (0,33 ms com pouco mais de 200
criaturas, medido em `SimSelfTest`), mas ainda assim não há motivo para
pagar isso a cada quadro: uma fronteira de território não precisa reagir em
16 ms a uma criatura que andou meio tile. `Simulation` recalcula uma vez
por segundo (`TERRITORY_RECOMPUTE_INTERVAL_SECONDS`), a mesma lógica que já
existia para não recalcular coisa cara todo frame sem necessidade.

Duas coisas que este sistema **não** faz, de propósito, porque não foram
pedidas ainda: território não influencia nenhuma decisão de criatura hoje —
ninguém evita terra de outra facção, ninguém briga por fronteira — e não há
nada desenhado na tela. É dado de simulação, testado sem abrir janela
nenhuma, esperando o sistema 7 (guerra) para importar de verdade e o
`render/` para virar mapa colorido.

---

## Ferramentas de apoio

Duas ferramentas em Java puro, sem Gradle e sem baixar dependência:

```bash
# compile a simulação uma vez
javac -d build/sim $(find core/src/main/java/com/emannuel/mundovivo/sim -name '*.java')
javac -d build/tools -cp build/sim tools/*.java

# gere um PNG do mundo da semente 12345, com 3 px por tile
java -cp build/sim:build/tools WorldPreview 12345 3 previa.png

# acompanhe a população por 5 minutos simulados
java -cp build/sim:build/tools SimulationReport 12345 5

# rode a verificação inteira
java -cp build/sim:build/tools SimSelfTest
```

`WorldPreview` encurta o ciclo de calibragem de minutos para segundos:
mexeu em `WorldConfig`, roda e olha o PNG. `SimulationReport` faz o mesmo
para `CreatureConfig`: mostra a população, os nascimentos, as mortes por
fome e por velhice e o custo por passo ao longo do tempo — foi com ele que
o ciclo de explosão e colapso apareceu. `SimSelfTest` cobre as mesmas
invariantes da suíte JUnit, mas roda em qualquer máquina com JDK.

---

## O que foi verificado, e o que não foi

Vale a pena ser exato aqui, para você não descobrir na hora errada.

**Verificado de fato:**

- Todo o código, incluindo `render/` e os dois launchers, compila com
  `-Xlint:all` sem um único aviso.
- As 55 verificações de `SimSelfTest` passam. Mundo: determinismo por
  semente, sementes diferentes divergindo, todo tile com tipo válido,
  elevação sempre em [0,1], proporção terra/mar jogável em 6 sementes,
  presença de oceano profundo e de montanha, geração em 18 ms. Comida:
  mundo novo na capacidade, rebrota respeitando o teto, consumo limitado ao
  que existe, água sem comida. Pool: contagens coerentes, mil nascimentos
  em dez slots, remoção do meio sem perder ninguém, morte dupla ignorada.
  Facções: ids sequenciais, fundador nasce com um membro, join/leave
  corretos, extinção sem ir negativo, id inexistente lança exceção.
  Território: mundo novo sem dono nenhum, uma criatura reivindica tudo que
  alcança, corredor reto divide exatamente na metade, água nunca é
  reivindicada, busca contorna água e resolve empate pela ordem do pool.
  Simulação: mesma semente com a mesma história criatura por criatura
  (facção incluída), ninguém saindo do mundo nem pisando na água, fome e
  vida sempre em [0,1], passo gigante cortado, comida caindo com o
  pastoreio, nenhuma facção nova depois da fundação inicial, todo dono de
  território é uma facção que existe, território nunca reivindica água.
- A saída visual do mundo foi conferida: mapas em várias sementes, com
  continentes, cordilheiras com neve no cume, litoral e calota polar.
- Comportamento da população: 12 sementes rodadas por 30 minutos
  simulados, **nenhuma extinção e nenhuma batida no teto do pool**,
  populações finais entre 334 e 1082.
- Estabilidade por taxa de quadros: 10 sementes a 20 minutos, população
  média de 494, 432 e 463 a 30, 60 e 90 quadros por segundo — dentro da
  variação entre sementes. A 20 quadros há desvio para cima (706), porque
  com passos grossos cada visita a um tile rende uma mordida maior.
- Custo de um passo: 0,04 ms com ~200 criaturas e 0,086 ms com 2100, em um
  quadro que tem 16,6 ms. A simulação não é o gargalo.
- Custo de um recálculo de território: 0,33 ms com pouco mais de 200
  criaturas em um mundo padrão (49 mil tiles) — bem abaixo do segundo
  inteiro de folga que o intervalo de recálculo dá.

**Não verificado:**

- O build do Gradle nunca rodou, nem o do Android. O ambiente onde este
  projeto foi montado não tem acesso ao Maven Central nem ao repositório do
  Google, então nem o libGDX nem o plugin do Android puderam ser baixados.
- A camada `render/` compilou contra *stubs* da API do libGDX escritos à
  mão, não contra o libGDX real. Isso pega erro de sintaxe, import faltando
  e método de interface não implementado — mas **não** garante que as
  assinaturas batem com as do libGDX 1.14.2.
- O jogo nunca foi executado. Não há medição de FPS real, nem confirmação
  de que o `flipY` da textura deixa o mapa na orientação certa na tela — e
  a mesma dúvida vale para a posição das criaturas, que usam a mesma
  inversão de eixo.
- A suíte JUnit nunca rodou (JUnit não pôde ser baixado). O que rodou foi o
  `SimSelfTest`, que cobre as mesmas invariantes em Java puro.

Tradução prática: a simulação inteira está testada e funcionando; a camada
gráfica está escrita com cuidado mas não foi provada. O primeiro
`./gradlew desktop:run` é o teste que falta, e é onde eventual divergência
de assinatura vai aparecer.

---

## Dívida técnica conhecida

Registrada de propósito, para não virar surpresa:

- **Reenvio de textura inteiro.** Ao mudar um tile, o `Pixmap` todo sobe
  para a GPU (196 KB no mundo padrão), não só a região alterada. Aceitável
  enquanto as alterações são esporádicas; vira gargalo quando os poderes de
  deus pintarem terreno continuamente. A correção é acumular a região suja
  e subir só ela.
- **A busca por parceiro percorre todas as criaturas vivas.** Hoje é barato
  porque só quem está procurando parceiro varre, e a maioria não está. Com
  alguns milhares de criaturas vira quadrático. A correção é indexar as
  criaturas em uma grade espacial; o lugar é `Simulation.resolveMate`.
- **A rebrota percorre o mapa inteiro a cada passo.** 49 mil tiles por
  passo, custo baixo mas desnecessário. A correção é crescer uma fatia do
  mapa por quadro com o `dt` multiplicado.
- **O teto do pool não é um limitador de população saudável.** Atingi-lo
  bloqueia nascimentos mas não mortes, e a população entra em declínio em
  vez de estabilizar. Medido: com o teto em 250, a população de uma semente
  que normalmente vive foi a zero. O teto existe como limite de memória; a
  comida é que deve limitar. Se um dia o teto passar a ser alcançado em
  jogo, ele precisa virar um limite suave.
- **Criaturas andam em linha reta.** Não há busca de caminho: quando o
  passo seguinte cairia na água, ele é recusado e a criatura escolhe outro
  destino. Funciona, mas uma criatura pode levar tempo para contornar uma
  baía. O sistema de território não resolve isto: o BFS de `Territory` só
  decide de quem é cada tile, não guia ninguém — o movimento continua sem
  busca de caminho nenhuma.
- **Recálculo de território é sempre do mundo inteiro.** Uma vez por
  segundo, os 49 mil tiles são todos revisitados, mesmo quando quase
  nenhuma criatura se moveu desde o recálculo anterior — o mesmo tipo de
  desperdício já anotado para a rebrota de comida, e a mesma correção
  serviria: recalcular uma fatia por vez.
- **Território não influencia decisão nenhuma de criatura.** Hoje é
  informação pura: ninguém evita terra de outra facção, ninguém disputa
  fronteira. É o levantamento que falta antes de o sistema 7 (guerra) ter
  do que se alimentar.
- **Herança de facção num casal de facções diferentes é uma moeda cara.**
  Metade de chance para cada pai, sem meio-termo. Funciona porque hoje isso
  quase nunca acontece — vizinhos tendem a ser parentes —, mas quando a
  guerra ou fronteiras fechadas entrarem, pode valer a pena decidir se um
  casal assim deveria sequer poder reproduzir.
- **Temperatura e umidade são descartadas após a geração.** Só a elevação
  fica guardada. Quando o crescimento de vegetação ou a migração sazonal
  entrarem, elas terão que ser recalculadas ou armazenadas.
- **Sem save/load.** `World`, `FoodMap` e `Rng` já expõem o estado bruto
  pensando nisso, e a semente já é guardada, mas não há serialização.
- **Toque longo regenera o mundo.** Comportamento de desenvolvimento, vai
  colidir com gestos de jogo mais adiante.
- **`minifyEnabled` desligado no release.** As regras do ProGuard já estão
  escritas para quando for ligado, mas ligar sem testar quebra o app na
  abertura, não na compilação.
- **Ícone provisório.** Vetor desenhado à mão, sem identidade visual.

---

## Próximo passo

**Antes de qualquer código novo: rodar `./gradlew desktop:run`.** É a única
parte do projeto que nunca foi provada, e continuar empilhando sistemas
sobre uma camada gráfica não verificada só aumenta o tamanho do estrago se
algo lá estiver errado. Isso ainda vale depois do sistema 4: facções e
território são só `sim/`, não tocaram em `render/`, e essa dívida não foi
paga por eles.

Depois disso, sistema 5: save/load. `World`, `FoodMap`, `CreaturePool` e
agora `FactionRegistry`/`Territory` guardam estado simples o bastante para
serializar; falta decidir o formato e escrever o carregamento — e conferir
que um mundo recarregado recalcula o mesmo território que tinha antes de
salvar, já que ele deriva da posição das criaturas em vez de ser salvo
como tal.

O sistema 7 (guerra) segue dependendo do território para ter uma fronteira
para disputar — agora existe; falta a IA que decida atacá-la.
