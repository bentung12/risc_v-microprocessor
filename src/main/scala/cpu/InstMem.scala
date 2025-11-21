package cpu

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

/// DO NOT MODIFY THIS FILE /// 

class InstMem_io extends Bundle {
    val addr = Input(UInt(32.W)) 
    val inst = Output(UInt(32.W))
}

class InstMem() extends Module {
    val io = IO(new InstMem_io()) 

    // addr divided by 4, byte -> word address
    val index = io.addr >> 2.U 

    val pipeline = sys.env.getOrElse("PIPELINE","0") == "1"

    // declare a memory that can store 1024 32-bit instructions
    val mem: MemBase[UInt] = if(pipeline) {
        SyncReadMem(1024, UInt(32.W))
    } else {
        Mem(1024, UInt(32.W))
    }

    val inst_file = sys.env.getOrElse("INST_FILE", "src/test/pattern/p1.hex")
    loadMemoryFromFile(mem, inst_file, MemoryLoadFileType.Hex)
    println(s"Loading instruction memory from: " + inst_file)
    
    io.inst := mem.read(index)
}
