package cpu

import chisel3._
import chisel3.util._

// This module calculates the next PC based on control signals
// Outputs:
    //pc:               PC
    //pcPlus4:          PC+4
    //if_flush:         Set to 1 if previous instruction needs to be flushed       
// Inputs:
    //jump_next_pc:     Jump PC Result
    //branch_next_pc:   Branch PC Result
    //to_branch:        Set to 1 if it branches
    //ctrlBranch:       Set to 1 if branch instruction
    //ctrlJump:         Set to 1 if jump instruction
    //write:            Set to 0 if stall


class PCHandle_io extends Bundle {
    val pc              = Output(UInt(32.W))
    val pcPlus4         = Output(UInt(32.W))
    val if_flush        = Output(Bool())

    val jump_next_pc    = Input(UInt(32.W))
    val branch_next_pc  = Input(UInt(32.W))
    val to_branch       = Input(Bool())
    val ctrlBranch      = Input(Bool())
    val ctrlJump        = Input(Bool())
    val write           = Input(Bool())
}

class PCHandle extends Module {
    val io = IO(new PCHandle_io())  

    val pc_reg = RegInit(4.U(32.W))
    val next_pc = WireDefault(pc_reg + 4.U)
    
    when (io.ctrlBranch && io.to_branch) {
        next_pc := io.branch_next_pc
        io.if_flush := true.B
    } .elsewhen (io.ctrlJump) {
        next_pc := io.jump_next_pc
        io.if_flush := true.B
    } .otherwise {
        io.if_flush := false.B
    }

    when (io.write) {
        pc_reg := next_pc
    }

    io.pc := pc_reg
    io.pcPlus4 := pc_reg + 4.U
}