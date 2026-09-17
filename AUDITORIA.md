# Auditoria de paridade de funcionalidades — Mundo Vivo vs WorldBox

**Data:** 17 de setembro de 2026
**Commit auditado:** `093b09a` — 5.543 linhas em 44 arquivos
**Referência:** WorldBox 0.51.4 (25/05/2026)
**Atualizado até:** commit `15dd0f8` — ver "Histórico de entregas" no fim do documento

Este documento não é uma lista de desejos nem um roteiro. É um retrato do que
existe e do que não existe, com evidência verificável no código, para que
qualquer decisão sobre o que construir a seguir parta de fato e não de
impressão.

---

## Metodologia

**Lado Mundo Vivo.** O repositório foi clonado no commit acima e lido. Foram
lidos integralmente `Simulation.java` (499 linhas), `Creature.java`,
`CreatureConfig.java`, `FactionRegistry.java`, `Territory.java`, `FoodMap.java`,
`TileType.java`, `WorldConfig.java`, `CameraController.java`,
`WorldRenderer.java`, `MundoVivoGame.java` e o `README.md`. Foi feita busca
textual por oitenta termos de sistema em todo o repositório, e cada resultado
positivo foi verificado um a um para separar ocorrência real de falso positivo
— "war" aparece dentro de `moveToward`, "city" dentro de `capacity`, "gene"
dentro de `WorldGenerator`. Onde um método existe, foi verificado se alguém o
chama: código escrito e nunca invocado não conta como funcionalidade.

**Lado WorldBox.** A versão mais recente verificável na data é a 0.51.4,
conforme o changelog oficial. Foram usados: changelog oficial em
superworldbox.com, página da Steam, verbete da Wikipédia e a enciclopédia
NamuWiki, que documenta os sistemas em detalhe numérico. O Fandom recusou
acesso automatizado (HTTP 402) e não foi contornado.

**Limitação declarada.** Sem o Fandom, listas individuais de traits, biomas
especiais e conquistas podem estar incompletas do lado WorldBox. Isso torna o
placar abaixo conservador: o número real de ausências tende a ser maior, nunca
menor.

**Critério de classificação.** Compara-se funcionalidade observável de produto —
o que o jogador consegue fazer ou ver. Decisões internas de engenharia (pools de
objeto, estratégia de alocação, padrões de projeto, nomes de classe) não entram
como funcionalidade, mesmo quando são boas decisões. A pergunta que cada linha
responde é: abrindo os dois jogos lado a lado, o que existe em um e não no
outro?

| Símbolo | Significado |
|---|---|
| 🟢 | Implementado e funcional |
| 🟡 | Parcial — existe parte relevante, falta parte relevante |
| 🔴 | Ausente |
| ⚪ | Incerto — há código, não foi possível confirmar comportamento |

---

## Placar

| Medida | Valor |
|---|---|
| Funcionalidades analisadas | 217 |
| 🟢 Implementadas | 26 (12,0%) |
| 🟡 Parciais | 6 (2,8%) |
| 🔴 Ausentes | 184 (84,8%) |
| ⚪ Incertas | 1 (0,5%) |
| Cobertura aproximada | ~13,4% |

O placar acima já inclui a entrega do item 156 em `15dd0f8`. No commit
auditado originalmente (`093b09a`) era 25 / 6 / 185 / 1, cobertura ~12,9%.

---

## 1. Mundo e terreno

**1.** 🟢 **Geração procedural de terreno.** WorldBox possui gerador procedural.
Mundo Vivo implementa com ruído fBm. Evidência: `WorldGenerator.generate()`,
`FractalNoise.java`, classificação em `WorldGenerator.java:125`. Testado em
`WorldGeneratorTest`.

**2.** 🟢 **Determinismo por semente.** Mesma semente produz mundo idêntico.
Evidência: `WorldConfig.seed:18`, `Rng.java`, `Simulation.java:79`.

**3.** 🟢 **Biomas distintos.** WorldBox tem dezenas, incluindo especiais
(cogumelo, mágico, corrompido, inferno, doce, cristal). Mundo Vivo tem 16 tipos
de terreno. Evidência: `TileType.java:17-32`. Falta: nenhum bioma fantástico.

**4.** 🟢 **Água em profundidades diferentes.** `DEEP_OCEAN`, `OCEAN`,
`SHALLOW_WATER`, com cor própria e intransponíveis. Evidência:
`TileType.java:17-19`.

**5.** 🟢 **Montanhas e cordilheiras.** `MOUNTAIN` e `SNOW_PEAK` não caminháveis;
ruído de cristas produz cordilheiras alongadas em vez de manchas. Evidência:
`WorldConfig.mountainRidges:49`.

**6.** 🟢 **Ilhas e arquipélagos.** Afundamento de borda isola massas de terra.
Evidência: `WorldConfig.islandFalloff:55`.

