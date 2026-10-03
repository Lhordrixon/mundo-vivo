# Diagnóstico CLEAR-X da trilha `aprender/`

Para mantenedores e agentes. O Alan não precisa ler isto.
Medido em 03/10/2026, na main `868a9b6`. Medições refeitas com
`python3 docs/clear-x/medir.py`.

Rótulos de evidência: FORTE, MODERADA, HIPÓTESE, METÁFORA.

## A. Estado atual

- 17 aulas em 3 níveis abertos (0 Python que falta, 1 O mundo em
  Python, 2 Mexendo no jogo). Níveis 3 e 4 só descritos.
- Cada aula: `.md` (lido no GitHub) + `.py` (rodado no Pydroid ou
  IDLE). Formato fixo: pergunta da aula anterior, 🎯, 💡, PRIMM,
  ⚠️, ✅, 🔓 resposta, ➡️ próxima.
- O `.py` para numa dica (`assert`) até a lacuna ser preenchida e
  termina em "✅ Aula concluída!".
- O job "Aulas" (`aprender/verificar.py`) garante resposta, dica,
  links, Java citado, limites e PEP 8.
- APK por link direto (Release `ultima-versao` e `pr-<n>`), com
  chave fixa.
- Ajuda: modelo de dúvida (texto livre), tutor com LLM desligado
  (falta segredo), lembrete diário sem LLM.
- O Alan: Python e C++ iniciais, PEP 8 à risca, "absorvendo tudo
  primeiro", programa no celular, baixou o ZIP e o app do GitHub.
  Nenhuma aula marcada na #12 até hoje.

## B. Mapa de fricção (medido)

**B1. Primeira vitória longa demais.**
- Medida: do README ao primeiro ▶ no Pydroid, 19 toques (1 link do
  README, 1 "Começar", 2 do convite, 4 para instalar o Pydroid, 1 do
  ZIP, 3 para extrair, 6 para achar o arquivo, 1 ▶). Até o primeiro
  ✅, mais 6 (duas lacunas, duas execuções): 25.
- Causa: convite e ZIP vêm antes de rodar qualquer coisa. Achar o
  arquivo no Pydroid é o trecho mais frágil (pasta repetida).
- Consequência: muito esforço antes de qualquer resultado. Esforço
  desperdiçado, não desejável (Bjork, MODERADA).
- Solução: copiar o Raw e colar no Pydroid vem primeiro; convite e
  ZIP depois (Fase A4).

**B2. Instrução da lacuna longe da lacuna.**
- Medida: nos níveis 0 e 1, Mudar e Montar ficam no `.md`. A lacuna
  fica no `.py`. Trocas de app por aula: cerca de 6 (ler no Chrome →
  rodar no Pydroid → voltar para ler Mudar → Pydroid → voltar para
  Montar → Pydroid → voltar para a próxima).
- Causa: o formato pôs o PRIMM inteiro no `.md`.
- Consequência: atenção dividida no celular, que é o pior caso
  (carga cognitiva, FORTE).
- Solução: a instrução vai para dentro do `.py`, na linha acima da
  lacuna; o `.md` aponta para lá (Fase A1). Meta: 2 trocas por aula.

**B3. Texto visível demais em metade das aulas.**
- Medida (palavras visíveis, fora de `<details>` fechado):
  - mínimo 271 (0.1) · mediana 441 · máximo 619 (2.1).
  - Acima da mediana: 0.0 (483), 1.1 (494), 1.2 (468), 1.3 (481),
    1.4 (497), 1.5 (463), 1.7 (485), 2.1 (619).
- Causa: a seção "🔗 No jogo de verdade" mostra Java e Python
  abertos; 2.1 mostra a Roseta inteira; 0.0 mostra os três caminhos.
- Consequência: "absorvendo tudo primeiro" vira ler sem fazer.
- Solução: teto no `verificar.py` = 441 (nunca aumentar). O que é
  "por quê" ou "exceção" vai para `<details>` (camadas L3 e L4).

**B4. Termos usados antes de explicados.**
- Medida (primeira aparição sem explicação):
  - `assert` (0.0, explicado de passagem), `Raw` e `ZIP` (0.0,
    explicados só depois), `commit` (1.1, explicado só na 2.3),
    `issue` e `pull request` (COMECE-AQUI, explicados na 2.2),
    `APK` (título da 2.4, explicado no passo 6).
- Causa: nenhum lugar único para termos.
- Solução: `aprender/GLOSSARIO.md`, com link na primeira aparição
  (Fase A5).

**B5. Arquivos de build no caminho.**
- Medida: a raiz do ZIP e do GitHub tem 19 itens. 8 são de build ou
  de máquina: `gradlew`, `gradlew.bat`, `gradle/`, `build.gradle`,
  `settings.gradle`, `gradle.properties`, `.devcontainer/`,
  `.github/`. O Alan provavelmente abriu o `gradlew` no celular.
- Causa: estrutura padrão de projeto Gradle.
- Solução: já existe "Arquivos para ignorar" no COMECE-AQUI. Mover
  arquivos de código exige aprovação; não mexer.

**B6. Dica do `assert` sem direção.**
- Medida: em 62 dicas, quase todas dizem o que deu errado; poucas
  dizem onde olhar.
- Solução: dica em 4 partes curtas: o que aconteceu, por quê, onde
  olhar, tente (Fase A3; feedback elaborado, MODERADA).

**B7. Pedir ajuda depende de alguém.**
- Medida: a dúvida é texto livre; o tutor com LLM está desligado.
- Consequência: novato sem resposta desiste (MODERADA).
- Solução: formulário com aula e passo, e resposta automática sem
  LLM com o "Não funcionou?" do passo (Fase B).

