# 0003 — Mundo desenhado como textura

**Problema.** O mundo padrão tem 49 mil tiles. Desenhar cada um como um
sprite seria dezenas de milhares de chamadas de desenho por quadro.

**Decisão.** Cada tile vira um pixel de um `Pixmap`, enviado à placa de
vídeo como uma textura e desenhado como um único retângulo, com filtro
`Nearest` para os pixels continuarem quadrados (`WorldRenderer`).

**Alternativas.** Um sprite por tile, ou um mapa de tiles do libGDX. Mais
flexível para arte, muito mais caro para uma grade que muda pouco.

**Consequências.**
- Uma chamada de desenho por quadro para o terreno inteiro.
- Mudar um tile exige reenviar a textura. Um controle de "sujo" faz o
  envio acontecer só nos quadros em que algo mudou.
- Hoje o envio é da textura inteira (196 KB), não só da parte que mudou.
  Está na [dívida técnica](../divida-tecnica.md).
- A textura é desenhada com o eixo vertical invertido. `TileMapping`
  desfaz a inversão para o toque, e há teste de ida e volta.

**Estado.** Em vigor.
