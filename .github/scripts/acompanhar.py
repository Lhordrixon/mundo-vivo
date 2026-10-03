"""Acompanhamento diário da trilha, sem Claude (acompanhar.yml).

1. Revisão espaçada: ~2 e ~7 dias depois de cada aula marcada na trilha
   (#12), posta uma pergunta de recuperação (o 🔁 da aula seguinte). O
   rótulo "sem-revisao" na #12 desliga.
2. Trava silenciosa: se a lista não avançar em 4 dias, pergunta em que
   aula ele parou. Depois de 4 semanas parado, para de perguntar.
   Teto: 1 mensagem por semana somando revisão e lembrete.
3. Dúvidas paradas: fecha issues "duvida" cuja última palavra é de outra
   pessoa (o tutor ou o dono) e está sem resposta há 7 dias.
4. Métricas: às segundas, grava docs/clear-x/METRICAS.md (o workflow faz
   o commit). Nunca mede tempo de tela nem cliques.

O estado (aulas marcadas e quando, revisões feitas, última mensagem)
fica num comentário de progresso da própria #12, editado em silêncio.

Só biblioteca padrão. Variáveis de ambiente:
  GH_TOKEN, GITHUB_REPOSITORY  obrigatórias
  HOJE=AAAA-MM-DD              finge outra data (para testar)
  SIMULAR=1                    só mostra o que faria, sem escrever nada
"""
import datetime
import glob
import json
import os
import re
import urllib.request

REPO = os.environ["GITHUB_REPOSITORY"]
TOKEN = os.environ["GH_TOKEN"]
SIMULAR = os.environ.get("SIMULAR") == "1"
TRILHA = 12
ALAN = "wrgw34wg3gghw3g"
DIAS_PARADO = 4
DIAS_ENTRE_LEMBRETES = 7
DIAS_SEM_RESPOSTA = 7
DIAS_PARA_DESISTIR = 28
REVISOES = (2, 7)
RAIZ = os.path.dirname(os.path.dirname(os.path.dirname(
    os.path.abspath(__file__))))
MARCA = "<!-- progresso-da-trilha -->"
DUVIDA = ("https://github.com/" + REPO
          + "/issues/new?template=duvida.yml")

hoje = datetime.date.fromisoformat(
    os.environ.get("HOJE") or datetime.date.today().isoformat())


def api(metodo, caminho, dados=None):
    req = urllib.request.Request(
        "https://api.github.com/repos/" + REPO + caminho,
        method=metodo,
        data=json.dumps(dados).encode() if dados is not None else None,
        headers={"Authorization": "Bearer " + TOKEN,
                 "Accept": "application/vnd.github+json",
                 "Content-Type": "application/json"})
    with urllib.request.urlopen(req) as r:
        corpo = r.read()
        return json.loads(corpo) if corpo else None


def escrever(metodo, caminho, dados, descricao):
    print(("[simulação] " if SIMULAR else "") + descricao)
    print("::notice title=Acompanhamento::" + descricao)
    if not SIMULAR:
        api(metodo, caminho, dados)


def comentarios(numero):
    saida, pagina = [], 1
    while True:
        lote = api("GET", "/issues/%d/comments?per_page=100&page=%d"
                   % (numero, pagina))
        saida += lote
        if len(lote) < 100:
            return saida
        pagina += 1


def data(texto):
    return datetime.date.fromisoformat(texto[:10])


def aulas_em_ordem():
    nomes = sorted(os.path.basename(n)[:-3] for n in glob.glob(
        os.path.join(RAIZ, "aprender", "[0-9].[0-9]-*.md")))
    return nomes


def pergunta_de_revisao(codigo):
    """O 🔁 da aula seguinte pergunta sobre esta aula."""
    nomes = aulas_em_ordem()
    atual = [n for n in nomes if n.startswith(codigo + "-")]
    if not atual or nomes.index(atual[0]) + 1 >= len(nomes):
        return None, None
    seguinte = nomes[nomes.index(atual[0]) + 1]
    with open(os.path.join(RAIZ, "aprender", seguinte + ".md"),
              encoding="utf-8") as f:
        for linha in f:
            if linha.startswith("🔁"):
                texto = re.sub(r"^🔁\s*\*\*[^*]*\*\*\s*", "", linha.strip())
                return texto, seguinte
    return None, None


