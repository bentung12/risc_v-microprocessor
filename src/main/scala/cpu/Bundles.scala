package cpu

import chisel3._

//This module is used to define Bundles and constants used across multiple modules

// collect all control signals
class CtrlSignal extends Bundle {
    val ctrlJump        = Output(Bool())
    val ctrlBranch      = Output(Bool())
    val ctrlRegWrite    = Output(Bool())
    val ctrlMemRead     = Output(Bool())
    val ctrlMemWrite    = Output(Bool())
    val ctrlALUSrc      = Output(Bool())
    val ctrlALUOp       = Output(UInt(5.W))
    val ctrlMemToReg    = Output(Bool())
}

// collect all register addresses
class RegAddr extends Bundle {
    val rs1_addr    = Output(UInt(5.W))
    val rs2_addr    = Output(UInt(5.W))
    val rd_addr     = Output(UInt(5.W))
}

// ALUOp values
object OP_TYPES { 
    val OP_NOP      = "b00000".U
    val OP_ADD      = "b00001".U
    val OP_SUB      = "b00010".U
    val OP_AND      = "b00011".U
    val OP_OR       = "b00100".U
    val OP_XOR      = "b00101".U
    val OP_SLT      = "b00110".U
    val OP_SLL      = "b00111".U
    val OP_SRL      = "b01000".U
    val OP_SRA      = "b01001".U
    val OP_BEQ      = "b01010".U
    val OP_BNE      = "b01011".U
    val OP_BLT      = "b01100".U
    val OP_BGE      = "b01101".U
    val OP_JAL      = "b01110".U
    val OP_JALR     = "b01111".U
    val OP_LUI      = "b10000".U
    val OP_AUIPC    = "b10001".U
}