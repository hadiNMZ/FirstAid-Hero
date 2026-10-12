#!/usr/bin/env python3
import sys
import subprocess
import platform

def run_gradle_task(task_name, description):
    print(f"\n{description}")
    
    # Auto-detect OS to use the correct Gradle wrapper
    is_windows = platform.system().lower() == "windows"
    gradle_cmd = "gradlew.bat" if is_windows else "./gradlew"
    
    try:
        # Execute the Gradle task and pipe the output to the terminal
        subprocess.run([gradle_cmd, task_name], check=True)
        print(f"[OK] {task_name} passed.")
    except subprocess.CalledProcessError as e:
        print(f"[X] {task_name} failed with exit code {e.returncode}. Exiting pipeline.")
        sys.exit(e.returncode)
    except FileNotFoundError:
        print(f"[X] Could not find {gradle_cmd}. Are you in the project root?")
        sys.exit(1)

def main():
    print("========================================")
    print("Starting FirstAid Hero Test Suite...")
    print("========================================")

    # Layer 1: Isolated Unit Tests
    run_gradle_task("testDebugUnitTest", "[1/3] Running Isolated Unit Tests...")
    
    # Layer 2: Integration & UI Tests (Requires Emulator/Device)
    run_gradle_task("connectedAndroidTest", "[2/3] Running E2E & Database Integration Tests...")
    
    # Layer 3: Security & Crash Hardening Validation
    run_gradle_task("lintDebug", "[3/3] Running Lint & Code Quality Validation...")

    print("\n========================================")
    print("All pipelines clear! Zero crashes detected.")
    print("========================================")

if __name__ == "__main__":
    main()
