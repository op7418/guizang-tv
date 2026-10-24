# TCL 适配截图与运行检查

所有图片均为原始 ADB 截图，没有拼接、裁剪或修改像素。逐张检查未见账号、城市、局域网地址、个人接线名称或主机路径；PNG 无文本、EXIF 或时间元数据。文件名不含设备地址。

当前贡献构建 APK SHA-256：`bca2c40e8da5e3f2f2370d44af59cb04536867005732d66fc7fea4895ffbdcf7`。TCL 覆盖安装后拉回 APK，哈希一致。

| 文件 | 来源与用途 |
| --- | --- |
| [tcl-settings-before.png](tcl-settings-before.png) | TCL 实机，较早本地集成版本 tcl.1；不是原始上游 APK |
| [tcl-settings-after.png](tcl-settings-after.png) | TCL 实机，当前贡献构建；设置总览 |
| [tcl-settings-last.png](tcl-settings-last.png) | TCL 实机，当前贡献构建；末项聚焦、小字完整 |
| [tcl-back-focus.png](tcl-back-focus.png) | TCL 实机，当前贡献构建；返回按钮完整圆角 |
| [emulator-chinese-focus.png](emulator-chinese-focus.png) | Android 14 模拟器，当前贡献构建；中文测试名称，非个人应用配置 |
| [emulator-nine-items.png](emulator-nine-items.png) | Android 14 模拟器，当前贡献构建；九项测试列表、serif、最大字号、系统字体比例 1.3 |
| [emulator-wallpaper-preview.png](emulator-wallpaper-preview.png) | Android 14 模拟器，当前贡献构建；浏览未确认的壁纸 |
| [emulator-codex-off.png](emulator-codex-off.png) | Android 14 模拟器，当前贡献构建；既有 Codex 开关默认关闭 |

## 运行检查

- TCL Smart TV Pro / Android 14：19 个断言通过，涵盖 Home、连续 Back、设置返回按钮、设置子页、Home 关闭父子弹窗、内置应用列表、设置末项、TCL 系统设置打开与返回。当前贡献构建使用原有用户偏好；仅公开不显示这些偏好的设置截图。
- Android 14 模拟器 / 1920×1080 / density 320：24 个断言通过，覆盖上述通用导航、非默认 HOME 的普通应用返回、标准系统设置 fallback、壁纸取消、大字号九项滚动与 Codex 关闭入口。天气关闭，首页名称是文字布局测试数据。
- 静态截图只能展示画面，按键行为由实际操作与 UI 层级回读确认。信源真实音画、完整重启、待机、其他固件与小米实机未验。
- 未聚焦的视口边缘行可能只显示一部分；聚焦行应滚动到完整可见。

## 图片校验

```text
fa52d1708ac89f639b4b7bd0d1a490433c9a0665fe370f077fc0898b3866363b  tcl-settings-before.png
2af2ebb856bddfbe773c59df4fbc39ddce7ab26561b90d6f9d2e796ce1a53bc5  tcl-settings-after.png
c6745fd96e80024b23683530b67c2fe6dfa82cc5819182c3ad670b7a2635e95f  tcl-settings-last.png
f743c130414c1538eee225ce9e7f91ccf96b76e721ce0cbea194642468e152ca  tcl-back-focus.png
995f1e089eb0b272f070066467779842e57835fa82778572954b4a505f9c09eb  emulator-chinese-focus.png
e79f4837d0e608474873e33422af53170a1af3210e09a4e400eb676c96e979df  emulator-nine-items.png
54a6d8a80fb641ef7bb3262cfee3619f28350fe51609a422640314261b771425  emulator-wallpaper-preview.png
3d8d1727433e31e298508ef7d84b296df734bf09fe7c5f2e8e9fcdfc1e7bc5b9  emulator-codex-off.png
```
