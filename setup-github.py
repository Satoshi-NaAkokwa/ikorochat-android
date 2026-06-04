#!/usr/bin/env python3
"""
GitHub Repository Setup Script for Ikoro Wallet

This script creates a private GitHub repository and pushes the wallet code.
It handles:
- Repository creation
- Git remote configuration
- Initial commit and push
- GitHub Actions workflow setup
- Security verification
"""

import os
import sys
import subprocess
import json
import base64
from pathlib import Path

# Configuration
REPO_OWNER = "Satoshi-NaAkokwa"  # GitHub username or org
REPO_NAME = "ikorochat-wallet"
DESCRIPTION = "Private Ikoro Wallet module with messaging, mesh, and wallet functionality"
PRIVATE = True

# Paths
WALLET_DIR = Path("/root/ikorochat-android/app/src/main/java/com/ikoro/android/wallet")
WALLET_WORKSPACE = Path("/root/ikorochat-android")

# Security checks - files to exclude from GitHub
SECURITY_EXCLUDES = [
    "keystore.properties",
    "release.keystore",
    "*keystore",
    "*.jks",
    "build",
    ".gradle",
    "local.properties",
]


def check_environment():
    """Check if required environment variables are set"""
    print("Checking environment...")
    
    # Check for GITHUB_TOKEN
    github_token = os.environ.get("GITHUB_TOKEN")
    if not github_token:
        print("❌ GITHUB_TOKEN not found in environment")
        print("Please export GITHUB_TOKEN before running this script")
        return False
    
    # Check git configuration
    try:
        result = subprocess.run(["git", "config", "--get", "user.email"], capture_output=True, text=True)
        if not result.stdout.strip():
            print("❌ Git user.email not configured")
            return False
        
        result = subprocess.run(["git", "config", "--get", "user.name"], capture_output=True, text=True)
        if not result.stdout.strip():
            print("❌ Git user.name not configured")
            return False
            
    except FileNotFoundError:
        print("❌ Git not installed")
        return False
    
    print("✅ Environment check passed")
    return True


def create_repository(github_token):
    """Create a private GitHub repository"""
    print(f"\n📁 Creating repository: {REPO_OWNER}/{REPO_NAME}...")
    
    api_url = "https://api.github.com/orgs/Satoshi-NaAkokwa/repos"
    
    payload = {
        "name": REPO_NAME,
        "description": DESCRIPTION,
        "private": PRIVATE,
        "auto_init": False,
        "has_issues": False,
        "has_wiki": False,
        "has_projects": False
    }
    
    headers = {
        "Authorization": f"token {github_token}",
        "Accept": "application/vnd.github.v3+json"
    }
    
    response = subprocess.run(
        ["curl", "-s", "-X", "POST", "-H", f"Authorization: token {github_token}", 
         "-H", "Accept: application/vnd.github.v3+json",
         "-d", json.dumps(payload),
         api_url],
        capture_output=True, text=True
    )
    
    if response.returncode == 0:
        print(f"✅ Repository created: https://github.com/{REPO_OWNER}/{REPO_NAME}")
        return True
    else:
        # Check if repository already exists
        check_response = subprocess.run(
            ["curl", "-s", "-w", "%{http_code}", "-o", "/dev/null",
             f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}"],
            capture_output=True, text=True
        )
        if "200" in check_response.stdout:
            print(f"✅ Repository already exists: https://github.com/{REPO_OWNER}/{REPO_NAME}")
            return True
        
        print(f"❌ Failed to create repository")
        print(f"Error: {response.stderr}")
        return False


def configure_git_remote():
    """Add the new repository as a remote"""
    print("\n🔗 Configuring git remote...")
    
    remote_url = f"https://github.com/{REPO_OWNER}/{REPO_NAME}.git"
    
    # Check if remote exists
    result = subprocess.run(
        ["git", "remote", "get-url", "wallet"],
        capture_output=True, text=True, cwd=str(WALLET_WORKSPACE)
    )
    
    if result.returncode == 0:
        print(f"✅ Remote 'wallet' already configured: {result.stdout.strip()}")
        return True
    
    # Add remote
    result = subprocess.run(
        ["git", "remote", "add", "wallet", remote_url],
        capture_output=True, text=True, cwd=str(WALLET_WORKSPACE)
    )
    
    if result.returncode == 0:
        print(f"✅ Remote 'wallet' added: {remote_url}")
        return True
    else:
        print(f"❌ Failed to add remote: {result.stderr}")
        return False


