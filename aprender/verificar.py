"""Confere as aulas de aprender/. Roda na CI (job "Aulas") e à mão:

    python3 aprender/verificar.py          # todas as aulas
    python3 aprender/verificar.py 2.4-cor  # só uma

O que confere:
1. Cada .py: linhas <= 60 colunas, sintaxe do Python 3.8, <= 40 linhas
   de código, só caracteres que o IDLE antigo mostra.
2. Com as lacunas vazias, o .py para numa dica em português
   (AssertionError com texto), nunca noutro erro.
3. Com as respostas de respostas.json, uma por vez, cada etapa mostra a
   dica seguinte, e no fim aparece "✅ Aula concluída!".
4. A saída: linhas <= 40 colunas, e mapa <= 30 colunas.
5. Cada resposta aparece no .md da aula (no <details> 🔓 Resposta).
6. Links relativos entre os .md (e âncoras) apontam para algo que existe.
7. Links para github.com/.../blob|edit/main/<arquivo> apontam para um
   arquivo que existe.
8. Cada linha de Java citada no Nível 2 (referencias.json) ainda contém
   o texto esperado. Se o Java mudar, a aula desatualizada vira erro.

Só biblioteca padrão. Sai com código 1 se houver problema.
"""
import ast
import json
import os
import re
import subprocess
import sys
import tempfile

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.dirname(AQUI)
REPO_URL = "https://github.com/Lhordrixon/mundo-vivo/"

problemas = []


def problema(onde, msg):
    problemas.append(onde + ": " + msg)
    print("  ✗ " + msg)


def ler(caminho):
    with open(caminho, encoding="utf-8") as f:
        return f.read()


def linhas_de_codigo(src):
    return sum(1 for l in src.splitlines()
               if l.strip() and not l.strip().startswith("#"))


def linha_de_mapa(linha):
    """Linha desenhada: sem letras, fora o 'o' da criatura e o 'X'."""
    t = linha.strip()
    return len(t) >= 3 and not re.search(r"[A-WYZa-np-zÀ-ÿ]", t)


def rodar(src, pasta_tmp):
    caminho = os.path.join(pasta_tmp, "aula.py")
    with open(caminho, "w", encoding="utf-8") as f:
        f.write(src)
    env = dict(os.environ, PYTHONIOENCODING="utf-8")
    return subprocess.run([sys.executable, caminho], capture_output=True,
                          text=True, encoding="utf-8", timeout=60,
                          cwd=AQUI, env=env)


def conferir_aula(aula, respostas, pasta_tmp):
    print("== " + aula)
    src = ler(os.path.join(AQUI, aula + ".py"))
    md_caminho = os.path.join(AQUI, aula + ".md")
    md = ler(md_caminho) if os.path.exists(md_caminho) else ""
    if not md:
        problema(aula, "falta o " + aula + ".md")

    for i, linha in enumerate(src.splitlines(), 1):
        if len(linha) > 60:
            problema(aula, "linha %d do .py tem %d colunas (máx. 60)"
                     % (i, len(linha)))
        if any(ord(c) > 0xFFFF for c in linha):
            problema(aula, "linha %d do .py tem emoji que o IDLE "
                     "antigo não mostra" % i)
    try:
        ast.parse(src, feature_version=(3, 8))
    except SyntaxError as e:
        problema(aula, "não roda no Python 3.8: %s" % e)
        return
    n = linhas_de_codigo(src)
    if n > 40:
        problema(aula, "%d linhas de código (máx. 40)" % n)

    lacunas = respostas.get(aula)
    if not lacunas:
        problema(aula, "sem respostas em respostas.json")
        return

    md_junto = " ".join(md.split())
    for velho, novo in lacunas:
        if src.count(velho) != 1:
            problema(aula, "a lacuna %r aparece %d vezes no .py"
                     % (velho.strip(), src.count(velho)))
        for linha in novo.splitlines():
            s = linha.split("  #")[0].strip()
            if s and not s.startswith("#") \
                    and " ".join(s.split()) not in md_junto:
                problema(aula, "a resposta %r não está no .md" % s)

    etapa_src = src
    for k in range(len(lacunas) + 1):
        if k > 0:
            velho, novo = lacunas[k - 1]
            etapa_src = etapa_src.replace(velho, novo, 1)
        try:
            p = rodar(etapa_src, pasta_tmp)
        except subprocess.TimeoutExpired:
            problema(aula, "etapa %d passou de 60 segundos" % k)
            return
        saida = p.stdout.rstrip("\n").splitlines()
        erro = p.stderr.rstrip("\n").splitlines()
        ultima = erro[-1] if erro else ""
        for linha in saida:
            if len(linha) > 40:
                problema(aula, "etapa %d mostra uma linha de %d colunas "
                         "(máx. 40): %r" % (k, len(linha), linha))
                break
            if linha_de_mapa(linha) and len(linha) > 30:
                problema(aula, "etapa %d desenha um mapa de %d colunas "
                         "(máx. 30)" % (k, len(linha)))
                break
        if k < len(lacunas):
            dica = ultima[len("AssertionError: "):]
            if p.returncode == 0 or not ultima.startswith(
                    "AssertionError: ") or len(dica) < 10:
                problema(aula, "com %d resposta(s), esperava uma dica "
                         "em português; veio: %r"
                         % (k, ultima or (saida[-1:] or [""])[0]))
            else:
                print("  dica %d: %s" % (k, dica))
            if k == 0 and not saida:
                problema(aula, "nada aparece na tela antes da dica")
        elif p.returncode != 0 or not saida \
                or saida[-1] != "✅ Aula concluída!":
            problema(aula, "com todas as respostas, não terminou em "
                     "'✅ Aula concluída!': %r" % (ultima or saida[-1:]))
        else:
            print("  ✅ com as respostas")


