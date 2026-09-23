# Mundo Vivo

> **[Roadmap →](docs/roadmap.md)** — o que existe, o que falta e onde
> cada coisa está no código.

Jogo de sandbox de deus para Android: um mundo em grade que nasce de uma
semente, com biomas, relevo e criaturas autônomas que vivem, se reproduzem
e brigam entre reinos.

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
| 4b. Nome e cor por facção | pronto e testado, sem consumidor na tela |
| 4c. Fundação de reinos contíguos | pronto e testado |
| 4d. Genoma de blocos, herdável | pronto e testado |
| 5. Save/load | não começou |
| 6. Poderes de deus | não começou |
| 7. IA de guerra | não começou |
| 8. Passo de otimização e medição de FPS | não começou |
| 9. Dano externo e terreno perigoso | pronto e testado |
| 10. Primeiro poder do jogador: toque que fere | pronto e testado |
| 11. Espécie herdável, com acasalamento restrito | pronto e testado |
| 12. Combate corpo a corpo entre reinos | pronto e testado |

O que roda hoje: o app abre, gera um mundo de 256x192 tiles, espalha 240
criaturas pela terra firme e as deixa viver. Elas procuram comida, comem,
procuram parceiro, se reproduzem, envelhecem e morrem — de fome, de
velhice ou em combate. A comida cresce de volta conforme a fertilidade do bioma, então a
população cresce onde a terra é boa e míngua onde não é.

A cor de cada criatura mostra o que ela está fazendo: branco vagando,
amarelo procurando comida, verde comendo, rosa procurando parceiro. É o que
torna a simulação legível de relance — dá para ver o amarelo se espalhar
por uma região antes de a população cair ali.

Por baixo, cada criatura também já pertence a um de quatro reinos, herdado
dos pais, e criaturas de reinos diferentes se ferem quando ficam próximas.
O mundo também já sabe de quem é cada tile de terra — o território de cada
facção, recalculado uma vez por segundo. Isso ainda não aparece na
tela: não há bandeira, nem cor de facção, nem fronteira desenhada. É
território no sentido de dado da simulação, pronto para o sistema de
guerra usar; a parte visual fica para quando `render/` for a vez.

Toque simples fere a criatura tocada: é o primeiro poder do jogador e a
primeira vez que um gesto muda a simulação em vez de só mover a câmera.
Três toques matam. Não há seleção de poder, espera nem custo — é um poder
só, escolhido para provar o caminho do dedo até a criatura antes de
existir interface para escolher outro.

Toque longo descarta o mundo e gera outro — atalho de desenvolvimento, sai
quando a interface de verdade entrar.

---

## Como compilar

Requisitos: JDK 17 ou superior e Android Studio (que já traz o SDK do
Android). Na primeira execução o Gradle baixa o libGDX e o plugin do
Android — precisa de internet.

**Caminho mais curto — rodar no PC, sem celular.** Precisa do SDK do
Android instalado (vem com o Android Studio), porque o Gradle carrega o
plugin do Android até para o módulo `desktop`:

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
               (menos TileMapping: aritmética pura, para ser testável)
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

Uma observação que vale registrar porque contraria a intuição: as criaturas
quase não morrem de fome. A comida limita a população pela **natalidade**,
não pela mortalidade — quem está com fome acima do limiar simplesmente não
reproduz. É um regime mais estável que o da fome matando em massa, e é bom
saber disso antes de mexer nos números: baixar a rebrota não vai matar mais
criaturas, vai fazer nascerem menos.

**A espécie quase tirou o mundo desse regime, e o número de fundadores o
trouxe de volta.** Com 120 fundadores e duas espécies, sobrava comida no
mapa (99% intocada) e a população definhava mesmo assim: o gargalo tinha
virado encontrar parceiro. Com 240, a comida volta a ser consumida — cai
para 66–77% e oscila — e as mortes por fome voltam a existir (679 e 1086 em
quarenta minutos, contra 19 antes). A história está na seção sobre
espécies, logo abaixo, e é a coisa mais importante a saber antes de mexer
em qualquer parâmetro de população.

### Como as espécies se separam

Toda criatura pertence a uma espécie. A população inicial sorteia a sua; daí
em diante ninguém mais escolhe — todo nascimento herda a dos pais. E os dois
pais têm sempre a mesma, porque a busca por parceiro passou a exigir espécie
igual: duas criaturas de espécies diferentes nunca formam par, por mais
próximas e disponíveis que estejam. É por isso que o filho herda sem sorteio,
diferente da facção, onde o sorteio existe justamente porque os pais podem
divergir.

