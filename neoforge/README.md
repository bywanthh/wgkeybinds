# WildeGate KeyBinds — NeoForge

Клиентский NeoForge-мод для сервера WildeGate: кейбинды «Способность №1» (R) и
«Способность №2» (G) + пакет `wildegate:keys`.

* Название мода: **WildeGate KeyBinds**, автор — **#bywanthh**.
* Описание в метаданных: «Мод написан специально для WildeGate для бинда активных
  способностей рас. Открытый код есть на github: https://github.com/bywanthh/wgkeybinds».
* Лицензия: **MIT**. Исходники: https://github.com/bywanthh/wgkeybinds
  (в метаданных также `displayURL` и `issueTrackerURL`).
* Логотип: `icon.png` в корне JAR (`logoFile="icon.png"`) + `assets/wildegate/icon.png`,
  256×256.
* Minecraft **1.21.11**, NeoForge **21.11.48**, ModDevGradle 2.0.148, Java 21.
* Только клиент: `@Mod(value = "wildegate", dist = Dist.CLIENT)`.
* Категория кейбиндов: **WildeGate: Способности** (`key.category.wildegate.abilities`).
* Логика: `src/main/java/ru/wildegate/keys/WildeGateKeysClient.java`,
  пакет: `src/main/java/ru/wildegate/keys/AbilityPayload.java`.

## Сборка

```bash
gradlew.bat build        # Windows
./gradlew build          # Linux/macOS
```

Wrapper уже в проекте (Gradle 9.8.1), ставить Gradle не нужно.

Требования (проверено на этой сборке):

* **Gradle 9.8.1** (ModDevGradle 2.0.148), сборка идёт на **JDK 25**;
* тулчейн компиляции — **Java 21** (`java.toolchain.languageVersion = 21`),
  недостающий JDK Gradle подтянет сам (foojay-resolver).

Результат: `build/libs/wgkeybinds-neoforge-1.0.jar` → в `.minecraft/mods/`.
Запуск в дев-среде: `gradlew.bat runClient`.

## Настройка версий

`gradle.properties`: `minecraft_version`, `neo_version`, `parchment_*`, `mod_name`.
Актуальные NeoForge: https://projects.neoforged.net/neoforged/neoforge

## Поведение

1. **При входе в мир** отправляется мини-хендшейк — пакет `wildegate:keys` с ID `0`
   (до 5 попыток раз в секунду). Серверная часть помечает игрока как мод-игрока и
   сразу отключает старые жесты (двойной F / Shift+F).
2. У NeoForge нет аналога `ClientPlayNetworking#canSend`, поэтому по нажатию
   отправляются **и пакет `wildegate:keys`, и команда `/wgkey <ник> <1|2>`**.
   Лишнее срабатывание отсекает анти-дубликат в `WildeGate_Races.sk` (опция `mod_dedup`),
   поэтому в чате/на сервере реакция всё равно одна.
