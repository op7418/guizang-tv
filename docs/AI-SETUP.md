# AI 适配与用户引导指南

供 Codex、Claude Code 或其他能读源码、执行本地工具的 AI 使用。目标是把用户自己的设备接通，并留下可复现的结果；不是照抄开发者家的地址、电脑路径和小米参数。

## 先分清用户要做什么

1. **只用简洁桌面**：走 [README](../README.md) 的安装和设置；不需要 Codex、电脑常驻或登录模型账号。
2. **电视/安卓设备与电脑上的 Codex 对话**：先完成桌面安装，再按本文的电脑和实验功能流程操作。
3. **把桌面本身运行在 Windows/macOS/Linux**：本项目是 Android APK，不是电脑原生桌面程序。可评估安卓模拟器进行开发验证，但模拟器不具备真实 HDMI、遥控器麦克风或电视开机能力；不能声称安装电脑桥接服务就获得电脑桌面版。
4. **换其他模型、接米家、日程或电脑操作**：属于扩展开发，先明确需求和权限；不要将现有对话试用描述为已具备这些能力。

交互时一次给用户一个可完成的步骤，说明“现在做什么、成功会看到什么、失败需要什么信息”。能通过已授权工具查到的版本和状态自行读取；只问拿不到的设备、接线、账号或偏好。

## 当前证据，而非通用保证

| 环境 | 已知状态 | 下一台设备需要验证 |
| --- | --- | --- |
| 小米 MiTV-MTSU0 / Android 14 / OS3.0.119.0.UTSMATV | 私人同源 APK 的桌面、Codex 文字、搜狗输入法语音回填有实机记录 | 公开包签名差异、实际输入法版本及所有关键交互 |
| 其他 Android TV / 盒子 | 最低 Android 8 / API 26；未完成实机兼容验证 | 安装、遥控器焦点、包发现、输入法、后台限制；不继承小米参数 |
| TCL Smart TV Pro / Android 14 | 本地集成版本有系统设置、Home/Back 和中文显示记录；main 有限定型号适配 | 按 [TCL 说明](TCL.md)核对注册信源；贡献分支回归、真实音画及重启另验 |
| 安卓手机 / 平板 | 有普通 LAUNCHER 入口，固定横屏，布局面向电视 | 触控、屏幕尺寸、键盘遮挡；不是已适配的移动聊天客户端 |
| macOS 电脑 | 已验证 Python 3.11+ 与本机登录的 Codex CLI 0.163.0-alpha.11 | 当前 CLI 协议、局域网绑定、证书、睡眠行为 |
| Windows / Linux 电脑 | 以下为配置与诊断路径，未做本项目完整实机验证 | Python、OpenSSL、原生 CLI 路径、UTF-8、进程与网络权限 |

正式 APK 和 main 不一定有同一组功能。先看版本、源代码及变更记录，再决定安装 Release 还是自行构建。不能让下载 v0.3.0 的用户寻找尚未进入该版的 Codex 菜单。

## A. Android 设备：先检查，再安装

获准连接目标设备后，使用官方 Android Platform Tools。多台设备时每个命令带 `adb -s <序列号>`；不要自动选第一台。以下占位符必须替换，输出中不要公开序列号和网络地址。

```sh
adb devices -l
adb -s <序列号> shell getprop ro.product.model
adb -s <序列号> shell getprop ro.build.version.release
adb -s <序列号> shell getprop ro.build.version.sdk
adb -s <序列号> shell getprop ro.build.display.id
adb -s <序列号> shell wm size
adb -s <序列号> shell wm density
adb -s <序列号> shell settings get secure default_input_method
```

- 网络调试的配对/端口按该固件提示操作；不是所有设备都用 5555。授权提示必须由设备拥有者确认。ADB 只用于安装和诊断，系统键盘语音及配对后的对话运行不依赖 ADB。
- 已安装时检查 `dumpsys package io.github.op7418.guizangtv` 和本地 APK 签名；私人包 `com.gathin.tvhome` 是另一个应用。不要为了覆盖安装而自动卸载旧包；必要时保留旧版，用独立包名试用。
- 从普通应用入口试用。不要先改默认桌面、禁用系统包或设置开机目标。ADB 启动公开版：`adb -s <序列号> shell am start -n io.github.op7418.guizangtv/.FrameHomeActivity`。
- 检查列表和 Dock 的上下左右、确认、返回及设置入口。应用包名按本机查询结果配置；名称“PS5”并不代表识别到了设备。
- 其他品牌的系统设置优先标准 Android Intent。信号源、开机、待机分开验证，并记录组件和失败回退；不要放宽 `XiaomiControls` 的机型判断来伪造兼容。