**7.** 🟢 **Temperatura como variável de geração.** Evidência:
`WorldConfig.temperatureFrequency:36`, `altitudeCooling:61`, consumidas em
`WorldGenerator.java:125`. Falta: não é estado vivo — não muda depois da
geração, e não há array de temperatura no mundo.

**8.** 🟢 **Umidade como variável de geração.** Evidência:
`WorldConfig.moistureFrequency:32`.

**9.** 🟡 **Tamanho de mundo variável.** WorldBox oferece de Tiny a Extra Huge,
escolhido pelo jogador. Mundo Vivo tem presets `small()` 128x96, `medium()`
256x192 e `large()` 384x288 em `WorldConfig.java:73-85`, mas o jogo chama
`medium` fixo em `MundoVivoGame.java:57`. Falta: escolha do jogador. Depende de:
interface.

**10.** 🔴 **Tipos/presets de mundo.** WorldBox tem 15+ formatos (continente,
arquipélago, donut, xadrez, vulcão adormecido, entre outros). Mundo Vivo tem um
único algoritmo.

**11.** 🔴 **Rios.** Busca por "river/rio" em todo o repositório: nenhuma
ocorrência.

**12.** 🔴 **Lagos como entidade.** Água interior existe por acidente do ruído,
sem modelagem própria.

**13.** 🔴 **Vegetação como entidade.** WorldBox tem árvores individuais,
contáveis, que crescem e queimam. Mundo Vivo tem fertilidade como número por
tile. Evidência: `TileType.fertility():65`.

**14.** 🔴 **Crescimento e propagação de vegetação.** Existe apenas regeneração
numérica de comida. Evidência: `FoodMap.regrow():108`.

**15.** 🔴 **Recursos de terreno.** WorldBox tem cadeia de materiais (cobre,
bronze, ferro, aço, prata, mithril, adamantium) ligada a conhecimento cultural.
Busca por "resource/minério/ore": nada.

**16.** 🔴 **Estradas.** WorldBox usa tiles de estrada que aceleram unidades.

**17.** 🔴 **Terreno cultivado.** WorldBox tem pincel de farmland.

**18.** 🔴 **Erosão.** É lei do mundo no WorldBox.

**19.** 🔴 **Propagação ambiental.** Fogo que alastra, lava que esfria, ácido que
corrói. Busca por "fire/lava/acid": nada.

**20.** 🔴 **Transformação de bioma em cadeia.** WorldBox tem sementes de bioma
que se espalham (corrupção, inferno, congelamento).

**21.** 🟡 **Alteração de terreno em tempo de execução.** É pilar do WorldBox. No
Mundo Vivo a fundação existe e está morta: `World.setTile()`,
`WorldRenderer.setTile():77` e `FoodMap.retile():129` estão escritos, e o
comentário em `WorldRenderer.java:75` diz textualmente "é por aqui que os
poderes de deus deverão modificar o terreno" — mas nenhum código de jogo os
chama. Verificação: os únicos chamadores são `SimSelfTest.java:510,563,580`,
`TerritoryTest` e o próprio `WorldGenerator:125`. Depende de: sistema de
interação.

**22.** 🔴 **Ilhas identificadas como entidade.** WorldBox mostra "número de
ilhas" nas estatísticas.

**23.** 🔴 **Camadas de visualização do mapa.** WorldBox tem toggles de zona.
Mundo Vivo desenha uma única camada de cor de bioma. Evidência:
`WorldRenderer.render():88-100` desenha uma textura só.

**24.** 🔴 **Minimapa.**

---

## 2. Clima, eras e tempo

**25.** 🔴 **Estações do ano.** Busca por "season/estação": nada.

**26.** 🔴 **Clima dinâmico.** Chuva, neve caindo. Busca por "weather/clima":
nada.

**27.** 🔴 **Ciclo dia/noite.**

**28.** 🔴 **Eras do Mundo.** WorldBox tem Era da Esperança, do Gelo e do
Desespero, alterando biomas e criaturas. Mundo Vivo tem apenas
`Simulation.elapsedSeconds:113` como relógio cru.

**29.** 🔴 **Progressão histórica com efeito global.**

**30.** 🟢 **Tempo em segundos, independente de quadros.** Entra na lista porque
tem efeito observável: a simulação se comporta igual a 30 e a 60 quadros por
segundo. Evidência: `Simulation.step(float):141`, `MAX_STEP_SECONDS:40`.

**31.** 🔴 **Controle de velocidade da simulação.** WorldBox tem 2x e 3x. Mundo
Vivo roda sempre em 1x: `MundoVivoGame.render():97` passa
`Gdx.graphics.getDeltaTime()` direto, sem multiplicador.

**32.** 🔴 **Pausa.** Mesma evidência acima.

