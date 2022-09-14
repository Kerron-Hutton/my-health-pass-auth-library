# My Health Pass Auth Library

[![Maven Lint and Test](https://github.com/Kerron-Hutton/my-health-pass-auth-library/actions/workflows/maven_lint_and_test.yml/badge.svg)](https://github.com/Kerron-Hutton/my-health-pass-auth-library/actions/workflows/maven_lint_and_test.yml)

Standalone package that is used by the MyHealthPass team at ZS. This package was created  
to secure the backend solution that manages health records. **Registration**, **Authentication**  
and **Authorization** are just a few core functionality that this package implements.  

## Configuring IntelliJ IDE

In order to ensure that the code quality match our organization standards, **[Checkstyle](https://checkstyle.sourceforge.io/index.html)** was used to   
automate the checking of our code base. For ease of development it is recommended that each   
developer configure IntelliJ to integrate with our custom checkstyle configuration.

1. Install the IntelliJ IDEA **[Checkstyle Plugin](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)**
    - File →  Settings →  Plugins
    - Restart IDE
   
2. Update IDEA's Code Style to obey the Checkstyle Rules
    - File → Settings → Editor → Code Style
    - Click the small gear icon next to `Scheme`, Select `Import Scheme` → CheckStyle Configuration
    - Select our checkstyle.xml
    - Click OK
    - Click Apply

3. Configure auto wrap lines at right margin
    - File → Settings → Editor → Code Style → Java → Wrapping and Braces
    - CHECK `Ensure right margin is not exceeded`
    - Click OK

