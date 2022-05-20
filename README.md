# eMoflon-headless

**eMoflon headless** utilities.
Currently, there are two headless applications implemented.

## SiteXMLBuilder

Can be used to trigger the *Build All...* functionality of an Eclipse updatesite.

`./eclipse -noSplash -consoleLog -application org.emoflon.site.build.SiteXMLBuilder -data /my/path/to/workspace -project my.project.name`

## JarAntBuilderApp

Can be used to export a runnable ant configuration file to build a "fat jar" from an Eclipse workspace

`./eclipse -noSplash -consoleLog -application org.emoflon.jar.build.JarAntBuilderApp -data /my/path/to/workspace -project my.project.name -launchPath RunnerRunner.launch -jarPath myJar.jar -antPath myAnt.xml`
