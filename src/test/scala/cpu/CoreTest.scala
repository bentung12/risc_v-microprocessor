package cpu

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import chisel3.util._
import org.scalatest.flatspec.AnyFlatSpec

import scala.io.Source
import java.io.File

trait CoreTestFunc {

  def testFn(dut: Core, golden_file: String): Unit = {
    var pc_prev = -1
    var cycle = 0
    val max_cycle = 20000
    dut.clock.setTimeout(0)

    // read golden memory
    val golden_mem = Array.fill(1024)(0)
    val golden_lines = Source.fromFile(golden_file)
      .getLines()
      .filter(_.nonEmpty)
      .map(_.trim)
      .map(line => Integer.parseInt(line, 16))
      .toArray
    Array.copy(golden_lines, 0, golden_mem, 0, golden_lines.length.min(1024))

    println(s"Load golden memory from $golden_file")

    val peek_mem = Array.fill(1024)(0)

    // start simulation
    println("=== Start Simulation ===")
    while (dut.io.inst.peekInt().toInt != 0 && cycle < max_cycle) {
      val pc = dut.io.pc.peekInt().toInt
      val inst = dut.io.inst.peekInt()

      if(dut.io.peek_write.peekBoolean()) {
        val index = dut.io.peek_addr.peekInt().toInt >> 2
        val data = dut.io.peek_data.peekInt().toInt
        peek_mem(index) = data
      } 

      if (pc != pc_prev) {
        println(f"[Cycle $cycle%05d] PC = 0x$pc%08x, INST = 0x$inst%08x")
        pc_prev = pc
      }

      dut.clock.step(1)
      cycle += 1
    }

    // check data memory
    println("\n=== Checking Data Memory ===")
    var error = 0

    for (i <- 0 until 1024) {
      val peek_val = peek_mem(i)
      val golden_val = golden_mem(i)
      if (peek_val != golden_val) {
        println(f"[Mismatch @ index $i%04d] DUT = 0x$peek_val%08x, GOLD = 0x$golden_val%08x")
        error += 1
      }
    }

    assert(error == 0, s"Data memory mismatch ($error errors)")
    if (error == 0) {
      println(s" \n=== PASS ===")
    } else {
      println(s" \n=== FAIL ===\n$error errors found.")
    }
  }
}

class CoreTest extends AnyFlatSpec with ChiselScalatestTester with CoreTestFunc {
  "Core" should "match golden memory" in {
    test(new Core).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val golden_file = sys.env.getOrElse("GOLDEN_FILE", "src/test/pattern/p1_golden.hex")
      testFn(dut, golden_file)
    }
  }
}
