# Innometrics-JB-plugin

### Innometrics plugin for JetBrains products (IntelliJ IDEA, PyCharm)

**Installation**: 

`File` -> `Settings...` -> `Plugins` -> `Install plugin from disk...` -> path to the zip archive.


**Collecting measurements**:

Innometrics Activity name: `"JetBrains IDE code location"`

- "file path" - absolute file path, e.g. `/home/albert/prog/innometrics/plugin-test-project/src/ru/innopolis/university/test/Main.java`
- "code path" - code elements path, e.g. `PROJ:plugin-test-project|LANG:Java|NS:ru.innopolis.university.test|CLASS:Main|CLASS:Inner|CLASS:[ANONYMOUS]|FUNC:main|FUNC:[LAMBDA]|LINE:6`. Element labels: 
    - `PROJ` - project name
    - `LANG` - programming language name
    - `NS` - namespace, package or module name
    - `CLASS` - class name
    - `FUNC` - function or method name
    - `LINE` - line in the file
- "code begin time" - epoch time in seconds, e.g. `1511857258`
- "code end time" - epoch time in seconds, e.g. `1511857258`
- "version name" - name of the IDE application, e.g. `IntelliJ IDEA`
- "full version" - version of the IDE application, e.g. `2017.2.5`
- "company name" - vendor of the IDE application, e.g. `JetBrains s.r.o.`

Anonymous classes are denoted as `CLASS:[ANONYMOUS]`.

Lambda expressions are denoted as `FUNC:[LAMBDA]`.

**Sending measurements and Settings**:

`Tools` -> `Innometrics plugin`

**Debug messages**:
to enable debug level in the application go to `Help` -> `Debug Log Settings...` -> and add `#ru.innopolis.university.innometrics` line. The `idea.log` log file can be found in `Help` -> `Show Log in Explorer/Files`.
 