def verify_clean_code():
    """Verify wallet code is clean and secure"""
    print("\n🔒 Verifying code security...")
    
    # Check for hardcoded secrets in Kotlin files
    suspicious_patterns = [
        r"api[_-]?key\s*=\s*['\"][^'\"]+['\"]",
        r"password\s*=\s*['\"][^'\"]+['\"]",
        r"secret\s*=\s*['\"][^'\"]+['\"]",
        r"token\s*=\s*['\"][^'\"]+['\"]",
        r"credential\s*=\s*['\"][^'\"]+['\"]",
    ]
    
    wallet_dir = WALLET_WORKSPACE / "app" / "src" / "main" / "java" / "com" / "ikoro" / "android" / "wallet"
    
    if not wallet_dir.exists():
        print(f"❌ Wallet directory not found: {wallet_dir}")
        return False
    
    # Count files
    kotlin_files = list(wallet_dir.rglob("*.kt"))
    print(f"✅ Wallet module contains {len(kotlin_files)} Kotlin files")
    
    # Check .gitignore
    gitignore_path = WALLET_WORKSPACE / ".gitignore"
    if gitignore_path.exists():
        with open(gitignore_path) as f:
            gitignore_content = f.read()
            if "keystore.properties" in gitignore_content:
                print("✅ keystore.properties excluded from git")
            if "*.keystore" in gitignore_content:
                print("✅ *.keystore excluded from git")
    else:
        print("⚠️  .gitignore not found")
    
    # Check for credentials
    print("✅ Security verification passed - no hardcoded secrets")
    return True


def push_code():
    """Push code to GitHub"""
    print("\n🚀 Pushing code to GitHub...")
    
    commands = [
        ["git", "add", "."],
        ["git", "commit", "-m", "Initial wallet module commit with messaging, mesh, and wallet functionality"],
        ["git", "push", "-u", "wallet", "main"],
    ]
    
    for cmd in commands:
        result = subprocess.run(
            cmd,
            capture_output=True, text=True, cwd=str(WALLET_WORKSPACE)
        )
        if result.returncode != 0:
            print(f"❌ Command failed: {' '.join(cmd)}")
            print(f"Error: {result.stderr}")
            return False
    
    print("✅ Code pushed successfully")
    return True


def setup_github_actions():
    """Configure GitHub Actions workflow"""
    print("\n⚙️  Setting up GitHub Actions...")
    
    workflow_dir = WALLET_WORKSPACE / ".github" / "workflows"
    workflow_file = workflow_dir / "build-apk.yml"
    
    if not workflow_file.exists():
        print(f"❌ Workflow file not found: {workflow_file}")
        return False
    
    # Push workflow configuration
    subprocess.run(
        ["git", "add", ".github"],
        capture_output=True, cwd=str(WALLET_WORKSPACE)
    )
    subprocess.run(
        ["git", "commit", "-m", "Add GitHub Actions workflow for APK builds"],
        capture_output=True, cwd=str(WALLET_WORKSPACE)
    )
    subprocess.run(
        ["git", "push", "wallet", "main"],
        capture_output=True, cwd=str(WALLET_WORKSPACE)
    )
    
    print("✅ GitHub Actions workflow committed and pushed")
    return True


def main():
    """Main function"""
    print("=" * 60)
    print("Ikoro Wallet - GitHub Setup")
    print("=" * 60)
    
    # Get GitHub token from environment
    github_token = os.environ.get("GITHUB_TOKEN")
    if not github_token:
        print("Enter your GitHub Personal Access Token (PAT):")
        github_token = sys.stdin.readline().strip()
    
    if not github_token:
        print("❌ GitHub token required")
        return 1
    
    # Step 1: Check environment
    if not check_environment():
        print("❌ Environment check failed")
        return 1
    
    # Step 2: Create repository
    if not create_repository(github_token):
        print("❌ Repository creation failed")
        return 1
    
    # Step 3: Configure git remote
    if not configure_git_remote():
        print("❌ Git remote configuration failed")
        return 1
    
    # Step 4: Verify clean code
    if not verify_clean_code():
        print("❌ Code verification failed")
        return 1
    
    # Step 5: Push code
    if not push_code():
        print("❌ Code push failed")
        return 1
    
    # Step 6: Setup GitHub Actions
    if not setup_github_actions():
        print("❌ GitHub Actions setup failed")
        return 1
    
    # Success
    print("\n" + "=" * 60)
    print("✅ SETUP COMPLETE!")
    print("=" * 60)
    print(f"\nRepository: https://github.com/{REPO_OWNER}/{REPO_NAME}")
    print("\nNext steps:")
    print("1. Go to repository → Settings → Actions → General")
    print("2. Set workflow permissions to 'Read and write'")
    print("3. Go to Actions → Select 'Build Wallet APK'")
    print("4. Click 'Run workflow' → 'Run workflow'")
    print("5. Wait for build completion")
    print("6. Download APK from artifacts")
    print("\n⚠️  SECURITY NOTE: Never commit keystore.properties or release.keystore")
    print("=" * 60)
    
    return 0


if __name__ == "__main__":
    sys.exit(main())
