# WildeGate: KeyBinds

**Две клавиши для активных способностей рас на сервере WildeGate.** Нажали **R** — сработала
способность №1, нажали **G** — способность №2. Без двойных нажатий F, без команд в чате
и без макросов.

> Мод **клиентский**: он работает только на сервере **WildeGate**, где уже установлена
> серверная часть. На других серверах он просто не находит канал `wildegate:keys`
> и ничего не делает — никаких ошибок и сообщений в чат.

## Что делает мод

* Добавляет категорию **«WildeGate: Способности»** в настройки управления.
* Две переназначаемые клавиши: **R** — способность №1, **G** — способность №2.
* Отправляет на сервер короткий ID способности по каналу `wildegate:keys`.
* При входе здоровается с сервером, чтобы тот сразу выключил старые жесты
  (двойной F остаётся обычной сменой рук).
* Русская и английская локализации клавиш.

## Установка

1. Установите загрузчик для Minecraft **1.21.11**:
   * **Fabric** — [Fabric Loader](https://fabricmc.net/use/installer/) + **Fabric API**;
   * **NeoForge** — [NeoForge 21.11.48+](https://neoforged.net/).
2. Положите JAR в папку `.minecraft/mods/`:
   * Fabric — `wgkeybinds-fabric-1.1.jar`;
   * NeoForge — `wgkeybinds-neoforge-1.1.jar`.
3. Запустите игру и зайдите на сервер WildeGate.

Клавиши меняются как обычно: **Настройки → Управление → Назначение клавиш →
WildeGate: Способности**.

## Команда `/wildegatekeybinds`

На сервере WildeGate введите `/wildegatekeybinds`:

* **мод не найден** — придёт кликабельная ссылка на скачивание;
* **мод установлен** — сервер выдаст разовый стартовый набор ресурсов под вашу расу.

Набор даётся один раз на аккаунт. Сама награда живёт в серверном скрипте — мод лишь
помогает серверу отличить игрока с биндами от игрока без мода.

## Совместимость

| | |
|---|---|
| Minecraft | **1.21.11** |
| Fabric | Loader 0.19.5+, Fabric API |
| NeoForge | 21.11.48+ |
| Java | 21+ |
| Сторона | **только клиент** (на сервер ставить не нужно) |

Мод не содержит миксинов, телеметрии и обфускации — три класса, исходники открыты.

## Ссылки

* Исходники и сборки: [github.com/bywanthh/wgkeybinds](https://github.com/bywanthh/wgkeybinds)
* Discord WildeGate: [discord.gg/ubT3GMsqwh](https://discord.gg/ubT3GMsqwh)
* Telegram WildeGate: [t.me/WildeGate](https://t.me/WildeGate)

## Лицензия

MIT. Автор — **#bywanthh**.

---

# English

**Two keybinds for the active racial abilities on the WildeGate server.** Press **R** for
ability #1 and **G** for ability #2 — no double-tap F, no chat commands, no macros.

> This is a **client-side** mod. It only works on **WildeGate**, which runs the matching
> server-side script. On any other server it simply finds no `wildegate:keys` channel
> and stays silent.

## Features

* A **“WildeGate: Abilities”** category in Controls.
* Two rebindable keys: **R** (ability #1) and **G** (ability #2).
* Sends a short ability ID over the `wildegate:keys` channel.
* Sends a hello packet on join so the server can disable the legacy double-tap-F gestures.
* Russian and English keybind localizations.
* Client-side only, no mixins, no telemetry, open source (three classes).

## Installation

1. Install **Minecraft 1.21.11** with **Fabric** (Loader 0.19.5+ and **Fabric API**) or
   **NeoForge 21.11.48+**.
2. Drop the matching JAR into `.minecraft/mods/`:
   * Fabric — `wgkeybinds-fabric-1.1.jar`;
   * NeoForge — `wgkeybinds-neoforge-1.1.jar`.
3. Join WildeGate.

## Compatibility

Minecraft **1.21.11**, Java **21+**, client-side only. Fabric requires Fabric API;
NeoForge needs nothing else.

## License

MIT. Made by **#bywanthh**.