## B. 电脑：准备独立的局域网桥接

本项目的路径是：安卓面板 → HTTPS 连接服务 → 本机 stdio App Server → Codex。不是直接连接桌面 App 当前聊天，也不复制用户的其他 Codex 会话。

在目标电脑检查 Python 3.11+（使用 tomllib）、OpenSSL 是否支持 `req -addext`、Codex CLI 的可执行路径和版本、`codex login status`、`codex app-server --help`。登录需要用户自己完成。没有 CLI 时按 [OpenAI 官方文档](https://learn.chatgpt.com/docs/app-server)及该平台安装指南准备；不要假设本机安装路径或复制另一个账号的凭证。选择账号可用模型，不要硬编码开发者的模型名。

**macOS / Linux，以下从项目根目录运行：**

```sh
python3 --version
openssl version
command -v codex
codex --version
codex login status
codex app-server --help
python3 companion/server.py --home "$HOME/.guizang-tv" --init --host <电脑的局域网IPv4>
python3 companion/server.py --home "$HOME/.guizang-tv"
```

如果 `codex` 不在 PATH，可在启动命令后加 `--codex "/实际路径/codex"`。Linux 尚未完成整套实测，需继续做 C 节验证。初始化目录须是源码仓库外、当前用户私有的目录。已有 config.json 时不覆盖；先检查当前配置或新建目录，不删除旧凭证。

**Windows 原生 PowerShell，候选路径：**

```powershell
py -3 --version
Get-Command openssl
Get-Command codex -All
codex --version
codex login status
codex app-server --help
$env:PYTHONUTF8 = "1"
$gtvHome = Join-Path $env:LOCALAPPDATA "GuiZangTV"
py -3 companion/server.py --home $gtvHome --init --host <电脑的局域网IPv4>
py -3 companion/server.py --home $gtvHome --codex "C:\实际路径\codex.exe"
```

确认 `py -3` 实际是 3.11+；无 `py` 时使用已验证的 Python 绝对路径。替换 CLI 路径，不按示例猜目录。Windows CLI 安装上下文见 [官方部署文档](https://learn.chatgpt.com/docs/enterprise/windows-deployment)。若发现的只有 `.cmd` / `.ps1` 启动器，先定位其实际可执行程序，验证 Python 能通过 stdin/stdout 启动；不要直接改成 `shell=True` 拼接命令。OpenSSL 缺失或 `-addext` 不支持时先安装/选择支持的版本，不能去掉证书 IP SAN。

Python 的 Unix chmod 不能替代 Windows ACL；检查 `$gtvHome` 的访问权限，不能让其他普通用户读取私钥和配对文件。使用 UTF-8 避免中文配置/回复被系统代码页错误解码。不要将本节命令当成 Windows 已实测报告。

**WSL**：Python、OpenSSL、Codex 登录和连接服务放在同一环境里；WSL 的地址不一定能被电视访问。先证明电视可达再生成配对。不要将 Windows 的 localhost 或 WSL 内网地址直接当成家庭局域网地址，也不要自动设置路由器公网转发。

**网络共同要求**：绑定电视能访问的电脑私有 IPv4，不使用 `0.0.0.0` 或把 `127.0.0.1` 写给另一台设备。证书绑定这个 IP。电脑入站端口默认 8765；只为实际需要的家庭局域网连接配置防火墙，不关闭整机防火墙。留意访客网络/AP 隔离、VPN 路由和电脑睡眠。更换电脑或 IP 时生成新配对，不复用另一台电脑的私钥。

## C. 用户手动开启与配对

1. 引导用户打开「设置 → 实验性功能 → Codex 对话 → 开启」。默认关闭；已有旧配对也不会自动开启。
2. 返回首页打开 Codex。首次在浮层显示连接表单；电脑上生成的 `tv-pairing.txt` 是完整配对码，通过可信的本地输入方式填写。当前没有扫码配对网页。不要把配对码发到公开聊天、Issue 或测试日志。
3. 看到“已连接”才开始发送一个简单文字问题。确认键盘没有遮住输入条，返回先收键盘再收浮层；发送后输入框清空，自己的消息和回答胶囊都在当前画面可见；仅检查后台记录不够。
4. 连续两轮，再关闭重进。确认最新消息显示、可以滚动历史、新消息不偷走遥控器焦点。测试停止和新对话前先明确会中断/清空当前试用会话，不抹掉用户正在阅读的内容。
5. 最后才做语音：进入浮层应自动打开输入法，也可点击左侧麦克风图标（「键盘 / 语音」）；看到真实输入法，再请用户按住语音键说一句简单问题。确认回填后收起键盘、核对并点面板“发送”。保持入口就绪再让用户说话，不设置用户还没准备好的短倒计时。
6. 若弹出系统助手，不要声称输入完成，也不要自动禁用助手。记录输入法、前台窗口和实际行为，优先普通输入/蓝牙键盘；独占遥控器麦克风是另一项适配工作。

开启只显示入口，不代替配对、不授予日程/米家/电脑操作权限，也不承诺免费额度。关闭开关隐藏入口并停止后续电视请求，保留配对；已经发出的请求可能在电脑继续。需要完全停用时在电脑 Ctrl+C 停止连接服务，旧目录保留或按用户明确要求处理。

## D. 按现象定位，不让用户盲目重试

| 现象 | 首先检查 |
| --- | --- |
| 首页没有 Codex | 是否 main 试用版；实验开关是否由用户开启；不是配对故障 |
| 未连接到电脑 | 进程是否在运行、绑定 IP、证书有效期与主机名、局域网可达、防火墙、睡眠；不要跳过 HTTPS 校验 |
| 电脑服务启动失败 | Python/CLI 路径、登录、OpenSSL、端口占用、App Server 初始化；保留原配置 |
| 已连接但回答失败 | 模型/账号可用性、额度、网络、CLI 协议版本；不先要求用户重新刷电视 |
| 点击发送无变化 | 是否仍由键盘占焦点、按钮是否真触发、busy/error、请求是否抵达、最新消息是否在可视范围；不要重复发同一句制造多轮 |
| 首次正常、重进失败 | 窗口生命周期、轮询状态、输入法连接、滚动位置、保存的配对；先复现再改 |
| 小爱回答但 Codex 无文字 | 是否先打开输入法；系统助手与输入法语音是不同路径；不要读取全部长期语音日志当“语音接管” |
| 中文乱码（尤其 Windows） | Python UTF-8、stdio 编解码、文件编码；不要把编码故障误判成模型不支持中文 |

## E. 修改源码时的入口与验收

| 范围 | 文件 |
| --- | --- |
| 开关、配对、输入法、消息、滚动 | `app/src/main/java/io/github/op7418/guizangtv/HomeAssistant.java` |
| 设置与首页入口/焦点 | 同目录 `HomeSettings.java`、`HomeSurface.java`、`FrameHomeActivity.java` |
| 品牌入口与应用 | 同目录 `XiaomiControls.java`、`HomeItems.java` |
| 电脑协议和安全边界 | `companion/server.py`、`companion/test_server.py` |
| 设备验证和版本记录 | `docs/COMPATIBILITY.md`、`docs/changes/` |

构建依赖见 README。按实际改动运行：

```sh
gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
python3 -m unittest discover -s companion -v
```

CLI 协议变化时，先看本机 `codex app-server --help`，可使用 `codex app-server generate-json-schema --out <本地临时目录>` 生成该版本协议。核对 initialize/initialized、thread/start、turn/start、流式事件和中断；不要直接把模型接口和原始 RPC 开给电视。[官方 App Server 文档](https://learn.chatgpt.com/docs/app-server)解释了 stdio 协议与版本对应的 schema。

验收至少覆盖：新装默认关、旧配对升级仍关、手动开后入口显示、重启保留开关、关闭不再连接且其他三项操作可达；再做文字/连续发送/重进/语音等对应功能测试。关功能不应清除壁纸、首页顺序、天气和启动偏好。关闭不等于撤销已经发送的模型请求。

交付报告写清：设备型号/固件、系统和 CLI 版本、源码 commit、包名/签名来源、实际通过项、未验证项、恢复方法。只输出脱敏摘要。一次成功的结果只证明该组合；没有 Windows 或另一品牌实机时明确写“未实测”，不要升级成全平台支持。

## 用户可以直接交给 AI 的提示词

> 请先阅读仓库 AGENTS.md、docs/AI-SETUP.md 和 docs/CODEX.md，帮我把归藏电视适配到我的安卓设备，并配置电脑端 Codex 实验。先检测设备、电脑系统、工具版本和现有安装，保留已有配置；让我手动开启实验开关。先验证文字、连续发送和重进，再引导语音测试。不要公开凭证或改动系统助手。最后告诉我哪些已通过、哪些未验证以及如何恢复。
