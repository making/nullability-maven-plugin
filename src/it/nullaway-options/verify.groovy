def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("[nullability] Configuring ErrorProne") : "Plugin should log configuration message"
assert buildLog.contains("BUILD SUCCESS") : "KnownInitializers passed via nullAwayOptions should make the build succeed"
