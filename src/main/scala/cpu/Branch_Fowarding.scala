package cpu

import chisel3._
import chisel3.util._

//This module handles forwarding for branch and jump instructions
//Outputs:
    // forwardA: 2-bit signal to select source for operand A
    // forwardB: 2-bit signal to select source for operand B
        // 0: from ID stage register file output
        // 1: from MEM stage ALU result
        // 2: from EX stage ALU result
//Inputs:
    // id_rs1: Source register 1 address in ID stage
    // id_rs2: Source register 2 address in ID stage 
    // ex_rd: Destination register address in EX stage
    // mem_rd: Destination register address in MEM stage
    // ex_regwrite: Indicates if EX stage instruction writes to a register
    // mem_regwrite: Indicates if MEM stage instruction writes to a register
    // ctrlBranch: Indicates if current instruction is a branch
    // ctrlJump: Indicates if current instruction is a jump

class Branch_Forwarding_io extends Bundle {
    val forwardA        = Output(UInt(2.W))
    val forwardB        = Output(UInt(2.W))

    val id_rs1          = Input(UInt(5.W))
    val id_rs2          = Input(UInt(5.W))
    val ex_rd           = Input(UInt(5.W))
    val mem_rd          = Input(UInt(5.W))
    val ex_regwrite     = Input(Bool())
    val mem_regwrite    = Input(Bool())
    val ctrlBranch      = Input(Bool())
    val ctrlJump        = Input(Bool())
}   

class Branch_Forwarding extends Module {
    val io = IO(new Branch_Forwarding_io())

    // Default no forwarding
    io.forwardA := 0.U
    io.forwardB := 0.U

    // Register-Data Forwarding for Branch/Jump Instructions
    when (io.ex_regwrite && (io.ex_rd =/= 0.U) && (io.ex_rd === io.id_rs1) && (io.ctrlBranch || io.ctrlJump)) {
        io.forwardA := 2.U
    } .elsewhen (io.mem_regwrite && (io.mem_rd =/= 0.U) && (io.mem_rd === io.id_rs1) && (io.ctrlBranch || io.ctrlJump)) {
        io.forwardA := 1.U
    }

    when (io.ex_regwrite && (io.ex_rd =/= 0.U) && (io.ex_rd === io.id_rs2) && io.ctrlBranch) {
        io.forwardB := 2.U
    } .elsewhen (io.mem_regwrite && (io.mem_rd =/= 0.U) && (io.mem_rd === io.id_rs2) && io.ctrlBranch) {
        io.forwardB := 1.U
    }
}
