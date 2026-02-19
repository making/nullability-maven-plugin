def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed"

// Main source package-info.java should be generated in src/main/java
def mainPackageInfo = new File(basedir, "src/main/java/com/example/package-info.java")
assert mainPackageInfo.exists() : "package-info.java should be generated in src/main/java/com/example"
assert mainPackageInfo.text.contains("@NullMarked") : "Generated file should contain @NullMarked"
assert mainPackageInfo.text.contains("package com.example;") : "Generated file should contain correct package"

// Test source package-info.java should be generated in src/test/java
def testPackageInfo = new File(basedir, "src/test/java/com/example/package-info.java")
assert testPackageInfo.exists() : "package-info.java should be generated in src/test/java/com/example"
assert testPackageInfo.text.contains("@NullMarked") : "Generated file should contain @NullMarked"
