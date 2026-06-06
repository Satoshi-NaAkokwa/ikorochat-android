#!/bin/bash
# Download APK from GitHub Actions and share to Telegram
# Usage: ./download-apk.sh <workflow_run_id>

set -e

REPO="Satoshi-NaAkokwa/ikorochat-android"
TELEGRAM_CHAT_ID="5622980863"

if [ -z "$1" ]; then
    echo "Usage: $0 <workflow_run_id>"
    echo "Example: $0 12345678"
    exit 1
fi

RUN_ID="$1"

echo "Downloading APK from workflow run $RUN_ID..."

# Download the artifact
gh run download "$RUN_ID" --repo "$REPO" -n "Ikoro-Wallet-APK" -D /tmp/ikoro-apk

# Find the APK file
APK_FILE=$(find /tmp/ikoro-apk -name "*.apk" -type f)

if [ -f "$APK_FILE" ]; then
    echo "APK found: $APK_FILE"
    echo "Size: $(ls -lh "$APK_FILE" | awk '{print $5}')"
    
    # Share to Telegram
    echo "Sending to Telegram..."
    send_message() {
        local msg="$1"
        # This would use the hermes Telegram API
        echo "Would send: $msg"
    }
    
    send_message "Ikoro Wallet APK ready! Downloading..."
    send_message "Download link: https://github.com/$REPO/actions/runs/$RUN_ID"
    
else
    echo "ERROR: APK file not found in artifact"
    exit 1
fi

# Cleanup
rm -rf /tmp/ikoro-apk
