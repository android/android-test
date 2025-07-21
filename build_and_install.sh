#!/bin/bash

# Script to build AndroidX Test m2repository and install orchestrator APKs
# Based on CONTRIBUTING.md instructions

set -e  # Exit on any error

echo "🔨 Building AndroidX Test m2repository..."
bazelisk build :axt_m2repository

echo "📦 Unpacking m2repository to ~/.m2/"
unzip -o bazel-bin/axt_m2repository.zip -d ~/.m2/

echo "📱 Installing orchestrator and services APKs..."

# Find and install test services APK
SERVICES_APK=~/.m2/repository/androidx/test/services/test-services/1.7.0-alpha01/test-services-1.7.0-alpha01.apk
if [ -n "$SERVICES_APK" ]; then
    echo "Installing test services APK: $SERVICES_APK"
    adb install --force-queryable -r "$SERVICES_APK"
else
    echo "❌ Test services APK not found in ~/.m2/repository"
    exit 1
fi

# Find and install orchestrator APK  
ORCHESTRATOR_APK=~/.m2/repository/androidx/test/orchestrator/1.7.0-alpha01/orchestrator-1.7.0-alpha01.apk
if [ -n "$ORCHESTRATOR_APK" ]; then
    echo "Installing orchestrator APK: $ORCHESTRATOR_APK"
    adb install --force-queryable -r "$ORCHESTRATOR_APK"
else
    echo "❌ Orchestrator APK not found in ~/.m2/repository"
    exit 1
fi

echo "✅ Build and installation complete!"