São duas espécies, com nomes deliberadamente sem graça (`ALPHA` e `BETA`).
Elas não têm atributo, dieta, velocidade nem hostilidade própria, e não
aparecem na tela. A única coisa que uma espécie faz hoje é decidir quem pode
ter filho com quem. Nome evocativo prometeria predação e cultura que o código
não tem — e este projeto já pagou caro por promessa não cumprida.

**A espécie custou caro antes de ser paga, e a conta vale ser lida por
inteiro** — porque é ela que explica por que o mundo nasce com 240
criaturas e não com 120.

Separar a reprodução em duas espécies reduziu a população de equilíbrio em
cerca de sete vezes. Medido isolando a causa: mesma semente, mesmo
sorteador, mesmo código, mudando só se o filtro barra alguém.

| | população média a 20 min | pior semente |
|---|---|---|
| Uma espécie (filtro inerte) | 470 | 63 |
| Duas espécies, 120 fundadores | 63 | 2 |

Essa tabela é a medição de isolamento original, feita antes de as facções
ganharem nome e cor. O nome e a cor gastam um sorteio por fundação, então a
sequência inteira andou e cada semente conta outra história desde então — o
braço "uma espécie" não foi remedido, porque reproduzi-lo exige desligar o
filtro no código. Os números atuais das duas espécies estão na tabela
abaixo. A conclusão não depende de qual das duas medições se olhe: o filtro
de espécie derruba a população em cerca de uma ordem de grandeza, e é isso
que os 240 fundadores pagam.

O mecanismo não é o raio de busca: alargá-lo de 70 para 105, 140 ou 175
tiles não recupera nada (médias de 70, 55 e 49, e extinções aparecendo nos
raios maiores). O gargalo é outro — a qualquer instante só um punhado de
criaturas está em `SEEKING_MATE` ao mesmo tempo, e exigir espécie igual
corta esse punhado ao meio. Elas não estão longe demais; estão procurando
em momentos diferentes.

**A correção foi dobrar os fundadores, e só isso.** Se o problema é
densidade de candidatos simultâneos, o conserto honesto é devolver a
densidade — não alargar o raio (não funciona) nem encurtar a espera entre
reproduções, que exagera para o outro lado (média 927, contra os ~470 de
antes). Com `initialPopulation` em 240, cada espécie volta a ter a
densidade que a população inteira tinha antes. Medido em 12 sementes, 20
minutos simulados:

| quadros por segundo | 120 fundadores | 240 fundadores |
|---|---|---|
| 30 | média 142, 0 extinções | **média 532**, 0 extinções (254–1023) |
| 60 | média 63, 0 extinções | **média 479**, 0 extinções (285–742) |
| 90 | média 66, 0 extinções | **média 458**, 0 extinções (271–853) |

Nenhuma das 36 execuções com 240 fundadores extinguiu, nenhuma bateu no
teto do pool, e a média a 60 quadros (479) ficou onde estava antes de
existir espécie (470). A dispersão entre taxas também voltou a ser
aceitável: 532/479/458, contra 142/63/66 do mundo de 120.

**Estes números foram remedidos depois que as facções ganharam nome e
cor,** e mudaram um pouco: fundar uma facção agora gasta um sorteio, 240
deles no início do mundo, então a sequência inteira anda para frente e cada
semente conta outra história. As médias com 240 fundadores saíram de
538/445/403 para 532/479/458 — mesma ordem de grandeza, mesma conclusão.
O que mudou de verdade foi o retrato dos 120: naquela medição três
execuções extinguiam (as sementes 99 e 4242), e nesta nenhuma extingue, com
médias de 142, 63 e 66. **Isso não quer dizer que 120 fundadores ficaram
seguros** — quer dizer que a extinção ali sempre foi questão de sorteio, e
que a diferença que sustenta a decisão continua sendo a mesma: uma ordem de
grandeza entre 63 e 479 sobreviventes a 60 quadros.

