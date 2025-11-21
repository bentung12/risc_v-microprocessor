package cpu

import chisel3._
import chisel3.util._

// This module creates the register pipeline between IF and ID
// Registers:
    // pc:          PC        
    // pc_plus4:    PC+4  
    // flush:       Uses control signals to calculate if the next instruction should be flushed
    // inst_write:  Signal that controls whether the next instruction should write or stall

class Reg_IF_ID_io extends Bundle { 
    val pc_in           = Input(UInt(32.W))
    val pc_plus4_in     = Input(UInt(32.W))
    val inst_write_in   = Input(Bool())
    val if_flush        = Input(Bool())

    val pc_out          = Output(UInt(32.W))
    val pc_plus4_out    = Output(UInt(32.W))
    val id_flush        = Output(Bool())
    val inst_write_out  = Output(Bool())
}

class Reg_IF_ID extends Module {
    val io = IO(new Reg_IF_ID_io())

    // register the values using RegNext; next value is muxed to 0 on flush
    io.pc_out         := RegNext(Mux(io.if_flush, 0.U(32.W), io.pc_in), 0.U(32.W))
    io.pc_plus4_out   := RegNext(Mux(io.if_flush, 0.U(32.W), io.pc_plus4_in), 0.U(32.W))
    io.id_flush       := RegNext(io.if_flush, false.B)
    io.inst_write_out := RegNext(io.inst_write_in, true.B)
}