**33.** 🔴 **Eventos aleatórios do mundo.**

**34.** 🔴 **Invasões.**

**35.** 🔴 **Histórico de acontecimentos.** WorldBox registra morte de rei,
fundação e destruição de cidade, guerras e tratados. Mundo Vivo tem três
contadores globais: `Simulation.births():121`, `deathsByStarvation()`,
`deathsByOldAge():131`.

---

## 3. Criaturas — ciclo de vida

**36.** 🟢 **Criaturas autônomas.** Evidência: `Simulation.stepCreature():167`,
máquina de estados em `CreatureState.java`.

**37.** 🟢 **Fome como necessidade.** Evidência: `Creature.hunger:33`,
`config.hungerPerSecond` aplicado em `Simulation:170`.

**38.** 🟢 **Busca de comida.** Busca em anéis quadrados crescentes, que termina
no primeiro acerto. Evidência: `Simulation.findFoodNear():416`.

**39.** 🟢 **Alimentação e saciedade.** Evidência: `Simulation.stepEating():228`,
`FoodMap.consume():85`.

**40.** 🟢 **Morte por inanição.** Evidência: `Simulation:172-182`, contador
`deathsByStarvation`.

**41.** 🟢 **Envelhecimento.** Evidência: `Creature.age:31`, limite
`config.maxAgeSeconds` em `Simulation:184`.

**42.** 🟢 **Morte por velhice.** Com contador próprio. Evidência:
`Simulation:184-188`.

**43.** 🟢 **Vida e regeneração.** Evidência: `Creature.health:36`, regeneração
quando saciada em `Simulation:175-177`.

**44.** 🟢 **Maturidade.** Evidência: `Creature.isAdult():90`,
`config.adultAgeSeconds`.

**45.** 🟢 **Reprodução sexuada.** Exige dois adultos próximos. Evidência:
`Simulation.reproduce(a, b):272`, `resolveMate():323`.

**46.** 🟢 **Custo biológico da reprodução.** Ambos os pais pagam fome e entram
em espera. Evidência: `Simulation:273-276`.

**47.** 🟢 **Cadáver vira alimento.** Evidência: `Simulation.die():305` chama
`foodMap.deposit(..., config.corpseFoodValue)`.

**48.** 🟢 **Teto populacional.** Pool cheio não gera filho. Evidência:
`Simulation:290`.

**49.** 🟢 **Bloqueio de terreno intransponível.** Evidência:
`Simulation.moveTo():387` rejeita tile não caminhável.

**50.** 🔴 **Reprodução assexuada e partenogênese.** WorldBox tem múltiplos
métodos reprodutivos.

**51.** 🔴 **Dieta e cadeia alimentar.** WorldBox tem herbívoros, carnívoros e
estatística de morte por predação. No Mundo Vivo toda criatura come do mesmo
`FoodMap`; não há predador nem outra fonte de alimento.

**52.** 🔴 **Sede.**

**53.** 🔴 **Sono e descanso.**

**54.** 🔴 **Pathfinding real.** WorldBox contorna obstáculos e usa estradas. No
Mundo Vivo o movimento é em linha reta; quando o passo bate em água, o alvo é
descartado e outro é sorteado. Evidência: `Simulation.moveTo():387-398`, com o
comentário explicando a decisão, e dívida técnica registrada no README.

**55.** 🔴 **Nível e progressão individual.**

---

## 4. Genética, traits e condições

**56.** 🔴 **Genes e herança genética.** WorldBox tem sistema de genes desde a
0.50. Busca por "gene" no Mundo Vivo retorna apenas `worldGenerationIsFast` em
`SimSelfTest` — nenhum gene existe. Depende de: reprodução, que já existe
(item 45).

**57.** 🔴 **Traits.** WorldBox tem dezenas, editáveis pelo jogador: combate,
sobrevivência, comportamento, mágicos. Busca por "trait": nenhuma ocorrência.

**58.** 🔴 **Editor de traits.**

**59.** 🔴 **Mutações.**

**60.** 🔴 **Metamorfose.**

**61.** 🔴 **Subespécies emergentes.**

**62.** 🔴 **Doenças e epidemias.** WorldBox tem praga, infecção zumbi, esporos
de cogumelo, e conta mortes por doença. Busca por "disease/plague/doença": nada.

**63.** 🔴 **Status effects temporários.** Bênção, maldição, loucura, escudo,
aceleração.

**64.** 🔴 **Imunidades.** Fogo, veneno, vida eterna.

**65.** 🔴 **Herança de características dos pais.** A única coisa herdada no
Mundo Vivo é o id de facção, sorteado meio a meio entre os pais. Evidência:
`Simulation:299` — `rng.chance(0.5f) ? a.factionId : b.factionId`. Nenhum
atributo biológico é transmitido.

