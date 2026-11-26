# TOIX Game Engine

Единый каркас game engine на Kotlin с архитектурой **World → Actor → Component**, визуальным скриптом на графе узлов (Blueprint VM), режимом Play-In-Editor (PIE) и разделением на модули Gradle: ядро, редактор, рантайм для Android.

## Структура

```
toix-engine/
├── settings.gradle.kts
├── build.gradle.kts
├── toix-core/               # World, Actor, Component, Blueprint VM, снэпшот сцены / PIE
├── toix-editor/             # Outliner, Details, Viewport state, сохранение уровня, PIE-контроллер
├── toix-runtime-android/    # Android-модуль (SurfaceView / GameActivity)
└── toix-sample/             # Запускаемый демо-пример (Blueprint + Editor + PIE)
```

## Модули

### toix-core

Ядро движка, не зависит от редактора и целевой платформы.

- `World` — хранит актёров, управляет циклом игры (`startPlay`, `tick`, `stopPlay`) и снэпшотами сцены.
- `Actor` — сущность со списком компонентов (`TransformComponent`, `BlueprintComponent` и др.).
- `Component` — интерфейс с хуками `onStart` / `onTick` / `snapshot`.
- `BlueprintNode` — узел визуального скрипта с цепочкой `nextNodeId`. Реализации: `MoveByNode`, `LogPositionNode`.
- `BlueprintComponent` — выполняет граф узлов каждый кадр с защитой от бесконечных циклов.

### toix-editor

Редактор уровня и Play-In-Editor.

- `ToixEditorController` — связывает Outliner (выбор актёра), Details (свойства) и PIE.
- Режимы: `EDIT`, `PLAYING`.
- `play()` — делает снэпшот `editWorld`, запускает симуляцию на копии, не 파괴я исходную сцену.
- `stop()` — восстанавливает режим редактирования.
- `stepFrame(dt)` — шаг симуляции в PIE с ограничением `maxDtSeconds`.
- `inspectSelected()` — возвращает `DetailsEntry` с позицией и флагом наличия Blueprint.

### toix-runtime-android

Заготовка Android-модуля (`com.android.library`). Подключает `toix-core` и `toix-editor`, готова к интеграции цикла с `SurfaceView` / `GameActivity`.

### toix-sample

Демо-пример из спецификации:

```kotlin
fun main() {
    val world = World("RTS_Test_Level")
    val n1 = MoveByNode("move_right", vx = 40f, vy = 0f, nextNodeId = "log_pos")
    val n2 = LogPositionNode("log_pos", tag = "PIE")
    val bp = BlueprintComponent(entryNodeId = "move_right", nodesById = mapOf(n1.id to n1, n2.id to n2))
    val peon = Actor("peon_1", "Peon_01").add(bp)
    world.spawn(peon)

    val editor = ToixEditorController(world)
    editor.selectActor("peon_1")
    println("До старта PIE: ${editor.inspectSelected()}")
    editor.play()
    editor.stepFrame(0.5f)
    editor.stepFrame(0.5f)
    println("Во время PIE: ${editor.inspectSelected()}")
    editor.stop()
    println("После Stop (сцена восстановлена): ${editor.inspectSelected()}")
}
```

## Начало работы

Требуется JDK 17+.

```bash
cd toix-engine
./gradlew :toix-sample:run
```

Ожидаемый вывод:

```
До старта PIE: DetailsEntry(actorId=peon_1, actorName=Peon_01, x=0.0, y=0.0, hasBlueprint=true)
[PIE] 20.0, 0.0
[PIE] 40.0, 0.0
Во время PIE: DetailsEntry(actorId=peon_1, actorName=Peon_01, x=40.0, y=0.0, hasBlueprint=true)
После Stop (сцена восстановлена): DetailsEntry(actorId=peon_1, actorName=Peon_01, x=0.0, y=0.0, hasBlueprint=true)
```

## Weitere шаги

- Рендер и ввод: добавить `RenderComponent` (спрайт/тайл/меш) и подключить касания/мышь.
- Сериализация: сохранение `World` и `BlueprintNode` в JSON/бинарный формат.
- Экспорт на Android: подключить `com.android.application` + `com.android.library`, собрать `.apk` / `.aab`.
