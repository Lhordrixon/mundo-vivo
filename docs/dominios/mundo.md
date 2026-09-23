# 🌍 Mundo

- **O que é:** a grade de tiles onde tudo acontece, gerada a partir de uma
  semente. O mundo padrão tem 256×192 tiles.
- **Classes principais:** `sim/world/WorldGenerator`, `World`,
  `WorldConfig`, `TileType`; `sim/noise/FractalNoise`.
- **Quem depende:** `ecology`, `faction`, `Simulation`, `render/` e
  `MundoVivoGame`. `world` depende só de `noise`.
- **Testes que cobrem:** `WorldGeneratorTest`, `FractalNoiseTest` e a
  parte de mundo do `tools/SimSelfTest`.

---

## Como o mundo é gerado?

Em duas passadas sobre a grade:

1. Um ruído fractal preenche a elevação de cada tile e anota o menor e o
   maior valor.
2. A elevação é esticada para ocupar a faixa inteira de 0 a 1. Depois
   ganha contraste, cristas de montanha e afunda nas bordas.
3. Temperatura e umidade são calculadas. Junto com a altura, elas decidem
   o tipo de cada tile.

A **temperatura** sai da latitude, perturbada por ruído e derrubada pela
altitude. A **umidade** sai de um segundo campo de ruído.

> [!IMPORTANT]
> O passo 2 não é detalhe. O ruído fractal soma várias camadas e se
> concentra no meio da faixa. Sem esticar, nenhum ponto chega a ser
> montanha, e o mundo sai plano. Foi o que aconteceu na primeira versão do
> gerador.

Separar clima de bioma é o que põe deserto ao lado de selva na mesma
latitude. Pintando o bioma direto da altura, os biomas sairiam em faixas
horizontais.

---

## Que tipos de tile existem?

Dezesseis, em `TileType`. Cada um tem cor, se dá para andar, fertilidade
e se é perigoso.

- **Água:** oceano profundo, oceano e água rasa. Ninguém anda na água.
- **Terra:** praia, deserto, savana, campo, floresta, selva, pântano,
  taiga, tundra, neve e rocha.
- **Parede:** montanha e pico nevado. Não dá para andar, mas não ferem.
- **Perigoso:** só o oceano profundo. Veja [criaturas](criaturas.md).

A fertilidade decide quanta comida o tile sustenta. Veja
[ecologia](ecologia.md).

---

## O mundo muda depois de gerado?

Ainda não. `World.setTile` e `FoodMap.retile` existem e estão testados,
mas nenhum código de jogo os chama. Eles esperam os poderes de terreno.

Temperatura e umidade são descartadas depois da geração. Só a altura fica
guardada.

---

## Como vejo um mundo sem abrir o jogo?

Com o `WorldPreview`, que gera um PNG de qualquer semente em segundos:

```bash
java -cp build/sim:build/tools WorldPreview 12345 3 previa.png
```

A compilação está em [verificação](../verificacao.md). É a forma mais
rápida de ajustar `WorldConfig`: mudou um número, roda e olha a imagem.

---

## Decisões relacionadas

- [Mundo em arrays planos](../decisoes/0002-arrays-planos.md)
- [Mundo desenhado como textura](../decisoes/0003-mundo-como-textura.md)
- [Determinismo pela semente](../decisoes/0004-determinismo-por-semente.md)