**66.** 🔴 **Variação individual.** Todas as criaturas compartilham a mesma
`CreatureConfig`. Fora posição, fome, vida, idade e facção, não há atributo por
indivíduo. Evidência: campos de `Creature.java:24-57`.

**67.** 🔴 **Nomes individuais.** WorldBox tem sistema de onomástica. Mundo Vivo
tem `Creature.id:24`, um inteiro incremental.

**68.** 🔴 **Favoritar criatura.** No WorldBox protege da borracha e dobra
chance de liderança e sucessão.

---

## 5. Espécies, raças e monstros

**69.** 🔴 **Múltiplas espécies.** Existe uma criatura genérica, sem tipo — a
classe `Creature` não tem campo de espécie.

**70.** 🔴 **Raças civilizáveis.** WorldBox tem humanos, elfos, anões e orcs, com
tendências culturais próprias. Busca por "race/raça/species": nada.

**71.** 🔴 **Atributos raciais e hostilidade entre raças.** No WorldBox orcs são
hostis a todos; elfos e anões, entre si.

**72.** 🔴 **Animais não civilizados.** Lobo, urso, vaca, gato, tartaruga, com
contagem própria.

**73.** 🔴 **Criação de espécie pelo jogador.**

**74.** 🔴 **Mortos-vivos.** Zumbis, esqueletos, fantasmas.

**75.** 🔴 **Demônios.**

**76.** 🔴 **Dragões.**

**77.** 🔴 **Slimes, ratos, vermes.**

**78.** 🔴 **Alienígenas e UFO.**

**79.** 🔴 **Chefes colossais.** Crabzilla e equivalentes.

**80.** 🔴 **Bandidos e unidades hostis sem reino.**

---

## 6. Combate e exércitos

**81.** 🔴 **Combate corpo a corpo.** É pilar do WorldBox. No Mundo Vivo
criaturas não podem se ferir: a única perda de vida é inanição, em
`Simulation:174`.

**82.** 🔴 **Combate à distância e projéteis.**

**83.** 🔴 **Exércitos e unidades militares.** WorldBox trouxe sistema de
exércitos na 0.51.

**84.** 🔴 **Treinamento militar por conhecimento.**

**85.** 🔴 **Equipamento e materiais.**

**86.** 🔴 **Moral e lealdade em batalha.**

**87.** 🔴 **Cerco a assentamentos.**

**88.** 🔴 **Magia usada por unidades.** Ritos e feitiços lançados pelas próprias
criaturas.

---

## 7. Assentamentos e construção

**89.** 🔴 **Vilas como entidade.** No WorldBox ocupam área definida e se
expandem. Busca por "village/vila": nada.

**90.** 🔴 **Cidades.**

**91.** 🔴 **Capitais.** No WorldBox a capital dá grande bônus de lealdade.

**92.** 🔴 **Fundação de assentamento por colonos.**

**93.** 🔴 **Casas com níveis de evolução.** WorldBox tem seis níveis, de tenda a
pedra avançada.

**94.** 🔴 **Prefeitura com tiers.**

**95.** 🔴 **Estruturas militares.** Quartel, torre de vigia.

**96.** 🔴 **Infraestrutura econômica.** Mina, poço, moinho, porto.

**97.** 🔴 **Monumentos e estátuas.**

**98.** 🔴 **Fogueira como primeiro estágio.**

**99.** 🔴 **Destruição de construções.**

**100.** 🔴 **Ruínas e reconstrução.**

**101.** 🔴 **Estoque de recursos por assentamento.**

---

## 8. Reinos e governo

**102.** 🟡 **Grupos/facções como entidade.** No WorldBox reinos têm identidade
completa. No Mundo Vivo o registro guarda **apenas a contagem de membros
vivos** — sem nome, cor, capital ou líder. Evidência: `FactionRegistry.java:24`,
onde o estado inteiro da classe é `int[] memberCount`; a documentação da própria
classe declara isso nas linhas 10-12.

**103.** 🟡 **Território de facção.** Calculado corretamente por busca em largura
multi-fonte sobre tiles caminháveis, contornando água em vez de cortá-la, e
recalculado uma vez por segundo. Evidência: `Territory.recompute():87-117`,
agendamento em `Simulation:157-164`. **Falta: qualquer consequência.** O
território não é desenhado, não altera comportamento e não gera conflito.
Verificação: busca por "faction|territor" em `render/` e em `MundoVivoGame.java`
retorna zero ocorrências.

**104.** 🔴 **Nomes de reino gerados.** No Mundo Vivo a facção é um inteiro
sequencial: `FactionRegistry.create():35` retorna `count++`.

**105.** 🔴 **Bandeiras e símbolos.** WorldBox tem banners customizáveis desde a
0.14.

**106.** 🔴 **Cor de reino.** No Mundo Vivo a cor da criatura indica estado, não
facção. Evidência: `CreatureRenderer.colorFor(CreatureState):65`.

