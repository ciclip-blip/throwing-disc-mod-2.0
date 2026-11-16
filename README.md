# Throwing Discs - Fabric-Mod für Minecraft Java 26.3

Baut die von Mojang am 14.11.2026 angekündigte **Throwing Disc** nach. Die echte Disc ist erst für den ersten
Game Drop 2027 geplant und noch in keiner Version drin - diese Mod ist also ein Nachbau nach der Ankündigung.

## Was die Scheibe kann
- **Rechtsklick** wirft sie, aus der **Haupthand oder der Nebenhand**
- fliegt **gerade** und geht **durch Gegner hindurch** (jeder nimmt pro Durchgang einmal Schaden)
- **prallt von Blöcken ab** - jeder Abpraller verlängert die Flugzeit
- **kehrt zu dir zurück** und landet wieder in der Hand, aus der du geworfen hast (sonst im Inventar bzw. vor deinen Füßen)
- 5 Stufen: Kupfer, Eisen, Gold, Diamant, Netherit (Kreativ-Tab "Kampf")
- pro Wurf geht 1 Haltbarkeitspunkt weg; im Amboss mit Barren/Diamanten reparierbar

## Rezepte (ausgedacht - Mojang hat noch keine veröffentlicht)
- Kupfer / Eisen / Gold / Diamant: 4 Barren (bzw. Diamanten) in Rautenform
  ```
  .X.
  X.X
  .X.
  ```
- Netherit: Diamant-Scheibe + Netherit-Barren + Netherit-Upgrade-Schmiedevorlage am Schmiedetisch

## Voraussetzungen
- **JDK 25** (Minecraft 26.3 braucht Java 25)
- **IntelliJ IDEA 2025.3 oder neuer** (volle Java-25-Unterstützung)
- Verwendete Versionen: Minecraft 26.3, Fabric Loader 0.20.1, Fabric API 0.165.0+26.3, Loom 1.17, Gradle 9.6.0
  (aktuelle Versionen siehe https://fabricmc.net/develop/)

## Starten
1. Ordner in IntelliJ als Gradle-Projekt öffnen (die Gradle-Version steht in `gradle/wrapper/gradle-wrapper.properties`).
2. Gradle-JVM auf JDK 25 stellen.
3. Run-Konfiguration "Minecraft Client" starten (oder im Terminal `gradlew runClient`).
4. Im Spiel eine Welt öffnen und: `/give @s throwing_discs:iron_throwing_disc`

**Falls `gradlew` fehlt** (ich konnte keinen Gradle-Wrapper mitliefern): Entweder `gradle wrapper --gradle-version 9.6.0`
ausführen, oder auf https://fabricmc.net/develop/template/ ein leeres Projekt erzeugen (Minecraft 26.3) und daraus
`gradlew`, `gradlew.bat` und den Ordner `gradle/` hierher kopieren.

## Ohne lokale Installation: auf GitHub bauen lassen
Die Datei `.github/workflows/build.yml` baut die Mod automatisch auf GitHub (Java 25 + Gradle 9.6.0, kein `gradlew` nötig).
1. Neues Repository auf github.com anlegen und alle Dateien dieses Ordners hochladen.
2. Tab **Actions** öffnen - der Build startet bei jedem Upload (oder manuell über "Run workflow").
3. Nach dem grünen Haken auf den Lauf klicken und unten unter **Artifacts** `throwing-discs-mod` herunterladen.
4. Bei rotem Kreuz: auf den Lauf -> **build** -> den roten Schritt aufklappen und die Fehlermeldung kopieren.

## Mod-Datei bauen
`gradlew build` -> `build/libs/throwing-discs-1.0.0.jar` (nicht die `-sources.jar`) in den `mods`-Ordner legen.
Zusätzlich wird die **Fabric API** für 26.3 gebraucht.

## Werte ändern
- Flug (Geschwindigkeit, Reichweite, Abpraller, Fangradius): Konstanten oben in `ThrownDiscEntity.java`
- Schaden, Haltbarkeit, Reparatur-Material pro Stufe: `DiscTier.java`
- Rezepte: `src/main/resources/data/throwing_discs/recipe/`
- Texturen: `src/main/resources/assets/throwing_discs/textures/item/` (16x16, Platzhalter)

## Bekannte Grenzen
- Die fliegende Scheibe wird wie ein Schneeball als Sprite gezeichnet (dreht sich nicht).
- Verzauberungen sind nicht eingebaut (nur Mending über Erfahrung ginge theoretisch).
- Der Code wurde gegen die Fabric-Dokumentation für 26.2/26.3 geschrieben, aber noch **nicht kompiliert oder getestet**.
  Bei Compile-Fehlern: Fehlermeldung kopieren und zurückschicken.
