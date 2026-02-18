def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("checking=TESTS") : "Plugin should log checking=TESTS"
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed"