**107.** 🔴 **Reis e líderes.** No WorldBox têm estatísticas de diplomacia,
liderança e guerra. Busca por "king/leader/líder": nada.

**108.** 🔴 **Sucessão.**

**109.** 🔴 **Famílias e árvore genealógica.** O filho não guarda referência aos
pais. Evidência: `Creature.reset():68-80` não registra ascendência.

**110.** 🔴 **Linhagem e dinastia.**

**111.** 🔴 **Federação de vilas em reino.**

**112.** 🔴 **Lealdade de assentamento.** No WorldBox é calculada a partir de
capital, proximidade, cultura, qualidade do líder, distância e excesso de
cidades.

**113.** 🔴 **Expansão territorial ativa.** No Mundo Vivo o território só reflete
onde as criaturas estão agora; ninguém conquista nada. Evidência:
`Territory.recompute` começa com `Arrays.fill(ownerFaction, UNCLAIMED)` na linha
88 — não há memória de posse anterior.

**114.** 🔴 **Queda de reino com consequência.** `FactionRegistry.isExtinct():50`
existe, mas nada acontece quando uma facção zera.

---

## 9. Sociedade profunda

**115.** 🔴 **Clãs.** WorldBox tem clãs com chefes e árvore familiar.

**116.** 🔴 **Chefes de clã.**

**117.** 🔴 **Culturas.** No WorldBox têm propagação, substituição e velocidade
de difusão.

**118.** 🔴 **Línguas com falantes.**

**119.** 🔴 **Religiões com sacerdotes e seguidores.**

**120.** 🔴 **Conversão religiosa.**

**121.** 🔴 **Conhecimento e tecnologia por cultura.** No WorldBox o conhecimento
é compartilhado entre membros e destrava construções, materiais e militar.

**122.** 🔴 **Difusão cultural entre grupos.**

**123.** 🔴 **Opiniões e relações entre indivíduos.** A única relação entre duas
criaturas no Mundo Vivo é o alvo temporário de acasalamento:
`Creature.targetMateSlot:45`.

**124.** 🔴 **Identidade coletiva com efeito mecânico.** A facção não altera
comportamento nenhum: duas criaturas de facções diferentes se reproduzem
normalmente. Evidência: `Simulation.resolveMate():323-350` não filtra por
`factionId`.

**125.** 🔴 **Onomástica.** Geração de nomes para pessoas, cidades e reinos.

---

## 10. Diplomacia e guerra

**126.** 🔴 **Relações diplomáticas entre reinos.** No WorldBox a opinião é
calculada a partir de diferença de poder, inimigo comum, diplomacia do rei,
alinhamento cultural, proximidade de fronteira e relação tribal.

**127.** 🔴 **Alianças.** WorldBox tem cinco níveis com bônus progressivos.

**128.** 🔴 **Guerras de conquista.**

**129.** 🔴 **Rebeliões.**

**130.** 🔴 **Tréguas e tratados de paz.**

**131.** 🔴 **Guerra por aliança.**

**132.** 🔴 **Fronteiras disputadas.** No Mundo Vivo as fronteiras existem no
dado mas não geram atrito — ver item 103.

**133.** 🔴 **Pilhagem de fronteira.**

**134.** 🔴 **Histórico de guerras.**

**135.** 🔴 **Multidões revoltadas.**

**136.** 🔴 **Conspirações.**

---

## 11. Poderes do jogador — terreno e criação

**137.** 🔴 **Pincel de terreno.** É o núcleo do WorldBox. No Mundo Vivo
**nenhum poder existe**: a única interação do jogador com o mundo é o toque
longo que descarta tudo e gera outro mundo. Evidência:
`CameraController.longPress():138` → `MundoVivoGame.regenerate():70`. O método
`tap()` está implementado vazio e retorna `false` em
`CameraController.java:147-149`.

**138.** 🔴 **Sementes de bioma.**

**139.** 🔴 **Ferramenta de cópia.**

**140.** 🔴 **Ferramenta de embaralhar terreno.**

**141.** 🔴 **Borracha.**

**142.** 🔴 **Remoção de água e lava.**

**143.** 🔴 **Ferramentas de corte e escavação.**

**144.** 🔴 **Demolidor de construções.**

**145.** 🔴 **Apagador de vida.**

**146.** 🔴 **Tamanho de pincel ajustável.**

**147.** ⚪ **Conversão toque para tile.** O código existe e está correto, mas
ninguém o chama: `WorldRenderer.tileX(float):107` e `tileY(float):115` convertem
coordenada de mundo em tile já compensando a inversão vertical do desenho.
Classificado como incerto porque é infraestrutura escrita e nunca exercitada em
jogo.

---

## 12. Poderes — destruição

**148.** 🔴 **Explosivo básico.**