E o regime voltou junto. Com 120 fundadores a comida ficava intocada (99%
do total ao fim de 40 minutos) enquanto a população definhava — sinal de
que o gargalo não era comida. Com 240 a comida é consumida de novo, cai
para 66–77% e oscila, e as mortes por fome voltam a aparecer: 679 e 1086 em
quarenta minutos, contra 19 antes. O mundo voltou a ser limitado por
comida, pela natalidade, que é o regime que este projeto quer.

**O preço do conserto, na época:** cada fundador fundava a sua própria
facção, então dobrar fundadores dobrou as facções iniciais — 240 em vez de
120. Isso foi conferido antes de aplicar: nem o `FactionRegistry` nem o
`Territory` têm teto ou suposição de contagem, e as 240 facções nasciam
todas com um membro, 237 delas com território, zero tiles com dono
inválido. **Isso deixou de valer:** hoje o mundo funda 4 reinos contíguos e
reparte os 240 fundadores entre eles (ver "Por que poucos reinos contíguos"
abaixo).

### Como a herança funciona

Toda criatura carrega um genoma: oito blocos de 32 bits. Cada bloco não é
um gene — é a semente de muitos. `Genome.gene(bloco, índice)` expande um
bloco em quantos valores se quiser, por hash puro, então 32 bytes viram um
número ilimitado de genes sem ocupar memória nenhuma.

No nascimento, `Inheritance.cross` dá ao filho cada bloco **inteiro** de um
dos dois pais, por moeda justa, e `Inheritance.mutate` inverte um bit com
2% de chance por bloco. `Phenotype` traduz o genoma em traços.

#### Por que blocos, e não uma semente por criatura

A alternativa óbvia é guardar uma semente só e fazer
`filho = hash(pai, mãe)`. É mais simples, e está errada — de um jeito que
não aparece olhando o código rodar.

O hash de dois pais é um valor descorrelacionado dos dois. Dois pais
grandes gerariam um filho pequeno com a mesma probabilidade de qualquer
outro. Sem correlação entre pai e filho não existe herdabilidade, e **sem
herdabilidade a seleção natural não seleciona nada**: nascer bem-adaptado
deixa de aumentar a chance de ter filhos bem-adaptados. O que sobra é
deriva aleatória com aparência de evolução, que é o pior resultado
possível — parece que funciona, os números se mexem, e não há nada ali.

Herdar blocos inteiros conserta isso, porque o bloco chega intacto e tudo
que ele codifica chega junto. De brinde vem a ligação gênica: genes do
mesmo bloco viajam juntos de geração em geração, que é como funciona na
natureza.

**O contraste está medido, não argumentado.** `HeritabilityTest` roda os
dois esquemas lado a lado, com 5000 casais cada, e faz a regressão do traço
do filho sobre a média dos pais:

| esquema | inclinação |
|---|---|
| blocos, sem mutação | **1,008** |
| blocos, mutação de 2% | **1,001** |
| blocos, mutação de 20% | 0,810 |
| `filho = hash(pai, mãe)` | **0,020** |

Inclinação 1 significa que o filho é, em média, exatamente a média dos
pais. Inclinação 0 significa que saber os pais não diz nada. A diferença
entre os dois esquemas não é de grau — é entre ter herança e não ter.

#### Traços aditivos, e por que nunca um gene só

Cada traço é a média de doze genes espalhados por quatro blocos. Um traço
de gene único seria um degrau: ou o filho herdou aquele bloco e tem o valor
do pai, ou herdou o outro e tem o da mãe. A média dos pais não preveria
nada, e a seleção só conseguiria mover a população aos saltos. Somando doze
genes, o traço vira contínuo e o filho cai perto da média dos pais.

O teorema central do limite entra de graça: a média de doze uniformes se
concentra no meio da faixa, então a maioria nasce mediana e os extremos são
raros — o formato certo para uma população.

Os traços **multiplicam** os valores base do `CreatureConfig`, numa faixa
estreita de 0,7× a 1,3×, e na prática quase toda criatura fica entre 0,85×
e 1,15×. A faixa é estreita de propósito: o equilíbrio populacional deste
jogo foi calibrado com medição, e genética não é desculpa para
desregulá-lo. Hoje o genoma decide velocidade, alcance de visão,
metabolismo e idade máxima. `size` é calculado e ainda não tem consumidor —
está declarado aqui para não virar o `WorldRenderer.tileX` da próxima
safra.

#### E a seleção, já acontece?

Herdabilidade é a condição para a seleção existir, não a prova de que ela
está agindo. Medindo a média dos traços no instante zero e depois de 20
minutos simulados, em três sementes:

