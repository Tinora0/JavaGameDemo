 # 2D平台跳跃

#### 项目介绍
这是款基于 **FXGL 2D 游戏引擎**开发的平台跳跃类游戏（类似 “I Wanna” 系列经典玩法），采用 Java 语言编写，具备完整的玩家控制、物理碰撞、动画渲染、死亡重生等核心机制。项目遵循 “低耦合、高内聚” 设计原则，通过模块化架构支撑游戏从启动到运行的全流程，同时预留扩展空间（如多关卡、新敌人类型）

## 一、软件架构

本项目采用 **“分层 + 模块化” 架构**，自上而下分为 “应用入口层、核心功能层、底层支撑层、资源层”，各层职责边界清晰，依赖关系单向可控，具体架构设计如下：

### 1. 应用入口层（Application Entry Layer）

作为游戏启动的 “总控中心”，负责统筹初始化全局配置、衔接各功能模块，核心载体为 `IWBTCSerApp` 类（继承 FXGL 的 `GameApplication`）。
其核心作用包括：

- 游戏基础配置初始化：定义窗口尺寸（800x608 像素）、游戏标题（“I Wanna Be The CSer”）、应用图标（`cherry.png`），并控制主菜单启用状态；
- 模块协同调度：调用 `BlockFactory` 生成游戏实体、绑定 `PlayerComponent` 实现玩家行为逻辑、注册物理碰撞规则、初始化输入控制与 UI 界面，确保各模块按流程协同运行。

### 2. 核心功能层（Core Function Layer）

是游戏玩法的 “核心载体”，实现玩家行为、实体生产、碰撞交互三大核心能力，支撑游戏核心玩法循环。

#### 2.1 玩家行为模块（Player Behavior Module）

以 `PlayerComponent` 类（继承 FXGL 的 `Component`）为核心，封装玩家所有行为逻辑与状态管理，具体功能包括：

- 运动控制：实现水平移动（左移 / 右移）、跳跃（单次跳 + 二段跳）、跳跃长按（最大持续 0.3 秒，通过延长向上速度实现更高跳跃高度），依托 `PhysicsComponent` 控制实体物理速度；
- 状态管理：维护玩家关键状态（是否在地面、是否处于跳跃中、是否死亡、朝向方向），为动画切换、逻辑判定（如二段跳权限）提供依据；
- 动画渲染：根据玩家实时状态（idle 待机 / 移动 / 跳跃 / 下落）切换对应动画通道（`AnimationChannel`），通过 `AnimatedTexture` 实现精灵帧循环播放，同时根据朝向翻转精灵（朝左时水平缩放为 -1）；
- 死亡与重生：处理死亡逻辑（停止物理运动、关闭碰撞检测、显示死亡特效与 “Game Over” overlay 界面、播放死亡音效），以及重生逻辑（重置物理状态、恢复碰撞检测、将玩家传送至预设重生点、重置死亡相关状态）。

#### 2.2 实体生产模块（Entity Production Module）

以 `BlockFactory` 类（实现 FXGL 的 `EntityFactory`）为核心，负责游戏内所有实体的标准化生产，避免实体配置分散。
其核心功能为：通过 `@Spawns` 注解为每种实体（玩家、地面、尖刺、平台、泥土、死亡特效等）定义统一生成规则，包括：

- 物理属性配置：设置实体物理类型（静态 / 动态，如地面为静态 `BodyType.STATIC`、玩家为动态 `BodyType.DYNAMIC`）；
- 碰撞盒定义：为不同实体配置匹配的碰撞盒（如尖刺用多边形碰撞盒、地面用矩形碰撞盒）；
- 视图资源绑定：关联实体对应的图片资源（如地面绑定 `ground.png`、尖刺绑定 `spikeup.png`）；
- 组件附加：为实体自动附加必要组件（如玩家附加 `PlayerComponent`、所有实体附加 `CollidableComponent` 启用碰撞）；
- 临时实体处理：如死亡特效实体（`death` 类型），通过附加 `ExpireCleanComponent` 实现 0.4 秒后自动销毁，避免内存泄漏。

#### 2.3 碰撞交互模块（Collision Interaction Module）

在 `IWBTCSerApp` 中依托 FXGL 的 `CollisionHandler` 实现，核心作用是定义不同实体碰撞时的行为逻辑，当前核心规则为：



