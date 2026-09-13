# Pipelined RISC-V CPU in Chisel

32-bit RV32I core with a five-stage pipeline (IF → ID → EX → MEM → WB), full forwarding, and hazard resolution.

**Stack:** Chisel · Scala · sbt · JDK 17
**Context:** Computer Architecture, National Taiwan University

## Highlights

- **Branches resolve in ID, not EX.** Costs a dedicated comparator; cuts the mispredict penalty from 2 bubbles to 1. Biggest performance lever available without a predictor.
- That decision forces a **second forwarding network** feeding the branch comparator, separate from the ALU's.
- It also forces **an earlier stall case**: a load feeding a branch must stall one stage sooner than a load feeding an ALU op. 
- Forwarding first, stalling only when unavoidable — the single load-use case where the result isn't ready until MEM.
- Verified against 5 golden-reference test patterns. `PIPELINE=0` runs the same patterns single-cycle, isolating datapath bugs from hazard-logic bugs.

## Instructions

| Type | Implemented |
|---|---|
| R-type | `add` `sub` `and` `or` `xor` `slt` `sll` `srl` `sra` |
| I-type | `addi` `slti` `slli` `srli` `srai` |
| Memory | `lw` `sw` |
| Branch | `beq` `bne` `blt` `bge` |
| Jump | `jal` `jalr` |
| U-type | `lui` `auipc` |

## Structure

| Module | Purpose |
|---|---|
| `Core.scala` | Top level, stage instantiation and wiring |
| `Decoder.scala` | Control signal generation |
| `ImmGen.scala` | Immediate extraction, all RV32I formats |
| `Regfile.scala` | 32 × 32-bit, `x0` hardwired to zero |
| `Alu.scala` / `Alu_Forwarding.scala` | ALU + its forwarding muxes |
| `Branch.scala` / `Branch_Fowarding.scala` | ID-stage compare, target calc, forwarding |
| `Hazard_Detection.scala` | Load-use and branch-load stall detection |
| `InstSel.scala` / `PCHandle.scala` | NOP injection on flush; next-PC select |
| `Reg_IF_ID` … `Reg_MEM_WB` | Pipeline registers, one per stage boundary |
| `InstMem.scala` / `DataMem.scala` | *Provided by the course, unmodified* |

## Build & test

```bash
PIPELINE=1 INST_FILE=pattern/p1.hex GOLDEN_FILE=pattern/p1_golden.hex sbt test
```

Patterns `p1`–`p5` in `src/test/pattern/`, each with a golden register-file reference.

## Limitations

- No branch prediction. Every taken branch costs one bubble.