| traço | 12345 | 2026 | 777 |
|---|---|---|---|
| metabolismo | −1,4% | +1,4% | −2,2% |
| idade máxima | +2,5% | +0,2% | +3,0% |
| visão | +0,2% | +2,2% | −2,4% |

Metabolismo e visão andam para os dois lados: em vinte minutos, que dá
umas sete gerações, o que se vê neles é deriva. **Idade máxima sobe nas
três**, que é o esperado — é o traço com a ligação mais direta com o
número de filhos, porque viver mais é ter mais tempo de reproduzir.

Três sementes é pouco para afirmar seleção com confiança, e a direção
consistente pode ser coincidência (1 em 8, se fosse moeda). O que se pode
dizer sem exagero: o mecanismo está de pé e o traço mais acoplado à
aptidão é o único que se move de forma consistente. Demonstrar seleção de
verdade pede corridas mais longas, e isso é outra fatia.

#### Alocação zero

Nenhum `new` por criatura, por nascimento ou por quadro. O `int[8]` de cada
criatura nasce no construtor do pool, junto com ela; `cross`, `mutate` e
`random` escrevem no array que recebem e não devolvem nada; `Phenotype`
escreve em campos primitivos. `Simulation` tem um único genoma de rascunho,
alocado uma vez, que carrega a herança da montagem até o recém-nascido.

### Como a vida é tirada

Até o sistema 4, a única linha do jogo que reduzia vida era a da fome, e
ela mexia no campo direto. Isso não escala: combate, desastre, veneno e
queda iam todos querer o mesmo, e cada um escreveria a sua própria
subtração e o seu próprio jeito de matar. Agora existe uma porta só —
`Creature.applyDamage(quanto, causa)` — e a fome passa por ela como
qualquer outra coisa.

O método tira vida, para em zero, registra a causa e devolve se **aquele**
golpe foi o fatal. O que ele não faz é matar: morrer envolve virar comida
no tile, sair da facção e devolver o slot ao pool, e nada disso mora no
pacote `creature`, que não conhece mundo, comida nem facção. Quem trata a
morte continua sendo `Simulation`, no mesmo ponto onde já tratava a morte
por fome — existe **um** caminho de morte, e a causa só decide em qual
contador a morte é lançada. Uma segunda pancada em quem já está com a vida
zerada devolve `false`, que é o que impede combate e terreno de contarem a
mesma morte duas vezes.

A causa é `String` livre em vez de enum fechado porque combate vai querer
dizer de quem levou a pancada, não só que levou; para as causas da própria
simulação existem constantes, que é o que evita erro de digitação virar
estatística errada.

O primeiro usuário dessa porta é o terreno. `TileType` ganhou uma coluna
`perigoso`, e só o **oceano profundo** está marcado: é a água mais funda
que existe no jogo, não há lava nem terreno extremo, e água rasa e oceano
comum não deveriam matar por contato. Montanha e pico nevado seguem
intocados de propósito — são parede, não armadilha.

Em jogo normal isso nunca dispara: o movimento recusa terreno não
caminhável, então ninguém entra andando no oceano profundo, e há um teste
que trava exatamente isso (dez minutos simulados, zero mortes por dano de
terreno). O caso que ele existe para cobrir é o chão mudar **debaixo** de
alguém — um poder de deus afundando a terra, um desastre alagando o vale —
e é por isso que os testes de dano constroem o mundo à mão e afundam o
terreno no meio da simulação, em vez de esperar que uma criatura faça
besteira sozinha.

Uma consequência que vale registrar: quem se afoga em tile que também
virou água no mapa de comida não deixa cadáver aproveitável, porque
`FoodMap.deposit` respeita a capacidade do tile e água tem capacidade
zero. O corpo afunda. É regra antiga do `FoodMap`, não do dano, mas só
agora ficou alcançável.

### Como o toque do jogador chega na criatura

O primeiro gesto que muda o mundo em vez de mover a câmera. Toque simples
fere quem estiver no tile tocado; três toques matam. Nada além disso: sem
barra de poderes, sem escolher qual poder, sem espera nem custo. Um poder
só, de propósito — o que estava faltando não era variedade, era a ligação.

O caminho tem quatro trechos, e cada um pertence a uma camada diferente:

