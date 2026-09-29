# Android App Inspector & Testbed Workshop Guide

Welcome to your AI App Inspector and Testbed workspace.

---

## 📁 Directory Structure

- `/incoming_app/` : Put raw AndroidIDE projects, files, or Git branches here.
- `/app/` : Active runnable project compiled and streamed live to the browser emulator.
- `/output_fixed/` : Exported, fixed, clean code ready for your phone's AndroidIDE.

---

## 🚀 Available Commands

1. **"Inspect incoming app"**
   - Scans `/incoming_app/` for manifests, dependencies, Gradle configs, and Kotlin/Java files.
   - Diagnoses bugs, missing permissions, API issues, and compile errors.

2. **"Mount and run incoming app"**
   - Moves the app into `/app/`, normalizes Gradle configurations (AGP 8.4+, Java 17, TargetSdk 34), builds the APK, and launches it in the live preview emulator.

3. **"Fix [feature/error]"**
   - Applies targeted bugfixes, refactorings, or new features to the mounted app.

4. **"Export fixed app"**
   - Copies the verified source tree into `/output_fixed/` for direct transfer to AndroidIDE.

5. **"Wipe and reset workspace"**
   - Cleans out `/incoming_app/`, `/output_fixed/`, and `/app/src/` so you can test your next app with zero cache residue.