- 检测 “玩家（`EntityType.PLAYER`）与尖刺（`EntityType.SPIKE`）” 的碰撞，碰撞开始时自动调用 `PlayerComponent` 的 `die()` 方法，触发玩家死亡逻辑，是游戏 “危险交互” 的核心实现。

### 3. 底层支撑层（Infrastructure Layer）

依赖 FXGL 引擎提供的基础能力，为上层功能提供 “物理模拟、资源加载、输入处理、视图渲染” 技术支撑，是游戏运行的 “技术底座”：



- 物理模拟：通过 `PhysicsComponent` 控制实体物理属性（速度、重力），通过 `PhysicsWorld` 管理物理世界，支持射线检测（如玩家通过射线判定是否接触地面）；
- 资源加载：依托 FXGL 的 `AssetLoader` 加载所有静态资源，包括精灵图（`playerSpriteSheet.png`）、音效（`jump1.wav` 单次跳音效、`jump2.wav` 二段跳音效、`death.wav` 死亡音效）、UI 图片（`gameover.png` 死亡界面、`openmusic.png` 音乐控制按钮）；
- 输入处理：通过 FXGL 的 `Input` 与 `UserAction` 绑定键盘操作，定义 “按键按下 / 松开” 的响应逻辑（如按下 A 键触发左移、松开 A 键停止移动）；
- 视图渲染：通过 `ViewComponent` 管理实体视图，通过 `AnimatedTexture` 实现精灵动画播放，通过 `GameScene` 管理 UI 节点与游戏场景分层渲染。

### 4. 资源层（Resource Layer）

存储游戏运行所需的所有静态资源，为视图渲染与交互反馈提供素材支持，核心资源类型与用途包括：



- 精灵图资源：`playerSpriteSheet.png` 包含玩家 idle、移动、跳跃、下落四种状态的动画帧，用于玩家动画渲染；
- 实体图片资源：`ground.png`（地面）、`spikeup.png`（尖刺）、`platform.png`（平台）、`soil.png`（泥土），用于非玩家实体的视图展示；
- 音效资源：`jump1.wav`（单次跳音效）、`jump2.wav`（二段跳音效）、`death.wav`（死亡音效），用于强化玩家操作反馈；
- UI 图片资源：`cherry.png`（游戏图标）、`gameover.png`（死亡界面 overlay）、`openmusic.png`（音乐控制按钮），用于 UI 界面渲染。

## 二、接口定义

本项目接口围绕 “模块间通信” 与 “功能调用” 设计，分为 “事件驱动接口”“功能调用接口”“状态查询接口” 三类，确保模块交互规范且低耦合。

### 1. 事件驱动接口（Event-Driven Interfaces）

基于 FXGL 的 “组件 - 实体” 通信模式，通过 “方法调用 + 状态变更” 触发特定事件，核心事件交互逻辑如下：



- 玩家与尖刺碰撞事件：由 `CollisionHandler`（在 `IWBTCSerApp` 中注册）检测碰撞，触发 `PlayerComponent` 的 `die()` 方法，执行死亡逻辑；
- 玩家跳跃事件：玩家按下 SPACE 键时，`UserAction` 触发 `PlayerComponent` 的 `jump()` 方法，执行跳跃逻辑；玩家松开 SPACE 键时，触发 `endJump()` 方法，终止跳跃；
- 玩家重生事件：玩家按下 R 键时，`UserAction` 触发 `PlayerComponent` 的 `respawn()` 方法，执行重生逻辑。

### 2. 功能调用接口（Function Call Interfaces）

各模块对外提供的核心功能方法，供其他模块调用以实现特定需求，核心接口如下：

#### 2.1 PlayerComponent 对外接口

