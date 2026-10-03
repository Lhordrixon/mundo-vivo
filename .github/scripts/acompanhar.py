"""Acompanhamento diário da trilha, sem Claude (acompanhar.yml).

1. Trava silenciosa: se a lista da issue da trilha (#12) não avançar em 4
   dias, comenta nela marcando o Alan, no máximo uma vez por semana.
   O estado (quantas aulas marcadas, desde quando, último lembrete) fica
   num comentário de progresso da própria issue, editado em silêncio.
2. Dúvidas paradas: fecha issues "duvida" cuja última palavra é de outra
   pessoa (o tutor ou o dono) e está sem resposta há 7 dias.

Só biblioteca padrão. Variáveis de ambiente:
  GH_TOKEN, GITHUB_REPOSITORY  obrigatórias
  HOJE=AAAA-MM-DD              finge outra data (para testar)
  SIMULAR=1                    só mostra o que faria, sem escrever nada
"""
import datetime
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


def trava_silenciosa():
    issue = api("GET", "/issues/%d" % TRILHA)
    if issue["state"] != "open":
        print("Trilha fechada: nada a fazer.")
        return
    corpo = issue["body"] or ""
    total = len(re.findall(r"^- \[[ xX]\]", corpo, re.M))
    marcadas = len(re.findall(r"^- \[[xX]\]", corpo, re.M))
    print("Trilha: %d de %d aulas marcadas." % (marcadas, total))

    progresso = next((c for c in comentarios(TRILHA)
                      if MARCA in (c["body"] or "")), None)
    estado = {"marcadas": marcadas, "desde": hoje.isoformat(), "aviso": ""}
    if progresso:
        m = re.search(r"<!-- estado (\{.*?\}) -->", progresso["body"])
        if m:
            estado = json.loads(m.group(1))

    if estado["marcadas"] != marcadas:
        estado["marcadas"] = marcadas
        estado["desde"] = hoje.isoformat()
    parado = (hoje - datetime.date.fromisoformat(estado["desde"])).days
    ultimo = estado["aviso"]
    pode_avisar = (not ultimo or (hoje - datetime.date.fromisoformat(
        ultimo)).days >= DIAS_ENTRE_LEMBRETES)
    print("Parado há %d dia(s); último lembrete: %s."
          % (parado, ultimo or "nenhum"))

    if 0 < total and marcadas < total and parado >= DIAS_PARADO \
            and pode_avisar:
        texto = ("Oi, @%s! Faz uns dias que a trilha não anda. "
                 "Em que aula você parou? Se travou em algum passo, "
                 "[abre uma dúvida](%s) que a gente destrava junto."
                 % (ALAN, DUVIDA))
        escrever("POST", "/issues/%d/comments" % TRILHA, {"body": texto},
                 "Lembrete na #%d: parado há %d dias." % (TRILHA, parado))
        estado["aviso"] = hoje.isoformat()

    novo = ("%s\n📊 Progresso da trilha: **%d de %d** aulas marcadas "
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
    trava_silenciosa()
    duvidas_paradas()
