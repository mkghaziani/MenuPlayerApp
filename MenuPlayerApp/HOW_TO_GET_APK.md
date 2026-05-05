# 🚀 How to Get Your APK in 5 Minutes

## Prerequisites
- A free GitHub account → https://github.com/signup

---

## Step 1 — Create a new GitHub repository

1. Go to https://github.com/new
2. Repository name: `MenuPlayerApp`
3. Set to **Private** (recommended)
4. Click **Create repository**

---

## Step 2 — Upload the project files

### Option A: Upload via GitHub website (no Git needed)

1. On your new empty repo page, click **"uploading an existing file"**
2. Extract the ZIP you downloaded
3. Drag ALL files and folders into the upload area
   > ⚠️ Important: Make sure `.github/workflows/build-apk.yml` is included!
4. Click **Commit changes**

### Option B: Upload via Git (if you have Git installed)

```bash
cd MenuPlayerApp          # extracted zip folder
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/MenuPlayerApp.git
git push -u origin main
```

---

## Step 3 — Watch it build

1. Go to your repo on GitHub
2. Click the **"Actions"** tab at the top
3. You'll see **"Build MenuPlayer APK"** running (yellow circle)
4. Wait ~5-8 minutes for it to finish (green ✅)

---

## Step 4 — Download your APK

1. Click on the completed workflow run
2. Scroll down to **"Artifacts"** section
3. Click **"MenuPlayer-debug-APK"** → downloads a ZIP
4. Extract the ZIP → you get `app-debug.apk`

---

## Step 5 — Install on your Android TV

### Via ADB (recommended for TV):
```bash
adb connect YOUR_TV_IP:5555
adb install app-debug.apk
```

### Via USB drive:
- Copy APK to USB → plug into TV → use file manager to install
- Enable "Install from unknown sources" in TV settings first

---

## ✅ App is pre-configured with:
- Server: https://banquet.thesmarterp.com/
- Screen ID: screen001
- Auto-starts on boot
- Goes straight to player (no setup screen needed)

## 🔑 Access hidden settings on TV:
Press on remote: **↑ ↑ ↓ ↓ ENTER** → enter PIN `1234`