| Trecho | Quem faz | Testável sem tela? |
|---|---|---|
| dedo → coordenada de tela | `GestureDetector` do libGDX | não, e não é nosso |
| tela → coordenada de mundo | `camera.unproject` | não: depende de `Gdx.graphics` |
| mundo → tile | `TileMapping` | **sim** |
| tile → criatura ferida | `Simulation.strikeAt` | **sim** |

`CameraController` ganhou um `onTap` no mesmo molde do `onLongPress` que já
existia, e entrega a coordenada crua, em pixels de tela. Ele move câmera e
não sabe o que existe no mundo; quem junta câmera, renderizador e simulação
é o `MundoVivoGame`, que já tinha os três na mão.

**Por que `TileMapping` saiu de dentro do `WorldRenderer`.** A conta é
aritmética e não toca em nada gráfico, mas o `WorldRenderer` carrega um
`Pixmap` e uma `Texture` no construtor, que exigem biblioteca nativa e
contexto de vídeo — enquanto a conta morasse lá, nenhum teste a alcançava
sem abrir uma janela, e a orientação do mapa seguia sendo a dívida que o
próprio README registrava. `WorldRenderer.tileX` e `tileY` continuam
existindo e continuam sendo o que o jogo chama; passaram a delegar.

O teste que interessa não confere a fórmula contra ela mesma: ele pega a
coordenada onde o `CreatureRenderer` desenha uma criatura e converte de
volta, linha por linha, exigindo que volte ao mesmo tile. Se desenho e
toque discordarem, o jogador mira em um bicho e acerta outro — e esse é
exatamente o erro que uma conferência de fórmula isolada deixaria passar.

O golpe não inventa caminho de morte: chama o mesmo `applyDamage` que fome
e terreno usam, com causa própria (`CAUSE_PLAYER_STRIKE`), e a morte sai
pelo mesmo ponto de sempre — cadáver vira comida, sai da facção, slot volta
ao pool. Tocar no mar, no vazio ou fora do mundo não faz nada e não é erro:
errar o alvo é parte de mirar.

### Como funcionam facções e território

Toda criatura pertence a uma facção. O mundo funda um punhado de reinos
(`CreatureConfig.initialFactions`, hoje 4) e reparte os fundadores entre
eles por proximidade. Toda reprodução daí em diante herda: o filho puxa a
facção de um dos dois pais, sorteado com metade de chance cada. Na prática
os dois pais quase sempre já são da mesma facção — a busca por parceiro
tende a achar vizinhos, e vizinhos agora nascem compatriotas —, mas nada
impede um casal de facções diferentes, e não existe um jeito óbvio de
"misturar" dois ids em um terceiro. Nenhuma facção nova é fundada depois do
povoamento inicial: `FactionRegistry.factionCount()` fica constante para o
resto do jogo, e todo mundo remonta a um dos fundadores — é isso que o
teste `noNewFactionsAfterInitialFounding` trava.

#### Por que poucos reinos contíguos, e não um por fundador

Até aqui cada fundador fundava a própria facção: 240 reinos de um membro,
salpicados pelo mapa. Parecia inofensivo — território era só informação, e
nada lia facção para decidir coisa nenhuma. Mas isso escondia que a
estrutura não significava nada: **o vizinho mais próximo de alguém quase
nunca era compatriota**, então "meu povo" não existia como região, só como
uma etiqueta por indivíduo.

A conta chegou quando o combate corpo a corpo entrou. Com todo vizinho
sendo estrangeiro, qualquer encontro virava briga, e as seis sementes
testadas extinguiam. Medido, ficou claro que o problema não era o combate
estar forte: era a fundação estar errada desde o começo.

`Simulation.placeFactionCentres` agora escolhe uma capital por reino e cada
fundador entra na facção da capital mais próxima:

- **Capitais bem espaçadas.** Para cada centro, sorteia 12 tiles
  caminháveis e fica com o mais distante dos centros já escolhidos — a
  heurística do melhor candidato de Mitchell. É barata, determinística e
  espalha muito melhor que sorteio puro, sem o viés de grade que fatiar o
  mapa criaria.
- **Fronteiras de graça.** Cada fundador na capital mais próxima dá as
  células de Voronoi dos centros, que são contíguas por construção. Não
  há passo de suavização nem BFS aqui: a distância é em linha reta, então
  um fundador do outro lado de uma baía pode cair num reino que não
  alcança a pé. Isso é aceitável porque é só a semente — o território de
  verdade é recalculado pelo BFS, que respeita o terreno.

