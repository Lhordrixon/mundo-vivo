# Regras de ofuscação para o build de release.
#
# Hoje minifyEnabled está desligado, então nada aqui é aplicado. As regras
# já ficam prontas porque, quando a minificação for ligada, o R8 remove por
# engano classes que o libGDX carrega por reflexão — o app compila e quebra
# só ao abrir, que é o pior momento para descobrir.

-keep class com.badlogic.gdx.** { *; }
-keep class com.badlogic.gdx.backends.android.** { *; }
-keepclassmembers class com.badlogic.gdx.backends.android.AndroidInput* {
   <init>(...);
}

-dontwarn android.support.**
-dontwarn com.badlogic.gdx.jnigen.**
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.physics.box2d.utils.Box2DBuild
-dontwarn com.badlogic.gdx.graphics.g2d.freetype.FreetypeBuild

# A simulação é toda nossa e não usa reflexão: pode ser ofuscada à vontade.
