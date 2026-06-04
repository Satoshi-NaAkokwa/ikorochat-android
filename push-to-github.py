#!/usr/bin/env python3
"""
Push Ikoro Wallet code to GitHub repository
"""

import subprocess
import sys
import os

# Configuration
GITHUB_REPO = "Satoshi-NaAkokwa/ikorochat-android"
PROJECT_DIR = "/root/ikorochat-android"

def run_cmd(cmd, cwd=None, check=True):
    """Run command and return result"""
    result = subprocess.run(
        cmd,
        shell=isinstance(cmd, str),
        capture_output=True,
        text=True,
        cwd=cwd or PROJECT_DIR,
        check=check
    )
    return result

def main():
    print("=" * 60)
    print("Push Ikoro Wallet to GitHub")
    print("=" * 60)
    
    # Get GitHub token
    token = os.environ.get("GITHUB_TOKEN")
    if not token:
        print("GITHUB_TOKEN not found in environment")
        print("Please set GITHUB_TOKEN with repo scope and retry")
        return 1
    
    # Configure git with PAT
    auth_url = f"https://x-access-token:{token}@github.com/{GITHUB_REPO}.git"
    
    print("\n1. Configuring git remote...")
    run_cmd(f"git remote set-url origin {auth_url}")
    
    print("2. Checking git status...")
    result = run_cmd("git status --porcelain")
    if result.stdout.strip():
        print("Files to commit:")
        print(result.stdout)
    
    print("\n3. Adding all files...")
    run_cmd("git add .")
    
    print("4. Committing changes...")
    result = run_cmd('git commit -m "Add wallet module with messaging, mesh, and wallet functionality [CI: skip]"', check=False)
    if result.returncode != 0 and "nothing to commit" in result.stderr:
        print("No changes to commit")
    else:
        print("Changes committed")
    
    print("\n5. Pushing to GitHub...")
    result = run_cmd("git push origin main")
    if result.returncode == 0:
        print("✅ Code pushed successfully to GitHub!")
        print(f"Repository: https://github.com/{GITHUB_REPO}")
        print("\nNext steps:")
        print("1. Go to Actions tab")
        print("2. Trigger 'Build Wallet APK' workflow")
        print("3. Download APK from artifacts after 5-10 minutes")
        return 0
    else:
        print(f"❌ Push failed: {result.stderr}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