**149.** 🔴 **Bomba atômica.**

**150.** 🔴 **Tsar Bomba.**

**151.** 🔴 **Bomba-relógio.**

**152.** 🔴 **Minas e granadas.**

**153.** 🔴 **Napalm.**

**154.** 🔴 **Bomba d'água.**

**155.** 🔴 **Poderes de piada.** Bola de boliche, moeda do infinito.

**156.** 🟢 **Dano a criaturas por fonte externa.** *Entregue em 17/09/2026,
commit `15dd0f8` — no commit auditado (`093b09a`) este item era 🔴.* Existe
`Creature.applyDamage(float amount, String cause)`, genérico por desenho: não
mata por conta própria, devolve se o golpe foi fatal e deixa `Simulation` tratar
a morte no mesmo ponto que já tratava a inanição, preservando um único caminho
de morte (cadáver vira comida, sai da facção, slot volta ao pool). A inanição
passou a rotear por ele, então a via é exercitada em toda partida. Primeiro
consumidor externo: terreno perigoso (`DEEP_OCEAN`, escolhido porque não existe
lava no enum), com taxa em `CreatureConfig.hazardDamagePerSecond`. Verificação:
68/68 na suíte JUnit, 65/65 no `SimSelfTest`, CI verde, e populações finais das
sementes de regressão idênticas às de antes (610 / 268 / 394).
**Ressalva:** o gatilho de terreno fica ocioso em partida normal, porque o
movimento recusa terreno não caminhável — nenhuma criatura pisa na água por
vontade própria. A via existe e está testada; o jogador ainda não a sente. Ela
acorda quando existir poder de deus (itens 137–147) ou desastre (157–165).

---

## 13. Poderes — natureza e desastres

**157.** 🔴 **Raio.**

**158.** 🔴 **Meteoro.**

**159.** 🔴 **Tornado.**

**160.** 🔴 **Terremoto.**

**161.** 🔴 **Vulcão e lava.**

**162.** 🔴 **Chuva ácida.**

**163.** 🔴 **Fogo que alastra.**

**164.** 🔴 **Onda de choque.**

**165.** 🔴 **Manipulação de temperatura.** A temperatura do Mundo Vivo existe só
como variável de geração; não há array de temperatura vivo no mundo — ver item
7.

---

## 14. Poderes — efeitos e condições

**166.** 🔴 **Chuva de cura.**

**167.** 🔴 **Chuva de loucura.**

**168.** 🔴 **Bênçãos e maldições.**

**169.** 🔴 **Epidemia como poder.**

**170.** 🔴 **Infecção zumbi.**

**171.** 🔴 **Esporos de cogumelo.**

**172.** 🔴 **Estruturas mágicas do jogador.** Monólito, espiral, torre.

---

## 15. Poderes — civilização e unidades

**173.** 🔴 **Spawn de criatura pelo jogador.** Não há nenhuma via de criação
manual: toda criatura nasce em `Simulation.spawnInitialPopulation():465`, na
construção do mundo, ou em `reproduce():272`.

**174.** 🔴 **Remoção de unidade.**

**175.** 🔴 **Declarar guerra.**

**176.** 🔴 **Forçar paz.**

**177.** 🔴 **Separar reino.**

**178.** 🔴 **Sussurro de guerra.**

**179.** 🔴 **Possessão de unidade.**

**180.** 🔴 **Alterar raça de uma unidade.**

---

## 16. Leis do mundo

**181.** 🔴 **Painel de leis do mundo.** WorldBox tem um conjunto de chaves
liga/desliga. O Mundo Vivo tem `CreatureConfig`, que é configuração de código
ajustada em teste, não painel de jogo.

**182.** 🔴 **Desligar fome.**

**183.** 🔴 **Desligar envelhecimento.**

**184.** 🔴 **Desligar diplomacia, rebelião e expansão.** Os sistemas nem
existem.

**185.** 🔴 **Ligar e desligar desastres.**

**186.** 🔴 **Ligar e desligar spawn de animais.**

**187.** 🔴 **Regras de crescimento da natureza.** A rebrota é fixa em código,
mas existe um setter sem interface que o chame:
`FoodMap.regrowthPerSecond(float):56`.

---

## 17. Interface, inspeção e estatísticas

**188.** 🔴 **Interface de usuário.** O jogo não tem UI nenhuma. O laço de
desenho produz exatamente dois objetos: terreno e criaturas. Evidência:
`MundoVivoGame.render():96-107`.

**189.** 🔴 **Barra de poderes com categorias.**

**190.** 🔴 **Inspeção de criatura.** O `toString()` de `Creature` (linhas
108-116) formata estado, fome, vida, idade e facção, mas isso só aparece em log
de terminal, nunca na tela.

**191.** 🔴 **Janela de vila e reino.**

