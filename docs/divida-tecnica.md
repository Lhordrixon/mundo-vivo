# Dívida técnica

O que já se sabe que está incompleto, lento ou provisório. Está escrito
aqui de propósito, para não virar surpresa.

**Em resumo:** o jogo funciona, mas tem três tipos de pendência. Há código
lento que só pesa com muitas criaturas. Há código pronto que ainda não
aparece na tela. E há um bug que ainda não acontece em jogo normal.

> [!TIP]
> Procurando algo para fazer? Os itens marcados com 🌱 são bons para
> começar: pequenos, isolados e fáceis de testar.

---

## 🐛 Que bugs conhecidos existem?

### Reproduzir com uma criatura sem reino quebra o jogo

Se um dos pais não tiver reino (`factionId == -1`), o filho herda o `-1`.
Aí `FactionRegistry.join(-1)` lança `IndexOutOfBoundsException`.

- **Onde:** `Simulation.reproduce`, na linha `factions.join(childFaction)`.
- **Quando acontece:** ainda não, em jogo normal. Todo fundador recebe
  reino, e todo filho herda um válido.
- **Quando vai acontecer:** no dia em que alguma coisa criar uma criatura
  sem reino. Um poder de "criar criatura", por exemplo.

---

## 🐢 O que fica lento com muita criatura?

- **A busca de inimigos compara cada criatura com todas as outras.** Com
  288 criaturas, um passo custa 0,165 ms. Com o pool cheio (2.999), custa
  **8,9 ms**, mais da metade de um quadro. O conserto é guardar as criaturas
  numa grade por região. O lugar é `Simulation.hostileNeighbours`.
- **A busca por parceiro também percorre todas as criaturas.** Hoje é
  barato, porque só quem procura parceiro faz a busca. A mesma grade
  resolveria. O lugar é `Simulation.resolveMate`.
- **A rebrota de comida percorre o mapa inteiro a cada passo.** São 49 mil
  tiles por passo. Daria para crescer uma fatia do mapa por quadro.
- **O território é sempre recalculado do mundo inteiro**, uma vez por
  segundo, mesmo sem ninguém ter se mexido.
- **Mudar um tile reenvia a textura inteira** para a placa de vídeo (196 KB),
  não só a parte que mudou. Vai pesar quando os poderes pintarem terreno.

---

## 💤 O que está pronto mas ainda não aparece?

- 🌱 **Nome e cor dos reinos.** `FactionRegistry.nameOf` e `colorOf`
  funcionam e têm teste. Só os testes os chamam. O lugar natural é um mapa
  de territórios colorido.
- 🌱 **O tamanho da criatura.** `Creature.size` sai do genoma, como os
  outros traços, mas nada o lê. O uso natural seria no combate ou na
  comida.
- **O território.** É calculado a cada segundo, mas ninguém evita terra de
  outro reino, e nada é desenhado. As criaturas brigam por proximidade, não
  por território.
- **O dano de terreno.** Funciona e tem teste, mas nada muda o chão debaixo
  de uma criatura durante o jogo. Espera um poder ou um desastre.

---

## 🧭 O que é simplificação provisória?

- **Criaturas andam em linha reta.** Não há busca de caminho. Se o próximo
  passo cairia na água, a criatura escolhe outro destino. Contornar uma
  baía pode demorar.
- **O limite do pool não segura a população de forma saudável.** Batendo no
  limite, param os nascimentos, mas não as mortes. A comida é que deveria
  limitar.
- **O filho de pais de reinos diferentes tira o reino na moeda.** Funciona
  porque é raro. Com guerra de verdade, talvez esse casal nem devesse
  existir.
- **Temperatura e umidade são descartadas depois de gerar o mundo.** Só a
  altura fica guardada.

---

## 📱 E no app Android?

- **Não existe salvar e carregar.** A semente e o estado do mundo já estão
  expostos para isso, mas falta o formato do arquivo.
- 🌱 **Toque longo apaga o mundo e gera outro.** É um atalho de
  desenvolvimento, e vai atrapalhar quando houver outros gestos.
- **O `minifyEnabled` está desligado na versão final.** As regras do
  ProGuard existem, mas ligar sem testar quebra o app ao abrir.
- **O ícone é provisório.** Um desenho vetorial simples, sem identidade.

---

## 🚀 O que vem depois?

1. **Abrir o jogo e olhar a tela.** Rodar `./gradlew desktop:run` e
   conferir se o mapa aparece de cabeça para cima. Isso nunca foi feito.
2. **Salvar e carregar o mundo.**
3. **Desenhar o território com a cor de cada reino.** Isso dá uso ao nome,
   à cor e ao território, que hoje não aparecem.
