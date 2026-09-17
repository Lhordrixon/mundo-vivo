# Mundo Vivo

Jogo de sandbox de deus para Android: um mundo em grade que nasce de uma
semente, com biomas, relevo e — nas etapas seguintes — criaturas e
civilizações autônomas.

Inspirado no gênero que o WorldBox popularizou. **Nenhum código, arte,
som ou texto do WorldBox foi usado**: tudo aqui é implementação original.
Mecânica de jogo não tem proteção de direito autoral; código e arte têm.

---

## Estado atual

| Sistema | Situação |
|---|---|
| 1. Grade de tiles e geração de mundo | pronto e testado |
| 2. Câmera com arraste, pinça e roda do mouse | pronto |
| 3. Unidades com máquina de estados | não começou |
| 4. Facções e território | não começou |
| 5. Save/load | não começou |
| 6. Poderes de deus | não começou |
| 7. IA de guerra | não começou |
| 8. Passo de otimização e medição de FPS | não começou |

O que roda hoje: o app abre, gera um mundo de 256x192 tiles e deixa você
navegar por ele. Toque longo descarta o mundo e gera outro — atalho de
desenvolvimento, sai quando a interface de verdade entrar.

---

## Como compilar

Requisitos: JDK 17 ou superior e Android Studio (que já traz o SDK do
Android). Na primeira execução o Gradle baixa o libGDX e o plugin do
Android — precisa de internet.

**Caminho mais curto — rodar no PC, sem celular e sem SDK do Android:**

```bash
./gradlew desktop:run
```

**Gerar o APK de depuração:**

```bash
./gradlew android:assembleDebug
# sai em: android/build/outputs/apk/debug/android-debug.apk
```

**Instalar direto no celular conectado por USB:**

```bash
./gradlew android:installDebug
```

**Rodar os testes:**

```bash
./gradlew core:test
```

Para o APK de release é preciso criar uma chave de assinatura e declará-la
em `android/build.gradle`; sem isso `assembleRelease` gera um APK não
assinado, que o Android recusa instalar.

### Versões fixadas

Ficam todas em `gradle.properties`, em um lugar só:

| Componente | Versão | Por quê |
|---|---|---|
| libGDX | 1.14.2 | versão estável atual |
| Android Gradle Plugin | 8.13.0 | exige Gradle 8.13+ e JDK 17+ |
| Gradle (wrapper) | 8.14.3 | satisfaz o mínimo do AGP |
| compileSdk / targetSdk | 36 | máximo suportado pelo AGP 8.13 |
| minSdk | 26 | Android 8; permite ícone adaptativo em XML puro |
| Java (fonte e destino) | 17 | sem backend HTML no projeto, não há motivo para ficar no 8 |

O Android Studio pode oferecer atualizar o AGP para a série 9.x. Funciona,
mas é uma versão com mudanças incompatíveis no DSL — se aceitar, faça em um
commit isolado para poder voltar atrás.

---

## Arquitetura

```
core/
  sim/      simulação pura — nenhuma linha de libGDX
    noise/  ruído fractal determinístico
    world/  tipos de tile, configuração, mundo, gerador
    util/   RNG determinístico
  render/   desenho e câmera — a única parte que conhece libGDX
android/    launcher e empacotamento do APK
desktop/    launcher para rodar no PC
tools/      ferramentas de apoio em Java puro (prévia, verificação)
```

### As quatro decisões que sustentam o resto

**A simulação não conhece a biblioteca gráfica.** Nada em `sim/` importa
libGDX. Consequência prática: dá para gerar e testar um mundo inteiro sem
abrir tela, sem emulador e sem SDK do Android — foi assim que o gerador foi
calibrado. Se um dia o libGDX for trocado, `sim/` não muda.

**O mundo vive em arrays planos, não em objetos Tile.** Um mundo grande
teria 110 mil objetos espalhados pela heap. São dois blocos contíguos,
cerca de 550 KB, percorridos em ordem de memória. É o que permite varrer o
mundo por frame sem pressionar o coletor de lixo — condição para as
unidades autônomas que entram no sistema 3.

**O mundo é desenhado como uma textura, não como 49 mil sprites.** Um
pixel por tile em um `Pixmap`, enviado à GPU, desenhado como um quad só,
com filtro `Nearest`. Uma chamada de desenho por frame em vez de dezenas de
milhares. O preço é que mudar o terreno exige reenviar a textura — por isso
existe o controle de "sujo", que faz a transferência acontecer só nos
frames em que algo mudou de fato.

**Tudo é determinístico a partir da semente.** O RNG é SplitMix64 próprio e
o ruído sai de hash inteiro, não de `java.util.Random` nem de tabela
sorteada em tempo de execução. Mesma semente, mesmo mundo, em qualquer
aparelho. Sem isso não há teste de regressão possível e o save teria que
guardar o mapa inteiro em vez de um número.

### Como o mundo é gerado

Duas passagens sobre a grade:

1. Ruído fractal preenche a elevação e registra o menor e o maior valor.
2. A elevação é reescalada para ocupar [0,1] inteiro, recebe curva de
   contraste, cristas de montanha e afundamento de borda; em seguida
   temperatura (latitude, perturbada por ruído, derrubada pela altitude) e
   umidade (segundo campo de ruído) classificam cada tile.

