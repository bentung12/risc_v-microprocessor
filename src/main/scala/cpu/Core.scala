package cpu

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}
import firrtl.options.TargetDirAnnotation

//This is the top-level CPU module
//Outputs:
        // pc: Current program counter
        // inst: Current instruction fetched from instruction memory
        // wb_result: Data being written back to the register file
        // peek_write: For testing, indicates if data memory is being written to
        // peek_addr: For testing, address being accessed in data memory
        // peek_data: For testing, data being written to data memory

// io for testbench to access
class Core_io extends Bundle {
  // for debugging
  val pc         = Output(UInt(32.W))
  val inst       = Output(UInt(32.W))
  val wb_result  = Output(UInt(32.W))
  // for grading, DO NOT MODIFY
  val peek_write = Output(Bool())
  val peek_addr  = Output(UInt(32.W))
  val peek_data  = Output(UInt(32.W))
}

class Core extends Module {

    val io = IO(new Core_io())
    val print = false.B //Set to true to see detailed pipeline print statements

    // instantiate all the modules
    val pc_handle       = Module(new PCHandle())
    val inst_mem        = Module(new InstMem())
    val decoder         = Module(new Decoder())
    val reg_file        = Module(new Regfile())
    val alu_forwarding  = Module(new ALU_Forwarding())
    val alu             = Module(new Alu())
    val data_mem        = Module(new DataMem())
    val imm_gen         = Module(new ImmGen())
    val wb              = Module(new Writeback())
    val hazard          = Module(new Hazard_Detection())
    val branch_unit     = Module(new Branch())
    val branch_forward  = Module(new Branch_Forwarding())
    val inst_sel        = Module(new InstSel())
    
    //pipeline registers
    val reg_if_id  = Module(new Reg_IF_ID())
    val reg_id_ex  = Module(new Reg_ID_EX())
    val reg_ex_mem = Module(new Reg_EX_MEM())
    val reg_mem_wb = Module(new Reg_MEM_WB())

    // PC + Instruction Memory
    // PC feeds instruction memory address
    inst_mem.io.addr    := pc_handle.io.pc
    pc_handle.io.write  := hazard.io.pc_write

    //IF/ID pipeline register
    reg_if_id.io.pc_in          := pc_handle.io.pc
    reg_if_id.io.pc_plus4_in    := pc_handle.io.pcPlus4
    reg_if_id.io.if_flush       := pc_handle.io.if_flush
    reg_if_id.io.inst_write_in  := hazard.io.if_id_write

    //Instruction Selection (for stalling/flushing)
    inst_sel.io.inst_in         := inst_mem.io.inst
    inst_sel.io.inst_write      := reg_if_id.io.inst_write_out
    inst_sel.io.id_flush        := reg_if_id.io.id_flush

    // Current instruction
    val inst = inst_sel.io.inst_out

    // Decode fields from inst
    val opcode = inst(6, 0)
    val rd     = inst(11, 7)
    val funct3 = inst(14, 12)
    val rs1    = inst(19, 15)
    val rs2    = inst(24, 20)
    val funct7 = inst(31, 25)

    // Feed decoder
    decoder.io.opcode := opcode
    decoder.io.funct3 := funct3
    decoder.io.funct7 := funct7
    decoder.io.flush  := hazard.io.control_flush 

    // Hazard Detection Unit
    hazard.io.id_rs1      := rs1
    hazard.io.id_rs2      := rs2
    hazard.io.ex_rd       := reg_id_ex.io.rd_addr_out
    hazard.io.ex_memread  := reg_id_ex.io.ctrl_signal_out.ctrlMemRead
    hazard.io.mem_rd      := reg_ex_mem.io.rd_addr_out
    hazard.io.mem_memread := reg_ex_mem.io.ctrlMemRead_out
    hazard.io.opcode      := opcode

    // Immediate generation
    imm_gen.io.inst := inst

    // Register file connections
    reg_file.io.reg_addr.rs1_addr := rs1
    reg_file.io.reg_addr.rs2_addr := rs2

    // Branch/Jump unit connections
    branch_unit.io.pc_id                := reg_if_id.io.pc_out
    branch_unit.io.imm_value            := imm_gen.io.imm
    branch_unit.io.reg1_value           := reg_file.io.data_read1
    branch_unit.io.reg2_value           := reg_file.io.data_read2
    branch_unit.io.ctrlALUOP            := decoder.io.ctrl_signal.ctrlALUOp
    branch_unit.io.forwardA             := branch_forward.io.forwardA
    branch_unit.io.forwardB             := branch_forward.io.forwardB
    branch_unit.io.alu_result_ex        := alu.io.alu_result
    branch_unit.io.alu_result_mem       := reg_ex_mem.io.alu_result_out

