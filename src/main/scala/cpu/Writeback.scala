package cpu

import chisel3._
import chisel3.util._

//This module handles the writeback stage of the CPU pipeline
//It selects the correct data to write back to the register file based on control signals
//Outputs:
    // data_write: Data to be written back to the register file
//Inputs:
    // data_ALU: Data from the ALU
    // data_mem: Data read from memory
    // pc_plus4: PC + 4 value for jump instructions
    // ctrlMemToReg: Control signal indicating if data from memory should be written back
    // ctrlJump: Control signal indicating if a jump instruction is being executed

class Writeback_io extends Bundle {
    val data_write      = Output(UInt(32.W))

    val data_ALU        = Input(UInt(32.W))
    val data_mem        = Input(UInt(32.W))
    val pc_plus4        = Input(UInt(32.W))
    val ctrlMemToReg    = Input(Bool())
    val ctrlJump        = Input(Bool())
}

class Writeback extends Module {
    val io = IO(new Writeback_io())

    when (io.ctrlMemToReg) {
        io.data_write := io.data_mem
    } .elsewhen (io.ctrlJump) {
        io.data_write := io.pc_plus4
    } .otherwise {
        io.data_write := io.data_ALU
    }
}
