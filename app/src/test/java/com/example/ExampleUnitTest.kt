package com.example

import com.example.engine.MyRepublicParams
import com.example.engine.ProjectExporter
import com.example.engine.ScriptAssistant
import com.example.model.VideoProject
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testScriptAssistantGeneration() {
    val res = ScriptAssistant.generate(
      category = ScriptAssistant.CAT_PROMO_WIFI,
      topic = "Paket Gamer",
      audience = "Gamers",
      myrep = MyRepublicParams(speed = "250 Mbps", price = "Rp380.000/bln", benefit = "Ping Rendah")
    )

    assertEquals(5, res.scenes.size)
    assertTrue(res.caption.contains("Paket Gamer"))
    assertTrue(res.scenes.any { it.narration.contains("250 Mbps") })
  }

  @Test
  fun testSrtExportFormat() {
    val project = VideoProject()
    val srt = ProjectExporter.generateSRT(project.scenes)
    assertTrue(srt.contains("00:00:00,000 -->"))
    assertTrue(srt.contains("1\n"))
  }

  @Test
  fun testTxtExportFormat() {
    val project = VideoProject(name = "OpenMontage Demo")
    val txt = ProjectExporter.generateTXT(project)
    assertTrue(txt.contains("JUDUL: OpenMontage Demo"))
    assertTrue(txt.contains("Scene 1"))
  }
}