def trilha():
    issue = api("GET", "/issues/%d" % TRILHA)
    if issue["state"] != "open":
        print("Trilha fechada: nada a fazer.")
        return
    corpo = issue["body"] or ""
    total = len(re.findall(r"^- \[[ xX]\]", corpo, re.M))
    marcadas = len(re.findall(r"^- \[[xX]\]", corpo, re.M))
    codigos = re.findall(r"^- \[[xX]\].*?(\d\.\d)\b", corpo, re.M)
    provas = re.findall(r"^- \[[xX]\].*?Prova (N\d)", corpo, re.M)
    sem_revisao = any(r["name"] == "sem-revisao" for r in issue["labels"])
    print("Trilha: %d de %d marcadas." % (marcadas, total))

    lista = comentarios(TRILHA)
    progresso = next((c for c in lista if MARCA in (c["body"] or "")),
                     None)
    estado = {"marcadas": marcadas, "desde": hoje.isoformat(), "aviso": ""}
    if progresso:
        m = re.search(r"<!-- estado (\{.*?\}) -->", progresso["body"])
        if m:
            estado = json.loads(m.group(1))
    estado.setdefault("aulas", {})
    estado.setdefault("revisoes", {})
    for codigo in codigos:
        estado["aulas"].setdefault(codigo, hoje.isoformat())

    if estado["marcadas"] != marcadas:
        estado["marcadas"] = marcadas
        estado["desde"] = hoje.isoformat()
    parado = (hoje - datetime.date.fromisoformat(estado["desde"])).days
    ultimo = estado["aviso"]
    pode_falar = (not ultimo or (hoje - datetime.date.fromisoformat(
        ultimo)).days >= DIAS_ENTRE_LEMBRETES)
    print("Parado há %d dia(s); última mensagem: %s."
          % (parado, ultimo or "nenhuma"))

    texto = None
    if pode_falar and not sem_revisao:
        for codigo, quando in sorted(estado["aulas"].items(),
                                     key=lambda item: item[1]):
            feitas = estado["revisoes"].get(codigo, [])
            dias = (hoje - datetime.date.fromisoformat(quando)).days
            devida = next((r for r in REVISOES
                           if dias >= r and r not in feitas), None)
            if devida is None:
                continue
            pergunta, seguinte = pergunta_de_revisao(codigo)
            estado["revisoes"][codigo] = feitas + [devida]
            if pergunta:
                texto = ("Oi, @%s! Revisão rápida da aula %s, sem "
                         "consultar: %s Responda aqui com as suas "
                         "palavras. Depois confira no 🔁 da aula %s. Não "
                         "quer revisões? Ponha o rótulo `sem-revisao` "
                         "nesta issue." % (ALAN, codigo, pergunta, seguinte))
                descricao = ("Revisão de %d dias da aula %s."
                             % (devida, codigo))
                break
    if texto is None and pode_falar and 0 < total and marcadas < total \
            and DIAS_PARADO <= parado < DIAS_PARA_DESISTIR:
        texto = ("Oi, @%s! Faz uns dias que a trilha não anda. "
                 "Em que aula você parou? Se travou em algum passo, "
                 "[abre uma dúvida](%s) que a gente destrava junto."
                 % (ALAN, DUVIDA))
        descricao = "Lembrete na #%d: parado há %d dias." % (TRILHA, parado)
    if texto:
        escrever("POST", "/issues/%d/comments" % TRILHA, {"body": texto},
                 descricao)
        estado["aviso"] = hoje.isoformat()

    novo = ("%s\n📊 Progresso da trilha: **%d de %d** marcadas "
            "(atualizado em %s).\n<!-- estado %s -->"
            % (MARCA, marcadas, total, hoje.isoformat(),
               json.dumps(estado)))
    if progresso is None:
        escrever("POST", "/issues/%d/comments" % TRILHA, {"body": novo},
                 "Criando o comentário de progresso na #%d." % TRILHA)
    elif progresso["body"] != novo:
        escrever("PATCH", "/issues/comments/%d" % progresso["id"],
                 {"body": novo}, "Atualizando o progresso: %d de %d."
                 % (marcadas, total))
    return estado, marcadas, total, codigos, provas, lista


