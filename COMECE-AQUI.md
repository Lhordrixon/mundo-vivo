# Comece aqui

Este é o caminho do zero até o seu primeiro pull request. Siga na ordem.
Cada passo diz o que digitar e o que deve aparecer na tela.

> [!IMPORTANT]
> **Se algum passo falhar, não tente adivinhar.** Copie a mensagem de erro
> inteira e [abra uma issue](https://github.com/Lhordrixon/mundo-vivo/issues/new/choose)
> com ela. Um erro relatado ajuda o projeto tanto quanto um código novo.

## Qual é o caminho inteiro?

1. Instalar o Git
2. Instalar o JDK 17
3. Instalar o Android Studio
4. Criar uma conta no GitHub
5. Baixar o projeto
6. Abrir o projeto no Android Studio
7. Rodar os testes
8. Rodar o jogo
9. Escolher uma tarefa
10. Criar uma branch
11. Fazer a mudança e testar
12. Salvar com um commit
13. Enviar para o GitHub
14. Abrir o pull request
15. Acompanhar a revisão

---

## Parte 1: como preparo o computador?

### 1. Instalar o Git

O **Git** guarda o histórico do código e envia suas mudanças para o GitHub.

Baixe em [git-scm.com](https://git-scm.com/downloads) e instale com as
opções padrão.

Para conferir, abra um terminal e digite:

```bash
git --version
```

Deve aparecer algo como:

```
git version 2.45.0
```

> [!NOTE]
> **No Windows, use o Git Bash** como terminal em todos os passos. Ele é
> instalado junto com o Git. Os comandos deste guia não funcionam no Prompt
> de Comando.

### 2. Instalar o JDK 17

O **JDK** é o kit que compila e roda programas em Java.

Baixe o **Temurin 17** em [adoptium.net](https://adoptium.net/temurin/releases/?version=17)
e instale.

Para conferir:

```bash
java -version
```

Deve aparecer uma versão **17 ou maior**:

```
openjdk version "17.0.12" ...
```

### 3. Instalar o Android Studio

O **Android Studio** traz o SDK do Android. O **SDK** é o conjunto de
ferramentas para montar apps Android. Este projeto precisa dele até para
rodar no computador.

1. Baixe em [developer.android.com/studio](https://developer.android.com/studio).
2. Instale e abra.
3. Siga o assistente com as opções padrão. Ele baixa o SDK sozinho.

Deu certo quando a tela de boas-vindas do Android Studio aparece.

### 4. Criar uma conta no GitHub

Se ainda não tiver, crie em [github.com](https://github.com/signup).

Depois, peça ao dono do repositório para te adicionar como
**colaborador**. Sem isso, o passo 13 vai falhar com erro de permissão.

---

## 🚀 Parte 2: como faço o projeto rodar?

### 5. Baixar o projeto

Escolha uma pasta e rode:

```bash
git clone https://github.com/Lhordrixon/mundo-vivo.git
cd mundo-vivo
```

Deve aparecer, no fim:

```
Resolving deltas: 100% ... done.
```

### 6. Abrir o projeto no Android Studio

1. No Android Studio, clique em **Open**.
2. Escolha a pasta `mundo-vivo`.
3. Espere a barra de baixo terminar de sincronizar o Gradle.

A primeira vez demora alguns minutos. Esse passo cria o arquivo
`local.properties`, que diz ao projeto onde está o SDK.

Deu certo quando a sincronização termina sem erro em vermelho.

### 7. Rodar os testes

No terminal, dentro da pasta `mundo-vivo`:

```bash
./gradlew core:test
```

A primeira vez baixa muita coisa e demora. Vão aparecer linhas terminando
em `PASSED`. No fim, deve aparecer:

```
BUILD SUCCESSFUL
```

### 8. Rodar o jogo

```bash
./gradlew desktop:run
```

Deve abrir uma janela com um mapa colorido, parecido com a imagem do
README. Arraste para mover e use a roda do mouse para dar zoom. Um clique
numa criatura a fere.

> [!WARNING]
> Este comando precisa do Android Studio (passo 3) instalado, mesmo
> rodando no computador. O jogo já rodou num celular, mas ninguém
> registrou ainda o `desktop:run`. Se a janela não abrir, isso é uma
> descoberta importante: [abra uma issue](https://github.com/Lhordrixon/mundo-vivo/issues/new/choose)
> com o erro copiado.

---

## Parte 3: como envio minha primeira contribuição?

### 9. Escolher uma tarefa

Procure as issues com o rótulo
[good first issue](https://github.com/Lhordrixon/mundo-vivo/labels/good%20first%20issue).
São tarefas pequenas, pensadas para quem está começando.

Se não houver nenhuma, escolha um item da
[dívida técnica](docs/tecnico.md#dívida-técnica-conhecida). Abra uma issue
dizendo que vai fazê-lo, e espere uma resposta antes de começar.

### 10. Criar uma branch

Uma **branch** é uma cópia paralela do código, onde você trabalha sem
mexer no dos outros.

```bash
git switch main
git pull
git switch -c docs/corrige-contagem-de-ferramentas
```

Troque o nome pelo da sua tarefa. O padrão de nomes está no
[CONTRIBUTING.md](CONTRIBUTING.md). Deve aparecer:

```
Switched to a new branch 'docs/corrige-contagem-de-ferramentas'
```

### 11. Fazer a mudança e testar

Faça a mudança. Depois, rode os testes de novo:

```bash
./gradlew core:test
```

Só siga em frente com `BUILD SUCCESSFUL`.

### 12. Salvar com um commit

Um **commit** é um ponto salvo no histórico, com uma mensagem explicando o
que mudou.

```bash
git add .
git commit -m "Corrige a contagem de ferramentas no README"
```

Deve aparecer um resumo como:

```
[docs/corrige-contagem-de-ferramentas 1a2b3c4] Corrige a contagem ...
 1 file changed, 2 insertions(+), 2 deletions(-)
```

### 13. Enviar para o GitHub

```bash
git push -u origin docs/corrige-contagem-de-ferramentas
```

Deve aparecer um link, parecido com este:

```
remote: Create a pull request for 'docs/corrige-contagem-de-ferramentas' on GitHub by visiting:
remote:      https://github.com/Lhordrixon/mundo-vivo/pull/new/docs/corrige-contagem-de-ferramentas
```

### 14. Abrir o pull request

Um **pull request** é o pedido para a sua mudança entrar no projeto.

1. Abra o link que apareceu no passo anterior.
2. Preencha o formulário. Ele já vem com as perguntas certas.
3. Clique em **Create pull request**.

### 15. Acompanhar a revisão

Na página do pull request, a CI roda os testes sozinha. A **CI** é um robô
que testa toda mudança antes de ela entrar.

- **Verde:** tudo certo. Agora é esperar a revisão.
- **Vermelho:** clique em **Details** para ver o erro. Se não entender,
  pergunte num comentário do próprio pull request.

Se pedirem mudanças, faça, e repita os passos 11 a 13. O pull request se
atualiza sozinho.

**Pronto: esse é o seu primeiro pull request.**
