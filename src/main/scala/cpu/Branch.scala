package cpu

import chisel3._
import chisel3.util._
import cpu.OP_TYPES._

// This module computes jump and branch target addresses and determines branch taken status
// Outputs:
    // jump_next_pc: Target address for jump instructions
    // branch_next_pc: Target address for branch instructions
    // to_branch: Signal indicating whether a branch is taken
    // reg1_peek: Debug output showing the actual first register value after forwarding
    // reg2_peek: Debug output showing the actual second register value after forwarding
// Inputs:
    // reg1_value: First register value from register file
    // reg2_value: Second register value from register file
    // imm_value: Immediate value for calculating target addresses
    // pc_id: Current program counter
    // ctrlALUOP: ALU operation code indicating instruction type
    // forwardA: Forwarding control for first register
    // forwardB: Forwarding control for second register
    // alu_result_ex: ALU result from EX stage for forwarding
    // alu_result_mem: ALU result from MEM stage for forwarding 

class Branch_io extends Bundle {
    val jump_next_pc    = Output(UInt(32.W))
    val branch_next_pc  = Output(UInt(32.W))
    val to_branch       = Output(Bool())
    val reg1_peek       = Output(UInt(32.W)) 
    val reg2_peek       = Output(UInt(32.W)) 

    val reg1_value      = Input(UInt(32.W))  
    val reg2_value      = Input(UInt(32.W))
    val imm_value       = Input(UInt(32.W))
    val pc_id           = Input(UInt(32.W))
    val ctrlALUOP       = Input(UInt(5.W))
    val forwardA        = Input(UInt(2.W))
    val forwardB        = Input(UInt(2.W))
    val alu_result_ex   = Input(UInt(32.W))
    val alu_result_mem  = Input(UInt(32.W))  
}

class Branch extends Module {
    val io = IO(new Branch_io())

    val to_branch       = WireDefault(false.B)
    val jump_next_pc    = WireDefault(0.U(32.W))

    // Default to no forwarding
    val reg1 = WireDefault(io.reg1_value)
    val reg2 = WireDefault(io.reg2_value)

    switch(io.forwardA) {
        is(1.U) { reg1 := io.alu_result_mem } // from MEM
        is(2.U) { reg1 := io.alu_result_ex } // from EX
    }

    switch(io.forwardB) {
        is(1.U) { reg2 := io.alu_result_mem } // from MEM
        is(2.U) { reg2 := io.alu_result_ex } // from EX
    }

    //Calculates jump and branch target addresses and branch taken status
    switch(io.ctrlALUOP) {
        is(OP_JAL)  { jump_next_pc := io.pc_id + io.imm_value }
        is(OP_JALR) { jump_next_pc := (reg1 + io.imm_value) & (~1.U(32.W))}
        is(OP_BEQ)  { to_branch := (reg1 === reg2) }
        is(OP_BNE)  { to_branch := (reg1 =/= reg2) }
        is(OP_BLT)  { to_branch := (reg1.asSInt < reg2.asSInt) }
        is(OP_BGE)  { to_branch := (reg1.asSInt >= reg2.asSInt) }
    }

    io.branch_next_pc   := io.pc_id + io.imm_value
    io.jump_next_pc     := jump_next_pc 
    io.to_branch        := to_branch

    io.reg1_peek        := reg1
    io.reg2_peek        := reg2
}

