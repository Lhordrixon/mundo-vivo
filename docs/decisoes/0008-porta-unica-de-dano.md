# 0008 — Porta única de dano

**Problema.** A fome era a única coisa que tirava vida, e mexia no campo
direto. Combate, desastre, veneno e poderes iam todos querer o mesmo. Cada
um escreveria a sua subtração e o seu jeito de matar, e a mesma morte
poderia ser contada duas vezes.

**Decisão.** Existe uma porta só: `Creature.applyDamage(quanto, causa)`.
Ela tira vida, para em zero, anota a causa e devolve `true` só se
**aquele** golpe foi o fatal. Ela não mata. Toda morte termina em
`Simulation.die()`: o corpo vira comida, a criatura sai do reino, o slot
volta ao pool.

**Alternativas.**
- Cada fonte de dano matar por conta própria: duplicaria o caminho de
  morte e o risco de morte contada duas vezes.
- Causa como `enum` fechado: o combate vai querer dizer de quem veio o
  golpe, não só que veio. A causa é `String`, com constantes
  (`Creature.CAUSE_*`) para as causas da própria simulação.

**Consequências.**
- Fome, terreno perigoso, golpe do jogador e combate passam pela mesma
  porta. Uma pancada em quem já está com a vida zerada devolve `false`.
- O pacote `creature` não precisa conhecer mundo, comida nem reino.
- O primeiro uso novo foi o terreno perigoso: só o oceano profundo fere.
  Em jogo normal nunca dispara, porque ninguém entra andando na água. Ele
  existe para quando o chão mudar debaixo de alguém.

<details>
<summary>O argumento completo, que antes morava no javadoc</summary>

A criatura não sabe o que a feriu: só registra a causa, para quem conta a
morte poder separar quem morreu de quê. Morrer envolve virar comida no
tile, sair da facção e devolver o slot ao pool, e nada disso mora em
`Creature`: o pacote `creature` não conhece mundo, comida nem facção, e
essa separação é o que deixa a criatura testável sozinha.

As constantes `CAUSE_*` existem para que um erro de digitação não vire
estatística errada.

</details>

**Estado.** Em vigor desde o commit `15dd0f8`.
