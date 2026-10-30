# Nifty Algo - Android APK

A thin Android shell (WebView) that opens your portal URL full screen. Everything (sign-in, Paper trading,
Live tab, Shoonya login) is the same web app you already use on laptop/mobile; the APK just wraps it.

## Build with GitLab
1. Create a new GitLab project and push this folder to it (`git init && git add . && git commit -m apk && git push`).
2. GitLab > **Settings > CI/CD > Variables > Add variable**: key `APP_URL`, value `http://YOUR-SERVER-IP`
   (or `https://your-domain`). Untick "Protect variable" if you push from a non-protected branch.
3. The pipeline runs on every push (or **Build > Pipelines > Run pipeline**). When it is green:
   **Build > Jobs > build_apk > Download artifacts** -> `dist/NiftyAlgo.apk`.
4. Copy the APK to the phone, allow "Install unknown apps", install.

Changing the server address later = change `APP_URL`, run the pipeline again.

## Notes
* Plain `http://` works (cleartext is enabled). Prefer HTTPS when you can; then you may remove
  `usesCleartextTraffic` and `network_security_config.xml`.
* The APK is signed with the debug key so it installs directly. For Play Store / long-term updates, create your own
  keystore and add a `signingConfigs.release` block.
* The portal login cookie lasts 12 hours (portal setting); the APK keeps it between launches until it expires.
* Pull down to refresh, Back button goes back in the app.
