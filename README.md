<h1 align="center">Bytecode CFG</h1>

## About

Bytecode CFG is a static analysis and control flow graph generation tool for Java source code. Powered by [JavaParser](https://javaparser.org/), the tool inspects Java source files (`.java`), constructs Abstract Syntax Trees (ASTs), executes configurable static analysis rules, generates structured reports (JSON and HTML), and reconstructs method-level Control Flow Graphs (CFGs) exported in Graphviz DOT format.

The primary goal is to make it easier to analyze unfamiliar Java codebases, inspect execution flow and cyclomatic complexity, enforce coding standards, and identify potential bugs such as null dereferences.

---

## Processing Pipeline

The analysis pipeline operates in sequential stages:

### Stage 1: Input Discovery
- The target path specified via `--input` is resolved.
- If a single `.java` file is provided, it is analyzed directly.
- If a directory is provided, it is scanned recursively to collect all `.java` source files.

### Stage 2: AST Parsing
- Source files are parsed into Abstract Syntax Trees using JavaParser.
- Structural elements (class declarations, methods, fields, parameters, and control statements) are extracted into an AST representation.

### Stage 3: Rule-Based Static Analysis
- The AST is evaluated against active analysis rules configured by the user or default settings.
- Violations are collected with file location, line number, and descriptive messages.

### Stage 4: Control Flow Graph (CFG) Construction
- When `--graph` is enabled, methods are traversed statement-by-statement to build basic blocks (`BasicBlock`).
- Decision points (`if`/`else`), loops (`for`, `while`, `do-while`, `for-each`), and jumps (`return`, `break`, `continue`) are modeled with directed edges and labels.
- CFGs are serialized into standard Graphviz DOT files (`.dot`) by `DotExporter`.

### Stage 5: Reporting
- Violations are aggregated and emitted to `stdout` or an output file (`--output`) in either JSON or HTML format (`--format`).

---

## Rule Set

Bytecode CFG includes static analysis rules to catch style, complexity, and reliability issues:

### Active Core Rules
- **`NamingRule`**: Enforces standard Java naming conventions (PascalCase for classes, camelCase for methods and variables, UPPER_SNAKE_CASE for static final constants).
- **`ComplexityRule`**: Computes cyclomatic complexity per method based on branching statements (`if`, loops, conditional operators) and flags methods exceeding a threshold (default: 10).
- **`NullCheckRule`**: Detects potential null dereferences when a variable is dereferenced after an explicit null assignment within local scope.

### Planned Extension Rules
- **`UnusedVariableRule`**: Detects declared local variables or method parameters that are never referenced within their enclosing scope.
- **`MagicNumberRule`**: Flags unexplained literal numeric values that appear directly in logic outside named constant declarations or loop initializers.

---

## CLI Reference

The tool is invoked via its executable shaded JAR:

```bash
java -jar target/bytecode-cfg-runner.jar [OPTIONS]
```

### Options

| Flag | Argument | Required | Description | Example |
| :--- | :--- | :--- | :--- | :--- |
| `-i`, `--input` | `<path>` | **Yes** | Path to a Java source file (`.java`) or root directory of a Java project | `--input demo/Sample.java` |
| `-o`, `--output` | `<path>` | No | File destination to save the analysis report (default: `stdout`) | `--output target/report.json` |
| `--format` | `<type>` | No | Report format: `json` (default) or `html` | `--format html` |
| `-c`, `--config` | `<path>` | No | Path to a YAML configuration file to override rule settings | `--config demo/bytecodecfg.yml` |
| `--graph` | `<dir>` | No | Output directory for Graphviz DOT files (emits one `<Class>_<method>.dot` per method) | `--graph demo/graphs` |
| `-h`, `--help` | — | No | Prints usage information and available command-line options | `--help` |

### Graph Rendering & Graphviz `dot` Requirement

The `--graph` option produces clean Graphviz DOT files (`.dot`). To render DOT files into visual image formats (e.g. PNG, SVG), the external Graphviz `dot` executable must be installed on your system:

```bash
# Convert a generated CFG to PNG
dot -Tpng demo/graphs/CfgSample_computeGrade.dot -o demo/graphs/CfgSample_computeGrade.png

# Convert a generated CFG to SVG
dot -Tsvg demo/graphs/CfgSample_collatz.dot -o demo/graphs/CfgSample_collatz.svg
```

*(Note: Direct automatic rendering via a `--render` flag requires the external Graphviz `dot` binary in the system `PATH` and is not bundled directly inside the JAR to avoid native binary dependencies).*

---

## Configuration

Analysis rules can be configured using a YAML configuration file passed via `--config <path>`.

### Schema & Example (`bytecodecfg.yml`)

```yaml
rules:
  naming:
    enabled: true
    severity: warning       # severity level (info, warning, error)
  complexity:
    enabled: true
    threshold: 5            # custom cyclomatic complexity limit (default: 10)
    severity: error
  nullCheck:
    enabled: false          # disable a rule entirely
    severity: warning
  unusedVariable:
    enabled: true
    severity: warning
  magicNumber:
    enabled: false
    severity: info
```

- **`enabled`** *(boolean)*: Toggles rule execution on or off (default: `true`).
- **`threshold`** *(integer)*: Configures numeric thresholds where applicable (e.g. `complexity`).
- **`severity`** *(string)*: Configures violation severity categorization (`info`, `warning`, `error`).
- Unrecognized or future configuration fields are ignored safely via `@JsonIgnoreProperties`.

---

## Quick Start

### 1. Build the Executable JAR

Ensure JDK 17+ and Maven are available, then compile and package:

```bash
mvn clean package
```

This produces `target/bytecode-cfg-runner.jar`.

### 2. Run Analysis (JSON Output to Console)

```bash
java -jar target/bytecode-cfg-runner.jar --input demo/Sample.java
```

Example JSON output:
```json
{
  "totalViolations" : 5,
  "violations" : [ {
    "rule" : "NamingRule",
    "file" : "Sample.java",
    "line" : 6,
    "message" : "Field name 'BadlyNamedField' should use camelCase (e.g. 'badlyNamedField')"
  }, {
    "rule" : "ComplexityRule",
    "file" : "Sample.java",
    "line" : 29,
    "message" : "Method 'complexMethod' has complexity of 11, exceeds threshold of 10"
  } ]
}
```

### 3. Generate HTML Report

```bash
java -jar target/bytecode-cfg-runner.jar --input demo/Sample.java --format html --output target/report.html
```

### 4. Generate Control Flow Graphs

```bash
java -jar target/bytecode-cfg-runner.jar --input demo/CfgSample.java --graph target/graphs
```

This writes DOT files into `target/graphs/`, for example:
- `target/graphs/CfgSample_computeGrade.dot`
- `target/graphs/CfgSample_collatz.dot`
- `target/graphs/CfgSample_sumPositives.dot`
- `target/graphs/CfgSample_countDown.dot`

---

## Known Limitations / Not Yet Supported

- **Source vs. Bytecode Analysis**: Despite the repository name, Bytecode CFG operates on Java source code (`.java`) via JavaParser ASTs, not compiled `.class` or `.jar` bytecode files.
- **Control Flow Graph Modeling Limitations**:
  - **Exception Handling**: `try`, `catch`, `finally`, and `throw` statements are not yet modeled as exceptional control flow edges.
  - **Switch Constructs**: `switch` statements and switch expressions are currently handled sequentially rather than as multi-target jump tables.
  - **Lambdas & Closures**: Lambda expressions and method references are not broken down into separate sub-CFGs.
  - **Inner & Anonymous Classes**: Method declarations inside anonymous or local classes are not traversed as part of the outer method's CFG.
- **External Graphviz Dependency**: Converting `.dot` files to visual images requires an external installation of Graphviz `dot` on the system PATH.
