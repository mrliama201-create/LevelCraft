# LevelCraft (Fabric server-side mod) — project template

Target: Minecraft 26.3, Fabric Loader 0.20.1, Java 25.

Features in this starter:
- 10 XP every five minutes while online.
- Mining XP for ores, including ancient debris.
- Levels start at 100 XP and each next level increases the requirement by 50.
- Example reward: four emeralds on every level-up.
- `/level check` to see level and current XP.
- Server-only mod metadata (players do not need to install it).

## Build
This project was not compiled in the provided environment. To build it:
1. Install Java 25 and Gradle 9.6+.
2. Run `gradle build` in the project folder.
3. The output should be under `build/libs/levelcraft-1.0.0.jar`.
4. Put the mod JAR in the server's `mods` folder. Fabric API must also be installed.

## Important
This is an uncompiled starter and may require API adjustments for 26.3 mappings. Make a backup of your server/world before testing.
