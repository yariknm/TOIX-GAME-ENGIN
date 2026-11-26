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
- `Animator` — проигрывание клипов, цикл, play/stop, смешивание.

**Animation State Machine** (`animation/statemachine/`):
- `AnimationState`, `AnimationTransition` — состояние и переход с условием.
- `AnimationStateMachine` — смена состояний, кроссфейд, `AnimatorStateMachineComponent`.

**Сцены** (`scene/`):
- `Scene` — группа актёров с именем.
- `SceneManager` — загрузка/выгрузка, аддитивные сцены.

**Префабы** (`prefab/`):
- `Prefab` — шаблон актора.
- `PrefabManager` — регистрация, инстанцирование.

**Сериализация** (`serialization/`):
- `WorldSerializer` — JSON-сериализация/десериализация мира, актёров, компонентов, Blueprint-графов. Без внешних зависимостей.

**Ассеты** (`assets/`, `asset/`):
- `Asset`, `TextureAsset`, `AudioAsset`, `PrefabAsset`.
- `AssetManager` — кэш, загрузка, выгрузка, `AssetLoader`.
- `AssetPipeline` — импорт (PNG/JPG, glTF/OBJ), кукинг, `AssetRegistry`.

**Материалы** (`material/`):
- `Shader` — вершинный и фрагментный код.
- `MaterialParameterValue` — Float/Color/Vector2/Vector3/Texture.
- `Material`, `BlendMode`, `MaterialComponent`.

**UI** (`ui/`):
- `Rect`, `Color`, `UIElement`, `Canvas`, `TextElement`, `ButtonElement`.

**Audio** (`audio/`, `audio/dsp/`):
- `AudioComponent`, `AudioSource`, `AudioListener`, `AudioMixer`.
- `DspEffect`, `GainEffect`, `LowPassFilter`, `ReverbEffect`.
- `AudioBus`, `SpatialAudioParams`, `SpatialAudioMixer` — 3D звук, громкость и пан по расстоянию.

**World Partition** (`worldpartition/`):
- `GridCell`, `WorldPartitionManager` — потоковая загрузка/выгрузка ячеек по радиусу камеры.
- `HLODCluster`, `WorldPartitionComponent`.

**Networking** (`networking/`):
- `NetworkIdentity`, `RpcDescriptor`, `RpcType`, `ReplicationCondition`, `ReplicatedProperty`.
- `ReplicationManager` — список репликации по соединению.
- `NetworkedComponent`, `RpcCall`, `NetworkConnection`.

**VFX / Частицы** (`vfx/`):
- `ParticleModule` (Spawn/Velocity/Size/Color/Lifetime), `Particle`, `ParticleSystem`, `SimulationSpace`.
- `ParticleSystemComponent` — симуляция частиц на тике.

### toix-editor

Редактор уровня и Play-In-Editor.

- `ToixEditorController` — связывает Outliner (выбор актёра), Details (свойства) и PIE.
- Режимы: `EDIT`, `PLAYING`.
- `play()` — снэпшот сцены → старт симуляции на копии, исходная сцена не разрушается.
- `stop()` — возврат в режим редактирования.
- `stepFrame(dt)` — шаг симуляции в PIE с ограничением `maxDtSeconds`.
- `inspectSelected()` — возвращает `DetailsEntry` с позицией и флагом наличия Blueprint.

### toix-editor/gui

- `ToixEditorGui` — вьюпорт, аутлайнер, инспектор, плей/стоп/степфрейм.
- `EditorWindow` — заглушка главного окна (Compose Desktop / Swing / Web).

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

## Как запустить

В репозитории **нет `gradlew`** (Gradle wrapper не привязан), поэтому запуск возможен через `kotlinc` напрямую.

### Требования

- **JDK 17+** (например, Temurin или OpenJDK 17)
- **Kotlin 2.1.10** (`kotlinc`)

### Установка инструментов

```bash
# JDK 17
sudo apt-get install -y openjdk-17-jdk-headless
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Kotlin компилятор
curl -sLO https://github.com/JetBrains/kotlin/releases/download/v2.1.10/kotlin-compiler-2.1.10.zip
unzip kotlin-compiler-2.1.10.zip
export PATH=$PWD/kotlin-compiler/kotlinc/bin:$PATH
```

### Компиляция

Из корня репозитория (`/workspace/app`):

```bash
kotlinc -cp "$(find /path/to/kotlin-compiler/kotlinc/lib -name '*.jar' | tr '\n' ':')" \
  $(find toix-core/src -name '*.kt') \
  $(find toix-editor/src -name '*.kt') \
  $(find toix-sample/src -name '*.kt') \
  -include-runtime -d /tmp/kilo2/toix.jar
```

### Запуск демо

```bash
java -cp /tmp/kilo2/toix.jar com.toix.sample.Main
```

### Ожидаемый вывод

```
До старта PIE: DetailsEntry(actorId=peon_1, actorName=Peon_01, x=0.0, y=0.0, hasBlueprint=true)
[PIE] 20.0, 0.0
[PIE] 40.0, 0.0
Во время PIE: DetailsEntry(actorId=peon_1, actorName=Peon_01, x=40.0, y=0.0, hasBlueprint=true)
После Stop (сцена восстановлена): DetailsEntry(actorId=peon_1, actorName=Peon_01, x=0.0, y=0.0, hasBlueprint=true)
```

### Через Gradle (опционально)

Если нужно использовать Gradle, сгенерируйте обёртку:

```bash
gradle wrapper
./gradlew :toix-sample:run
```

## Дальнейшие шаги

- Рендер и ввод: подключить реальный `RenderBackend` (OpenGL/Vulkan/Android) и передавать координаты касаний/мыши в `InputManager`.
- Сериализация: расширить `WorldSerializer` на все компоненты и Blueprint-графы.
- Экспорт на Android: подключить `com.android.application` + `com.android.library`, собрать `.apk` / `.aab`.
- Редактор UI: визуальный редактор Blueprint-графов, иерархия актёров, инспектор свойств.
- Физика: расширить `CollisionManager` до импульсов, триггеров, слоёв коллизий.