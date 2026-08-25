def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("[nullability] Configuring ErrorProne") : "Plugin should log configuration message"
// A duplicated -Xplugin:ErrorProne argument makes javac fail with "plug-in not found: ErrorProne"
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed with an ErrorProne argument declared as <compilerArg>"
