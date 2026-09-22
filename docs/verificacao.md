# Verificação

O que é testado, como rodar os testes e o que ainda não foi provado.

**Em resumo:** a simulação tem 121 testes JUnit, que rodam na CI a cada
pull request. Há também 95 verificações que rodam só com o JDK, sem baixar
nada. O jogo compila e gera APK na CI, mas ninguém conferiu a tela ainda.

---

## ✅ Como eu rodo os testes?

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

É esse comando que a CI roda em todo pull request.

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

## 🧰 Que ferramentas existem em `tools/`?

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

## 🔬 O que os testes garantem?

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

## 📊 Quais são os números atuais?

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

**Território:** um recálculo custa cerca de 0,3 ms, uma vez por segundo.

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

## ❓ O que ainda não foi provado?

- **Ninguém registrou o jogo rodando.** A CI compila tudo e gera o APK, mas
  não há registro de alguém ter aberto o jogo e olhado a tela.
- **A orientação do mapa não foi conferida na tela.** O desenho inverte o
  eixo vertical. Os testes provam que desenho e toque concordam entre si,
  mas não que o mapa aparece de cabeça para cima.
- **Não há medição de FPS real**, em celular ou no PC.
- **`CameraController.resize()` não tem teste.** A conta de matriz do
  libGDX exige biblioteca nativa, que não carrega nos testes.

O primeiro `./gradlew desktop:run` resolve os três primeiros de uma vez.
