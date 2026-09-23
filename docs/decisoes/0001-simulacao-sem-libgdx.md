# 0001 — Simulação sem libGDX

**Problema.** Um jogo que só roda com tela e placa de vídeo é difícil de
testar e de calibrar. Cada teste exigiria abrir janela ou emulador.

**Decisão.** Nada em `sim/` importa libGDX. A simulação é Java puro. Só
`render/`, `MundoVivoGame` e os lançadores conhecem a biblioteca. Até a
cor de um reino é um `int` RGBA, não um `Color` do libGDX
(`FactionRegistry.colorOf`).

**Alternativas.** Usar os tipos do libGDX (`Vector2`, `Color`, `Array`)
dentro da simulação, como é comum em jogos libGDX. Seria mais curto de
escrever.

**Consequências.**
- Dá para gerar e testar um mundo inteiro sem tela, sem emulador e sem
  SDK do Android. As ferramentas de `tools/` fazem isso só com o JDK.
- Se o libGDX for trocado, `sim/` não muda.
- Algumas contas ficam escritas à mão, como a conversão de HSV em
  `FactionRegistry`.
- A conta de tela para tile saiu do `WorldRenderer` para `TileMapping`,
  aritmética pura, para poder ser testada.

**Estado.** Em vigor. Conferível: nenhum `com.badlogic` em `sim/`.
