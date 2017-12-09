# Innometrics-JB-plugin

### Innometrics plugin for JetBrains products (IntelliJ IDEA, PyCharm)

Innometrics Activity name: `"JetBrains IDE code location"`

Collecting measurements:

- "file path" - absolute file path, e.g. `/home/albert/prog/innometrics/plugin-test-project/src/ru/innopolis/university/test/Main.java`
- "code path" - code elements path, e.g. `PROJ:plugin-test-project|LANG:Java|NS:ru.innopolis.university.test|CLASS:Main|CLASS:Inner|FUNC:main|LINE:6`. Element labels: 
    - `PROJ` - project name
    - `LANG` - programming language name
    - `NS` - namespace, package or module name
    - `CLASS` - class name
    - `FUNC` - function or method name
    - `LINE` - line in the file
- "code time" - UTC timestamp in ms, e.g. `1511857258858`
- "version name" - name of the IDE application, e.g. `IntelliJ IDEA`
- "full version" - version of the IDE application, e.g. `2017.2.5`
- "company name" - vendor of the IDE application, e.g. `JetBrains s.r.o.`