    // Branch forwarding connections
    branch_forward.io.id_rs1            := rs1
    branch_forward.io.id_rs2            := rs2
    branch_forward.io.ex_rd             := reg_id_ex.io.rd_addr_out
    branch_forward.io.mem_rd            := reg_ex_mem.io.rd_addr_out
    branch_forward.io.ex_regwrite       := reg_id_ex.io.ctrl_signal_out.ctrlRegWrite
    branch_forward.io.mem_regwrite      := reg_ex_mem.io.ctrlRegWrite_out
    branch_forward.io.ctrlBranch        := decoder.io.ctrl_signal.ctrlBranch
    branch_forward.io.ctrlJump          := decoder.io.ctrl_signal.ctrlJump

    // ID/EX pipeline register
    reg_id_ex.io.reg_data1_in   := reg_file.io.data_read1
    reg_id_ex.io.reg_data2_in   := reg_file.io.data_read2
    reg_id_ex.io.reg_addr1_in   := rs1
    reg_id_ex.io.reg_addr2_in   := rs2
    reg_id_ex.io.imm_in         := imm_gen.io.imm
    reg_id_ex.io.rd_addr_in     := rd
    reg_id_ex.io.ctrl_signal_in := decoder.io.ctrl_signal
    reg_id_ex.io.pc_in          := reg_if_id.io.pc_out
    reg_id_ex.io.pc_plus4_in    := reg_if_id.io.pc_plus4_out

    // Forwarding Unit
    alu_forwarding.io.ex_rs1            := reg_id_ex.io.reg_addr1_out
    alu_forwarding.io.ex_rs2            := reg_id_ex.io.reg_addr2_out
    alu_forwarding.io.mem_rd            := reg_ex_mem.io.rd_addr_out
    alu_forwarding.io.wb_rd             := reg_mem_wb.io.rd_addr_out
    alu_forwarding.io.mem_regwrite      := reg_ex_mem.io.ctrlRegWrite_out
    alu_forwarding.io.wb_regwrite       := reg_mem_wb.io.ctrlRegWrite_out

    // ALU connections
    alu.io.reg1      := reg_id_ex.io.reg_data1_out
    alu.io.reg2      := reg_id_ex.io.reg_data2_out
    alu.io.imm       := reg_id_ex.io.imm_out
    alu.io.pc        := reg_id_ex.io.pc_out
    alu.io.ALUSrc    := reg_id_ex.io.ctrl_signal_out.ctrlALUSrc
    alu.io.ctrlALUOp := reg_id_ex.io.ctrl_signal_out.ctrlALUOp
    alu.io.forwardA  := alu_forwarding.io.forwardA
    alu.io.forwardB  := alu_forwarding.io.forwardB
    alu.io.mem_data  := reg_ex_mem.io.alu_result_out
    alu.io.wb_data   := wb.io.data_write
    

    //EX/MEM pipeline register
    reg_ex_mem.io.reg_data2_in   := alu.io.src2_peek
    reg_ex_mem.io.alu_result_in  := alu.io.alu_result
    reg_ex_mem.io.rd_addr_in     := reg_id_ex.io.rd_addr_out
    reg_ex_mem.io.ctrl_signal_in := reg_id_ex.io.ctrl_signal_out
    reg_ex_mem.io.pc_plus4_in    := reg_id_ex.io.pc_plus4_out

    // Data memory connections
    data_mem.io.ctrlMemRead  := reg_mem_wb.io.ctrlMemRead_out
    data_mem.io.ctrlMemWrite := reg_ex_mem.io.ctrlMemWrite_out
    data_mem.io.addr         := reg_ex_mem.io.alu_result_out
    data_mem.io.data_in      := reg_ex_mem.io.reg_data2_out

    //MEM/WB pipeline register
    reg_mem_wb.io.alu_result_in         := reg_ex_mem.io.alu_result_out 
    reg_mem_wb.io.rd_addr_in            := reg_ex_mem.io.rd_addr_out
    reg_mem_wb.io.ctrlRegWrite_in       := reg_ex_mem.io.ctrlRegWrite_out
    reg_mem_wb.io.ctrlMemToReg_in       := reg_ex_mem.io.ctrlMemToReg_out
    reg_mem_wb.io.ctrlJump_in           := reg_ex_mem.io.ctrlJump_out
    reg_mem_wb.io.ctrlMemRead_in        := reg_ex_mem.io.ctrlMemRead_out
    reg_mem_wb.io.pc_plus4_in           := reg_ex_mem.io.pc_plus4_out

    // PCHandle (next PC logic)
    pc_handle.io.ctrlBranch     := decoder.io.ctrl_signal.ctrlBranch
    pc_handle.io.ctrlJump       := decoder.io.ctrl_signal.ctrlJump
    pc_handle.io.to_branch      := branch_unit.io.to_branch
    pc_handle.io.branch_next_pc := branch_unit.io.branch_next_pc
    pc_handle.io.jump_next_pc   := branch_unit.io.jump_next_pc

