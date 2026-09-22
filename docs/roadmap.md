# Roadmap

As funcionalidades planejadas para o Mundo Vivo, uma por linha, com o
estado de cada uma e a evidência no código: arquivo, classe e linha.

Este é o **único lugar** com números de progresso do projeto. Os outros
documentos apontam para cá.

**Levantamento inicial:** 17/09/2026, commit `093b09a`.
**Atualizado até:** commit `e35143b`. Veja o [histórico de entregas](#histórico-de-entregas).
**Referências de linha:** conferidas contra `e35143b`. O histórico mantém as
linhas da época em que cada registro foi escrito.

---

## Como ler o estado de cada item?

| Estado | Quer dizer |
|---|---|
| 🟢 implementado e testado | funciona em toda partida e tem teste |
| 🔵 implementado, sem consumidor na tela | o código existe e tem teste, mas nada no jogo o usa ou mostra |
| 🟡 parcial | existe uma parte relevante e falta outra parte relevante |
| ⚪ planejado | ainda não existe |
| 🔴 problema conhecido | existe, mas tem um defeito conhecido |

**Como a evidência é conferida.** Cada item cita onde está no código. Onde
um método existe, confere-se quem o chama: código chamado só por testes
fica em 🔵, nunca em 🟢. Buscas por palavra foram conferidas uma a uma para
separar ocorrência real de falso positivo — "war" aparece dentro de
`moveToward`, "city" dentro de `capacity`, "gene" dentro de `WorldGenerator`.

**O que entra na lista.** Funcionalidade que o jogador faz ou vê. Decisões
internas de engenharia (pool de objetos, alocação, nomes de classe) não
entram, mesmo quando são boas decisões. Elas estão em
[`docs/`](.).

---

## Quanto já existe?

| Estado | Itens | Parcela |
|---|---|---|
| 🟢 implementado e testado | 32 | 14,7% |
| 🔵 implementado, sem consumidor na tela | 3 | 1,4% |
| 🟡 parcial | 6 | 2,8% |
| ⚪ planejado | 176 | 81,1% |
| 🔴 problema conhecido | 0 | 0,0% |
| **Total** | **217** | |

**Progresso: ~17,5%**, contando 🟢 e 🔵 inteiros e 🟡 pela metade:
(32 + 3 + 3) ÷ 217.

Um item está com classificação incerta: o **145**, explicado no próprio
item.

---

## 🌍 1. Mundo e terreno

**1. Geração procedural de terreno** — 🟢 implementado e testado. Ruído fractal (fBm: várias camadas de ruído somadas). Evidência: `WorldGenerator.generate()`, `FractalNoise.java`, classificação em `WorldGenerator.java:125`. Testado em `WorldGeneratorTest`.

**2. Determinismo por semente** — 🟢 implementado e testado. Mesma semente produz mundo idêntico. Evidência: `WorldConfig.seed:18`, `Rng.java`, `Simulation.java:106`.

**3. Biomas distintos** — 🟢 implementado e testado. 16 tipos de terreno. Evidência: `TileType.java:27-42`. Falta: biomas fantásticos (cogumelo, mágico, corrompido, cristal).

**4. Água em profundidades diferentes** — 🟢 implementado e testado. `DEEP_OCEAN`, `OCEAN`, `SHALLOW_WATER`, com cor própria e intransponíveis. Evidência: `TileType.java:27-29`.

**5. Montanhas e cordilheiras** — 🟢 implementado e testado. `MOUNTAIN` e `SNOW_PEAK` não caminháveis; ruído de cristas produz cordilheiras alongadas em vez de manchas. Evidência: `WorldConfig.mountainRidges:49`.

**6. Ilhas e arquipélagos** — 🟢 implementado e testado. Afundamento de borda isola massas de terra. Evidência: `WorldConfig.islandFalloff:55`.

**7. Temperatura como variável de geração** — 🟢 implementado e testado. Evidência: `WorldConfig.temperatureFrequency:36`, `altitudeCooling:61`, consumidas em `WorldGenerator.java:125`. Falta: não é estado vivo — não muda depois da geração, e não há array de temperatura no mundo.

**8. Umidade como variável de geração** — 🟢 implementado e testado. Evidência: `WorldConfig.moistureFrequency:33`.

**9. Tamanho de mundo variável** — 🟡 parcial. Existem os presets `small()` 128x96, `medium()` 256x192 e `large()` 384x288 em `WorldConfig.java:73-85`, mas o jogo chama `medium` fixo em `MundoVivoGame.java:72`. Falta: o jogador escolher o tamanho. Depende de: interface.

**10. Tipos/presets de mundo** — ⚪ planejado. Formatos de mundo escolhíveis (continente, arquipélago, anel, xadrez). Hoje há um único algoritmo.

**11. Rios** — ⚪ planejado. Busca por "river/rio" em todo o repositório: nenhuma ocorrência.

**12. Lagos como entidade** — ⚪ planejado. Água interior existe por acidente do ruído, sem modelagem própria.

**13. Vegetação como entidade** — ⚪ planejado. Árvores individuais, contáveis, que crescem e queimam. Hoje a vegetação é só a fertilidade de cada tile. Evidência: `TileType.fertility():77`.

**14. Crescimento e propagação de vegetação** — ⚪ planejado. Existe apenas regeneração numérica de comida. Evidência: `FoodMap.regrow():108`.

**15. Recursos de terreno** — ⚪ planejado. Minérios e materiais (cobre, ferro, prata) ligados ao conhecimento de cada cultura. Busca por "resource/minério/ore": nada.

**16. Estradas** — ⚪ planejado. Tiles de estrada que aceleram quem anda sobre eles.

**17. Terreno cultivado** — ⚪ planejado. Pincel para criar terra cultivada.

**18. Erosão** — ⚪ planejado. O terreno se desgasta com o tempo, como regra do mundo.

**19. Propagação ambiental** — ⚪ planejado. Fogo que alastra, lava que esfria, ácido que corrói. Busca por "fire/lava/acid": nada.

**20. Transformação de bioma em cadeia** — ⚪ planejado. Biomas que se espalham e transformam os vizinhos (corrupção, congelamento).

**21. Alteração de terreno em tempo de execução** — 🟡 parcial. A base existe e não é usada: `World.setTile()`, `WorldRenderer.setTile():81` e `FoodMap.retile():129` estão escritos, e o comentário em `WorldRenderer.java:79` diz textualmente "é por aqui que os poderes de deus deverão modificar o terreno" — mas nenhum código de jogo os chama. Verificação: os únicos chamadores são `SimSelfTest.java:510,563,580`, `TerritoryTest` e o próprio `WorldGenerator:125`. Depende de: sistema de interação.

**22. Ilhas identificadas como entidade** — ⚪ planejado. Contar as ilhas e mostrar o número nas estatísticas.

**23. Camadas de visualização do mapa** — ⚪ planejado. Alternar camadas do mapa (bioma, território, população). Hoje há uma camada só, a cor do bioma. Evidência: `WorldRenderer.render():92-104` desenha uma textura só.

**24. Minimapa** — ⚪ planejado.

---

## ⚙️ 2. Clima, eras e tempo

**25. Estações do ano** — ⚪ planejado. Busca por "season/estação": nada.

**26. Clima dinâmico** — ⚪ planejado. Chuva, neve caindo. Busca por "weather/clima": nada.

**27. Ciclo dia/noite** — ⚪ planejado.

**28. Eras do Mundo** — ⚪ planejado. Eras que mudam biomas e criaturas por um período. Hoje há só `Simulation.elapsedSeconds:140` como relógio cru.

**29. Progressão histórica com efeito global** — ⚪ planejado.

**30. Tempo em segundos, independente de quadros** — 🟢 implementado e testado. Entra na lista porque tem efeito observável: a simulação se comporta igual a 30 e a 60 quadros por segundo. Evidência: `Simulation.step(float):188`, `MAX_STEP_SECONDS:45`.

**31. Controle de velocidade da simulação** — ⚪ planejado. Rodar a simulação a 2× e 3×. Hoje roda sempre a 1×: `MundoVivoGame.render():136` passa `Gdx.graphics.getDeltaTime()` direto, sem multiplicador.

**32. Pausa** — ⚪ planejado. Mesma evidência acima.

**33. Eventos aleatórios do mundo** — ⚪ planejado.

**34. Invasões** — ⚪ planejado.

**35. Histórico de acontecimentos** — ⚪ planejado. Registro de fundações, guerras, tratados e mortes de líderes. Hoje há só contadores globais: `Simulation.births():148`, `deathsByStarvation()`, `deathsByOldAge():156` e `deathsByCombat():176`.

---

## 🐾 3. Criaturas — ciclo de vida

**36. Criaturas autônomas** — 🟢 implementado e testado. Evidência: `Simulation.stepCreature():214`, máquina de estados em `CreatureState.java`.

**37. Fome como necessidade** — 🟢 implementado e testado. Evidência: `Creature.hunger:36`, `config.hungerPerSecond` aplicado em `Simulation:221`.

**38. Busca de comida** — 🟢 implementado e testado. Busca em anéis quadrados crescentes, que termina no primeiro acerto. Evidência: `Simulation.findFoodNear():659`.

**39. Alimentação e saciedade** — 🟢 implementado e testado. Evidência: `Simulation.stepEating():301`, `FoodMap.consume():85`.

**40. Morte por inanição** — 🟢 implementado e testado. A fome no máximo tira vida por `Creature.applyDamage` com a causa `CAUSE_STARVATION`, em `Simulation:223-228`; a morte é resolvida no caminho único `Simulation.resolveDeathFromDamage():441`, contador `deathsByStarvation`.

**41. Envelhecimento** — 🟢 implementado e testado. Evidência: `Creature.age:33`, limite individual `c.maxAgeSeconds` — herdado, ver item 66 — com `config.maxAgeSeconds` como base, em `Simulation:257`.

**42. Morte por velhice** — 🟢 implementado e testado. Com contador próprio. Evidência: `Simulation:257-261`.

**43. Vida e regeneração** — 🟢 implementado e testado. Evidência: `Creature.health:39`, regeneração quando saciada em `Simulation:226-228`.

**44. Maturidade** — 🟢 implementado e testado. Evidência: `Creature.isAdult():229`, `config.adultAgeSeconds`.

**45. Reprodução sexuada** — 🟢 implementado e testado. Exige dois adultos próximos. Evidência: `Simulation.reproduce(a, b):345`, `resolveMate():542`.

**46. Custo biológico da reprodução** — 🟢 implementado e testado. Ambos os pais pagam fome e entram em espera. Evidência: `Simulation:346-349`.

**47. Cadáver vira alimento** — 🟢 implementado e testado. Evidência: `Simulation.die():456` chama `foodMap.deposit(..., config.corpseFoodValue)`.

**48. Teto populacional** — 🟢 implementado e testado. Pool cheio não gera filho. Evidência: `Simulation:368`.

**49. Bloqueio de terreno intransponível** — 🟢 implementado e testado. Evidência: `Simulation.moveTo():611` rejeita tile não caminhável.

**50. Reprodução assexuada e partenogênese** — ⚪ planejado. Mais de um jeito de reproduzir.

**51. Dieta e cadeia alimentar** — ⚪ planejado. Herbívoros, carnívoros e morte por predação. Hoje toda criatura come do mesmo `FoodMap`; não há predador nem outra fonte de alimento.

**52. Sede** — ⚪ planejado.

**53. Sono e descanso** — ⚪ planejado.

**54. Pathfinding real** — ⚪ planejado. Contornar obstáculos em vez de andar em linha reta. Hoje, quando o passo bate em água, o alvo é descartado e outro é sorteado. Evidência: `Simulation.moveTo():611-622`, com o comentário explicando a decisão. Registrado na dívida técnica.

**55. Nível e progressão individual** — ⚪ planejado.

---

## 🧬 4. Genética, traits e condições

**56. Genes e herança genética** — 🟢 implementado e testado. *Entregue em 19/09/2026, commit `e35143b`. Antes: ⚪ planejado.* Cada criatura carrega um genoma de 8 blocos de 32 bits (`Creature.genome:88`, `Genome.BLOCKS:36`); cada bloco se expande em genes por hash puro (`Genome.gene():94`). O filho recebe blocos inteiros de cada pai por moeda justa (`Inheritance.cross():46`), chamado em todo nascimento em `Simulation:364`; fundadores recebem genoma sorteado em `Simulation:738`. Herdabilidade medida em `HeritabilityTest`: inclinação 1,008 da regressão filho × média dos pais, contra 0,020 do esquema ingênuo `filho = hash(pai, mãe)`. **Ressalva:** o jogador não vê genes — não há painel nem editor (editor é o item 58). A herança é exercitada em toda partida e se manifesta em comportamento (itens 65 e 66).

**57. Traits** — ⚪ planejado. Catálogo de traços com nome, visíveis e editáveis pelo jogador (de combate, de sobrevivência, de comportamento). Hoje há quatro traços contínuos e ocultos derivados do genoma — velocidade, visão, metabolismo e longevidade, em `Phenotype.apply():88` —, mas não esse catálogo.

**58. Editor de traits** — ⚪ planejado.

**59. Mutações** — 🟢 implementado e testado. *Entregue em 19/09/2026, commit `e35143b`. Antes: ⚪ planejado.* A cada nascimento, cada bloco do genoma tem `CreatureConfig.mutationRatePerBlock:103` (2%) de chance de ter um bit invertido (`Inheritance.mutate():69`), chamado em `Simulation:365`. Testado em `InheritanceTest`: um bit por bloco mutado, na taxa pedida. **Ressalva:** mutação não é visível; aparece só como variação nos traços.

**60. Metamorfose** — ⚪ planejado.

**61. Subespécies emergentes** — ⚪ planejado.

**62. Doenças e epidemias** — ⚪ planejado. Doenças, epidemias e contagem de mortes por doença. Busca por "disease/plague/doença": nada.

**63. Status effects temporários** — ⚪ planejado. Bênção, maldição, loucura, escudo, aceleração.

**64. Imunidades** — ⚪ planejado. Fogo, veneno, vida eterna.

**65. Herança de características dos pais** — 🟢 implementado e testado. *Entregue em 18–19/09/2026, commit `a95c4d3` e `e35143b`. Antes: ⚪ planejado.* O filho herda a espécie (`Simulation:385`) e o genoma (`Simulation:364`), e seus traços são recalculados do genoma herdado (`Phenotype.apply()`, chamado em `Simulation:371`). A inclinação de 1,008 em `HeritabilityTest` mostra que o traço do filho acompanha a média dos pais.

**66. Variação individual** — 🟢 implementado e testado. *Entregue em 19/09/2026, commit `e35143b`. Antes: ⚪ planejado.* Cada criatura tem velocidade, visão, metabolismo e idade máxima próprios (`Creature.speedTilesPerSecond:96` a `Creature.maxAgeSeconds:105`), entre 0,7× e 1,3× da base do `CreatureConfig`. Os quatro são lidos pela simulação: velocidade em `Simulation:592`, visão em `Simulation.visionOf():654`, metabolismo em `Simulation:221`, idade máxima em `Simulation:257`. **Ressalva:** a variação existe e muda comportamento, mas não é exibida. `Creature.size` é calculado e ainda não é lido por nada.

**67. Nomes individuais** — ⚪ planejado. Cada criatura com nome próprio. Hoje há só `Creature.id:26`, um inteiro incremental.

**68. Favoritar criatura** — ⚪ planejado. Marcar uma criatura como favorita para acompanhá-la.

---

## 🐾 5. Espécies, raças e monstros

**69. Múltiplas espécies** — 🟡 parcial. *Parcial desde 18/09/2026, commit `a95c4d3`. Antes: ⚪ planejado.* Existe identidade de espécie herdável (`Creature.species:74`, `Species:28`), sorteada nos fundadores (`Simulation:754`) e herdada pelo filho (`Simulation:385`). Ela tem consequência: espécies diferentes não formam par (`Simulation:546` e `Simulation:559`). **Falta:** aparência, atributos e comportamento próprios. Hoje são duas espécies abstratas, idênticas em tudo menos em quem pode ter filho com quem, e o jogador não consegue distingui-las.

**70. Raças civilizáveis** — ⚪ planejado. Raças com tendências culturais próprias. Hoje existe espécie (ver item 69), mas sem cultura, civilização nem tendência de comportamento associada.

**71. Atributos raciais e hostilidade entre raças** — ⚪ planejado. Hostilidade definida pela raça, e não só pelo reino.

**72. Animais não civilizados** — ⚪ planejado. Animais que não formam reino, com contagem própria.

**73. Criação de espécie pelo jogador** — ⚪ planejado.

**74. Mortos-vivos** — ⚪ planejado. Zumbis, esqueletos, fantasmas.

**75. Demônios** — ⚪ planejado.

**76. Dragões** — ⚪ planejado.

**77. Slimes, ratos, vermes** — ⚪ planejado.

**78. Alienígenas e UFO** — ⚪ planejado.

**79. Chefes colossais** — ⚪ planejado.

**80. Bandidos e unidades hostis sem reino** — ⚪ planejado.

---

## ⚔️ 6. Combate e exércitos

**81. Combate corpo a corpo** — 🟢 implementado e testado. *Entregue em 18/09/2026, commit `d997dc8`. Antes: ⚪ planejado.* Criaturas de facções diferentes a até `CreatureConfig.combatRangeTiles:146` (1,5 tile) se ferem a cada passo, pela mesma `Creature.applyDamage`, com causa `Creature.CAUSE_COMBAT:128`. O dano é `CreatureConfig.combatDamagePerSecond:166` multiplicado pelo número de inimigos ao alcance, então estar em menor número mata mais rápido. Evidência: `Simulation.hostileNeighbours():499`, aplicado em `Simulation:249`, contador `Simulation.deathsByCombat():176`. Medido na entrega: 538 mortes em combate em 6 sementes de 20 minutos, sem extinção. **Ressalva:** o combate é reação a proximidade, sem perseguição — escopo definido assim de propósito. Não há representação visível: o jogador vê criaturas morrendo, não vê a luta nem de que lado cada uma está. Quem procura parceiro não luta nem apanha (`Simulation:500`).

**82. Combate à distância e projéteis** — ⚪ planejado.

**83. Exércitos e unidades militares** — ⚪ planejado.

**84. Treinamento militar por conhecimento** — ⚪ planejado.

**85. Equipamento e materiais** — ⚪ planejado.

**86. Moral e lealdade em batalha** — ⚪ planejado.

**87. Cerco a assentamentos** — ⚪ planejado.

**88. Magia usada por unidades** — ⚪ planejado. Ritos e feitiços lançados pelas próprias criaturas.

---

## 👑 7. Assentamentos e construção

**89. Vilas como entidade** — ⚪ planejado. Vilas com área definida, que crescem. Busca por "village/vila": nada.

**90. Cidades** — ⚪ planejado.

**91. Capitais** — ⚪ planejado. Capital permanente, que dá lealdade ao reino. Hoje a capital só existe durante a fundação (ver item 102).

**92. Fundação de assentamento por colonos** — ⚪ planejado.

**93. Casas com níveis de evolução** — ⚪ planejado. Casas que evoluem em níveis, de tenda a pedra.

**94. Prefeitura com tiers** — ⚪ planejado.

**95. Estruturas militares** — ⚪ planejado. Quartel, torre de vigia.

**96. Infraestrutura econômica** — ⚪ planejado. Mina, poço, moinho, porto.

**97. Monumentos e estátuas** — ⚪ planejado.

**98. Fogueira como primeiro estágio** — ⚪ planejado.

**99. Destruição de construções** — ⚪ planejado.

**100. Ruínas e reconstrução** — ⚪ planejado.

**101. Estoque de recursos por assentamento** — ⚪ planejado.

---

## 👑 8. Reinos e governo

**102. Grupos/facções como entidade** — 🟡 parcial. A facção tem nome e cor próprios (`FactionRegistry:82-83`), e o mundo funda `CreatureConfig.initialFactions:87` (quatro) reinos contíguos em torno de capitais espaçadas (`Simulation.placeFactionCentres():788`). Criaturas de reinos diferentes lutam (item 81). **Falta:** a capital é usada só na fundação e não fica guardada; não há líder; e nome e cor não aparecem para o jogador (ver itens 104 e 106).

**103. Território de facção** — 🔵 implementado, sem consumidor na tela. Calculado por busca em largura multi-fonte sobre tiles caminháveis, contornando água em vez de cortá-la, e recalculado uma vez por segundo. Evidência: `Territory.recompute():87-117`, agendamento em `Simulation:204-211`. **Sem consumidor:** o território não é desenhado, não altera comportamento e não gera conflito. Verificação: busca por "faction|territor" em `render/` e em `MundoVivoGame.java` retorna zero ocorrências. Antes: 🟡 parcial; a nova legenda separa "pronto sem consumidor" de "parcial".

**104. Nomes de reino gerados** — 🔵 implementado, sem consumidor na tela. Cada facção recebe um nome sorteado da semente do mundo em `FactionRegistry.create()`, lido por `FactionRegistry.nameOf():141`, com teste. **Sem consumidor:** nada fora dos testes chama `nameOf`. Vira 🟢 implementado e testado quando o nome aparecer para o jogador.

**105. Bandeiras e símbolos** — ⚪ planejado. Bandeiras e símbolos de reino, personalizáveis.

**106. Cor de reino** — 🔵 implementado, sem consumidor na tela. Cada facção recebe uma cor em `FactionRegistry.colorOf():150`, com teste. **Sem consumidor:** nada fora dos testes a usa — na tela, a cor da criatura indica estado, não facção (`CreatureRenderer.colorFor(CreatureState):65`). Vira 🟢 implementado e testado quando a cor de facção for desenhada.

**107. Reis e líderes** — ⚪ planejado. Líderes com atributos de diplomacia, liderança e guerra. Busca por "king/leader/líder": nada.

**108. Sucessão** — ⚪ planejado.

**109. Famílias e árvore genealógica** — ⚪ planejado. O filho não guarda referência aos pais. Evidência: `Creature.reset():146-177` não registra ascendência.

**110. Linhagem e dinastia** — ⚪ planejado.

**111. Federação de vilas em reino** — ⚪ planejado.

**112. Lealdade de assentamento** — ⚪ planejado. Lealdade calculada a partir de capital, proximidade, cultura, qualidade do líder e distância.

**113. Expansão territorial ativa** — ⚪ planejado. Reinos que conquistam e guardam terra. Hoje o território só reflete onde as criaturas estão agora; ninguém conquista nada. Evidência: `Territory.recompute` começa com `Arrays.fill(ownerFaction, UNCLAIMED)` na linha 88 — não há memória de posse anterior. Fundar reinos contíguos (item 102) não é expansão.

**114. Queda de reino com consequência** — ⚪ planejado. `FactionRegistry.isExtinct():156` existe, mas nada acontece quando uma facção zera.

---

## 👑 9. Sociedade profunda

**115. Clãs** — ⚪ planejado. Clãs com chefes e árvore familiar.

**116. Chefes de clã** — ⚪ planejado.

**117. Culturas** — ⚪ planejado. Culturas que se propagam, se substituem e se difundem em velocidades diferentes.

**118. Línguas com falantes** — ⚪ planejado.

**119. Religiões com sacerdotes e seguidores** — ⚪ planejado.

**120. Conversão religiosa** — ⚪ planejado.

**121. Conhecimento e tecnologia por cultura** — ⚪ planejado. Conhecimento compartilhado entre membros, que destrava construções, materiais e forças militares.

**122. Difusão cultural entre grupos** — ⚪ planejado.

**123. Opiniões e relações entre indivíduos** — ⚪ planejado. Hoje a única relação entre duas criaturas é o alvo temporário de acasalamento: `Creature.targetMateSlot:47`.

**124. Identidade coletiva com efeito mecânico** — ⚪ planejado. A facção não altera comportamento nenhum: duas criaturas de facções diferentes se reproduzem normalmente. Evidência: `Simulation.resolveMate():542-572` não filtra por `factionId`.

**125. Onomástica** — ⚪ planejado. Geração de nomes para pessoas, cidades e reinos.

---

## ⚔️ 10. Diplomacia e guerra

**126. Relações diplomáticas entre reinos** — ⚪ planejado. Opinião entre reinos, calculada por diferença de poder, inimigo comum, líder, cultura e fronteira.

**127. Alianças** — ⚪ planejado. Alianças em níveis, com benefícios crescentes.

**128. Guerras de conquista** — ⚪ planejado.

**129. Rebeliões** — ⚪ planejado.

**130. Tréguas e tratados de paz** — ⚪ planejado.

**131. Guerra por aliança** — ⚪ planejado.

**132. Fronteiras disputadas** — ⚪ planejado. Hoje as fronteiras existem no dado, mas não geram atrito — ver item 103. Criaturas lutam por proximidade, não por fronteira.

**133. Pilhagem de fronteira** — ⚪ planejado.

**134. Histórico de guerras** — ⚪ planejado.

**135. Multidões revoltadas** — ⚪ planejado.

**136. Conspirações** — ⚪ planejado.

---

## ✋ 11. Poderes do jogador — terreno e criação

**137. Pincel de terreno** — ⚪ planejado. Pintar terreno com o dedo. Hoje não há poder que altere terreno. O jogador tem um único poder, o toque que fere a criatura tocada (ver item 147). O toque longo continua descartando o mundo e gerando outro (`CameraController.longPress():153` → `MundoVivoGame.regenerate():109`).

**138. Sementes de bioma** — ⚪ planejado.

**139. Ferramenta de cópia** — ⚪ planejado.

**140. Ferramenta de embaralhar terreno** — ⚪ planejado.

**141. Borracha** — ⚪ planejado.

**142. Remoção de água e lava** — ⚪ planejado.

**143. Ferramentas de corte e escavação** — ⚪ planejado.

**144. Demolidor de construções** — ⚪ planejado.

**145. Apagador de vida** — ⚪ planejado. Apagar criaturas numa área, com pincel. Existe um poder que mata criaturas: cada toque tira `CreatureConfig.playerStrikeDamage` (0,34) de vida da criatura no tile tocado, e três toques matam (`Simulation.strikeAt():416`).
**Classificação incerta:** não está decidido se ferir uma criatura por toque conta como este item, que age em área. Fica ⚪ planejado até essa decisão.

**146. Tamanho de pincel ajustável** — ⚪ planejado.

**147. Conversão toque para tile** — 🟢 implementado e testado. *Entregue em 17/09/2026, commit `5b03519`. Antes: classificação incerta.* A conversão foi extraída para `TileMapping.tileX():42` e `TileMapping.tileY():53`, que já compensam a inversão vertical do desenho, e agora é chamada em todo toque: `CameraController.tap():162` → `MundoVivoGame:66` → `Simulation.strikeAt():416`. Testado em `TileMappingTest`, incluindo a ida e volta: onde a criatura é desenhada é onde o toque a encontra.

---

## ✋ 12. Poderes — destruição

**148. Explosivo básico** — ⚪ planejado.

**149. Bomba atômica** — ⚪ planejado.

**150. Bomba de grande área** — ⚪ planejado.

**151. Bomba-relógio** — ⚪ planejado.

**152. Minas e granadas** — ⚪ planejado.

**153. Napalm** — ⚪ planejado.

**154. Bomba d'água** — ⚪ planejado.

**155. Poderes cômicos** — ⚪ planejado.

**156. Dano a criaturas por fonte externa** — 🟢 implementado e testado. *Entregue em 17/09/2026, commit `15dd0f8`. Antes: ⚪ planejado.* Existe `Creature.applyDamage(float amount, String cause)`, genérico por desenho: não mata por conta própria, devolve se o golpe foi fatal e deixa `Simulation` tratar a morte no mesmo ponto que já tratava a inanição, preservando um único caminho de morte (cadáver vira comida, sai da facção, slot volta ao pool). A inanição passou a rotear por ele, então a via é exercitada em toda partida. Primeiro consumidor externo: terreno perigoso (`DEEP_OCEAN`, escolhido porque não existe lava no enum), com taxa em `CreatureConfig.hazardDamagePerSecond`. Verificação: 68/68 na suíte JUnit, 65/65 no `SimSelfTest`, CI verde, e populações finais das sementes de regressão idênticas às de antes (610 / 268 / 394). **Ressalva:** o gatilho de terreno fica ocioso em partida normal, porque o movimento recusa terreno não caminhável — nenhuma criatura pisa na água por vontade própria. A via existe e está testada; o jogador ainda não a sente. Ela acorda quando existir poder de deus (itens 137–147) ou desastre (157–165).

---

## ✋ 13. Poderes — natureza e desastres

**157. Raio** — ⚪ planejado.

**158. Meteoro** — ⚪ planejado.

**159. Tornado** — ⚪ planejado.

**160. Terremoto** — ⚪ planejado.

**161. Vulcão e lava** — ⚪ planejado.

**162. Chuva ácida** — ⚪ planejado.

**163. Fogo que alastra** — ⚪ planejado.

**164. Onda de choque** — ⚪ planejado.

**165. Manipulação de temperatura** — ⚪ planejado. A temperatura do Mundo Vivo existe só como variável de geração; não há array de temperatura vivo no mundo — ver item 7.

---

## ✋ 14. Poderes — efeitos e condições

**166. Chuva de cura** — ⚪ planejado.

**167. Chuva de loucura** — ⚪ planejado.

**168. Bênçãos e maldições** — ⚪ planejado.

**169. Epidemia como poder** — ⚪ planejado.

**170. Infecção zumbi** — ⚪ planejado.

**171. Esporos de cogumelo** — ⚪ planejado.

**172. Estruturas mágicas do jogador** — ⚪ planejado.

---

## ✋ 15. Poderes — civilização e unidades

**173. Spawn de criatura pelo jogador** — ⚪ planejado. Não há nenhuma via de criação manual: toda criatura nasce em `Simulation.spawnInitialPopulation():707`, na construção do mundo, ou em `reproduce():345`.

**174. Remoção de unidade** — ⚪ planejado.

**175. Declarar guerra** — ⚪ planejado.

**176. Forçar paz** — ⚪ planejado.

**177. Separar reino** — ⚪ planejado.

**178. Instigar guerra entre reinos** — ⚪ planejado.

**179. Possessão de unidade** — ⚪ planejado.

**180. Alterar raça de uma unidade** — ⚪ planejado.

---

## ⚙️ 16. Leis do mundo

**181. Painel de leis do mundo** — ⚪ planejado. Chaves liga/desliga para as regras do mundo. Hoje há `CreatureConfig`, que é configuração de código ajustada em teste, não painel de jogo.

**182. Desligar fome** — ⚪ planejado.

**183. Desligar envelhecimento** — ⚪ planejado.

**184. Desligar diplomacia, rebelião e expansão** — ⚪ planejado. Os sistemas nem existem.

**185. Ligar e desligar desastres** — ⚪ planejado.

**186. Ligar e desligar spawn de animais** — ⚪ planejado.

**187. Regras de crescimento da natureza** — ⚪ planejado. A rebrota é fixa em código, mas existe um setter sem interface que o chame: `FoodMap.regrowthPerSecond(float):56`.

---

## 🎨 17. Interface, inspeção e estatísticas

**188. Interface de usuário** — ⚪ planejado. O jogo não tem UI nenhuma. O laço de desenho produz exatamente dois objetos: terreno e criaturas. Evidência: `MundoVivoGame.render():135-146`.

**189. Barra de poderes com categorias** — ⚪ planejado.

**190. Inspeção de criatura** — ⚪ planejado. O `toString()` de `Creature` (linhas 108-116) formata estado, fome, vida, idade e facção, mas isso só aparece em log de terminal, nunca na tela.

**191. Janela de vila e reino** — ⚪ planejado.

**192. Estatísticas de mundo em jogo** — ⚪ planejado. Os dados existem em memória — `population()`, `births()`, contadores de morte por causa, `foodMap.totalFood()` —, mas não há tela que os mostre. Só as ferramentas de linha de comando `tools/SimulationReport.java` e `tools/SimulationPreview.java`, que rodam fora do jogo.

**193. Gráficos de população ao longo do tempo** — ⚪ planejado. Existem no relatório de terminal, não no jogo.

**194. Log de acontecimentos na tela** — ⚪ planejado.

**195. Notificações de evento** — ⚪ planejado.

**196. Seleção de unidade** — ⚪ planejado. Tocar para selecionar uma criatura. Hoje o toque fere a criatura tocada (item 147) e não seleciona nada: `CameraController.tap():162` → `MundoVivoGame:66` → `Simulation.strikeAt():416`.

**197. Filtros e modos de visualização** — ⚪ planejado.

**198. Conquistas** — ⚪ planejado.

**199. Câmera com arraste, pinça e roda do mouse** — 🟢 implementado e testado. Com limites corretos — o zoom máximo é recalculado a cada redimensionamento para o mundo caber na tela. Evidência: `CameraController.pan():132`, `zoom():142`, `scrolled():194`, `resize():73`.

---

## ⚙️ 18. Persistência, editor e compartilhamento

**200. Salvar mundo** — ⚪ planejado. Não há código de gravação.

**201. Carregar mundo** — ⚪ planejado.

**202. Fundação para serialização** — 🟡 parcial. Os acessos brutos ao estado já estão expostos de propósito: `World.rawTiles():117`, `FoodMap.rawAmount():147`, estado interno do `Rng` na linha 23, e a semente é guardada. Falta o formato e o código de escrita e leitura. Classificado como parcial por ser preparação deliberada e verificável, não funcionalidade.

**203. Múltiplos slots de save** — ⚪ planejado.

**204. Autosave** — ⚪ planejado.

**205. Editor de mapa** — ⚪ planejado.

**206. Compartilhamento de mapas** — ⚪ planejado.

**207. Importação e exportação de mundo** — ⚪ planejado.

**208. Mods** — ⚪ planejado.

**209. Regenerar mundo** — 🟡 parcial. Existe, mas amarrado a um atalho de desenvolvimento: toque longo em qualquer lugar apaga tudo sem confirmação. Evidência: `MundoVivoGame.regenerate():109`, documentado na própria classe nas linhas 27-28 como atalho que "sai quando a interface de verdade entrar".

---

## 🎨 19. Apresentação

**210. Sprites de criatura** — ⚪ planejado. Cada criatura é um quadrado de cor sólida. Evidência: `CreatureRenderer.SIZE:28` e `batch.draw(pixel, ...)` na linha 60 — a textura é literalmente um pixel branco esticado, criado nas linhas 41-46.

**211. Animação** — ⚪ planejado. A posição muda, o desenho não.

**212. Sombreamento e relevo visual** — ⚪ planejado. `WorldRenderer.paintAll():68-75` escreve `colorRgba8888()` puro, sem modulação por altitude.

**213. Efeitos visuais** — ⚪ planejado. Partículas, explosões, fumaça.

**214. Som e música** — ⚪ planejado. Busca por "sound/music/audio": nada.

**215. Menu de opções** — ⚪ planejado.

**216. Localização** — ⚪ planejado. Textos fixos em português no código.

**217. Distinção visual de estado da criatura** — 🟢 implementado e testado. Branco vagando, âmbar procurando comida, verde comendo, rosa procurando parceiro. Evidência: `CreatureRenderer.colorFor():65-72`. É consequência de não haver sprites, não uma decisão de arte.

---

---

## O que falta de mais importante?

1. **Interação do jogador** (itens 137 a 180): 44 funcionalidades, uma
   implementada (147, o toque que chega à criatura). Sem o resto, o jogo é
   uma simulação que se assiste.
2. **Interface** (188): sem ela, nenhum sistema pronto fica acessível.
3. **Salvar e carregar** (200 a 204): o jogador perde o mundo ao fechar o
   app.
4. **Espécies** (69): parcial. As duas espécies não cruzam entre si, mas
   são idênticas em todo o resto. Isso segura raças, monstros, dieta,
   predação e civilizações diferentes.
5. **Assentamentos** (89 a 101): sem vila não há economia, construção,
   lealdade nem colonização.

**Pronto e sem uso na tela:**

- **Território** (103): correto, testado, recalculado a cada segundo, e
  consumido por ninguém.
- **Nome e cor de reino** (104, 106): gerados e testados, nunca mostrados.
- **Edição de terreno** (21): `setTile` e `retile` escritos para os
  poderes, nunca chamados por código de jogo.
- **Taxa de rebrota ajustável** (187): um setter sem nada que o ajuste.
- **Estatísticas** (192): seis medidas em memória (população, nascimentos e
  mortes por fome, velhice, dano externo e combate), visíveis só em
  ferramenta de terminal.
- **Genética** (56, 59, 65, 66): muda velocidade, visão, metabolismo e
  longevidade, sem nenhuma forma de o jogador ver.

Hoje o projeto é quase todo simulação e quase nada de interface. Isso
descreve a forma do projeto, não a qualidade.

---

## Em que ordem as coisas dependem umas das outras?

**Conteúdo puro**, que cabe na arquitetura atual sem sistema novo: biomas
adicionais, tipos de terreno, parâmetros de mundo, cores.

**Conteúdo com sistema pequeno:** atributos por espécie, animais sem
reino, nomes de criatura.

**Sistemas novos independentes**, que podem ser feitos em qualquer ordem:

| Sistema | Depende de | Destrava |
|---|---|---|
| Interação do jogador | item 147, que já existe | 44 funcionalidades |
| Salvar e carregar | decidir o formato; o estado já está exposto | persistência inteira |
| Interface | nada, tecnicamente | acesso a tudo que já existe |
| Sprites e animação | arte, não arquitetura | apresentação |
| Busca de caminho | nada | tudo que se move |

**Cadeias de dependência**, que não adianta fazer fora de ordem:

- Exércitos → combate (pronto) → dano (pronto).
- Guerra → exércitos e reinos com identidade.
- Assentamentos → espécies e recursos.
- Construções com níveis → assentamentos e conhecimento.
- Cultura, língua e religião → grupos com identidade que dura. **Bloqueio
  real de arquitetura:** hoje o território não tem memória, porque
  `Territory.recompute():88` apaga tudo e recalcula do zero a cada
  segundo.
- Diplomacia → reinos com identidade e fronteira com consequência.
- Rebelião → diplomacia e lealdade.
- Sucessão e dinastia → famílias → `Creature` guardar os pais, o que hoje
  `reset():146-177` não faz.

---

## Histórico de entregas

Cada linha diz o item, o commit e o que passou a existir. Toda entrega
nova acrescenta uma linha aqui e atualiza o item e a contagem acima.

| Data | Item | Antes → depois | Commit | Evidência |
|---|---|---|---|---|
| 17/09/2026 | 156 — Dano por fonte externa | ⚪ planejado → 🟢 implementado e testado | `15dd0f8` | `Creature.applyDamage`, `CreatureConfig.hazardDamagePerSecond`, `TileType.hazardous()`, checagem no passo de `Simulation`; 68/68 JUnit, 65/65 `SimSelfTest`, CI verde |
| 17/09/2026 | 147 — Conversão toque para tile | incerto → 🟢 implementado e testado | `5b03519` | `TileMapping.tileX():42`, `tileY():53`, chamada em todo toque por `MundoVivoGame:66` → `Simulation.strikeAt():416`; `TileMappingTest` |
| 17/09/2026 | 145 — Apagador de vida | ⚪ planejado, marcado como incerto | `5b03519` | Toque que fere (3 toques matam) em `Simulation.strikeAt():416`; incerto se atende o escopo do item, que age em área |
| 18/09/2026 | 69 — Múltiplas espécies | ⚪ planejado → 🟡 parcial | `a95c4d3` | `Creature.species:74`, `Species:28`, filtro de par em `Simulation:546` e `:559`; espécies sem aparência nem atributos próprios |
| 18/09/2026 | 104 — Nomes de reino | ⚪ planejado → 🔵 implementado, sem consumidor na tela | `a971dbd` | `FactionRegistry.nameOf():141`, com teste; só os testes chamam |
| 18/09/2026 | 106 — Cor de reino | ⚪ planejado → 🔵 implementado, sem consumidor na tela | `a971dbd` | `FactionRegistry.colorOf():150`, com teste; só os testes chamam |
| 18/09/2026 | 81 — Combate corpo a corpo | ⚪ planejado → 🟢 implementado e testado | `d997dc8` | `Simulation.hostileNeighbours():499`, aplicado em `Simulation:249`, `Creature.CAUSE_COMBAT:128`; 538 mortes em combate e 0/6 extinções na medição da entrega |
| 18/09/2026 | 102 — Facções como entidade | 🟡 parcial, sem mudança | `589df6a` | Reinos contíguos em `Simulation.placeFactionCentres():788`; falta capital guardada e líder |
| 19/09/2026 | 56 — Genes e herança genética | ⚪ planejado → 🟢 implementado e testado | `e35143b` | `Genome:36`, `Inheritance.cross():46` chamado em `Simulation:364`; inclinação 1,008 em `HeritabilityTest` |
| 19/09/2026 | 59 — Mutações | ⚪ planejado → 🟢 implementado e testado | `e35143b` | `Inheritance.mutate():69` chamado em `Simulation:365`, taxa `CreatureConfig.mutationRatePerBlock:103`; `InheritanceTest` |
| 19/09/2026 | 65 — Herança de características | ⚪ planejado → 🟢 implementado e testado | `a95c4d3`, `e35143b` | Espécie herdada em `Simulation:385`, genoma em `:364`, traços recalculados em `:371` |
| 19/09/2026 | 66 — Variação individual | ⚪ planejado → 🟢 implementado e testado | `e35143b` | Quatro traços por criatura (`Creature:96-105`) lidos em `Simulation:221`, `:257`, `:592` e `visionOf():654` |
| 22/09/2026 | 103 — Território de facção | 🟡 parcial → 🔵 implementado, sem consumidor na tela | — | Mudança de legenda: `Territory.recompute():87-117` está pronto; o que faltava era consumidor, e 🔵 diz isso com exatidão |

**Reclassificação de 22/09/2026.** As linhas de 18 e 19/09 foram
registradas numa revisão única, item a item, depois das entregas. A mesma
revisão trocou a legenda: o antigo "ausente" virou ⚪ planejado, e surgiu o
🔵 para código pronto que nada usa. Por isso 104, 106 e 103 mudaram de
estado sem mudar de código.

O item **113 (expansão territorial ativa)** continua ⚪ planejado. Fundar
reinos contíguos não é expansão: o território ainda é recalculado do zero
a cada segundo, sem memória de posse.

Todas as referências de linha foram conferidas contra `e35143b`. Quatro
estavam imprecisas desde o levantamento inicial e foram corrigidas: o
método em `World` se chama `rawTiles`, não `rawTypes`, e três campos
(`moistureFrequency`, `hunger`, `health`) apontavam para a linha do
javadoc em vez da declaração.

<details>
<summary>Contagens anteriores, na legenda antiga</summary>

A legenda antiga tinha quatro estados: implementado, parcial, ausente e
incerto.

| Momento | Implementados | Parciais | Ausentes | Incertos |
|---|---|---|---|---|
| Levantamento inicial (`093b09a`) | 25 | 6 | 185 | 1 |
| Depois do item 156 (`15dd0f8`) | 26 | 6 | 184 | 1 |

**Registro de 17/09/2026, sobre o item 156.** Era o item mais barato e o de
maior retorno: atrás dele estavam 36 itens que não podiam existir sem um
jeito de tirar vida — combate (81–88), poderes de destruição (148–155),
desastres (157–165) e guerra (126–136). Na época, a conversão de toque em
tile estava escrita em `WorldRenderer.tileX():107` e não tinha chamador;
isso foi resolvido no mesmo dia pelo item 147.

</details>
