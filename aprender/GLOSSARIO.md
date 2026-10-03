# 📒 Glossário

Cada palavra nova das aulas, em uma ou duas frases. Volte aqui quando esquecer.

## Rótulos dos blocos

Nos `.py`, comentários curtos dizem o papel de cada bloco. São sempre os mesmos:

- `# Preparar`: cria o que o código vai usar (variáveis, listas).
- `# Repetir`: um `for` que faz a mesma coisa várias vezes.
- `# Decidir`: um `if` que escolhe um caminho.
- `# Atualizar`: muda o estado (posição, fome, vida).
- `# Mostrar`: escreve na tela com `print`.
- `# Conferir`: um `assert` que confere o seu código e dá a dica.
- `# ✏️ Sua vez`: a lacuna é logo abaixo.

Quando vêm numerados (`# 1. Preparar`, `# 2. Repetir`), a ordem é a ordem em que o computador faz.

## Python

### None

Quer dizer "nada ainda". Nas aulas, marca o lugar que você vai preencher.

### pass e ...

Os dois querem dizer "não faça nada": só guardam o lugar. Troque pelo seu código.

### parâmetro

O nome que a função dá ao valor que recebe: em `def comer(c):`, o `c`.

### True e False

Verdadeiro e falso. Um `if` roda o bloco quando a condição dá `True`.

### and, or, not

`and`: as duas condições valem. `or`: pelo menos uma vale. `not`: o contrário.

### +=

`x += 2` é o mesmo que `x = x + 2`.

### append, len, range

`lista.append(x)` põe `x` no fim. `len(lista)` diz o tamanho. `range(3)` dá 0, 1, 2 para o `for`.

### min e max

`min(a, b)` devolve o menor; `max(a, b)`, o maior. Servem para pôr limite.

### assert

Uma conferência. Se a condição for falsa, o programa para e mostra a dica que vem depois da vírgula.

### dicionário

Uma ficha com campos: `{"fome": 3}`. Você pega o valor pelo nome: `c["fome"]`.

### função

Um bloco com nome, como um botão: `def comer(c):`. Você aperta (chama) quantas vezes quiser.

### return

O que a função devolve para quem chamou. Depois do `return`, a função para.

### classe

O molde de biscoito: diz como toda criatura é feita. `Creature` é a classe.

### objeto

Um biscoito feito com o molde: `Creature(1, 1)`.

### método

Uma função que mora dentro da classe: `c.comer()`.

### self

Dentro da classe, quer dizer "eu mesma": a criatura que está sendo usada.

### índice

O número da posição numa lista, contando do zero.

### PEP 8

O guia de estilo do Python: nomes em `snake_case`, espaços, linhas curtas. As aulas seguem.

### snake_case e camelCase

Dois jeitos de escrever nomes de várias palavras. Python usa `move_toward`; Java usa `moveToward`.

## Java e testes

### package e import

`package` diz em que pasta a classe mora. `import` traz uma classe de outra pasta para usar.

### exceção

Um erro que para o programa, com nome: `IndexOutOfBoundsException` quer dizer "posição que não existe".

### teste

Código que confere outro código. Roda sozinho e diz `PASSED` ou `FAILED`.

### JUnit

A ferramenta de testes do Java. `@Test` marca um método como teste; `@DisplayName` dá o nome em português.

### assertEquals e assertTrue

`assertEquals(esperado, real)` confere se os dois são iguais. `assertTrue(x)` confere se `x` é verdadeiro.

### tolerância

A diferença minúscula que o teste aceita com número quebrado: o `1e-6f` no fim do `assertEquals`.

### vermelho e verde

Primeiro um teste que falha (vermelho) e mostra o problema; depois o menor conserto que faz ele passar (verde).

### invariante

Uma regra do projeto que vale sempre. Estão no `CLAUDE.md`.

## PC

### JDK

O kit que compila e roda Java. O projeto usa a versão 17.

### SDK

As ferramentas do Android. Vêm com o Android Studio.

### gradlew

O comando que monta, testa e roda o projeto: `./gradlew core:test`.

### Git Bash

O terminal que vem com o Git no Windows. Os comandos das aulas rodam nele.

## O jogo

### tile

Cada quadradinho do mapa, como um azulejo.

### semente

O número que decide todos os sorteios. A mesma semente dá sempre o mesmo mundo.

### estado

O que a criatura está fazendo agora: vagando, procurando comida, comendo, procurando par.

### reino e território

Reino é o grupo de uma criatura (no código, facção: `factionId`). Território é o pedaço do mapa de cada reino.

### genoma e fenótipo

Genoma: os genes que a criatura herda dos pais. Fenótipo: o que eles viram no corpo, como o tamanho.

### render

A parte do código que desenha na tela (`render/`). É a única que usa a biblioteca gráfica libGDX.

### dt

O tempo de um passo do jogo, em segundos.

### protótipo

Uma versão rápida e simples, em Python, para testar a ideia antes do Java.

### roadmap

A lista do que o jogo tem e do que falta: `docs/roadmap.md`.

## GitHub

### Raw

O botão (ou link) que mostra só o texto de um arquivo, sem nada em volta. Bom para copiar.

### ZIP

Um arquivo compactado com o projeto inteiro dentro.

### issue

Um bilhete no GitHub com uma tarefa, uma dúvida ou um problema.

### fork

Uma cópia do projeto na sua conta. As aulas não usam: aceite o convite e trabalhe no projeto de verdade.

### branch

Uma cópia sua do projeto, separada da versão oficial (a `main`), para mexer sem medo.

### commit

Uma foto da mudança, com uma frase dizendo o que mudou.

### push

Enviar os seus commits para o GitHub.

### pull request

O pedido para a sua branch entrar na `main`. Abrevia **PR**. O dono revisa e aceita.

### CI

O robô do GitHub que testa toda mudança. Verde é ok; vermelho, algo falhou.

### log

O diário do robô: o texto que ele escreve enquanto trabalha. O erro aparece ali.

### APK

O instalador do jogo para Android, como os da loja de apps.
