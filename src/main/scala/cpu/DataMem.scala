package cpu

import chisel3._
import chisel3.util._

/// DO NOT MODIFY THIS FILE /// 

class DataMem_io extends Bundle {
    val ctrlMemRead = Input(Bool())
    val ctrlMemWrite = Input(Bool())
    val addr = Input(UInt(32.W))
    val data_in = Input(UInt(32.W))
    val data_out = Output(UInt(32.W))
    val peek_write = Output(Bool())
}

class DataMem extends Module {
    val io = IO(new DataMem_io)

    val pipeline = sys.env.getOrElse("PIPELINE","0") == "1"
    // declare a memory that can store 1024 32-bit data
    val mem = if(pipeline) {
        SyncReadMem(1024, UInt(32.W))
    } else {
        Mem(1024, UInt(32.W))
    }

    // addr divided by 4, byte -> word address
    val index = io.addr >> 2.U 
    val peek_write = WireDefault(false.B)
    
    when(io.ctrlMemWrite) {
        mem.write(index, io.data_in)
        peek_write := true.B
    }
    
    io.peek_write := peek_write
    io.data_out := Mux(io.ctrlMemRead, mem.read(index), 0.U)

}
