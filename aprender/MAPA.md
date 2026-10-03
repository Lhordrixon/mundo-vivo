# 🗺️ Mapa do código

Os arquivos Java do jogo, em grupos. Em cada um, as perguntas que ele responde. Toque na pergunta para abrir o trecho.

- `Simulation.java:223` quer dizer: arquivo `Simulation.java`, linha 223. As mensagens de erro do Java usam o mesmo jeito.
- O nome do arquivo abre a versão de hoje. A pergunta abre a versão v0.1.0 (`348baea`), que não muda.
- 📚 leva às aulas que usam o arquivo.

Quase tudo fica em `core/src/main/java/com/emannuel/mundovivo/`.

## 🚀 Onde o jogo começa

**[MundoVivoGame.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/MundoVivoGame.java)** — junta o mundo, a simulação, o desenho e a câmera.

- O que acontece a cada quadro da tela? → [MundoVivoGame.java:135](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/MundoVivoGame.java#L135-L146)
- De onde vem a semente de cada partida? → [MundoVivoGame.java:61](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/MundoVivoGame.java#L61)
- O que o toque longo faz? → [MundoVivoGame.java:65](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/MundoVivoGame.java#L65)
- 📚 🟨 [1.7 Loop](1.7-loop.md)

**[AndroidLauncher.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/android/src/main/java/com/emannuel/mundovivo/android/AndroidLauncher.java)** — abre o jogo no celular.

- Onde o jogo começa no Android? → [AndroidLauncher.java:13](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/android/src/main/java/com/emannuel/mundovivo/android/AndroidLauncher.java#L13-L24)

**[DesktopLauncher.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/desktop/src/main/java/com/emannuel/mundovivo/desktop/DesktopLauncher.java)** — abre o jogo no PC.

- Qual é o tamanho da janela no PC? → [DesktopLauncher.java:22](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/desktop/src/main/java/com/emannuel/mundovivo/desktop/DesktopLauncher.java#L22)

## ⚙️ O passo do mundo

**[Simulation.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/Simulation.java)** — o coração do jogo: comida, criaturas, nascimento, morte, briga e toque.

- O que acontece em cada passo? → [Simulation.java:188](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/Simulation.java#L188-L212)
- Por que a criatura morre? → [Simulation.java:223](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/Simulation.java#L223-L261)
- Para onde vai quem morre? → [Simulation.java:456](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/Simulation.java#L456-L460)
- Como nasce um filho? → [Simulation.java:345](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/Simulation.java#L345-L387)
- Quando duas criaturas brigam? → [Simulation.java:479](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/Simulation.java#L479-L508)
- O que o toque na tela faz com a criatura? → [Simulation.java:416](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/Simulation.java#L416-L429)
- Como a criatura anda até um lugar? → [Simulation.java:563](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/Simulation.java#L563-L581)
- 📚 🟨 [1.2 Andar](1.2-andar.md) · 🟨 [1.3 Fome](1.3-fome.md) · 🟨 [1.4 Comer](1.4-comer.md) · 🟨 [1.5 Morrer](1.5-morrer.md) · 🟨 [1.6 Reproduzir](1.6-reproduzir.md) · 🟨 [1.7 Loop](1.7-loop.md) · 🟩 [2.1 Ler Java](2.1-ler-java.md)

## 🌍 Mundo

**[WorldGenerator.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/world/WorldGenerator.java)** — cria o mapa a partir da semente.

- Como a semente vira mapa? → [WorldGenerator.java:57](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/world/WorldGenerator.java#L57-L61)
- Quando um tile vira praia, floresta ou neve? → [WorldGenerator.java:161](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/world/WorldGenerator.java#L161-L186)
- 📚 🟨 [1.1 Semente](1.1-semente.md)

**[World.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/world/World.java)** — a grade de tiles.

- Como saber se (x, y) está dentro do mapa? → [World.java:54](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/world/World.java#L54-L56)
- Como (x, y) vira um número só? → [World.java:59](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/world/World.java#L59-L61)
- 📚 🟨 [1.4 Comer](1.4-comer.md) · 🟩 [2.1 Ler Java](2.1-ler-java.md)

**[TileType.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/world/TileType.java)** — os tipos de terreno.

- Que terrenos existem? Onde dá para andar? Quanta comida cada um dá? → [TileType.java:26](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/world/TileType.java#L26-L42)
- 📚 🟨 [1.1 Semente](1.1-semente.md)

**[WorldConfig.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/world/WorldConfig.java)** — os números que desenham o mapa.

- Qual é o tamanho do mundo? → [WorldConfig.java:20](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/world/WorldConfig.java#L20-L21)
- Qual é o nível do mar? → [WorldConfig.java:24](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/world/WorldConfig.java#L24)
- 📚 🟨 [1.1 Semente](1.1-semente.md)

**[FractalNoise.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/noise/FractalNoise.java)** — o sorteio suave que forma continentes.

- Por que o mapa tem manchas, e não chuvisco de TV? → [FractalNoise.java:81](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/noise/FractalNoise.java#L81-L94)
- 📚 🟨 [1.1 Semente](1.1-semente.md)

**[Rng.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/util/Rng.java)** — o sorteador do jogo.

- Por que a mesma semente dá o mesmo mundo? → [Rng.java:19](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/util/Rng.java#L19-L38)
- 📚 🟨 [1.1 Semente](1.1-semente.md)

## 🌱 Comida

**[FoodMap.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/ecology/FoodMap.java)** — a comida de cada tile.

- Como a criatura come? → [FoodMap.java:85](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/ecology/FoodMap.java#L85-L93)
- Como a comida volta a crescer? → [FoodMap.java:108](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/ecology/FoodMap.java#L108-L122)
- O que sobra de quem morre? → [FoodMap.java:96](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/ecology/FoodMap.java#L96-L98)
- 📚 🟨 [1.4 Comer](1.4-comer.md) · 🟨 [1.5 Morrer](1.5-morrer.md)

## 🐾 Criaturas

**[Creature.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/creature/Creature.java)** — uma criatura: lugar, fome, vida e idade.

- O que cada criatura guarda? → [Creature.java:28](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/Creature.java#L28-L39)
- Como a criatura perde vida? → [Creature.java:196](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/Creature.java#L196-L203)
- Quando ela pode ter filhos? → [Creature.java:218](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/Creature.java#L218-L223)
- Quais são as causas de morte? → [Creature.java:107](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/Creature.java#L107-L128)
- 📚 🟨 [1.5 Morrer](1.5-morrer.md) · 🟨 [1.6 Reproduzir](1.6-reproduzir.md)

**[CreatureConfig.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/creature/CreatureConfig.java)** — os botões de ajuste das criaturas.

- Qual é a velocidade das criaturas? → [CreatureConfig.java:186](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/CreatureConfig.java#L186)
- Com quanta fome ela vai atrás de comida? → [CreatureConfig.java:163](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/CreatureConfig.java#L163)
- Quantos segundos ela vive? → [CreatureConfig.java:197](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/CreatureConfig.java#L197)
- 📚 🟨 [1.3 Fome](1.3-fome.md) · 🟨 [1.6 Reproduzir](1.6-reproduzir.md) · 🟩 [2.5 Parâmetro](2.5-parametro.md)

**[CreaturePool.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/creature/CreaturePool.java)** — a lista das criaturas vivas. Quem morre libera o lugar, como a cadeira de um cinema.

- Como nasce uma criatura num lugar livre? → [CreaturePool.java:115](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/CreaturePool.java#L115-L129)
- O que acontece com o lugar de quem morre? → [CreaturePool.java:132](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/CreaturePool.java#L132-L148)
- 📚 🟨 [1.5 Morrer](1.5-morrer.md) · 🟨 [1.7 Loop](1.7-loop.md)

**[CreatureState.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/creature/CreatureState.java)** — os quatro estados de uma criatura.

- Quais estados uma criatura pode ter? → [CreatureState.java:4](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/CreatureState.java#L4-L16)
- 📚 🟨 [1.3 Fome](1.3-fome.md) · 🟩 [2.4 Cor](2.4-cor.md)

**[Species.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/creature/Species.java)** — as espécies.

- O que muda de uma espécie para outra? → [Species.java:3](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/creature/Species.java#L3-L22)

## 🧬 Genética

**[Genome.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/genetics/Genome.java)** — o genoma: oito blocos de genes.

- Do que é feito o genoma? → [Genome.java:21](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/genetics/Genome.java#L21-L52)

**[Inheritance.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/genetics/Inheritance.java)** — como o filho herda dos pais.

- Como o filho herda dos pais? → [Inheritance.java:46](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/genetics/Inheritance.java#L46-L50)
- O que é uma mutação? → [Inheritance.java:69](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/genetics/Inheritance.java#L69-L78)

**[Phenotype.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/genetics/Phenotype.java)** — transforma genes em características.

- Como os genes mudam velocidade, visão, fome e idade? → [Phenotype.java:72](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/genetics/Phenotype.java#L72-L82)

## 👑 Reinos

**[FactionRegistry.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/faction/FactionRegistry.java)** — a lista de reinos.

- Como nasce um reino, com nome e cor? → [FactionRegistry.java:117](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/faction/FactionRegistry.java#L117-L127)
- Quando um reino perde gente? → [FactionRegistry.java:177](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/faction/FactionRegistry.java#L177-L189)

**[Territory.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/sim/faction/Territory.java)** — de quem é cada pedaço de terra.

- Como o jogo decide de quem é cada tile? → [Territory.java:87](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/sim/faction/Territory.java#L87-L117)

## 🎨 Tela e toque

**[CreatureRenderer.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/render/CreatureRenderer.java)** — desenha as criaturas.

- Qual é a cor de cada estado? → [CreatureRenderer.java:30](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/render/CreatureRenderer.java#L30-L33)
- Como o jogo escolhe a cor? → [CreatureRenderer.java:65](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/render/CreatureRenderer.java#L65-L72)
- 📚 🟨 [1.3 Fome](1.3-fome.md) · 🟩 [2.4 Cor](2.4-cor.md)

**[WorldRenderer.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/render/WorldRenderer.java)** — desenha o mapa.

- Como o mapa vai para a tela? → [WorldRenderer.java:90](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/render/WorldRenderer.java#L90-L102)

**[CameraController.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/render/CameraController.java)** — arrastar, aproximar e tocar.

- Como o dedo arrasta o mapa? → [CameraController.java:132](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/render/CameraController.java#L132-L139)
- Como dois dedos aproximam o mapa? → [CameraController.java:142](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/render/CameraController.java#L142-L150)
- Como o toque chega ao jogo? → [CameraController.java:162](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/render/CameraController.java#L162-L168)

**[TapListener.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/render/TapListener.java)** — o recado de um toque: onde ele foi.

- O que um toque simples avisa? → [TapListener.java:21](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/render/TapListener.java#L21)

**[TileMapping.java](https://github.com/Lhordrixon/mundo-vivo/blob/main/core/src/main/java/com/emannuel/mundovivo/render/TileMapping.java)** — converte a posição do dedo em tile.

- Como o toque vira um tile? → [TileMapping.java:36](https://github.com/Lhordrixon/mundo-vivo/blob/348baea45f64869ab0d96f224d2b8c19e5cce7aa/core/src/main/java/com/emannuel/mundovivo/render/TileMapping.java#L36-L49)

## 🧪 Testes

Os testes ficam em `core/src/test/java/com/emannuel/mundovivo/`. O nome diz o que testam: `CreatureTest.java` testa `Creature.java`. A comida ainda não tem teste próprio: é a issue [#9](https://github.com/Lhordrixon/mundo-vivo/issues/9).

Também há o `tools/SimSelfTest.java`, que testa a simulação inteira de uma vez.

➡️ Volte ao [COMECE-AQUI](COMECE-AQUI.md).