def ancoras(caminho):
    texto = re.sub(r"```.*?```", "", ler(caminho), flags=re.S)
    vistas, saida = {}, set()
    for m in re.finditer(r"^#{1,6} (.+)$", texto, re.M):
        s = re.sub(r"[^\w\- ]", "", m.group(1).strip().lower()).replace(
            " ", "-")
        n = vistas.get(s, 0)
        vistas[s] = n + 1
        saida.add(s if n == 0 else "%s-%d" % (s, n))
    return saida


def conferir_links():
    print("== links")
    padrao_repo = re.compile(re.escape(REPO_URL)
                             + r"(?:blob|edit)/main/([^)#?\s]+)")
    for nome in sorted(os.listdir(AQUI)):
        if not nome.endswith(".md"):
            continue
        caminho = os.path.join(AQUI, nome)
        texto = re.sub(r"```.*?```", "", ler(caminho), flags=re.S)
        for m in re.finditer(r"\]\(([^)\s]+)\)", texto):
            alvo = m.group(1)
            if alvo.startswith(("http", "mailto")):
                continue
            arquivo, _, ancora = alvo.partition("#")
            destino = os.path.normpath(os.path.join(AQUI, arquivo)) \
                if arquivo else caminho
            if not os.path.exists(destino):
                problema(nome, "link quebrado: " + alvo)
            elif ancora and destino.endswith(".md") \
                    and ancora not in ancoras(destino):
                problema(nome, "âncora não existe: " + alvo)
        for m in padrao_repo.finditer(texto):
            if not os.path.exists(os.path.join(RAIZ, m.group(1))):
                problema(nome, "o arquivo %s não existe mais na main"
                         % m.group(1))


def conferir_java():
    print("== Java citado no Nível 2")
    refs = json.loads(ler(os.path.join(AQUI, "referencias.json")))
    registradas = set()
    for r in refs["referencias"]:
        caminho = os.path.join(RAIZ, r["arquivo"])
        onde = "%s (%s:%d)" % (r["aula"], r["arquivo"].split("/")[-1],
                               r["linha"])
        registradas.add((r["arquivo"], r["linha"]))
        if not os.path.exists(caminho):
            problema(onde, "o arquivo não existe mais")
            continue
        linhas = ler(caminho).splitlines()
        atual = linhas[r["linha"] - 1] if r["linha"] <= len(linhas) else ""
        if r["contem"] not in atual:
            problema(onde, "a aula espera %r nesta linha, mas agora está "
                     "%r. Atualize a aula e referencias.json."
                     % (r["contem"], atual.strip()))
    # Todo link de linha para a main, nas aulas do Nível 2, precisa estar
    # registrado, senão ninguém percebe quando ele envelhecer.
    padrao = re.compile(re.escape(REPO_URL)
                        + r"blob/main/([^)#?\s]+\.java)#L(\d+)(?:-L(\d+))?")
    for nome in sorted(os.listdir(AQUI)):
        if not (nome.startswith("2.") and nome.endswith(".md")):
            continue
        for m in padrao.finditer(ler(os.path.join(AQUI, nome))):
            ini = int(m.group(2))
            fim = int(m.group(3) or ini)
            for n in range(ini, fim + 1):
                if (m.group(1), n) not in registradas:
                    problema(nome, "a linha %d de %s não está em "
                             "referencias.json" % (n, m.group(1)))


def main():
    respostas = json.loads(ler(os.path.join(AQUI, "respostas.json")))
    pedidas = sys.argv[1:]
    aulas = pedidas or sorted(
        n[:-3] for n in os.listdir(AQUI)
        if re.match(r"\d\.\d-.*\.py$", n))
    with tempfile.TemporaryDirectory() as pasta_tmp:
        for aula in aulas:
            conferir_aula(aula, respostas, pasta_tmp)
    if not pedidas:
        conferir_links()
        conferir_java()
    print()
    if problemas:
        print("❌ %d problema(s):" % len(problemas))
        for p in problemas:
            print(" - " + p)
        sys.exit(1)
    print("✅ Todas as aulas conferidas.")


if __name__ == "__main__":
    main()
