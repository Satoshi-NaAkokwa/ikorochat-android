#!/bin/bash
# Script to trigger build and send status to Telegram

# Get the latest run status for build-apk.yml workflow
WORKFLOW_ID="289226461"
REPO="Satoshi-NaAkokwa/ikorochat-android"

# Get latest runs
runs=$(curl -s "https://api.github.com/repos/$REPO/actions/workflows/$WORKFLOW_ID/runs?per_page=1")
echo "=== Latest Run Status ==="
echo "$runs" | python3 -c "
import sys, json
data = json.load(sys.stdin)
runs = data.get('workflow_runs', [])
if runs:
    r = runs[0]
    print(f\"Run ID: #{r.get('id')}\")
    print(f\"Status: {r.get('status')}\")
    print(f\"Conclusion: {r.get('conclusion') or 'pending'}\")
    print(f\"Display Title: {r.get('display_title', '')[:80]}\")
    print(f\"HTML URL: {r.get('html_url')}\")
else:
    print('No runs found')
"

# Send status to Telegram
curl -s -X POST "https://api.telegram.org/botXXXXX/sendMessage" \
  -d chat_id=5622980863 \
  -d text="Ikoro Wallet Build Status 🚀

Latest workflow run details:
• Run ID: Check link below
• Status: Check the link for details
• URL: https://github.com/Satoshi-NaAkokwa/ikorochat-android/actions/workflows/build-apk.yml

Fix applied:
• Added Android SDK components (platform-tools, android-31, build-tools 31.0.0)
• Added gradlew executable setup

Wait for build to complete (5-10 minutes), then download APK from artifacts." \
  -H "Content-Type: application/json"

echo ""
echo "=== Status sent to Telegram ==="
