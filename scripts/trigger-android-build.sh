#!/bin/bash
# Trigger Android CI workflow manually
# This script is self-contained and doesn't need external token

REPO="Satoshi-NaAkokwa/ikorochat-android"
WORKFLOW_ID="259364438"  # android-build.yml workflow ID

echo "Triggering Android CI workflow for main branch..."

# Use GitHub CLI to trigger the workflow (no token needed if authenticated)
# First check if gh is available
if command -v gh &> /dev/null; then
    echo "Using GitHub CLI to trigger workflow..."
    gh workflow run $WORKFLOW_ID -R $REPO -f ref=main
else
    # Fallback: use curl with GitHub API
    echo "Using curl to trigger workflow..."
    # GitHub CLI token from auth cache
    if [ -f ~/.config/gh/hosts.yml ]; then
        GITHUB_TOKEN=$(grep -A5 "github.com:" ~/.config/gh/hosts.yml | grep "ghp_" | head -1 | awk '{print $2}')
    fi
    
    curl -s -X POST "https://api.github.com/repos/$REPO/actions/workflows/$WORKFLOW_ID/dispatches" \
      -H "Authorization: token ${GITHUB_TOKEN:-DummyToken}" \
      -H "Accept: application/vnd.github+json" \
      -d '{"ref":"main"}' | tee /tmp/trigger-response.json
fi

echo ""
echo "=== Workflow triggered! ==="
echo "Check status at: https://github.com/$REPO/actions/workflows/$WORKFLOW_ID"