**192.** 🔴 **Estatísticas de mundo em jogo.** Os dados existem parcialmente —
`population()`, `births()`, dois contadores de morte, `foodMap.totalFood()` —
mas não há tela que os mostre. Só as ferramentas de linha de comando
`tools/SimulationReport.java` e `tools/SimulationPreview.java`, que rodam fora
do jogo.

**193.** 🔴 **Gráficos de população ao longo do tempo.** Existem no relatório de
terminal, não no jogo.

**194.** 🔴 **Log de acontecimentos na tela.**

**195.** 🔴 **Notificações de evento.**

**196.** 🔴 **Seleção de unidade.** `tap()` retorna `false` sem fazer nada:
`CameraController.java:147`.

**197.** 🔴 **Filtros e modos de visualização.**

**198.** 🔴 **Conquistas.**

**199.** 🟢 **Câmera com arraste, pinça e roda do mouse.** Com limites corretos —
o zoom máximo é recalculado a cada redimensionamento para o mundo caber na tela.
Evidência: `CameraController.pan():117`, `zoom():127`, `scrolled():175`,
`resize():58`.

---

## 18. Persistência, editor e compartilhamento

**200.** 🔴 **Salvar mundo.** O README declara na linha 23: "5. Save/load — não
começou".

**201.** 🔴 **Carregar mundo.**

**202.** 🟡 **Fundação para serialização.** Os acessos brutos ao estado já estão
expostos de propósito: `World.rawTypes():113`, `FoodMap.rawAmount():147`, estado
interno do `Rng` na linha 23, e a semente é guardada. Falta o formato e o código
de escrita e leitura. Classificado como parcial por ser preparação deliberada e
verificável, não funcionalidade.

**203.** 🔴 **Múltiplos slots de save.**

**204.** 🔴 **Autosave.**

**205.** 🔴 **Editor de mapa.**

**206.** 🔴 **Compartilhamento de mapas.**

**207.** 🔴 **Importação e exportação de mundo.**

**208.** 🔴 **Mods e Workshop.**

**209.** 🟡 **Regenerar mundo.** Existe, mas amarrado a um atalho de
desenvolvimento: toque longo em qualquer lugar apaga tudo sem confirmação.
Evidência: `MundoVivoGame.regenerate():70`, documentado na própria classe nas
linhas 27-28 como atalho que "sai quando a interface de verdade entrar".

---

## 19. Apresentação

**210.** 🔴 **Sprites de criatura.** Cada criatura é um quadrado de cor sólida.
Evidência: `CreatureRenderer.SIZE:28` e `batch.draw(pixel, ...)` na linha 60 — a
textura é literalmente um pixel branco esticado, criado nas linhas 41-46.

**211.** 🔴 **Animação.** A posição muda, o desenho não.

**212.** 🔴 **Sombreamento e relevo visual.** `WorldRenderer.paintAll():64-71`
escreve `colorRgba8888()` puro, sem modulação por altitude.

**213.** 🔴 **Efeitos visuais.** Partículas, explosões, fumaça.

**214.** 🔴 **Som e música.** Busca por "sound/music/audio": nada.

**215.** 🔴 **Menu de opções.**

**216.** 🔴 **Localização.** Textos fixos em português no código.

**217.** 🟢 **Distinção visual de estado da criatura.** Branco vagando, âmbar
procurando comida, verde comendo, rosa procurando parceiro. Evidência:
`CreatureRenderer.colorFor():65-72`. É o único item desta auditoria em que o
Mundo Vivo mostra algo que o WorldBox não mostra — e é consequência de não haver
sprites, não uma vantagem de design.

---

## Leitura do placar

**As ausências mais graves, por impacto:**

1. **Interação do jogador** (itens 137 a 180): 44 funcionalidades, nenhuma
   implementada. Sem isso não é um jogo de deus — é uma simulação que se
   assiste.
2. **Dano** (item 156): não existe via de ferir uma criatura. Isso sozinho
   bloqueia combate, guerra, desastres e metade dos poderes.
3. **Espécies** (item 69): uma criatura genérica bloqueia raças, monstros,
   dieta, predação e civilização diferenciada.
4. **Assentamentos** (itens 89 a 101): sem vila não há economia, construção,
   lealdade nem colonização.
5. **Save/load** (200 a 204): o jogador perde o mundo ao fechar o app.
6. **Interface** (188): sem ela, nenhum sistema construído fica acessível.

**Trabalho pronto e subutilizado:**

- **Território** (103): algoritmo correto, testado, recalculado a cada segundo —
  e consumido por ninguém. É o caso mais claro de esforço sem retorno visível.
- **Facções** (102): id herdado corretamente, sem nome, cor ou efeito
  comportamental.
- **Encanamento de edição de terreno** (21): `setTile` e `retile` escritos e
  comentados para os poderes de deus, nunca chamados por código de jogo.
