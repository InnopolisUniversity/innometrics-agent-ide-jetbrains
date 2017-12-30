# Tests Configuration


## JUnit configuration

- VM options: `-ea -Xbootclasspath/p:./out/classes/production/boot -XX:+HeapDumpOnOutOfMemoryError -Xmx512m -XX:MaxPermSize=320m -Didea.system.path=./test-system -Didea.home.path=./ -Didea.config.path=./test-config -Didea.test.group=ALL_EXCLUDE_DEFINED`
- Working directory: path to the source root of IntelliJ IDEA Community Edition

Set path to the `testData` directory in `resourcesTest/plugin.properties` properties file (`innometrics.jb_plugin.testData` property - relative path from "Working directory").
