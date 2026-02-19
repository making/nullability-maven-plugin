def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("[nullability] Generated @NullMarked package-info.java for:") : "Plugin should log generation message"
assert buildLog.contains("com.example") : "Should generate for com.example"
assert buildLog.contains("com.example.sub") : "Should generate for com.example.sub"
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed"

def generatedDir = new File(basedir, "target/generated-sources/nullability")
def comExamplePackageInfo = new File(generatedDir, "com/example/package-info.java")
assert comExamplePackageInfo.exists() : "package-info.java should be generated for com.example"
assert comExamplePackageInfo.text.contains("@NullMarked") : "Generated file should contain @NullMarked"
assert comExamplePackageInfo.text.contains("package com.example;") : "Generated file should contain correct package"

def subPackageInfo = new File(generatedDir, "com/example/sub/package-info.java")
assert subPackageInfo.exists() : "package-info.java should be generated for com.example.sub"
assert subPackageInfo.text.contains("package com.example.sub;") : "Generated file should contain correct package"
