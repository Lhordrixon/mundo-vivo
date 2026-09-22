# Arquitetura

Como o código está organizado e por quê.

**Em resumo:** a simulação (`sim/`) é Java puro e não sabe que existe tela.
O desenho (`render/`) é a única parte que usa o libGDX. Tudo nasce de uma
semente, então o mesmo número gera sempre o mesmo mundo.

---

## 🗂️ Onde fica cada coisa?

```
core/
  sim/              simulação pura — nenhuma linha de libGDX
    Simulation.java o laço de vida: comida, criaturas, tempo
    creature/       criatura, estados, pool, parâmetros, espécie
    ecology/        comida por tile e rebrota
    faction/        facções e território
    genetics/       genoma, herança e traços
    noise/          ruído fractal determinístico
    world/          tipos de tile, configuração, mundo, gerador
    util/           gerador de números aleatórios determinístico
  render/           desenho e câmera — a única parte que conhece libGDX
                    (menos TileMapping: aritmética pura, para ser testável)
android/            abre o jogo no celular e monta o APK
desktop/            abre o jogo numa janela do PC
tools/              ferramentas em Java puro, sem Gradle
```

> [!NOTE]
> **libGDX** é a biblioteca que desenha na tela e lê o toque. **APK** é o
> arquivo que se instala no Android.

---

## 🧱 Quais decisões sustentam o resto?

São quatro. Mudar qualquer uma delas mexe no projeto inteiro.

### 1. A simulação não conhece a biblioteca gráfica

Nada em `sim/` importa libGDX. Por isso dá para gerar e testar um mundo
inteiro sem abrir janela. As ferramentas de `tools/` fazem exatamente isso.

Se um dia o libGDX for trocado, `sim/` não muda.

### 2. O mundo vive em arrays, não em objetos

Um mundo grande teria 110 mil objetos `Tile` espalhados na memória. Em vez
disso, são dois blocos contínuos, de cerca de 550 KB.

Percorrer o mundo a cada quadro fica barato. E o coletor de lixo do Java
não interrompe o jogo.

### 3. O mapa é desenhado como uma imagem só

Cada tile vira um pixel de uma textura. A textura é desenhada de uma vez,
em vez de 49 mil desenhos por quadro.

O custo: mudar o terreno exige reenviar a imagem para a placa de vídeo. Por
isso o envio só acontece nos quadros em que algo mudou.

### 4. Tudo sai da semente

A **semente** é o número que dá origem ao mundo. O sorteador é um SplitMix64
próprio, e o ruído sai de uma conta de inteiros. Nada usa `java.util.Random`.

Mesma semente, mesmo mundo, em qualquer aparelho. Sem isso, os testes não
conseguiriam comparar uma execução com outra.

---

## 📦 Que versões o projeto usa?

Todas ficam em `gradle.properties`.

| Componente | Versão | Por quê |
|---|---|---|
| libGDX | 1.14.2 | versão estável atual |
| Android Gradle Plugin | 8.13.0 | exige Gradle 8.13+ e JDK 17+ |
| Gradle | 8.14.3 | satisfaz o mínimo do plugin do Android |
| compileSdk / targetSdk | 36 | máximo suportado pelo plugin 8.13 |
| minSdk | 26 | Android 8; permite ícone em XML puro |
| Java | 17 | versão mínima do plugin do Android |

> [!WARNING]
> O Android Studio pode oferecer atualizar o plugin do Android para a série
> 9.x. A série 9 muda a forma de escrever o build. Se aceitar, faça num
> commit separado, para poder desfazer.

---

## 👆 Como o toque do jogador chega na criatura?

Um toque simples fere a criatura no tile tocado. Três toques matam.

O caminho tem quatro trechos. Cada um pertence a uma camada diferente:

| Trecho | Quem faz | Dá para testar sem tela? |
|---|---|---|
| dedo → ponto na tela | `GestureDetector`, do libGDX | não |
| tela → ponto no mundo | `camera.unproject` | não |
| mundo → tile | `TileMapping` | **sim** |
| tile → criatura ferida | `Simulation.strikeAt` | **sim** |

O `CameraController` avisa o toque pelo `onTap`, em pixels de tela. Ele não
sabe o que existe no mundo. Quem liga câmera, desenho e simulação é o
`MundoVivoGame`.

<details>
<summary>Por que a conta do tile saiu de dentro do <code>WorldRenderer</code></summary>

A conta de tela para tile é aritmética pura. Mas o `WorldRenderer` cria uma
textura no construtor, e isso exige placa de vídeo. Enquanto a conta morava
lá, nenhum teste conseguia alcançá-la sem abrir uma janela.

Ela foi para `TileMapping`, que não depende de nada. O `WorldRenderer`
continua tendo `tileX` e `tileY`, mas só repassa a chamada.

O teste importante faz uma ida e volta. Ele pega o ponto onde o
`CreatureRenderer` desenha uma criatura e converte de volta para tile. Se
desenho e toque discordassem, o jogador miraria num bicho e acertaria outro.

O golpe não tem caminho de morte próprio. Ele usa o mesmo `applyDamage` da
fome e do terreno, com a causa `CAUSE_PLAYER_STRIKE`. Tocar na água ou fora
do mundo não faz nada, e isso não é erro.

</details>
