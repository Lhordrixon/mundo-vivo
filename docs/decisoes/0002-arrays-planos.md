# 0002 — Mundo em arrays planos

**Problema.** Um mundo grande (384×288) tem 110 mil tiles. Um objeto
`Tile` por tile espalharia 110 mil objetos pela memória, e o jogo teria de
varrer o mundo a cada quadro.

**Decisão.** O mundo vive em dois arrays contínuos em `World`: um `byte[]`
com o tipo de cada tile e um `float[]` com a elevação. Cerca de 550 KB no
mundo grande, percorridos em ordem de memória. `FoodMap` e `Territory`
seguem o mesmo padrão, com um índice por tile.

**Alternativas.** Um objeto `Tile` com campos. Mais legível, mas cada
acesso seguiria um ponteiro, e o coletor de lixo teria 110 mil objetos a
vigiar.

**Consequências.**
- Varrer o mundo por quadro é barato e não pressiona o coletor de lixo.
- O código acessa tiles por índice (`y * largura + x`), o que exige
  cuidado com os limites. `World` oferece acessos com e sem conferência.
- Salvar o mundo fica simples: os arrays brutos já estão expostos
  (`World.rawTiles`, `FoodMap.rawAmount`).

**Estado.** Em vigor.
