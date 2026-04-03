<h1 align="center">Bytecode CFG</h1>

## About

Bytecode CFG is a Java-based static analysis tool that reads compiled Java programs (`.jar` or `.class`) and reconstructs their structure. The tool extracts classes, methods, fields, inheritance, and relationships, and builds control flow graphs (CFGs), dependency graphs, and module structures.

The main goal is to make it easier to explore unfamiliar codebases, understand program flow, and identify highly connected or complex classes. This is useful for learning, analyzing legacy systems, or studying software architecture.

---

## Pipeline

The project works as a multi-stage pipeline that gradually reconstructs program structure and visualizes it:

### Stage 1: Input Submission

- User provides compiled Java files (`.jar` or `.class`).
- JAR files are unpacked, and all contained class files are identified for processing.

### Stage 2: Bytecode Parsing

- Each class file is read using a bytecode library (ASM).
- Extracted information includes:
    - Class names and definitions
    - Method names, signatures, and instructions
    - Fields and data types
    - Inheritance and implemented interfaces
- Key bytecode instructions analyzed: `invokevirtual`, `invokestatic`, `invokespecial`, `invokeinterface`, `new`, `getfield`, `putfield`.

### Stage 3: Structure Extraction

- Relationships between classes and methods are determined:
    - Method calls
    - Field accesses
    - Object creation
- Internal models are created to represent:
    - `ClassNode` – for each class
    - `MethodNode` – for each method
    - `DependencyEdge` – relationships between nodes

### Stage 4: Graph Construction

- Two main graphs are generated:
    1. **Class Dependency Graph**: Nodes represent classes; edges represent class-level dependencies.
    2. **Control Flow / Method Call Graph**: Nodes represent methods; edges represent method invocation relationships.

- Graphs are built using JGraphT to allow algorithmic analysis.

### Stage 5: Graph Analysis

- Metrics are computed to highlight important components:
    - **Node Degree**: identifies highly connected classes
    - **Centrality**: highlights critical classes or methods
    - **Coupling Metrics**: identifies classes that are tightly linked or complex
- Graph analysis helps in understanding the internal structure and identifying potential problem areas.

### Stage 6: Module Detection

- Clusters of closely connected classes are detected using graph clustering techniques.
- Logical modules represent cohesive sections of the program (e.g., authentication, payment, data access).
- Layered architecture is inferred from dependency directions.

### Stage 7: Visualization & Reporting

- Graphs and diagrams are generated for easier inspection:
    - Class dependency graphs
    - Method call / CFG graphs
    - Module diagrams showing clusters
- Reports summarize complexity, coupling, and central classes.

This pipeline ensures a stepwise, clear reconstruction of program structure from raw bytecode.

---

## Features

- Bytecode analysis of compiled Java programs
- Extraction of classes, methods, fields, and inheritance relationships
- Generation of class dependency graphs
- Generation of method call / control flow graphs
- Detection of logical modules and architecture layers
- Analysis of class complexity and coupling
- Visual reports for program structure and flow

---

## Expected Output

For a given Java program, Bytecode CFG produces:

- Class dependency graphs showing relationships between classes
- Method call / CFG graphs showing execution flow between methods
- Module diagrams identifying clusters of related classes
- Complexity and coupling reports highlighting key classes
