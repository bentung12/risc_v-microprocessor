# RISC_V-Microprocessor
A five stage pipeline processor that implements the RISC-V 32 bit instruction set, complete with data hazard and control hazard resolution.

# Chisel RISC-V CPU (5-Stage Pipeline)

This repository contains an implementation of a RISC-V CPU.
The core is written in **Chisel** (Scala-based hardware construction language) and implements a classic **five-stage pipelined** datapath:

> IF → ID → EX → MEM → WB

The design supports the RV32I subset required by the assignment and handles common data and control hazards with forwarding and stalling logic.

---

## Features

### Architecture

- 32-bit RISC-V CPU
- 5-stage pipeline (IF/ID/EX/MEM/WB)
- Separate instruction and data memories
- Branch and jump **resolved in ID stage** to reduce branch penalty
- Forwarding and hazard detection for:
  - ALU dependencies (data forwarding)
  - Load-use hazards
  - Branch dependencies (branch forwarding)

### Supported Instruction Types

A subset of the most common RV32I instructions are implemented:

- **Arithmetic (R-type):** `add, sub, and, or, xor, slt, sll, srl, sra`
- **Immediate (I-type):** `addi, slti, slli, srli, srai`
- **Load/Store:** `lw, sw`
- **Branch:** `beq, bne, blt, bge`
- **Jump:** `jal, jalr`
- **U-type:** `lui, auipc`

### Hazard Handling

- **Data hazards**
  - ALU result forwarding from EX and MEM
  - Forwarding into branch comparator in ID
- **Load-use hazards**
  - One-cycle stall when a branch or ALU op depends on a just-loaded value
- **Control hazards**
  - Branch/jump target computation in ID
  - Flush of IF/ID on taken branch/jump

---

## Project Structure

```text
.
├── build.sbt
├── project/                 # sbt configuration (build.properties, etc.)
└── src/
    ├── main/
    │   └── scala/
    │       └── cpu/
    │           ├── Core.scala            # Top-level CPU core
    │           ├── Alu.scala             # ALU implementation
    │           ├── Alu_Forwarding.scala  # EX/MEM forwarding to ALU operands
    │           ├── Branch.scala          # Branch/jump target + compare (ID stage)
    │           ├── Branch_Fowarding.scala# Forwarding control for branches
    │           ├── Bundles.scala         # Common IO bundles / type defs
    │           ├── DataMem.scala         # Data memory (provided, unmodified)
    │           ├── Decoder.scala         # Main control / decode logic
    │           ├── Hazard_Detection.scala# Load-use + branch hazard detection
    │           ├── ImmGen.scala          # Immediate generator for all formats
    │           ├── InstMem.scala         # Instruction memory (provided)
    │           ├── InstSel.scala         # Instruction select / NOP insertion
    │           ├── PCHandle.scala        # PC update and next-PC selection
    │           ├── Reg_IF_ID.scala       # IF/ID pipeline register
    │           ├── Reg_ID_EX.scala       # ID/EX pipeline register
    │           ├── Reg_EX_MEM.scala      # EX/MEM pipeline register
    │           ├── Reg_MEM_WB.scala      # MEM/WB pipeline register
    │           ├── Regfile.scala         # 32 x 32-bit register file (x0 hard-wired to 0)
    │           └── Writeback.scala       # Final write-back muxing
    └── test/
        └── scala/
            └── cpu/
                └── CoreTest.scala        # Testbench (provided + my tests)
````
---

## Building and Running

### Prerequisites

* **JDK 17**
* **sbt** (Scala Build Tool)

Check your installation:

```bash
java -version
sbt --version
```

### Running the Public Tests

From the project root:

```bash
# Example: run p1 with pipeline enabled
PIPELINE=1 INST_FILE=pattern/p1.hex GOLDEN_FILE=pattern/p1_golden.hex sbt test
```

You can change `INST_FILE` / `GOLDEN_FILE` to any of the other provided patterns (`p2.hex`, `p3.hex`, etc.).

---

## Implementation Notes

* Branches are resolved in **ID** using the `Branch` module, which:

  * Applies forwarding from EX/MEM stage ALU results
  * Compares the correct, forwarded register values
  * Produces `to_branch`, `branch_next_pc`, and `jump_next_pc`

* `Hazard_Detection` inserts a **single bubble** on load-use hazards and manages `pc_write`, `if_id_write`, and control flush signals.

* `Alu_Forwarding` and `Branch_Fowarding` generate the forwarding select signals used by the ALU and branch comparator, respectively.

* Pipeline registers (`Reg_IF_ID`, `Reg_ID_EX`, `Reg_EX_MEM`, `Reg_MEM_WB`) carry both data and control signals between stages and implement flush behavior when required.

---