- **Conversão toque para tile** (147): pronta, nunca exercitada.
- **Taxa de rebrota ajustável** (187): setter sem nada que o ajuste.
- **Estatísticas** (192): cinco medidas em memória (a quinta é o contador de
  mortes por dano externo, de 17/09), visíveis só em ferramenta
  de terminal.

**Contagem conferida item a item.** O placar acima foi recontado sobre a lista numerada: 26 verdes, 6 amarelos, 184 vermelhos, 1 incerto.

**Sistemas com backend e sem apresentação:** facções, território, contadores de
estatística, edição de terreno, conversão de toque.

**Sistemas com interface e sem simulação:** nenhum. Não há interface alguma. O
projeto é hoje cem por cento simulação e zero por cento interface — o que é uma
informação sobre a forma do projeto, não sobre sua qualidade.

---

## Gaps estruturais

**Conteúdo puro** — encaixa na arquitetura atual sem sistema novo: biomas
adicionais, tipos de terreno, parâmetros de mundo, cores.

**Conteúdo mais sistema pequeno:** espécies (campo de tipo em `Creature` mais
uma tabela de espécie), animais não civilizados, nomes gerados.

**Sistema novo independente** — podem ser feitos em qualquer ordem, não dependem
uns dos outros:

| Sistema | Depende de | Destrava |
|---|---|---|
| Interação do jogador | item 147, que já existe | 44 funcionalidades |
| Dano externo | nada | combate, desastres, poderes destrutivos |
| Save/load | decidir formato; estado já exposto | persistência inteira |
| Interface | nada tecnicamente | acesso a tudo que já existe |
| Sprites e animação | arte, não arquitetura | apresentação |
| Pathfinding | nada | tudo que se move |

**Cadeias de dependência** — não adianta tentar fora de ordem:

- Genética e traits → dependem de reprodução, **que já existe**. É a exceção
  barata: pronta para começar hoje.
- Combate → dano.
- Exércitos → combate → dano.
- Guerra → exércitos e reinos com identidade.
- Assentamentos → espécies e recursos.
- Construções com tiers → assentamentos e conhecimento.
- Cultura, língua e religião → grupos com identidade persistente. **Bloqueio
  arquitetural real:** hoje a facção não persiste, porque
  `Territory.recompute():88` apaga tudo e recalcula do zero a cada segundo.
  Cultura exige memória histórica, que a arquitetura atual não tem.
- Diplomacia → reinos com identidade e fronteira com consequência.
- Rebelião → diplomacia e lealdade.
- Sucessão e dinastia → famílias → `Creature` guardar ascendência, o que hoje
  `reset():68-80` não faz.

**Conclusão de sequenciamento.** Dois itens destravam desproporcionalmente mais
do que qualquer outro: **interação do jogador**, com 44 funcionalidades
bloqueadas e a infraestrutura já escrita, e **dano**, que abre combate,
desastres e poderes destrutivos de uma vez. Genética e traits são a exceção
barata, por dependerem apenas de reprodução, que já funciona.

---

## Fontes

- Changelog oficial: https://www.superworldbox.com/changelog
- Página na Steam: https://store.steampowered.com/app/1206560/WorldBox__God_Simulator/
- Wikipédia: https://en.wikipedia.org/wiki/WorldBox
- NamuWiki: https://en.namu.wiki/w/WorldBox%20-%20God%20Simulator

O Fandom (the-official-worldbox-wiki.fandom.com) recusou acesso automatizado
durante esta auditoria.

---

## Histórico de entregas

Esta seção registra o que mudou no repositório **depois** do commit auditado
(`093b09a`), para que o documento não envelheça em silêncio. Cada entrada diz o
item, o commit e o que efetivamente passou a existir.

| Data | Item | De → Para | Commit | Evidência |
|---|---|---|---|---|
| 17/09/2026 | 156 — Dano por fonte externa | 🔴 → 🟢 | `15dd0f8` | `Creature.applyDamage`, `CreatureConfig.hazardDamagePerSecond`, `TileType.hazardous()`, checagem no tick de `Simulation`; 68/68 JUnit, 65/65 `SimSelfTest`, CI verde |

**Por que este item foi escolhido primeiro.** Era o mais barato do quadro e o de
maior retorno por unidade de esforço: um único item de auditoria, e atrás dele
estavam 36 itens que não podiam existir sem uma via de tirar vida — combate
(81–88), poderes de destruição (148–155), desastres (157–165) e guerra
(126–136). Nenhum outro item da lista tem essa razão.

**O que ele não resolveu.** Não tornou nada visível para o jogador. O próximo
salto de percepção depende de interação do jogador (137–147), cuja peça central
— converter um toque em coordenada de tile — já está escrita e testada em
`WorldRenderer.tileX():107` e continua sem nenhum chamador.
