# WildeGate KeyBinds — Fabric

Клиентский Fabric-мод для сервера WildeGate: кейбинды «Способность №1» (R) и
«Способность №2» (G) + пакет `wildegate:keys`.

* Название мода: **WildeGate KeyBinds**, автор — **#bywanthh**.
* Описание в метаданных: «Мод написан специально для WildeGate для бинда активных
  способностей рас. Открытый код есть на github: https://github.com/bywanthh/wgkeybinds».
* Лицензия: **MIT**. Исходники: https://github.com/bywanthh/wgkeybinds
  (в метаданных также `contact.homepage` / `sources` / `issues`).
* Иконка: `src/main/resources/assets/wildegate/icon.png` (256×256, в JAR — как `icon`).
* Minecraft **1.21.11**, Fabric Loader 0.19.5, Fabric API, Java 21.
* `environment: client` — мод ставится только игрокам.
* Категория кейбиндов: **WildeGate: Способности** (`key.category.wildegate.abilities`).
* Локализации: `src/main/resources/assets/wildegate/lang/{en_us,ru_ru}.json`.
* Логика: `src/client/java/ru/wildegate/keys/WildeGateKeysClient.java`,
  пакет: `src/main/java/ru/wildegate/keys/AbilityPayload.java`.

## Сборка

```bash
gradlew.bat build        # Windows
./gradlew build          # Linux/macOS
```

Wrapper уже в проекте (Gradle 9.8.1), ставить Gradle не нужно.

Требования (проверено на этой сборке):

* **Gradle ≥ 9.8.0** — этого требует Loom 1.19.1;
* **JVM для самой сборки — Java 25** (Loom 1.19.1 отказывается работать на 21);
  задаётся через `JAVA_HOME`;
* сам мод компилируется под **Java 21** (`options.release = 21`, проверено: байткод major 65).

Если в системе только JDK 21 — сборка упадёт с сообщением
`Dependency requires at least JVM runtime version 25`.

Результат: `build/libs/wgkeybinds-fabric-1.0.jar` (файлы `-sources` игрокам не нужны)
→ положить в `.minecraft/mods/` вместе с Fabric API.

## Настройка версий

Все версии — в `gradle.properties` (`minecraft_version`, `loader_version`,
`fabric_api_version`, `loom_version`). Актуальные значения: https://fabricmc.net/develop

## Поведение

1. **При входе в мир** отправляется мини-хендшейк — пакет `wildegate:keys` с ID `0`.
   Серверная часть помечает игрока как мод-игрока и сразу отключает старые жесты
   (двойной F / Shift+F), поэтому F остаётся обычной сменой рук.
   Если мост-плагина нет, канал неизвестен — попытки прекращаются, а признак
   «есть мод» сервер поставит при первом нажатии бинда.
2. Нажатие R/G → проверка `ClientPlayNetworking.canSend(wildegate:keys)`.
3. Канал объявлен сервером (установлен `wildegate-channel-bridge`) → отправляется пакет
   (ID 1 или 2).
4. Канал не объявлен → отправляется резервная команда `/wgkey <ник> <1|2>`,
   которую обрабатывает тот же самый диспетчер в `WildeGate_Races.sk`.
