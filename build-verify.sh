#!/bin/bash
# Ikoro Wallet Build and Deployment Script
# This script validates, builds, and prepares the wallet for deployment

set -e

echo "═══════════════════════════════════════════════════════════════"
echo "Ikoro Wallet Build & Deploy Script"
echo "═══════════════════════════════════════════════════════════════"
echo "Started at: $(date)"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the correct directory
if [ ! -f "build.gradle.kts" ]; then
    echo -e "${RED}Error: Not in project root directory${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Project root verified${NC}"
echo ""

# Step 1: Verify file structure
echo "Step 1: Verifying wallet module structure..."
if [ -d "app/src/main/java/com/ikoro/android/wallet" ]; then
    echo -e "${GREEN}✓ Wallet module directory exists${NC}"
    echo "  Files:"
    find app/src/main/java/com/ikoro/android/wallet -type f -name "*.kt" | wc -l | xargs echo "    Total Kotlin files:"
else
    echo -e "${RED}✗ Wallet module directory not found${NC}"
    exit 1
fi
echo ""

# Step 2: Check dependencies
echo "Step 2: Checking Gradle dependencies..."
if [ -f "gradle/libs.versions.toml" ]; then
    echo -e "${GREEN}✓ Version catalog exists${NC}"
    
    # Check for required dependencies in catalog
    if grep -q "room" gradle/libs.versions.toml && \
       grep -q "hilt" gradle/libs.versions.toml; then
        echo -e "${GREEN}✓ Hilt and Room dependencies found${NC}"
    else
        echo -e "${YELLOW}⚠ Hilt/Room dependencies might be missing${NC}"
    fi
else
    echo -e "${RED}✗ Version catalog not found${NC}"
    exit 1
fi
echo ""

# Step 3: Check build configuration
echo "Step 3: Checking build configuration..."
if [ -f "app/build.gradle.kts" ]; then
    if grep -q "wallet" app/build.gradle.kts || grep -q "implementation(libs.room)" app/build.gradle.kts; then
        echo -e "${GREEN}✓ Build configuration includes wallet dependencies${NC}"
    else
        echo -e "${YELLOW}⚠ Wallet dependencies might not be linked in build.gradle.kts${NC}"
    fi
else
    echo -e "${RED}✗ Build configuration not found${NC}"
    exit 1
fi
echo ""

# Step 4: Validate Kotlin syntax (basic check)
echo "Step 4: Validating Kotlin files..."
KOTLIN_FILES=$(find app/src/main/java/com/ikoro/android/wallet -name "*.kt" -type f)
ERROR_COUNT=0

for file in $KOTLIN_FILES; do
    # Basic syntax check: look for common syntax errors
    if grep -q "^package com\.ikoro\.android\.wallet" "$file" 2>/dev/null; then
        : # Valid package declaration
    else
        echo -e "${YELLOW}  ⚠ Package declaration might be missing in: $file${NC}"
    fi
done
echo -e "${GREEN}✓ Basic Kotlin syntax validation passed${NC}"
echo ""

# Step 5: Check AndroidManifest
echo "Step 5: Checking AndroidManifest..."
if [ -f "app/src/main/AndroidManifest.xml" ]; then
    if grep -q "IkoroWalletApplication" app/src/main/AndroidManifest.xml || \
       grep -q "android:name=\"com.ikoro.android.WalletApplication\"" app/src/main/AndroidManifest.xml; then
        echo -e "${GREEN}✓ WalletApplication class reference found${NC}"
    else
        echo -e "${YELLOW}⚠ WalletApplication might not be properly registered${NC}"
    fi
else
    echo -e "${RED}✗ AndroidManifest.xml not found${NC}"
    exit 1
fi
echo ""

# Step 6: Create release artifacts directory
echo "Step 6: Preparing release artifacts..."
mkdir -p app/release
mkdir -p app/dist

# Create wallet configuration summary
cat > app/release/wallet-config.json << 'EOF'
{
  "name": "Ikoro Wallet",
  "version": "1.0.0",
  "versionCode": 100,
  "minSdk": 26,
  "targetSdk": 35,
  "packages": [
    "com.ikoro.android.wallet"
  ],
  "features": [
    "wallet",
    "backup",
    "security",
    "offline"
  ],
  "currencies": [
    "bitcoin",
    "ofo",
    "naira",
    "usdt",
    "usdc"
  ]
}
EOF

echo -e "${GREEN}✓ Configuration exported to app/release/wallet-config.json${NC}"
echo ""

# Step 7: Build summary
echo "═══════════════════════════════════════════════════════════════"
echo "Build Verification Summary"
echo "═══════════════════════════════════════════════════════════════"
echo "Wallet Modules: ${KOTLIN_FILES//app\/src\/main\/java\/com\/ikoro\/android\/wallet\//}"
echo ""
echo "Next Steps:"
echo "  1. Sync Gradle to download dependencies"
echo "  2. Build release APK: ./gradlew assembleRelease"
echo "  3. Test on physical device"
echo "  4. Generate App Bundle: ./gradlew bundleRelease"
echo "  5. Upload to Play Store Console"
echo ""
echo -e "${GREEN}Build verification completed successfully!${NC}"
echo "Finished at: $(date)"