- `jump()`：无入参，无返回值，触发玩家跳跃逻辑（含单次跳 / 二段跳判定，根据地面状态与二段跳权限决定是否执行跳跃），调用场景为玩家按下 SPACE 键时（`IWBTCSerApp` 输入绑定）；
- `endJump()`：无入参，无返回值，终止玩家跳跃，重置跳跃速度与跳跃状态，调用场景为玩家松开 SPACE 键时（`IWBTCSerApp` 输入绑定）；
- `moveLeft()`：无入参，无返回值，设置玩家水平速度为 -180.0（向左移动），标记移动状态与朝向，调用场景为玩家按下 A 键时（`IWBTCSerApp` 输入绑定）；
- `moveRight()`：无入参，无返回值，设置玩家水平速度为 180.0（向右移动），标记移动状态与朝向，调用场景为玩家按下 D 键时（`IWBTCSerApp` 输入绑定）；
- `stop()`：无入参，无返回值，设置玩家水平速度为 0（停止移动），重置移动状态，调用场景为玩家松开 A/D 键时（`IWBTCSerApp` 输入绑定）；
- `die()`：无入参，无返回值，触发玩家死亡逻辑（停止物理运动、关闭碰撞、显示死亡特效与 overlay、播放死亡音效），调用场景为玩家与尖刺碰撞时（`CollisionHandler` 回调）；
- `respawn()`：无入参，无返回值，触发玩家重生逻辑（重置物理状态、恢复碰撞、传送至重生点、重置死亡相关状态），调用场景为玩家按下 R 键时（`IWBTCSerApp` 输入绑定）；
- `setRespawnPoint(Point2D p)`：入参为 `Point2D` 类型（重生点坐标），无返回值，设置玩家重生点，调用场景为关卡切换、存档加载时（扩展场景）；
- `setJumpHeld(boolean held)`：入参为 `boolean` 类型（是否长按跳跃键），无返回值，标记跳跃键长按状态，用于跳跃长按逻辑判定，调用场景为玩家按下 / 松开 SPACE 键时（`IWBTCSerApp` 输入绑定）。

#### 2.2 BlockFactory 对外接口

所有接口均通过 `@Spawns` 注解标识，入参统一为 `SpawnData` 类型（包含实体生成坐标与额外参数），返回值为 `Entity` 类型（生成的实体实例）：



- `spawnPlayer(SpawnData data)`：生成玩家实体，配置动态物理属性、矩形碰撞盒、`PlayerComponent` 组件，调用场景为游戏初始化、玩家重生时（`IWBTCSerApp` 调用）；
- `spawnGround(SpawnData data)`：生成地面实体，配置静态物理属性、矩形碰撞盒、`ground.png` 视图，调用场景为游戏初始化、关卡加载时（`IWBTCSerApp` 调用）；
- `spawnSpikeup(SpawnData data)`：生成尖刺实体，配置静态物理属性、多边形碰撞盒、`spikeup.png` 视图，调用场景为游戏初始化、关卡加载时（`IWBTCSerApp` 调用）；
- `spawnPlatform(SpawnData data)`：生成平台实体，配置静态物理属性、矩形碰撞盒、`platform.png` 视图，调用场景为关卡加载时（`IWBTCSerApp` 调用）；
- `spawnSoil(SpawnData data)`：生成泥土实体，配置静态物理属性、矩形碰撞盒、`soil.png` 视图，调用场景为关卡加载时（`IWBTCSerApp` 调用）；
- `spawnDeath(SpawnData data)`：生成死亡特效实体，配置红色矩形视图、`ExpireCleanComponent`（0.4 秒后自动销毁），调用场景为玩家死亡时（`PlayerComponent` 调用）。

#### 2.3 IWBTCSerApp 对外接口（隐含）

均为 FXGL 引擎回调的初始化方法，无入参无返回值：



- `initSettings()`：初始化游戏基础配置（窗口、标题、图标等），调用场景为游戏启动时（FXGL 引擎回调）；
- `initGame()`：初始化游戏世界，注册实体工厂、生成玩家与初始实体、设置初始重生点，调用场景为游戏启动时（FXGL 引擎回调）；
- `initInput()`：绑定键盘输入与 `PlayerComponent` 的行为方法（如 A 键→`moveLeft()`），调用场景为游戏启动时（FXGL 引擎回调）；
- `initPhysics()`：注册实体碰撞规则（如玩家 - 尖刺碰撞触发死亡），调用场景为游戏启动时（FXGL 引擎回调）；
- `initUI()`：初始化 UI 界面（加载音乐控制按钮并添加到场景），调用场景为游戏启动时（FXGL 引擎回调）。

### 3. 状态查询接口（State Query Interfaces）

`PlayerComponent` 提供玩家状态查询方法，供其他模块判断逻辑分支，核心接口如下：