O efeito medido: **95–98% das criaturas nascem com o vizinho mais próximo
na mesma facção**, contra praticamente zero antes. É o teste
`foundingKingdomsAreSpatiallyCoherent` que trava isso, e é a propriedade
inteira da mudança — sem ela, reino é etiqueta, não lugar.

A quantidade de reinos foi escolhida medindo, em 6 sementes a 20 minutos.
Primeiro sem combate, para saber se agrupar por si só custa população:

| reinos | população média | extinções | nascimentos |
|---|---|---|---|
| 4 | 475 (229–622) | 0/6 | 15.342 |
| 5 | 369 (171–592) | 0/6 | 15.258 |
| 6 | 361 (165–523) | 0/6 | 14.779 |

Não custa: o mundo antigo de 240 facções salpicadas dava 432 e 15.864, e
entre 4 e 6 a diferença é quase toda variação de semente.

Depois com o combate corpo a corpo ligado, que é o que não funcionava
antes:

| reinos | população média | extinções | nascimentos | mortes em combate |
|---|---|---|---|---|
| 3 | 166 (38–314) | 0/6 | 8.188 | 492 |
| **4** | **158 (33–238)** | **0/6** | **8.471** | **538** |
| 5 | 143 (23–321) | 0/6 | 6.716 | 620 |
| 6 | 164 (24–290) | 0/6 | 6.912 | 653 |
| 8 | 77 (4–177) | 0/6 | 5.065 | 724 |

Nenhuma configuração extingue — contra 6/6 extinções com as 240 facções
salpicadas. **Quatro reinos** é o padrão porque ganha nas duas medições
independentes: a maior população sem combate (475) e a melhor pior-semente
com combate (33 contra 23 e 24). A partir de 8 o mundo começa a ficar
frágil: a pior semente termina com 4 criaturas vivas.

O preço honesto do combate está nessa tabela: a população de equilíbrio cai
de ~432 para ~158, cerca de um terço. Não é bug, é a fronteira cobrando —
e agora ela cobra sem matar o mundo, que era o ponto.

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

