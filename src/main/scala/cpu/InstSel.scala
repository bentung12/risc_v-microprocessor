package cpu

import chisel3._
import chisel3.util._

// This module decides what the next instruction is based on the flush/write signals
// Outputs:
    //inst_out:     Instruction output
//Inputs:
    //inst_in:      Default next instruction
    //inst_write:   Set to 1 to go to next instruction
    //id_flush:     Set to 1 to flush next instruction

class InstSel_io extends Bundle {
    val inst_out    = Output(UInt(32.W))

    val inst_in     = Input(UInt(32.W))
    val inst_write  = Input(Bool())
    val id_flush    = Input(Bool())
}

class InstSel extends Module {
    val io = IO(new InstSel_io())

    val prev_inst_reg = RegInit(0.U(32.W))

    when (io.id_flush) {
        io.inst_out := 0.U
    } .elsewhen(io.inst_write) { //default case, stores prev instruction in register
        io.inst_out := io.inst_in
        prev_inst_reg := io.inst_in
    } .otherwise {               //uses previous instruction as current instruction
        io.inst_out := prev_inst_reg
    }
}