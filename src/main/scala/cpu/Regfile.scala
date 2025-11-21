package cpu

import chisel3._
import chisel3.util._

// Register Module
// Implements a 32-register file with 2 read ports and 1 write port
// Supports bypassing to handle write-after-read hazards
// Outputs:
    // data_read1: Data read from source register 1
    // data_read2: Data read from source register 2
// Inputs:
    // ctrlRegWrite: Control signal to enable register write
    // reg_addr: Register addresses for read and write operations
    // data_write: Data to be written to the destination register

class Regfile_io extends Bundle {
    val data_read1   = Output(UInt(32.W))
    val data_read2   = Output(UInt(32.W))

    val ctrlRegWrite = Input(Bool())
    val reg_addr     = Flipped(new RegAddr()) // input
    val data_write   = Input(UInt(32.W))
}

class Regfile extends Module {
    val io = IO(new Regfile_io())

    // 32 registers, 32 bits each
    val regs = Reg(Vec(32, UInt(32.W)))

    val rs1 = io.reg_addr.rs1_addr
    val rs2 = io.reg_addr.rs2_addr
    val rd  = io.reg_addr.rd_addr

    // Base data from the register file
    val rs1Data = regs(rs1)
    val rs2Data = regs(rs2)

    // Bypassing Logic (write-after-read hazard)
    val rs1Bypassed = Mux(
        io.ctrlRegWrite && (rd =/= 0.U) && (rd === rs1),
        io.data_write,
        rs1Data
    )

    val rs2Bypassed = Mux(
        io.ctrlRegWrite && (rd =/= 0.U) && (rd === rs2),
        io.data_write,
        rs2Data
    )

    // Read ports with x0 hardwired to zero
    io.data_read1 := Mux(rs1 === 0.U, 0.U, rs1Bypassed)
    io.data_read2 := Mux(rs2 === 0.U, 0.U, rs2Bypassed)

    // Write port (x0 is always zero, ignore writes to x0)
    when(io.ctrlRegWrite && (rd =/= 0.U)) {
        regs(rd) := io.data_write
    }
}