Recalcular o mundo inteiro é barato (cerca de 0,3 ms com pouco mais de 350
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

#### Nome e cor: por que uma facção deixou de ser um número

Até aqui uma facção era um inteiro sequencial e nada mais. Em qualquer log
ou tela ela apareceria como "facção 37", que é um índice, não um reino.
Agora cada uma nasce batizada: `FactionRegistry.create(rng)` sorteia nome e
tom no momento da fundação, e `nameOf(id)`/`colorOf(id)` devolvem isso pelo
resto do jogo.

**O sorteio vem do `Rng` da simulação, não de um `Random` próprio.** É a
mesma regra que vale para o terreno e para a espécie: a semente do mundo
decide tudo. O mundo da semente 12345 funda Dundor, Aelrok, Dunholm e
Breholm, nessa ordem, toda vez, em qualquer aparelho; o da semente 2026
funda Garvik, Tyrdor, Ashrok e Ornnen. Um único `nextLong()` por facção
alimenta nome e tom — um sorteio em vez de três, para mexer o mínimo
possível na sequência que o resto da simulação consome.

Os nomes saem de duas tabelas de sílabas inventadas (24 cabeças × 16
caudas = 384 combinações), lidas de faixas de bits diferentes do mesmo
sorteio. Com 4 reinos a repetição é rara, e mesmo quando acontece incomoda
menos que "Reino 37". Unicidade entra quando facção virar entidade de
verdade. As sílabas não remetem a
nenhum povo real pela mesma razão que as espécies se chamam ALPHA e BETA —
batizar de "Reino Élfico" prometeria cultura e diplomacia que o código não
tem.

**A cor é um `int` RGBA8888, não um `Color`.** Nada em `sim/` importa
libGDX, e essa é a primeira das quatro decisões que sustentam o projeto: é
o que permite rodar mundo e população inteiros sem abrir tela. O formato é
o mesmo de `TileType.colorRgba8888()`, então a camada de desenho consome os
dois do mesmo jeito. A conversão de HSV está escrita à mão dentro do
próprio registro, por isso.

**O matiz não é sorteado — é calculado.** Ele vem do índice da facção
girado pelo inverso da razão áurea (0,618…), então cada facção nova cai a
cerca de 137,5° da anterior no círculo de cor. Esse é o giro que maximiza a
menor distância entre matizes para qualquer quantidade de facções. Sortear
seria pior, não melhor: sorteio agrupa, e dois reinos com o mesmo tom de
verde é exatamente o que se quer evitar. O que o sorteio decide é o par
saturação/brilho, escolhido entre quatro — é o que separa duas facções que,
com muitos reinos, acabaram em matizes vizinhos. Nenhum dos quatro pares é
escuro: o fundo do jogo é quase preto, e cor escura sobre ele some.

O que este esquema **não** garante, e vale dizer: a separação é no espaço
de ids, não no mapa. Duas facções vizinhas *no terreno* podem ter ids
distantes e, com azar, cores parecidas. Resolver isso exigiria olhar o
território para escolher a cor — e o território é recalculado a cada
segundo, então a cor mudaria junto, o que é pior que o problema. O
objetivo é reino distinguível do vizinho na maioria dos casos, não paleta
perfeita.

**Ninguém desenha isso ainda.** `nameOf` e `colorOf` hoje só são chamados
pelos testes — a tela de território não existe, e `CreatureRenderer`
colore por *estado* da criatura, que é outro conceito. Isso é uma dívida
declarada, não um descuido: o consumidor natural é o mapa de territórios,
que está atrás do bloqueio arquitetural do sistema de sociedade. Está na
lista de dívida técnica abaixo, e é o mesmo erro que `WorldRenderer.tileX`
cometeu por meses — a diferença é que desta vez ele está escrito na
página.

---

## Ferramentas de apoio

Quatro ferramentas em Java puro, sem Gradle e sem baixar dependência:

```bash
# compile a simulação uma vez
javac -d build/sim $(find core/src/main/java/com/emannuel/mundovivo/sim -name '*.java')
javac -d build/tools -cp build/sim tools/*.java

# gere um PNG do mundo da semente 12345, com 3 px por tile
java -cp build/sim:build/tools WorldPreview 12345 3 previa.png

# o mesmo mundo com as criaturas, depois de 2 minutos simulados
java -cp build/sim:build/tools SimulationPreview 12345 2 3 vivo.png

# acompanhe a população por 5 minutos simulados
java -cp build/sim:build/tools SimulationReport 12345 5

# rode a verificação inteira
java -cp build/sim:build/tools SimSelfTest
```

`WorldPreview` encurta o ciclo de calibragem de minutos para segundos:
mexeu em `WorldConfig`, roda e olha o PNG. `SimulationPreview` desenha as
criaturas por cima, com a mesma cor de estado do jogo. `SimulationReport` faz o mesmo
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
- A saída visual do mundo foi conferida: mapas em várias sementes, com
  continentes, cordilheiras com neve no cume, litoral e calota polar.
- Comportamento da população, **remedido com a herança genética ligada**:
  12 sementes a 20 minutos e 60 quadros, populações finais entre 84 e 459,
  média 233, **nenhuma extinção e nenhuma batida no teto do pool**.
- Estabilidade por taxa de quadros, **remedida com a herança genética
  ligada**: 12 sementes a 20 minutos, população média de 262, 233 e 195 a
  30, 60 e 90 quadros por segundo, **sem nenhuma extinção nas 36
  execuções**. Eram 218, 176 e 200 antes da genética: os traços variam em
  torno de 1,0×, então a mudança é deslocamento do sorteador, não um
  ganho que a genética tenha trazido. A dispersão entre taxas continua existindo — passos grossos
  rendem mordidas maiores — mas é variação, não diferença entre viver e
  morrer. Os números caíram para cerca de um terço do que eram sem
  combate (532/479/458): é a fronteira cobrando, não uma regressão.
- Custo de um passo, **remedido com o combate ligado**: 0,165 ms com 288
  criaturas e **8,9 ms com o pool cheio** (2.999), em um quadro que tem
  16,6 ms. Em jogo normal a simulação não é o gargalo; com o pool cheio, a
  busca de inimigos (quadrática) passa a pesar — ver a dívida técnica. O
  valor antigo de 0,090 ms com o pool cheio era de antes do combate.
- Custo de um recálculo de território, que roda uma vez por segundo: 0,284
  ms com 403 criaturas e 0,264 ms com 3000, em um mundo padrão de 49 mil
  tiles — bem abaixo do segundo inteiro de folga que o intervalo dá. O custo
  quase não depende da população porque a busca é multi-fonte: ela varre os
  tiles uma vez, não uma vez por criatura.
- **O build do Gradle roda na CI** (GitHub Actions) a cada envio para a
  `main` e em todo pull request: compila `core` (incluindo `render/`) e
  `android` contra o libGDX real, roda a suíte JUnit (109 métodos `@Test`)
  com `./gradlew core:test` e gera o APK de depuração. O módulo `desktop`
  não é compilado na CI.
- **O APK já abriu num celular Android**, com o mapa e as criaturas na
  tela (relato do dono do projeto, com capturas de tela).

**Ainda não verificado:**

- Não há medição de FPS real, nem no celular nem no PC.
- `./gradlew desktop:run` não tem registro de execução.
- A orientação do mapa na tela não foi comparada com a do `WorldPreview`
  para a mesma semente. Os testes provam que desenho e toque concordam
  entre si, não que o mapa aparece do lado certo.
- `CameraController.resize()` não tem teste: a multiplicação de matriz do
  libGDX é nativa e não carrega em teste sem backend.

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
- ~~**O mundo nasce com 240 facções, uma por fundador.**~~ Resolvido: o
  mundo funda quatro reinos contíguos e reparte os fundadores entre eles.
  A previsão que estava escrita aqui — "provavelmente vai fazer mais
  sentido fundar poucas e grandes do que uma por indivíduo" — se
  confirmou, e pelo motivo mais caro possível: foi o que impediu o
  combate corpo a corpo de funcionar. Ver "Por que poucos reinos
  contíguos" acima.
- **Reproduzir com uma criatura sem reino quebra o jogo.** Em
  `Simulation.reproduce`, o filho herda a facção de um dos pais e entra
  nela com `factions.join(childFaction)`. Se esse pai tiver facção `-1`,
  `FactionRegistry.join(-1)` lança `IndexOutOfBoundsException`. Hoje não
  acontece — todo fundador recebe reino e todo filho herda um válido —, mas
  vai acontecer no dia em que algo criar criatura sem reino (um poder de
  criar criatura, por exemplo).
- **A busca de inimigos é quadrática.** `Simulation.hostileNeighbours`
  compara cada criatura com todas as outras vivas. Com 288 criaturas o
  passo custa 0,165 ms; com o pool cheio (2.999), 8,9 ms, mais da metade
  do quadro. A correção é a mesma grade espacial da busca por parceiro.
- **`Creature.size` não tem consumidor.** O genoma calcula o tamanho, como
  os outros traços, mas nada o lê. O uso natural é no combate ou na
  comida.
- **Nome e cor de facção não são desenhados em lugar nenhum.**
  `FactionRegistry.nameOf` e `colorOf` existem, são determinísticos e têm
  teste, mas quem chama hoje são só os testes. O consumidor natural é um
  mapa de territórios colorido, e esse está atrás do bloqueio do sistema de
  sociedade. É a mesma situação de `WorldRenderer.tileX`, que ficou meses
  escrito sem ser chamado — a diferença é que este está declarado aqui em
  vez de esquecido.
- **Dano de terreno não tem gatilho em jogo.** Nada hoje transforma o
  chão debaixo de uma criatura, então a via existe testada mas ociosa: ela
  é a base para o sistema 6 (poderes de deus) e o 7 (guerra), não uma
  mecânica que o jogador já sinta. Enquanto não houver gatilho, o custo
  dela é uma consulta de tile por criatura por passo.
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

**Primeiro: olhar o jogo na tela com atenção.** O APK já abriu num
celular, mas ninguém mediu FPS nem comparou a orientação do mapa com o
`WorldPreview` da mesma semente. Rodar `./gradlew desktop:run` no PC
resolve as duas coisas de uma vez.

Depois, sistema 5: save/load. `World`, `FoodMap`, `CreaturePool` e
`FactionRegistry`/`Territory` guardam estado simples o bastante para
serializar; falta decidir o formato e escrever o carregamento — e conferir
que um mundo recarregado recalcula o mesmo território que tinha antes de
salvar, já que ele deriva da posição das criaturas em vez de ser salvo
como tal.

Em paralelo, o que já existe e não aparece: desenhar o território com o
nome e a cor de cada reino. O combate já usa os reinos; falta o jogador
vê-los.
