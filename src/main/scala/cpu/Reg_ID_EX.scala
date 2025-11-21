package cpu

import chisel3._
import chisel3.util._

// This module is the pipeline register between ID and EX stage
// It holds the necessary data and control signals to be passed from ID to EX stage
// Registers:
    // reg_data1:      Data from first source register
    // reg_data2:      Data from second source register
    // reg_addr1:      Address of first source register
    // reg_addr2:      Address of second source register
    // imm:            Immediate value
    // rd_addr:        Destination register address
    // ctrl_signal:    Control signals for the EX stage
    // pc:             Current PC value
    // pc_plus4:       PC + 4 value

class Reg_ID_EX_io extends Bundle {
    val reg_data1_out   = Output(UInt(32.W))
    val reg_data2_out   = Output(UInt(32.W))
    val reg_addr1_out   = Output(UInt(5.W))
    val reg_addr2_out   = Output(UInt(5.W))
    val imm_out         = Output(UInt(32.W))
    val rd_addr_out     = Output(UInt(5.W))
    val ctrl_signal_out = Output(new CtrlSignal())
    val pc_out          = Output(UInt(32.W))
    val pc_plus4_out    = Output(UInt(32.W))
    
    val reg_data1_in    = Input(UInt(32.W))
    val reg_data2_in    = Input(UInt(32.W))
    val reg_addr1_in    = Input(UInt(5.W))
    val reg_addr2_in    = Input(UInt(5.W))
    val imm_in          = Input(UInt(32.W))
    val rd_addr_in      = Input(UInt(5.W))
    val ctrl_signal_in  = Flipped(new CtrlSignal())
    val pc_in           = Input(UInt(32.W))
    val pc_plus4_in     = Input(UInt(32.W))
}

class Reg_ID_EX extends Module {
    val io = IO(new Reg_ID_EX_io())

    io.reg_data1_out     := RegNext(io.reg_data1_in, 0.U(32.W))
    io.reg_data2_out     := RegNext(io.reg_data2_in, 0.U(32.W))
    io.reg_addr1_out     := RegNext(io.reg_addr1_in, 0.U(5.W))
    io.reg_addr2_out     := RegNext(io.reg_addr2_in, 0.U(5.W))
    io.imm_out           := RegNext(io.imm_in, 0.U(32.W))
    io.rd_addr_out       := RegNext(io.rd_addr_in, 0.U(5.W))
    io.ctrl_signal_out   := RegNext(io.ctrl_signal_in, 0.U.asTypeOf(new CtrlSignal()))
    io.pc_out            := RegNext(io.pc_in, 0.U(32.W))
    io.pc_plus4_out      := RegNext(io.pc_plus4_in, 0.U(32.W))
}
