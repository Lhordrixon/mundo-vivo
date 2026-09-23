# CLAUDE.md

Guia para agentes e pessoas que vão mexer no código. O que o jogo é e
como rodá-lo está no [README](README.md); aqui ficam as regras.

## Pacotes

Tudo sob `core/src/main/java/com/emannuel/mundovivo/`.

| Pacote | Responsabilidade |
|---|---|
| `sim/Simulation` | o passo de vida: comida, criaturas, reprodução, morte, território, **combate** (`hostileNeighbours`) e **poder do jogador** (`strikeAt`) |
| `sim/world` | tipos de tile, configuração, grade e gerador |
| `sim/noise` | ruído fractal determinístico |
| `sim/ecology` | comida por tile e rebrota (`FoodMap`) |
| `sim/creature` | criatura, estados, espécie, pool e parâmetros (`CreatureConfig`) |
| `sim/genetics` | genoma de blocos, herança e fenótipo |
| `sim/faction` | registro de reinos e território |
| `sim/util` | sorteador determinístico (`Rng`) |
| `render/` | desenho, câmera e toque; a única parte com libGDX (exceto `TileMapping`, aritmética pura) |
| `MundoVivoGame` | junta simulação, desenho e câmera |

`creature` e `genetics` dependem uma da outra (ciclo conhecido). Combate e
jogador não têm pacote próprio.

## Invariantes

Cada um foi conferido no código. Não quebre sem discutir antes.

1. **`sim/` não importa libGDX.** Nenhum `com.badlogic` em `sim/`.
2. **Determinismo pela semente.** Todo sorteio passa por `sim/util/Rng.java`.
   Nada de `java.util.Random`, `Math.random()` ou relógio em `sim/`.
3. **Zero alocação por passo, criatura ou nascimento.** Em `sim/`, `new`
   só aparece em construtores e na fundação do mundo
   (`Simulation.spawnInitialPopulation`). O pool
   (`sim/creature/CreaturePool.java`) recicla slots.
4. **Porta única de dano.** A vida só diminui em `Creature.applyDamage`
   (`sim/creature/Creature.java`). Fome, terreno, combate e golpe do
   jogador passam por ela.
5. **Caminho único de morte.** Toda morte termina em `Simulation.die()`:
   vira comida, sai do reino, devolve o slot.
6. **Território uma vez por segundo.**
   `Simulation.TERRITORY_RECOMPUTE_INTERVAL_SECONDS = 1f`.

## Regras de modificação

- Lógica nova de simulação vai em `sim/`, com teste em `core/src/test/`.
- Parâmetro de população (`CreatureConfig`) só muda com medição em várias
  sementes, feita com `tools/SimulationReport.java`. Registre os números.
- Toda entrega atualiza o [roadmap](docs/roadmap.md): estado do item,
  evidência com arquivo e linha, e uma linha no histórico.
- Mecanismo sem consumidor na tela vai para a dívida técnica
  ([docs/tecnico.md](docs/tecnico.md#dívida-técnica-conhecida)).
- Números de progresso só no roadmap.
- Refatoração estrutural (dividir `Simulation`, criar pacotes, quebrar o
  ciclo) só com aprovação do dono do projeto.

## Comandos

```bash
./gradlew core:test           # suíte JUnit, a mesma da CI
./gradlew desktop:run         # jogo no PC (precisa do SDK do Android)
./gradlew android:assembleDebug
```

Sem Gradle, só com o JDK: compile `sim/` e `tools/` com `javac` e rode
`SimSelfTest`, `WorldPreview`, `SimulationPreview` ou `SimulationReport`
(passo a passo em [docs/tecnico.md](docs/tecnico.md#ferramentas-de-apoio)).

## Antes de criar algo novo, procure em

- `CreatureConfig` e `WorldConfig`: o parâmetro talvez já exista.
- `Creature.CAUSE_*`: causas de morte já definidas.
- `FactionRegistry.nameOf` / `colorOf`, `Territory`, `World.setTile`,
  `FoodMap.retile`: prontos e sem consumidor na tela.
- O [roadmap](docs/roadmap.md): o item pode já ter evidência parcial.
