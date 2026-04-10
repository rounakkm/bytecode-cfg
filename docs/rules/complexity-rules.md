# Complexity Rules

## What it checks
Measures the cyclomatic complexity of each method.
Cyclomatic complexity counts the number of independent
paths through a method — the higher the number, the
harder the method is to test and maintain.

## Threshold
Default maximum complexity per method: **10**

## How complexity is counted
Each of the following adds +1 to complexity:
- `if` / `else if`
- `for` / `while` / `do-while`
- `case` in a `switch`
- `catch` block
- `&&` / `||` in conditions

## Examples

### Acceptable (complexity = 3)
```java
public String classify(int n) {
    if (n > 0) return "positive";
    else if (n < 0) return "negative";
    return "zero";
}
```

### Too complex (complexity > 10)
```java
public void process(int a, int b, int c) {
    if (a > 0) {
        if (b > 0) {
            if (c > 0) { ... }
            else if (c < 0) { ... }
        } else if (b < 0) { ... }
    } else if (a < 0) {
        for (int i = 0; i < b; i++) {
            if (i % 2 == 0) { ... }
        }
    }
    // ... and so on
}
```

