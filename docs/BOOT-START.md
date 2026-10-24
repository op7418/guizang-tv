# 开机自动进入（实验）

main 的 TCL Smart TV Pro 实验入口使用系统 `BOOT_COMPLETED` 广播，默认关闭，不配置小米的 `start_3rd_app`。
仅在匹配设备、开关开启、开机窗口内尝试一次；完整重启仍待实机验收。详见 [TCL 说明](TCL.md)。
下文是既有小米版本的配置流程，不适用于 TCL。

v0.2.0 新增。**默认关闭，需要应用内开关和一次电脑配置同时完成**，不是安装后自动接管电视。

目前仅适配 **MiTV-MTSU0**，实机验证固件为 **OS3.0.119.0.UTSMATV / Android 14**。其他型号在应用内不可启用；同型号的其他固件也需要单独验证。

## 它如何工作？

这版小米桌面的启动流程会读取 `Settings.System.start_3rd_app`，使用 Android 的包启动接口打开指定应用。它与“开机直接播放信号源”是两个设置。

归藏电视提供一个很小的启动判断入口：

1. 小米系统打开这个入口。
2. 检查已适配机型、应用内开关、系统本次启动编号和开机经过时间。
3. 开机十分钟内、且本次开机尚未尝试过时，进入归藏电视。
4. 开关关闭、重复调用、通知过晚或无法确定启动编号时，直接结束。

记录在请求打开界面之前保存，避免系统重复调用时再次抢占画面。不使用常驻服务、开机广播接收器、无障碍服务或悬浮窗权限，也不修改默认 Home。

实现使用 `CATEGORY_INFO` 的 `BootGateActivity`；普通电视应用列表中的 `LAUNCHER` / `LEANBACK_LAUNCHER` 仍直接进入主界面。其他软件如果使用 `getLaunchIntentForPackage()` 打开本应用，也会经过这个启动判断入口；需要手动打开时，请使用电视应用列表或明确指定 `FrameHomeActivity`。

## 启用步骤

以下命令需要电脑上的 Android SDK Platform-Tools，以及电视已经授权的 ADB 连接。将 `TV_IP:PORT` 替换为实际地址与端口。不要把手机 HyperOS 的自启动教程直接当成这台电视的操作步骤。

### 1. 安装新版并打开一次

安装 [v0.3.0 APK](https://github.com/op7418/guizang-tv/releases/tag/v0.3.0)，从电视应用列表打开“归藏电视”。不要给 v0.1.1 或更早版本配置这个启动目标，它们没有防止重复进入的判断入口。

私人试用版“简洁桌面”和公开版“归藏电视”是不同应用。本文命令只针对公开版 `io.github.op7418.guizangtv`，不会迁移私人版的偏好。

### 2. 核对机型并保存原设置

```sh
adb -s TV_IP:PORT shell getprop ro.product.model
adb -s TV_IP:PORT shell getprop ro.build.display.id
adb -s TV_IP:PORT shell settings get system start_3rd_app
adb -s TV_IP:PORT shell settings get system boot_auto_play
```

将输出保存到自己的本地记录中。`start_3rd_app` 为空或 `null` 表示没有明确目标；若已有其他包名，先记录并确认是否要替换，不能当成空值覆盖。

在电视上关闭“开机直接播放信号源”，使 `boot_auto_play` 为 `0`。原生 HDMI 自动播放或设备发起的一触即播可能优先于第三方应用启动；本项目不自动调整 CEC。

### 3. 配置系统目标

确认新版已安装，并且你希望这台电视开机进入归藏电视后执行：

```sh
adb -s TV_IP:PORT shell settings put system start_3rd_app io.github.op7418.guizangtv
adb -s TV_IP:PORT shell settings get system start_3rd_app
```

第二条命令应返回 `io.github.op7418.guizangtv`。

### 4. 开启应用内开关

在归藏电视进入 **“设置” → “电视与启动” → “开机自动进入” → “开启自动进入”**。

面板应显示“系统入口：已配置”。若显示尚未配置，仅打开应用内开关不会自动配置系统；按前面的步骤检查目标包名。

### 5. 完整重启并验证

使用电视系统的重启选项，或在确认没有正在进行的播放和操作后执行：

```sh
adb -s TV_IP:PORT shell reboot
```

等待开机画面结束，观察是否进入归藏电视。普通遥控器关屏后再打开可能只是待机唤醒，不能替代这项验证。

已测固件在完整重启后会关闭网络调试端口，需要在电视设置中重新开启才能读取日志。**自动进入本身不依赖 ADB 或电脑保持在线。**

恢复调试连接后可读取诊断：

```sh
adb -s TV_IP:PORT shell dumpsys activity io.github.op7418.guizangtv
```

查找 `BootStart:`：`enabled=true`、`configured=true`，以及“已在本次开机后进入桌面”。该确认来自桌面恢复显示，不把单纯发出启动请求当作成功。

## 关闭、升级与恢复

日常关闭只需进入 **设置 → 电视与启动 → 开机自动进入 → 关闭自动进入**。启动判断入口会立即结束，不打开桌面。以后可在同一面板重新开启。

这个开关不会清除系统的 `start_3rd_app` 值。小米桌面还可能缓存自动启动目标，因此，关闭开关比直接删除配置更可靠；保留本应用的判断入口，可以处理系统后续的缓存调用。

同签名正常升级保留开关和偏好。**不要在系统仍指向本应用时直接降级到 v0.1.x**：旧版没有启动判断入口，系统调用它时会直接进入主界面。

如需更换开机目标或完全恢复：

1. 先关闭归藏电视的自动进入开关。
2. 按此前记录恢复 `start_3rd_app`。原本有值就恢复原值；原本不存在的键可删除，原本为空字符串则恢复为空字符串。不要把文本 `null` 写成包名。
3. 完整重启核对结果。小米可能仍保留内部缓存；在确认恢复前保留已关闭的判断入口，不通过清除小米桌面数据来强行处理。

恢复命令模板，按原始记录选择一种：

```sh
# 原来有目标包名：把 ORIGINAL_PACKAGE 替换为保存的原值
adb -s TV_IP:PORT shell settings put system start_3rd_app ORIGINAL_PACKAGE

# 原来返回 null，表示没有这个键
adb -s TV_IP:PORT shell settings delete system start_3rd_app

# 原来是空字符串；引号保留给电视端 shell
adb -s TV_IP:PORT shell "settings put system start_3rd_app ''"
```

## 验证范围

2026-10-21，在上述机型与固件的私人同源实现上：

- 完整重启后，用户现场确认自动进入；启动编号和应用日志相符。
- Home 键仍返回小米原桌面。
- 重复调用入口时，保持原桌面；在 Apple TV HDMI 画面中重复调用，也保持原信号源。
- 应用内关闭、再开启开关均验证通过。
- 未禁用系统应用，未启用无障碍服务或悬浮窗权限。

公开 v0.2.0 移植该实现并增加未适配机型保护，验证方式见[发布记录](changes/2026-10-21-boot-start.md)。**公开签名 APK 尚未重新执行完整重启测试，私人版实测不等于所有公开版本和机型都已验证。**

未完成实际待机睡眠/唤醒、冷启动时 CEC 竞态、长期待机及其他固件的回归。系统在开机早期启动后再次抢占画面时，本应用不会反复拉回自己。该设置是小米固件内部实现，后续系统更新可能改变它。
