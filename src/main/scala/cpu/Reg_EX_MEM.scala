package cpu

import chisel3._
import chisel3.util._

//This module is the pipeline register between EX and MEM stage
//It holds the necessary data and control signals to be passed to the MEM stage
//Registers:
    // reg_data2:       Passed data from second source register
    // alu_result:      Passed ALU result
    // rd_addr:         Passed destination register address
    // ctrlMemRead:     Control signal for memory read
    // ctrlMemWrite:    Control signal for memory write
    // ctrlMemToReg:    Control signal for memory to register writeback
    // ctrlRegWrite:    Control signal for register write enable
    // ctrlJump_out:    Control signal for jump instructions
    // pc_plus4_out:    Passed PC + 4 value

class Reg_EX_MEM_io extends Bundle {
    val reg_data2_out       = Output(UInt(32.W))
    val pc_plus4_out        = Output(UInt(32.W))
    val alu_result_out      = Output(UInt(32.W))
    val rd_addr_out         = Output(UInt(5.W))

    val ctrlMemRead_out     = Output(Bool())
    val ctrlMemWrite_out    = Output(Bool())
    val ctrlMemToReg_out    = Output(Bool())
    val ctrlRegWrite_out    = Output(Bool())
    val ctrlJump_out        = Output(Bool())

    val reg_data2_in        = Input(UInt(32.W))
    val alu_result_in       = Input(UInt(32.W))
    val rd_addr_in          = Input(UInt(5.W))
    val ctrl_signal_in      = Flipped(new CtrlSignal())
    val pc_plus4_in         = Input(UInt(32.W))
}

class Reg_EX_MEM extends Module {
    val io = IO(new Reg_EX_MEM_io())

    val ctrl_signal_reg = RegNext(io.ctrl_signal_in, 0.U.asTypeOf(new CtrlSignal()))

    io.reg_data2_out    := RegNext(io.reg_data2_in, 0.U(32.W))
    io.alu_result_out   := RegNext(io.alu_result_in, 0.U(32.W))
    io.rd_addr_out      := RegNext(io.rd_addr_in, 0.U(5.W))
    io.pc_plus4_out     := RegNext(io.pc_plus4_in, 0.U(32.W))

    io.ctrlMemRead_out  := ctrl_signal_reg.ctrlMemRead
    io.ctrlMemWrite_out := ctrl_signal_reg.ctrlMemWrite
    io.ctrlMemToReg_out := ctrl_signal_reg.ctrlMemToReg
    io.ctrlRegWrite_out := ctrl_signal_reg.ctrlRegWrite
    io.ctrlJump_out     := ctrl_signal_reg.ctrlJump
}