    // Writeback mux
    wb.io.data_ALU              := reg_mem_wb.io.alu_result_out
    wb.io.data_mem              := data_mem.io.data_out
    wb.io.pc_plus4              := reg_mem_wb.io.pc_plus4_out
    wb.io.ctrlMemToReg          := reg_mem_wb.io.ctrlMemToReg_out
    wb.io.ctrlJump              := reg_mem_wb.io.ctrlJump_out

    // Connect writeback result to regfile
    reg_file.io.data_write       := wb.io.data_write
    reg_file.io.reg_addr.rd_addr := reg_mem_wb.io.rd_addr_out
    reg_file.io.ctrlRegWrite     := reg_mem_wb.io.ctrlRegWrite_out

    // Core debug IO
    io.pc        := pc_handle.io.pc
    io.inst      := inst_mem.io.inst
    io.wb_result := wb.io.data_write

    when (print) {
        println("Here's the detailed cycle information:")
        printf(p"\n[Cycle] -----------------------------\n")
        // IF
        printf(p"[IF ] pc=0x${Hexadecimal(pc_handle.io.pc)}" +
                p" pc_fetch = 0x${Hexadecimal(pc_handle.io.pcPlus4)}" +
                p" Flush=${(pc_handle.io.if_flush)}}" +
                p" wr = ${hazard.io.if_id_write}\n")
        // ID
        printf(p"[ID ] pc=0x${Hexadecimal(reg_if_id.io.pc_out)} inst=0x${Hexadecimal(inst)} " +
                p"rs1=${rs1} rs2=${rs2} rd=${rd} " +
                p"ALUop(dec)=${decoder.io.ctrl_signal.ctrlALUOp} " +
                p"ALUSrc(dec)=${decoder.io.ctrl_signal.ctrlALUSrc} " +
                p"brTaken=${branch_unit.io.to_branch} " +
                p"Branch=${decoder.io.ctrl_signal.ctrlBranch} Jump=${decoder.io.ctrl_signal.ctrlJump} " +
                p"JumpAddr=0x${Hexadecimal(branch_unit.io.jump_next_pc)} " +
                p"Flush=${(reg_if_id.io.id_flush)} Write=${(reg_if_id.io.inst_write_out)} " +
                p"Reg1=0x${Hexadecimal(reg_file.io.data_read1)} Reg2=0x${Hexadecimal(reg_file.io.data_read2)} " +
                p"Reg1_True=0x${Hexadecimal(branch_unit.io.reg1_peek)} Reg2_True=0x${Hexadecimal(branch_unit.io.reg2_peek)} " +
                p"ForwardA=${branch_forward.io.forwardA} ForwardB=${branch_forward.io.forwardB} " +
                p"ALUResult_EX=0x${Hexadecimal(alu.io.alu_result)}\n ")

        // EX
        printf(p"[EX ] pc=0x${Hexadecimal(reg_id_ex.io.pc_out)} " +
                p"A=0x${Hexadecimal(alu.io.operand1_peek)} B=0x${Hexadecimal(alu.io.operand2_peek)} " +
                p"imm_EX=0x${Hexadecimal(reg_id_ex.io.imm_out)} " +
                p"ForwardA=${alu.io.forwardA} ForwardB=${alu.io.forwardB} " +
                p"ALUop_EX=${reg_id_ex.io.ctrl_signal_out.ctrlALUOp} " +
                p"ALUSrc_EX=${reg_id_ex.io.ctrl_signal_out.ctrlALUSrc} " +
                p"ALU=0x${Hexadecimal(alu.io.alu_result)}\n ")

        // MEM
        printf(p"[MEM] MemR=${reg_ex_mem.io.ctrlMemRead_out} MemW=${reg_ex_mem.io.ctrlMemWrite_out} " +
                p"addr=0x${Hexadecimal(data_mem.io.addr)} " +
                p"din=0x${Hexadecimal(data_mem.io.data_in)} " +
                p"dout=0x${Hexadecimal(data_mem.io.data_out)} \n ")

        // WB
        printf(p"[WB ] rd=${reg_mem_wb.io.rd_addr_out} " +
                p"RegW=${reg_mem_wb.io.ctrlRegWrite_out} MemToReg=${reg_mem_wb.io.ctrlMemToReg_out} Jump_WB=${reg_mem_wb.io.ctrlJump_out} " +
                p"WBData=0x${Hexadecimal(wb.io.data_write)}\n")
    }

    /// DO NOT MODIFY ///
    io.peek_write := data_mem.io.peek_write
    io.peek_addr  := data_mem.io.addr
    io.peek_data  := data_mem.io.data_in
}

// Verilog code generation by command: sbt "runMain cpu.main" ///
// object main extends App {

//     (new ChiselStage).execute(
//     Array("--target-dir", "verilog_output"),
//     Seq(ChiselGeneratorAnnotation(() => new Core()))
//     )
// }