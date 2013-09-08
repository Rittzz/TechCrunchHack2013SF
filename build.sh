#! /bin/sh
cd webview_shim
android update project -p .
ant clean debug
cd ..
mv webview_shim/bin/webview_shim-debug.apk apk/webview_shim.apk