A normalização entre as duas passagens não é detalhe: o fBm é uma soma de
oitavas e concentra seus valores no meio da faixa, quase nunca chegando
perto de 0 ou de 1. Sem reescalar, nenhum ponto alcança altura de montanha
e o mundo sai inteiramente plano — foi exatamente o que aconteceu na
primeira versão deste gerador.

Separar clima de bioma (em vez de pintar bioma direto da altura) é o que
faz aparecer deserto ao lado de selva na mesma latitude, em vez de faixas
horizontais uniformes.

---

## Ferramentas de apoio

Duas ferramentas em Java puro, sem Gradle e sem baixar dependência:

```bash
# compile a simulação uma vez
javac -d build/sim $(find core/src/main/java/com/emannuel/mundovivo/sim -name '*.java')
javac -d build/tools -cp build/sim tools/WorldPreview.java tools/SimSelfTest.java

# gere um PNG do mundo da semente 12345, com 3 px por tile
java -cp build/sim:build/tools WorldPreview 12345 3 previa.png

# rode a verificação da simulação
java -cp build/sim:build/tools SimSelfTest
```

`WorldPreview` encurta o ciclo de calibragem de minutos para segundos:
mexeu em `WorldConfig`, roda e olha o PNG. `SimSelfTest` cobre as mesmas
invariantes da suíte JUnit, mas roda em qualquer máquina com JDK.

---

## O que foi verificado, e o que não foi

Vale a pena ser exato aqui, para você não descobrir na hora errada.

**Verificado de fato:**

- A simulação (`sim/`) compila com `-Xlint:all` sem um único aviso.
- As 21 verificações de `SimSelfTest` passam: determinismo por semente,
  seeds diferentes divergindo, todo tile com tipo válido, elevação sempre
  em [0,1], proporção terra/mar jogável em 6 sementes, presença de oceano
  profundo e de montanha, recusa de configuração inválida, acesso fora dos
  limites lançando exceção, e geração do mundo padrão em 18 ms.
- A saída visual foi conferida: mapas gerados em várias sementes, com
  continentes, cordilheiras com neve no cume, litoral e calota polar.
- Todo o código, incluindo `render/` e os dois launchers, compila com
  `-Xlint:all` sem avisos.

**Não verificado:**

- O build do Gradle nunca rodou, nem o do Android. O ambiente onde este
  projeto foi montado não tem acesso ao Maven Central nem ao repositório do
  Google, então nem o libGDX nem o plugin do Android puderam ser baixados.
- A camada `render/` compilou contra *stubs* da API do libGDX escritos à
  mão, não contra o libGDX real. Isso pega erro de sintaxe, import faltando
  e método de interface não implementado — mas **não** garante que as
  assinaturas batem com as do libGDX 1.14.2.
- O jogo nunca foi executado. Não há medição de FPS, nem confirmação de que
  o `flipY` da textura deixa o mapa na orientação certa na tela.
- A suíte JUnit nunca rodou (JUnit não pôde ser baixado). O que rodou foi o
  `SimSelfTest`, que cobre as mesmas invariantes em Java puro.

Tradução prática: a lógica de mundo está testada e funcionando; a camada
gráfica está escrita com cuidado mas não foi provada. O primeiro
`./gradlew desktop:run` é o teste que falta, e é onde eventual divergência
de assinatura vai aparecer.

---

## Dívida técnica conhecida

Registrada de propósito, para não virar surpresa:

- **Reenvio de textura inteiro.** Ao mudar um tile, o `Pixmap` todo sobe
  para a GPU (196 KB no mundo padrão), não só a região alterada. Aceitável
  enquanto as alterações são esporádicas; vira gargalo quando os poderes de
  deus pintarem terreno continuamente. A correção é acumular a região suja
  e subir só ela.
- **Sem pooling de objetos ainda.** Não faz falta hoje porque não existem
  unidades. Precisa existir antes do sistema 3, não depois.
- **Temperatura e umidade são descartadas após a geração.** Só a elevação
  fica guardada. Quando o crescimento de vegetação ou a migração sazonal
  entrarem, elas terão que ser recalculadas ou armazenadas.
- **Sem save/load.** O `World` já expõe `rawTiles()` e `rawElevation()`
  pensando nisso, e a semente já é guardada, mas não há serialização.
- **Toque longo regenera o mundo.** Comportamento de desenvolvimento, vai
  colidir com gestos de jogo mais adiante.
- **`minifyEnabled` desligado no release.** As regras do ProGuard já estão
  escritas para quando for ligado, mas ligar sem testar quebra o app na
  abertura, não na compilação.
- **Ícone provisório.** Vetor desenhado à mão, sem identidade visual.

---

## Próximo passo

Sistema 3: uma unidade com máquina de estados (procurar comida, comer,
procurar parceiro, reproduzir, morrer), em `sim/`, com testes, antes de
existir qualquer desenho dela na tela. A ordem importa: a máquina de
estados é testável sem tela, e tudo que for testável sem tela deve ser
escrito sem tela.