**B8. Sem provas nem revisão.**
- Medida: nenhuma prova de nível; nenhuma revisão espaçada.
- Consequência: reconhecimento em vez de retenção.
- Solução: provas N0, N1, N2 com teste de pular; revisões na #12 em
  ~2 e ~7 dias (Fase C; recuperação e espaçamento, FORTE).

## C. Como o conhecimento é apresentado

- Exemplo pronto → lacuna pequena → lacuna maior: o fading existe
  dentro de cada aula (exemplos trabalhados, FORTE).
- PRIMM em todas as aulas (MODERADA). O "Prever" é curto, como deve
  ser para iniciante.
- Analogia antes do nome técnico (biscoito para classe, prédio para
  lista de listas).
- Ponte Python → Java pela Roseta. O C++ entra como ponte na 2.1.
- Falta: rastreio passo a passo (máquina nocional, MODERADA),
  rótulos fixos de subobjetivo (MODERADA), problema de Parsons.

## D. Grafo de conhecimento

Conceito → pré-requisitos → aula → arquivo Java (ver `MAPA.md`):

- Lista 2D → variável, lista → 0.1 → `World.java` (grade)
- Dicionário/objeto → variável → 0.2, 0.4 → `Creature.java`
- Função, `return` → variável → 0.3 → métodos em geral
- Classe, método, `self` → função, dicionário → 0.4 →
  `Creature.java`
- Semente → classe → 1.1 → `WorldGenerator.java`, `Rng.java`
- Passo de tempo (`dt`) → função → 1.2 → `Simulation.moveToward`
- Estado → `if`, classe → 1.3 → `CreatureState.java`,
  `CreatureRenderer.colorFor`
- Índice plano → lista 2D → 1.4 → `World.index`, `FoodMap.java`
- Porta única de dano, laço de trás para frente → lista → 1.5 →
  `Creature.applyDamage`, `Simulation.step`
- Condição composta → `and` → 1.6 → `Creature.canReproduce`
- Loop do mundo → tudo acima → 1.7 → `Simulation.step`
- Ler Java → C++ básico, nível 1 → 2.1 → `Simulation`, `World`
- PR, branch, commit, CI → nenhum → 2.2, 2.3
- Mudar Java e ver no APK → 2.1, 2.2 → 2.4, 2.5 →
  `CreatureRenderer`, `CreatureConfig`

## E. Dependências cognitivas

variável → lista → lista 2D → dicionário → função → classe →
estado → passo de tempo → laço do mundo → ler Java → mudar Java →
ver no APK → ler teste → escrever teste → corrigir bug → projetar
→ construir sozinho (meta do dono).

Hoje a trilha cobre até "ver no APK". Ler e escrever teste,
corrigir e projetar ficam nos níveis 3 e 4 (Fase D).

## F. Experiência atual × proposta

- Celular hoje: Chrome ↔ Pydroid umas 6 vezes por aula; 19 toques
  até o primeiro ▶.
- Celular proposto: lê o 🎯 no GitHub, cola o Raw, faz tudo no
  Pydroid (instrução, dica, rastreio), volta só para a próxima. Meta:
  2 trocas por aula; menos de 19 toques até o primeiro ▶.
- Travou hoje: abre issue em texto livre e espera uma pessoa.
- Travou proposto: dica de 4 partes → "Não funcionou?" → formulário
  → resposta automática em minutos → LLM se houver.
- Rápido hoje: lê tudo. Proposto: roda a prova e pula o nível.
- Sumiu hoje: lembrete no máximo 1 por semana. Proposto: o mesmo
  teto, somando lembrete e revisão, e para depois de 4 semanas.

## G. Interação, feedback, progressão, medição

- Interação: uma coisa por tela; código de até 60 colunas.
- Feedback: imediato no `.py` (dica e ✅); rastreio impresso onde o
  estado muda (1.2, 1.3, 1.4, 1.7).
- Progressão: aula → prova do nível → nível seguinte. Pular com a
  prova (reversão da expertise, FORTE).
- Medição (sem tempo de tela nem cliques): aulas marcadas na #12,
  dúvidas por aula e passo, provas passadas de primeira, revisões
  respondidas, PRs do Alan. Gravado em `docs/clear-x/METRICAS.md`.
- Sem streak, ponto, medalha ou contagem regressiva (gamificação,
  HIPÓTESE de risco).

## H. Restrições técnicas reais

- `.py`: biblioteca padrão, Python 3.8+, até 40 linhas de código
  (comentário não conta), 60 colunas, mapa até 30, PEP 8.
- Sem aparelho real: Pydroid, editor do GitHub no celular, nomes de
  botões e GitHub Desktop não são testáveis daqui.
- Sem admin: não dá para fixar issue nem criar segredo. O tutor com
  LLM depende do segredo `CLAUDE_CODE_OAUTH_TOKEN`.
- Workflows com `GITHUB_TOKEN` não disparam outros workflows.
- `desktop:run` nunca rodou (README); a camada `render/` não tem
  prova de execução.

## I. Prioridades (dependência e impacto)

1. Fase A: tirar atrito (instrução no `.py`, 0.0 mais curta, teto de
   palavras, glossário, dicas, rastreio). Tudo depois depende do
   Alan conseguir fazer a primeira aula.
2. Fase B: ajuda automática sem LLM. Responde enquanto não há tutor.
3. Fase C: provas, pular e revisar. Mede retenção e permite pular.
4. Fase D: níveis 3 e 4. Só servem depois do nível 2.
5. Fase E: autonomia (routine, APK no emulador, aprendiz simulado).
6. Fase F: reconhecimento sem gamificação.
