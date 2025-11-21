package cpu

import chisel3._
import chisel3.util._
import cpu.OP_TYPES._

//This module calculates the ALU result based on the ALU operation and operands
//Outputs:
    // alu_result: Result of the ALU operation
    // operand1_peek: Debug output showing the actual first operand after forwarding
    // operand2_peek: Debug output showing the actual second operand after forwarding and muxing
    // src2_peek: Debug output showing the original second register value before muxing
//Inputs:
    // ctrlALUOp: ALU operation code
    // reg1: First register value
    // reg2: Second register value
    // imm: Immediate value
    // pc: Current program counter
    // ALUSrc: Control signal to select between reg2 and imm for second operand
    // forwardA: Forwarding control for first operand
    // forwardB: Forwarding control for second operand
    // wb_data: Data from WB stage for forwarding
    // mem_data: Data from MEM stage for forwarding

class Alu_io extends Bundle {
    val alu_result = Output(UInt(32.W))

    val operand1_peek   = Output(UInt(32.W))
    val operand2_peek   = Output(UInt(32.W))
    val src2_peek       = Output(UInt(32.W))

    /// TODO ///
    val ctrlALUOp       = Input(UInt(5.W))
    val reg1            = Input(UInt(32.W))
    val reg2            = Input(UInt(32.W))
    val imm             = Input(UInt(32.W))
    val pc              = Input(UInt(32.W))
    val ALUSrc          = Input(Bool())
    val forwardA        = Input(UInt(2.W))
    val forwardB        = Input(UInt(2.W))
    val wb_data         = Input(UInt(32.W))
    val mem_data        = Input(UInt(32.W))
}

class Alu extends Module {
    val io = IO(new Alu_io())

    val to_branch  = WireDefault(false.B)
    val alu_result = WireDefault(0.U(32.W))
    val jump_addr  = WireDefault(0.U(32.W))

    //default to no forwarding
    val operand1 = WireDefault(io.reg1)
    val src2     = WireDefault(io.reg2)

    switch(io.forwardA) {
        is(1.U) { operand1 := io.wb_data } // from WB
        is(2.U) { operand1 := io.mem_data } // from MEM
    }

    switch(io.forwardB) {
        is(1.U) { src2 := io.wb_data } // from WB
        is(2.U) { src2 := io.mem_data } // from MEM
    }

    //selects between reg2 and imm
    val operand2 = Mux(io.ALUSrc, io.imm, src2)

    //calculates ALU result based on ALUOp 
    switch(io.ctrlALUOp) {
        is(OP_ADD)   { alu_result := operand1 + operand2 }
        is(OP_SUB)   { alu_result := operand1 - operand2 }
        is(OP_AND)   { alu_result := operand1 & operand2 }
        is(OP_OR)    { alu_result := operand1 | operand2 }
        is(OP_XOR)   { alu_result := operand1 ^ operand2 }
        is(OP_SLT)   { alu_result := Mux(operand1.asSInt < operand2.asSInt, 1.U, 0.U) }
        is(OP_SLL)   { alu_result := operand1 << operand2(4,0) }
        is(OP_SRL)   { alu_result := operand1 >> operand2(4,0) }
        is(OP_SRA)   { alu_result := (operand1.asSInt >> operand2(4,0)).asUInt }
        is(OP_LUI)   { alu_result := operand2 }
        is(OP_AUIPC) { alu_result := io.pc + operand2 }
    }

    //assign outputs
    io.alu_result    := alu_result

    io.operand1_peek := operand1
    io.operand2_peek := operand2
    io.src2_peek     := src2
}