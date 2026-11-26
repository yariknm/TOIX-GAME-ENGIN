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

- `World` — создание мира, спавн/поиск актёров (`findById`, `allActors`), контроль Play-цикла (`startPlay`, `tick`, `stopPlay`) и снэпшоты сцены.
- `Actor` — сущность с набором компонентов (`TransformComponent`, `BlueprintComponent` и др.).
- `Component` — интерфейс с хукерами `onStart` / `onTick` / `snapshot`.
- `TransformComponent` — компонент позиционирования (Transform: x/y/z + scaleX/scaleY), копирование при снэпшоте.

**Blueprint VM** (`Blueprint.kt`):
- `BlueprintNode` — sealed interface с `id`, `nextNodeId`, `execute(actor, world, dt)`.
- `MoveByNode` — перемещает актёра на `vx * dt` / `vy * dt`.
- `LogPositionNode` — печатает позицию в теге.
- `BlueprintComponent` — исполняет граф из `entryNodeId` по `nextNodeId`, с защитой от бесконечных циклов (`guard < 256`).

**Рендер** (`render/`):
- `Renderer` — собирает `DrawCommand` по всем актёрам с `SpriteRendererComponent`, сортирует по слою, передаёт в `RenderBackend`.
- `Camera` — позиция, размер вьюпорта, ortho-проекция.
- `SpriteRendererComponent` — спрайт, размер, цвет, видимость, слой.
- `RenderBackend` — интерфейс бэкенда (OpenGL/Vulkan/Android), `MockRenderBackend` — заглушка для тестов.

**Ввод** (`input/`):
- `InputManager` — диспетчер событий, слушатели.
- `InputComponent` — компонент на акторе для обработки ввода.
- `InputEvent` / `InputType` — мышь, клавиатура, тач.

**Физика** (`physics/`):
- `BoxCollider` — AABB-коллайдер, `isTrigger`.
- `RigidBody` — масса, скорость, гравитация, кинематический режим.
- `CollisionManager` — проверка AABB-пересечений, импульс при столкновении.

**Анимация** (`animation/`):
- `AnimationClip`, `AnimationKeyframe` — клип и ключевой кадр.
- `Animator` — проигрывание клипов, цикл, play/stop/addClip.

**Сцены** (`scene/`):
- `Scene` — группа актёров с именем.
- `SceneManager` — загрузка/выгрузка, аддитивные сцены.

**Префабы** (`prefab/`):
- `Prefab` — шаблон актора.
- `PrefabManager` — регистрация, инстанцирование.

**Сериализация** (`serialization/`):
- `WorldSerializer` — JSON-сериализация/десериализация мира, актёров, компонентов, Blueprint-графов. Без внешних зависимостей.

**Ассеты** (`assets/`):
- `Asset`, `TextureAsset`, `AudioAsset`, `PrefabAsset`.
- `AssetManager` — кэш, загрузка, выгрузка, `AssetLoader`.

**UI** (`ui/`):
- `Rect`, `Color`, `UIElement`, `Canvas`, `TextElement`, `ButtonElement`.

**Аудио** (`audio/`):
- `AudioComponent`, `AudioSource`, `AudioListener`, `AudioMixer`.

### toix-editor

Редактор уровня и Play-In-Editor.

- `ToixEditorController` — связывает Outliner (выбор актёра), Details (свойства) и PIE.
- Режимы: `EDIT`, `PLAYING`.
- `play()` — снэпшот сцены → старт симуляции на копии, исходная сцена не 파괴ется.
- `stop()` — возврат в режим редактирования.
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

## Дальнейшие шаги

- Рендер и ввод: подключить реальный `RenderBackend` (OpenGL/Vulkan/Android) и передавать координаты касаний/мыши в `InputManager`.
- Сериализация: расширить `WorldSerializer` на все компоненты и Blueprint-узлы.
- Экспорт на Android: подключить `com.android.application` + `com.android.library`, собрать `.apk` / `.aab`.
- Редактор UI: визуальный редактор Blueprint-графов, иерархия актёров, инспектор свойств.
- Физика: расширить `CollisionManager` до импульсов, триггеров, слоёв коллизий.