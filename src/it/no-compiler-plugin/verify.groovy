def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("[nullability] Configuring ErrorProne") : "Plugin should log configuration message"
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed even without explicit compiler plugin declaration"
