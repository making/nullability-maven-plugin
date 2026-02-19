def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("RequireExplicitNullMarking") : "Build should fail due to RequireExplicitNullMarking"
