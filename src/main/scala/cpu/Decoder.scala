package cpu

import chisel3._
import chisel3.util._
import cpu.OP_TYPES._

/// This module decodes the instruction opcode and funct fields
/// to generate appropriate control signals for the CPU datapath.
/// Outputs:
    // ctrl_signal: Bundle containing all control signals
/// Inputs:
    // opcode: 7-bit opcode field from instruction
    // funct3: 3-bit funct3 field from instruction
    // funct7: 7-bit funct7 field from instruction
    // flush: Signal to indicate if the current instruction should be flushed

class Decoder_io extends Bundle {
    val ctrl_signal = Output(new CtrlSignal())

    val opcode      = Input(UInt(7.W))
    val funct3      = Input(UInt(3.W))
    val funct7      = Input(UInt(7.W))
    val flush       = Input(Bool()) 
}

class Decoder extends Module {
    val io = IO(new Decoder_io())

    // set safe defaults to avoid inferred latches
    io.ctrl_signal.ctrlALUSrc   := false.B
    io.ctrl_signal.ctrlMemToReg := false.B
    io.ctrl_signal.ctrlRegWrite := false.B
    io.ctrl_signal.ctrlMemRead  := false.B
    io.ctrl_signal.ctrlMemWrite := false.B
    io.ctrl_signal.ctrlBranch   := false.B
    io.ctrl_signal.ctrlJump     := false.B
    io.ctrl_signal.ctrlALUOp    := OP_NOP

    // decode by opcode; nested switches for subfields

    when (io.flush === false.B) {
        switch(io.opcode) {
            is("b0110011".U) { // R-type
                io.ctrl_signal.ctrlRegWrite := true.B

                switch(io.funct7) {
                    is("b0000000".U) {
                        switch(io.funct3) {
                            is("b000".U) { io.ctrl_signal.ctrlALUOp := OP_ADD }
                            is("b111".U) { io.ctrl_signal.ctrlALUOp := OP_AND }
                            is("b110".U) { io.ctrl_signal.ctrlALUOp := OP_OR }
                            is("b100".U) { io.ctrl_signal.ctrlALUOp := OP_XOR }
                            is("b010".U) { io.ctrl_signal.ctrlALUOp := OP_SLT }
                            is("b001".U) { io.ctrl_signal.ctrlALUOp := OP_SLL }
                            is("b101".U) { io.ctrl_signal.ctrlALUOp := OP_SRL }
                        }
                    }
                    is("b0100000".U) {
                        switch(io.funct3) {
                            is("b000".U) { io.ctrl_signal.ctrlALUOp := OP_SUB }
                            is("b101".U) { io.ctrl_signal.ctrlALUOp := OP_SRA }
                        }
                    }
                }
    }

            is("b0010011".U) { // I-type (ALU imm)
                io.ctrl_signal.ctrlALUSrc := true.B
                io.ctrl_signal.ctrlRegWrite := true.B

                switch(io.funct3) {
                    is("b000".U) { io.ctrl_signal.ctrlALUOp := OP_ADD }
                    is("b010".U) { io.ctrl_signal.ctrlALUOp := OP_SLT }
                    is("b001".U) { io.ctrl_signal.ctrlALUOp := OP_SLL }
                    is("b100".U) { io.ctrl_signal.ctrlALUOp := OP_XOR }
                    is("b110".U) { io.ctrl_signal.ctrlALUOp := OP_OR }
                    is("b111".U) { io.ctrl_signal.ctrlALUOp := OP_AND }
                    // SRL/SRA use funct7 to distinguish; handled conservatively as SRL here
                    is("b101".U) { 
                        when(io.funct7 === "b0000000".U) { io.ctrl_signal.ctrlALUOp := OP_SRL }
                        .elsewhen(io.funct7 === "b0100000".U) { io.ctrl_signal.ctrlALUOp := OP_SRA }
                    }
                }
            }

            is("b0000011".U) { // Load
                io.ctrl_signal.ctrlALUSrc   := true.B
                io.ctrl_signal.ctrlMemToReg := true.B
                io.ctrl_signal.ctrlRegWrite := true.B
                io.ctrl_signal.ctrlMemRead  := true.B
                io.ctrl_signal.ctrlALUOp    := OP_ADD
            }

            is("b0100011".U) { // Store
                io.ctrl_signal.ctrlALUSrc   := true.B
                io.ctrl_signal.ctrlMemWrite := true.B
                io.ctrl_signal.ctrlALUOp    := OP_ADD
            }

            is("b1100011".U) { // Branches
                io.ctrl_signal.ctrlBranch := true.B
                // use funct3 to pick branch type
                switch(io.funct3) {
                    is("b000".U) { io.ctrl_signal.ctrlALUOp := OP_BEQ }
                    is("b001".U) { io.ctrl_signal.ctrlALUOp := OP_BNE }
                    is("b100".U) { io.ctrl_signal.ctrlALUOp := OP_BLT }
                    is("b101".U) { io.ctrl_signal.ctrlALUOp := OP_BGE }
                }
            }

            is("b1101111".U) { // JAL
                io.ctrl_signal.ctrlALUSrc   := true.B
                io.ctrl_signal.ctrlJump     := true.B
                io.ctrl_signal.ctrlRegWrite := true.B
                io.ctrl_signal.ctrlALUOp    := OP_JAL
            }

            is("b1100111".U) { // JALR
                io.ctrl_signal.ctrlALUSrc   := true.B
                io.ctrl_signal.ctrlJump     := true.B
                io.ctrl_signal.ctrlRegWrite := true.B
                io.ctrl_signal.ctrlALUOp    := OP_JALR
            }

            is("b0110111".U) { // LUI
                io.ctrl_signal.ctrlALUSrc := true.B
                io.ctrl_signal.ctrlRegWrite := true.B
                io.ctrl_signal.ctrlALUOp := OP_LUI
            }

            is("b0010111".U) { // AUIPC
                io.ctrl_signal.ctrlALUSrc := true.B
                io.ctrl_signal.ctrlRegWrite := true.B
                io.ctrl_signal.ctrlALUOp := OP_AUIPC
            }

            // otherwise: keep defaults (NOP)
        }
    }
}