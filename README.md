# MiniBot-Agent-EFPaaS

为[ EFPaaS 小程序](https://www.nantian.com.cn/industry/919.html)自动化测试提供支持

## Features

- 设置 WebView 调试
- 打开小程序
- 屏蔽弹窗和跳出界面
- 设置代理并自动 SSL Unpinning

## Usage

### 初始化准备

1. 为宿主应用启用插件：

```
adb shell mkdir -p /sdcard/ijm_sandbox/minibot_efpaas/<宿主包名>
```

2. 启用爱加密沙箱中 Aop 框架的 inline hook 支持：

```
adb shell mkdir -p /sdcard/ijm_sandbox/others_control/enable_inline_hook
```

3. 重新打开宿主 App

### 打开小程序


```
adb shell am broadcast -p <宿主 App 包名> -a cn.ijiami.minibot.efpaas.OPEN --es url minibot://mpaas/?<key>=<value>
```

目前已知的 url 参数列表如下：

| key | desc | sample |
|-|-|-|
| appId | 小程序ID | app2022070800000001 |
| launchFrom | 具体含义不明，建行中为固定值 | functionId |
| appType | 小程序类型，具体含义不明 | 1 |
| startPage | 页面路由 | index.html%23 |
| ... | 其他参数都会自动作为小程序启动参数 | |

### 禁用弹窗

防止小程序跳出到外部界面，通过广播开启：

```
adb shell am broadcast -p <宿主 App 包名> -a cn.ijiami.minibot.efpaas.ALERTS --ei enable 1
```

enable 参数默认为 1 ，表示开启，传 0 则取消禁用。

### 中间人代理支持

插件会自动进行 SSL Unpinning (部分)，若不便使用全系统代理，可以通过插件提供的设置代理功能：

```
adb shell am broadcast -p <宿主 App 包名>  -a cn.ijiami.minibot.efpaas.PROXY --es host <Proxy IP> --ei port <Proxy Port>
```

当 host 或者 port 为空时，表示取消代理。
