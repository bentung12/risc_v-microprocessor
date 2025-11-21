package cpu

import chisel3._
import chisel3.util._

//This module detects data hazards in the pipeline and generates control signals
// to stall the pipeline when necessary.
//Outputs:
    // pc_write:        Signal to control whether the PC should be updated
    // if_id_write:     Signal to control whether the IF/ID pipeline register should be updated
    // control_flush:   Signal to flush control signals in the ID/EX stage
//Inputs:
    // ex_memread:  Indicates if the instruction in EX stage is a load
    // ex_rd:       Destination register address in EX stage
    // mem_memread: Indicates if the instruction in MEM stage is a load (for branch/jump stall)
    // mem_rd:      Destination register address in MEM stage (for branch/jump stall)
    // id_rs1:      Source register 1 address in ID stage
    // id_rs2:      Source register 2 address in ID stage
    // opcode:      Used to detect if instruction is Branch/Load

class Hazard_Detection_io extends Bundle {
    val pc_write        = Output(Bool())
    val if_id_write     = Output(Bool())
    val control_flush   = Output(Bool())
   
    val ex_memread      = Input(Bool())
    val ex_rd           = Input(UInt(5.W))
    val mem_memread     = Input(Bool())
    val mem_rd          = Input(UInt(5.W))
    val id_rs1          = Input(UInt(5.W))
    val id_rs2          = Input(UInt(5.W))
    val opcode          = Input(UInt(7.W))
}

class Hazard_Detection extends Module {
    val io = IO(new Hazard_Detection_io())

    // Default values
    io.pc_write         := true.B
    io.if_id_write      := true.B
    io.control_flush    := false.B
    
    val uses_rs1 = WireDefault(false.B)
    val uses_rs2 = WireDefault(false.B)
    val isBranch = io.opcode === "b1100011".U
    val isJalr   = io.opcode === "b1100111".U

    switch(io.opcode) {
        is("b0110011".U,"b0100011".U,"b1100011".U) { // R/S/B-Type
            uses_rs1 := true.B
            uses_rs2 := true.B                
        }
        is("b0010011".U,"b0000011".U,"b1100111".U) { // I-Type/LW/JALR
            uses_rs1 := true.B
        }
    }

    // Calculates execution stage hazard
    val ex_hazard_rs1 = uses_rs1 && (io.ex_rd === io.id_rs1)
    val ex_hazard_rs2 = uses_rs2 && (io.ex_rd === io.id_rs2)
    val ex_hazard     = io.ex_memread && (io.ex_rd =/= 0.U) && (ex_hazard_rs1 || ex_hazard_rs2)

    // Jump/Branch memory stage hazard resolution 
    val mem_hazard_rs1 = (io.mem_rd === io.id_rs1) && (isBranch || isJalr)
    val mem_hazard_rs2 = (io.mem_rd === io.id_rs2) && isBranch
    val mem_hazard     = io.mem_memread && (io.mem_rd =/= 0.U) && (mem_hazard_rs1 || mem_hazard_rs2)

    // Stalling logic
    when (ex_hazard || mem_hazard) {
        io.pc_write         := false.B
        io.if_id_write      := false.B
        io.control_flush    := true.B
    } 
}