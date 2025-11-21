package cpu

import chisel3._
import chisel3.util._

// This module generates the immediate value based on instruction type
// Outputs:
    // imm: 32-bit immediate value
// Inputs:
    // inst: 32-bit instruction

class ImmGen_io extends Bundle {
    val imm = Output(UInt(32.W))
    val inst = Input(UInt(32.W))
}

class ImmGen extends Module {
    val io = IO(new ImmGen_io())

    val opcode = io.inst(6,0)

    // default I-type immediate (direct concatenation)
    val imm_i = Cat(Fill(20, io.inst(31)), io.inst(31,20))
    val imm_s = Cat(Fill(20, io.inst(31)), io.inst(31,25), io.inst(11,7))
    val imm_b = Cat(Fill(19, io.inst(31)), io.inst(31), io.inst(7), io.inst(30,25), io.inst(11,8), 0.U(1.W))
    val imm_u = Cat(io.inst(31,12), Fill(12, 0.U))
    val imm_j = Cat(Fill(11, io.inst(31)), io.inst(31), io.inst(19,12), io.inst(20), io.inst(30,21), 0.U(1.W))

    // single output wire with default to I-type immediate
    val outImm = WireDefault(imm_i)

    switch(opcode) {
        is("b0100011".U) {outImm := imm_s}                  // S-type
        is("b1100011".U) {outImm := imm_b}                  // B-type
        is("b0110111".U, "b0010111".U) {outImm := imm_u}    // U-type
        is("b1101111".U) {outImm := imm_j}                  // J-type
    }
    io.imm := outImm
}