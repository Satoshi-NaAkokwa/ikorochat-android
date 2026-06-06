#!/bin/bash
set -x
cd /root/ikorochat-android

# Update files
git add .github/workflows/*.yml gradle/libs.versions.toml gradle.properties app/build.gradle.kts

# Create commit message
COMMIT_MSG="Fix: download-apk.yml with proper trigger

- Changed workflow_run trigger to push/workflow_dispatch
- Updated build steps
- Added verification step"

git commit -m "$COMMIT_MSG"

# Push with SSH
GIT_SSH_COMMAND="ssh -i ~/.ssh/ikorochat_github -o StrictHostKeyChecking=accept-new" git push origin main 2>&1
