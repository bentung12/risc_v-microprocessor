package cpu

import chisel3._
import chisel3.util._

//This module is the pipeline register between MEM and WB stage
//It holds the necessary data and control signals to be passed to the WB stage
//Registers:
    // alu_result:      Passed ALU result
    // rd_addr:         Passed destination register address
    // ctrlMemToReg:    Control signal for memory to register writeback 
    // ctrlRegWrite:    Control signal for register write enable
    // ctrlJump:        Control signal for jump instructions
    // ctrlMemRead:     Control signal for memory read
    // pc_plus4:        Passed PC + 4 value 

class Reg_MEM_WB_io extends Bundle {
    val alu_result_out      = Output(UInt(32.W))
    val rd_addr_out         = Output(UInt(5.W))
    val ctrlMemToReg_out    = Output(Bool())
    val ctrlRegWrite_out    = Output(Bool())
    val ctrlJump_out        = Output(Bool())
    val ctrlMemRead_out     = Output(Bool())
    val pc_plus4_out        = Output(UInt(32.W))

    val alu_result_in       = Input(UInt(32.W))
    val rd_addr_in          = Input(UInt(5.W))
    val ctrlRegWrite_in     = Input(Bool())
    val ctrlMemToReg_in     = Input(Bool())
    val ctrlJump_in         = Input(Bool())
    val ctrlMemRead_in      = Input(Bool())
    val pc_plus4_in         = Input(UInt(32.W))
}

class Reg_MEM_WB extends Module {
    val io = IO(new Reg_MEM_WB_io())

    // drive outputs directly from RegNext
    io.alu_result_out   := RegNext(io.alu_result_in, 0.U(32.W))
    io.rd_addr_out      := RegNext(io.rd_addr_in, 0.U(5.W))
    io.ctrlRegWrite_out := RegNext(io.ctrlRegWrite_in, false.B)
    io.ctrlMemToReg_out := RegNext(io.ctrlMemToReg_in, false.B)
    io.ctrlJump_out     := RegNext(io.ctrlJump_in, false.B)
    io.ctrlMemRead_out  := RegNext(io.ctrlMemRead_in, false.B)
    io.pc_plus4_out     := RegNext(io.pc_plus4_in, 0.U(32.W))
}


