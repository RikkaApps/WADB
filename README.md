# WADB - a simple switch for ADB over network

[简体中文](README-zh_rCN.md) | [English](README.md)

## Feature

switch the state of ADB over network by the following command，**Need root !!!**

```shell
// Start ADB over network
setprop service.adb.tcp.port 5555
stop adbd
start adbd

// Stop ADB over network
setprop service.adb.tcp.port -1
stop adbd
start adbd

```

Support the new [Tile API](https://developer.android.com/preview/api-overview.html#tile_api) which add in Android N, so you need a Android N device to use the tile. But other functions also work well in Android 6.0.

## Screenshot

<img src="readme.res/01.png" width="30%" height="30%">
<img src="readme.res/02.png" width="30%" height="30%">
<img src="readme.res/03.png" width="30%" height="30%">
<img src="readme.res/04.png" width="30%" height="30%">

## Download

Support us in Play Store：

<a href='https://play.google.com/store/apps/details?id=moe.haruue.wadb&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="250"/></a>

... or directly download the APK file [here](https://github.com/RikkaApps/WADB/releases/latest).

## Translation

If you want to translate ADB over Network into your language, just open a Pull Request.