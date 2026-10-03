"""Resposta automática a uma dúvida, sem LLM (ajuda.yml).

Lê o formulário da issue (aula, passo, mensagem de erro), acha a aula
em aprender/ e responde com o "⚠️ Não funcionou?" do passo, tirado do
próprio .md da aula (fonte única). Se faltar a mensagem de erro, pede.

Variáveis: GH_TOKEN, GITHUB_REPOSITORY, NUMERO, CORPO.
SIMULAR=1 só imprime a resposta.
"""
import glob
import json
import os
import re
import urllib.request

RAIZ = os.path.dirname(os.path.dirname(os.path.dirname(
    os.path.abspath(__file__))))
MARCA = "<!-- ajuda -->"
VAZIO = ("", "_No response_", "None")
DUVIDA = ("https://github.com/" + os.environ.get("GITHUB_REPOSITORY", "")
          + "/issues/new?template=duvida.yml")

# Palavras que ligam um passo do formulário aos blocos da aula.
PALAVRAS = {
    "Instalar": ["Pydroid", "IDLE", "Instalar", "Python", "ZIP", "Raw"],
    "APK": ["APK", "comentário", "Checks", "Instalar", "App não"],
    "PR": ["Commit", "commit", "pull request", "branch", "editor",
           "Fork", "Desktop", "botão", "Propose"],
}

# A última linha do erro diz o tipo; cada tipo tem uma saída comum.
ERROS = [
    ("AssertionError", "É a dica da própria aula: leia o texto depois de "
     "`AssertionError:`. Ele diz o que aconteceu e onde olhar."),
    ("NameError", "O Python não conhece um nome. Quase sempre é texto sem "
     "aspas (`A` em vez de `\"A\"`) ou um nome escrito diferente."),
    ("SyntaxError", "O Python não entendeu a linha. Confira aspas, "
     "parênteses e os dois-pontos `:` no fim do `if`, `for` e `def`."),
    ("IndentationError", "O recuo está errado. O que fica dentro do "
     "`if`, `for` ou `def` vai 4 espaços para a direita."),
    ("TypeError", "Você juntou coisas de tipos diferentes, como texto com "
     "número. Use `str(numero)` para virar texto."),
    ("IndexError", "Você pediu uma posição que a lista não tem. Lembre: "
     "a contagem começa no 0."),
    ("KeyError", "O dicionário não tem essa chave. Confira o nome entre "
     "aspas, letra por letra."),
    ("AttributeError", "O objeto não tem esse campo ou método. Confira o "
     "nome e o `self.` dentro da classe."),
]


def campos(corpo):
    """Lê o formulário: '### Título' seguido do valor."""
    saida = {}
    partes = re.split(r"^### (.+)$", corpo or "", flags=re.M)
    for i in range(1, len(partes) - 1, 2):
        valor = partes[i + 1].strip()
        valor = re.sub(r"^```\w*\n?|\n?```$", "", valor).strip()
        saida[partes[i].strip()] = "" if valor in VAZIO else valor
    return saida


def arquivo_da_aula(aula):
    m = re.match(r"(\d\.\d)\b", aula or "")
    if m:
        achados = glob.glob(os.path.join(RAIZ, "aprender",
                                         m.group(1) + "-*.md"))
        return achados[0] if achados else None
    m = re.match(r"Prova (N\d)", aula or "")
    if m:
        for ext in (".md", ".py"):
            p = os.path.join(RAIZ, "aprender", "provas", m.group(1) + ext)
            if os.path.exists(p):
                return p
    return None


def blocos(md):
    """Cada '⚠️ Não funcionou?' com o passo que vem antes dele."""
    saida = []
    padrao = re.compile(r"<details><summary>⚠️ Não funcionou\?</summary>"
                        r"(.*?)</details>", re.S)
    for m in padrao.finditer(md):
        antes = md[:m.start()].rstrip().splitlines()
        passo = next((linha.strip() for linha in reversed(antes)
                      if linha.strip()), "")
        itens = [linha.strip() for linha in m.group(1).splitlines()
                 if linha.strip().startswith("- ")
                 and "Travou mesmo assim" not in linha]
        saida.append((passo, itens))
    return saida


def resposta(f):
    aula, passo = f.get("Aula", ""), f.get("Passo", "")
    erro = f.get("Mensagem de erro", "")
    linhas = ["Oi! Resposta automática enquanto ninguém chega. 👋", ""]
    caminho = arquivo_da_aula(aula)
    nome = os.path.basename(caminho) if caminho else ""
    if erro:
        ultima = erro.strip().splitlines()[-1]
        for tipo, saida in ERROS:
            if tipo in ultima:
                linhas += ["**Sobre o erro (`%s`):** %s" % (tipo, saida), ""]
                break
    if passo in ("Prever", "Investigar"):
        linhas += ["Para %s, tente responder antes de olhar. As respostas "
                   "ficam no **🔓 Resposta**, no fim da aula." % passo, ""]
    elif passo in ("Mudar", "Montar"):
        linhas += ["A instrução está no `.py`, no comentário **✏️ Sua vez** "
                   "do %s. Rode e leia a última linha: a dica diz o que "
                   "aconteceu e onde olhar." % passo.upper(), ""]
    if caminho and caminho.endswith(".md"):
        with open(caminho, encoding="utf-8") as arq:
            achados = blocos(arq.read())
        chaves = PALAVRAS.get(passo)
        if chaves:
            achados = [b for b in achados
                       if any(c in b[0] + " ".join(b[1]) for c in chaves)]
        if achados:
            linhas.append("**Da aula %s, o que costuma resolver:**" % nome)
            linhas.append("")
            for passo_txt, itens in achados[:4]:
                titulo = re.sub(r"\*\*|`|\[|\]\([^)]*\)", "", passo_txt)
                titulo = titulo.lstrip("0123456789. ")
                if len(titulo) > 80:
                    titulo = titulo[:80].rsplit(" ", 1)[0] + "…"
                linhas.append("_%s_" % titulo)
                linhas += itens
                linhas.append("")
    if not erro:
        linhas += ["Pode colar aqui a **mensagem de erro**? A última linha "
                   "que apareceu já ajuda muito.", ""]
    linhas.append("Se isto não resolveu, responda aqui mesmo contando o que "
                  "aconteceu.")
    return MARCA + "\n" + "\n".join(linhas) + "\n"


def comentar(texto):
    repo = os.environ["GITHUB_REPOSITORY"]
    req = urllib.request.Request(
        "https://api.github.com/repos/%s/issues/%s/comments"
        % (repo, os.environ["NUMERO"]), method="POST",
        data=json.dumps({"body": texto}).encode(),
        headers={"Authorization": "Bearer " + os.environ["GH_TOKEN"],
                 "Accept": "application/vnd.github+json"})
    urllib.request.urlopen(req).read()


if __name__ == "__main__":
    texto = resposta(campos(os.environ.get("CORPO", "")))
    if os.environ.get("SIMULAR") == "1":
        print(texto)
    else:
        comentar(texto)
