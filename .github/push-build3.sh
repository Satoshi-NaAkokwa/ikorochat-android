#!/bin/bash
set -x
cd /root/ikorochat-android

git add .github/workflows/build.yml
git commit -m "Add token to checkout step"
git push origin main 2>&1
