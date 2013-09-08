#! /bin/sh

set -e

echo "Building Webview Shim"
cd webview_shim
android update project -p .
ant clean debug
cd ..
mv webview_shim/bin/webview_shim-debug.apk apk/webview_shim.apk

echo "Building Clover Service"
cd clover_service
android update project -p .
ant clean debug
cd ..
mv clover_service/bin/clover_service-debug.apk apk/clover_service.apk

echo "Done Building"

