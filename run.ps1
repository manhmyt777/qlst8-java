param([ValidateSet('run','test','build')][string]$Task='run')
$ErrorActionPreference='Stop'
Set-Location -LiteralPath $PSScriptRoot
$taskMavenVersion='3.9.11'
$taskMavenDir="tools/apache-maven-$taskMavenVersion"
if (-not (Test-Path "$taskMavenDir/boot/plexus-classworlds-2.9.0.jar")) {
 New-Item -ItemType Directory -Force tools | Out-Null
 $taskDownload="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$taskMavenVersion/apache-maven-$taskMavenVersion-bin.zip"
 Invoke-WebRequest -UseBasicParsing $taskDownload -OutFile tools/maven.zip
 $taskExpected=((Invoke-WebRequest -UseBasicParsing "$taskDownload.sha512").Content.Trim() -split '\s+')[0]
 if ((Get-FileHash tools/maven.zip -Algorithm SHA512).Hash -ne $taskExpected) {throw 'Maven checksum mismatch'}
 Expand-Archive -LiteralPath tools/maven.zip -DestinationPath tools -Force
}
[string[]]$taskGoals=@(switch($Task){'run'{'compile';'exec:java'} 'test'{'test'} 'build'{'package'}})
& java "-Dmaven.home=$taskMavenDir" "-Dclassworlds.conf=$taskMavenDir/bin/m2.conf" '-Dmaven.multiModuleProjectDirectory=.' -cp "$taskMavenDir/boot/plexus-classworlds-2.9.0.jar" org.codehaus.plexus.classworlds.launcher.Launcher '-Dmaven.repo.local=tools/repository' @taskGoals
exit $LASTEXITCODE
