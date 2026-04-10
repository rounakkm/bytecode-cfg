# Naming Rules

## What it checks
- Class names must start with an uppercase letter (PascalCase)
- Method names must start with a lowercase letter (camelCase)
- Variable names must start with a lowercase letter (camelCase)
- Constants must be fully uppercase with underscores (UPPER_SNAKE_CASE)

## Examples

### Valid
```java
public class MyAnalyzer { }
public void parseFile() { }
int lineCount = 0;
static final int MAX_DEPTH = 5;
```

### Invalid
```java
public class myAnalyzer { }     // class should be PascalCase
public void ParseFile() { }     // method should be camelCase
int LineCount = 0;              // variable should be camelCase
```