def metricas(resultado):
    """Grava METRICAS.md às segundas (o workflow faz o commit)."""
    if hoje.weekday() != 0 and os.environ.get("METRICAS") != "1":
        return
    estado, marcadas, total, codigos, provas, lista = resultado
    duvidas = api("GET", "/issues?labels=duvida&state=all&per_page=100")
    por_aula = {}
    for issue in duvidas:
        m = re.search(r"### Aula\s+(\S+)", issue["body"] or "")
        chave = m.group(1) if m else "?"
        por_aula[chave] = por_aula.get(chave, 0) + 1
    revisoes = sum(len(v) for v in estado["revisoes"].values())
    respostas = sum(1 for c in lista if c["user"]["login"] == ALAN)
    prs = [p for p in api("GET", "/pulls?state=closed&per_page=100")
           if p["user"]["login"] == ALAN and p.get("merged_at")]
    linhas = [
        "## Semana de %s" % hoje.isoformat(), "",
        "- Marcadas na trilha: %d de %d (aulas: %s)" % (
            marcadas, total, ", ".join(codigos) or "nenhuma"),
        "- Provas marcadas: %s" % (", ".join(provas) or "nenhuma"),
        "- Dúvidas por aula: %s" % (", ".join(
            "%s: %d" % kv for kv in sorted(por_aula.items())) or "nenhuma"),
        "- Revisões enviadas: %d · comentários do Alan na trilha: %d"
        % (revisoes, respostas),
        "- PRs do Alan mesclados: %d" % len(prs), ""]
    caminho = os.path.join(RAIZ, "docs", "clear-x", "METRICAS.md")
    antigo = ""
    if os.path.exists(caminho):
        with open(caminho, encoding="utf-8") as f:
            antigo = f.read()
    if "## Semana de %s" % hoje.isoformat() in antigo:
        return
    topo = ("# Métricas da trilha\n\nGravado às segundas pelo "
            "acompanhar.yml. Mede aprendizagem e contribuição, nunca "
            "tempo de tela.\n\n")
    corpo = antigo[len(topo):] if antigo.startswith(topo) else antigo
    print("[simulação] " if SIMULAR else "", "\n".join(linhas))
    if not SIMULAR:
        with open(caminho, "w", encoding="utf-8") as f:
            f.write(topo + "\n".join(linhas) + "\n" + corpo)


def duvidas_paradas():
    abertas = api("GET", "/issues?labels=duvida&state=open&per_page=100")
    for issue in abertas:
        if "pull_request" in issue:
            continue
        lista = comentarios(issue["number"])
        if not lista:
            continue          # ninguém respondeu ainda: não é dele a vez
        ultimo = lista[-1]
        if ultimo["user"]["login"] == issue["user"]["login"]:
            continue          # a última palavra é de quem perguntou
        dias = (hoje - data(ultimo["created_at"])).days
        if dias < DIAS_SEM_RESPOSTA:
            continue
        texto = ("Fechando porque a conversa parou há %d dias. Se ainda "
                 "precisar, toque em **Reopen issue** ou abra outra "
                 "dúvida." % dias)
        escrever("POST", "/issues/%d/comments" % issue["number"],
                 {"body": texto}, "Fechando a dúvida #%d (parada há %d "
                 "dias)." % (issue["number"], dias))
        escrever("PATCH", "/issues/%d" % issue["number"],
                 {"state": "closed", "state_reason": "completed"},
                 "Dúvida #%d fechada." % issue["number"])


if __name__ == "__main__":
    print("Hoje: %s%s" % (hoje, " (simulação)" if SIMULAR else ""))
    resultado = trilha()
    duvidas_paradas()
    if resultado:
        metricas(resultado)
