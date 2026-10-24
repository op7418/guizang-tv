# TCL Smart TV Pro 适配

当前适配限定 `Build.MODEL=Smart TV Pro` 且 manufacturer 或 brand 为 TCL。
在一台 Android 14 / API 34、渲染尺寸 1920×1080、density 320 的电视上取得了入口和界面记录。
其他型号或同型号不同固件仍需核对；不能仅凭品牌名复用信源参数。

## 系统入口

- 设置：尝试 `com.tcl.settings/com.tcl.settings.ui.MainActivity`，失败后使用标准 Android 设置 Intent。
- 原桌面：`com.tcl.cyberui/.MainActivity`。保留原厂桌面，不禁用其组件。
- 信源：使用已观察的 `com.tcl.tv/com.tcl.player.TVActivity`，再尝试固件候选 `com.tcl.tv.TVActivity`。
- 显式启动前检查 Activity 启用状态、导出属性和调用权限；启动被拒绝时继续候选，最终失败时给出遥控器信源键提示。

HDMI 1–4 对应 `com.tcl.tvinput/.TvPassThroughService/HW15`–`HW18`；TV 为
`com.tcl.tvinput/.TunerInputService/HW2`，AV 为 `com.tcl.tvinput/.TvPassThroughService/HW3`。
信源选择弹窗和实际调用前还会与 `TvInputManager.getTvInputList()` 的已注册项核对，不存在的映射不会调用。
注册信息与启动请求本身不证明实际画面和声音正常，仍需接入有信号设备验收。

TCL 新配置使用通用的 HDMI 1、HDMI 2、原桌面入口，不内置贡献者的应用或接线偏好。
已有首页顺序和保存的名称保留；其余应用可通过“全部应用”打开，或在设置中添加。
非 TCL 的既有默认入口不变；小米适配保持独立。

## Home 与 Back

应用不会自动改系统默认 HOME，不使用无障碍按键劫持。
先从普通应用入口试用。只有用户希望以归藏作为桌面时，才通过电视支持的默认应用设置选择归藏。
若自行使用 ADB 配置，应先记录当前默认项，并只对用户明确选定的设备操作：

```sh
adb -s <设备地址> shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb -s <设备地址> shell cmd package set-home-activity --user 0 io.github.op7418.guizangtv/.FrameHomeActivity
```

恢复时用同一命令将目标换回先前实际记录的组件，不猜测原桌面。
厂商固件可能忽略默认选择，必须实际按 Home 检查。

默认 HOME 为归藏时，首页 Back 保留在首页；收到 Home Intent 时关闭设置、应用列表、信源及 Codex 浮层。
未选为默认 HOME 时，保留普通应用和原有小米返回路径。第三方应用内部的 Back 不由归藏控制。
进入系统设置前先收起归藏设置；返回后的设备表现见下方验证记录。

## 实验性完整开机入口

TCL 使用 `BOOT_COMPLETED`，不使用小米的 `start_3rd_app` 配置。
功能默认关闭；显式开启后每次完整开机最多请求一次，超过开机十分钟跳过，真正恢复首页后才确认进入。
接收器只处理系统完整开机广播，不处理待机唤醒；小米仍使用既有 Gate，不同时从广播重复进入。
Android 后台启动限制仍可能阻止请求，未完成真实重启前不能标为通过。

## 验证边界

2026-10-24 当前贡献构建已覆盖安装到同一 TCL，拉回 APK 的哈希与本地一致。
实测 Home 回归藏、首页 Back 五次保留、内置列表返回、TCL 设置面板打开/返回、设置返回按钮与末项小字。
具体构建、运行检查与尚未覆盖项见 [变更记录](changes/2026-10-24-tcl-adaptation.md)。
真实 HDMI 音画、完整重启、待机、其他 TCL 固件及本次小米实机回归仍需补充。
截图只公开无个人信息的界面；原始设备日志、偏好、局域网地址及安装维护操作不随源码公开。