- `isOnGround()`：返回 `boolean` 类型，查询玩家是否在地面，调用场景为跳跃逻辑判定（仅地面可触发单次跳）；
- `isMoving()`：返回 `boolean` 类型，查询玩家是否在移动，调用场景为动画切换（移动状态播放 `animMove` 动画）；
- `isJumping()`：返回 `boolean` 类型，查询玩家是否在跳跃，调用场景为动画切换（跳跃状态播放 `animJump` 动画）；
- `isFacingRight()`：返回 `boolean` 类型，查询玩家朝向（true 为朝右，false 为朝左），调用场景为精灵翻转（朝左时设置 `scaleX = -1`）；
- `isDead()`：返回 `boolean` 类型，查询玩家是否死亡，调用场景为输入控制判定（死亡时屏蔽移动、跳跃等操作）。

## 三、模块定义

本项目按 “职责单一” 原则划分 3 个核心模块，模块间通过接口调用交互，无直接耦合，具体模块设计如下：

### 1. 玩家行为模块（player-behavior-module）

- 模块定位：封装玩家所有行为逻辑与状态管理，是 “玩家实体” 的核心逻辑载体，决定玩家如何响应输入、与物理世界交互；
- 核心类：`PlayerComponent`；
- 子模块划分：
  - 运动控制子模块：负责实现 `moveLeft()`/`moveRight()`/`jump()`/`endJump()` 等运动方法，通过 `PhysicsComponent` 控制实体物理速度，处理跳跃长按、二段跳等差异化运动逻辑；
  - 状态管理子模块：维护 `isOnGround`/`isJumping`/`isDead`/`isFacingRight` 等状态变量，提供 `isXxx()` 状态查询方法与 `setJumpHeld()` 状态修改方法，为其他子模块提供状态依据；
  - 动画渲染子模块：通过 `updateAnimation()` 方法，根据玩家实时状态切换对应动画通道（`animIdle`/`animMove`/`animJump`/`animFall`），并根据朝向调整精灵缩放，确保动画与行为同步；
  - 死亡重生子模块：实现 `die()` 方法（处理死亡时的物理、碰撞、视图、音效逻辑）与 `respawn()` 方法（处理重生时的状态重置、位置恢复、视图清理逻辑）；
- 依赖模块：底层支撑层（FXGL 的 `PhysicsComponent`/`AnimatedTexture`/`AssetLoader`/`GameScene`）；
- 对外提供接口：所有 `public` 方法（如 `jump()`/`respawn()`/`isOnGround()`/`setRespawnPoint()`）。

### 2. 实体工厂模块（entity-factory-module）

- 模块定位：标准化生产游戏内所有实体，统一管理实体的物理属性、碰撞盒、视图资源与组件，避免实体配置分散导致的维护成本；
- 核心类：`BlockFactory`；
- 子模块划分：
  - 玩家实体子模块：通过 `spawnPlayer()` 方法，生成配置完整的玩家实体（动态物理属性、碰撞盒、`PlayerComponent`），确保玩家实体初始化一致性；
  - 静态实体子模块：通过 `spawnGround()`/`spawnSpikeup()`/`spawnPlatform()`/`spawnSoil()` 等方法，生成地面、尖刺、平台、泥土等静态实体，统一配置静态物理属性、碰撞盒与对应视图；
  - 临时实体子模块：通过 `spawnDeath()` 方法，生成死亡特效等临时实体，附加自动销毁组件，避免临时实体占用内存；
- 依赖模块：底层支撑层（FXGL 的 `EntityBuilder`/`PhysicsComponent`/`HitBox`/`ExpireCleanComponent`/`AssetLoader`）；
- 对外提供接口：所有 `@Spawns` 注解的实体生成方法（如 `spawnPlayer()`/`spawnGround()`/`spawnSpikeup()`）。

### 3. 应用统筹模块（app-coordination-module）

- 模块定位：作为游戏入口，统筹初始化所有模块，绑定输入、碰撞、UI 与核心功能的关联，是游戏运行的 “总控中心”；
- 核心类：`IWBTCSerApp`；
- 子模块划分：
  - 配置初始化子模块：通过 `initSettings()` 方法，定义游戏窗口、标题、图标等基础配置，为游戏运行提供基础环境；
  - 游戏世界初始化子模块：通过 `initGame()` 方法，注册 `BlockFactory` 为实体工厂、生成玩家与初始实体（地面、尖刺）、获取 `PlayerComponent` 实例、设置初始重生点，构建游戏初始世界；
  - 输入控制子模块：通过 `initInput()` 方法，绑定键盘


ToDo List：
触发器，可移动障碍物，地图制作，平台（下边缘可上），水，吹风口~~存档点设置，射击子弹，本地存档，关卡间传送门，敌人~~
