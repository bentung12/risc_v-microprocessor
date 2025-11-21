package cpu

import chisel3._
import chisel3.util._

//This module handles ALU operand forwarding to resolve data hazards
//Outputs:
    // forwardA: 2-bit signal to select source for ALU operand A
    // forwardB: 2-bit signal to select source for ALU operand B
        // 0: from EX stage register file output
        // 1: from MEM stage ALU result
        // 2: from WB stage ALU result or memory data
//Inputs:
    // ex_rs1: Source register 1 address in EX stage
    // ex_rs2: Source register 2 address in EX stage
    // mem_rd: Destination register address in MEM stage
    // wb_rd: Destination register address in WB stage
    // mem_regwrite: Indicates if MEM stage instruction writes to a register
    // wb_regwrite: Indicates if WB stage instruction writes to a register

class ALU_Forwarding_io extends Bundle {
    val forwardA        = Output(UInt(2.W))
    val forwardB        = Output(UInt(2.W))

    val ex_rs1          = Input(UInt(5.W))
    val ex_rs2          = Input(UInt(5.W))
    val mem_rd          = Input(UInt(5.W))
    val wb_rd           = Input(UInt(5.W))
    val mem_regwrite    = Input(Bool())
    val wb_regwrite     = Input(Bool())
}

class ALU_Forwarding extends Module {
    val io = IO(new ALU_Forwarding_io())

    // Default no forwarding
    io.forwardA := 0.U
    io.forwardB := 0.U

    /// TODO ///
    when (io.mem_regwrite && (io.mem_rd =/= 0.U) && (io.mem_rd === io.ex_rs1)) {
        io.forwardA := 2.U  
    } .elsewhen (io.wb_regwrite && (io.wb_rd =/= 0.U) && (io.wb_rd === io.ex_rs1)) {
        io.forwardA := 1.U  
    }

    when (io.mem_regwrite && (io.mem_rd =/= 0.U) && (io.mem_rd === io.ex_rs2)) {
        io.forwardB := 2.U  
    } .elsewhen (io.wb_regwrite && (io.wb_rd =/= 0.U) && (io.wb_rd === io.ex_rs2)) {
        io.forwardB := 1.U  
    }
}
