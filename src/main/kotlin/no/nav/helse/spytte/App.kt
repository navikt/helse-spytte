package no.nav.helse.spytte

import no.nav.rapids_and_rivers.cli.AivenConfig
import no.nav.rapids_and_rivers.cli.ConsumerProducerFactory
import no.nav.rapids_and_rivers.cli.RapidsCliApplication
import no.nav.rapids_and_rivers.cli.seekTo
import org.apache.kafka.clients.producer.ProducerRecord
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.system.exitProcess

private val log = LoggerFactory.getLogger("no.nav.helse.spytte.App")

private fun setOffset(factory: ConsumerProducerFactory, consumerGroup: String, topic: String, time: LocalDateTime) {
    RapidsCliApplication(factory)
        .start(consumerGroup, listOf(topic)) { consumer ->
            consumer.seekTo(topic, time)
            println("Finished setting consumer offsets for $consumerGroup")
            exitProcess(0)
        }
}

fun main() {
    val fnr = "FNR" // TODO les fra liste

    val factory = ConsumerProducerFactory(AivenConfig.default)

    /**
     * Sette offsets basert på tid, f.eks. til 1. desember 2021:
     * setOffset(factory, "min-consumer-gruppe", "tbd.rapid.v1", LocalDateTime.of(2021, 12, 1, 0, 0, 0))
     */

    @Language("json") val json = """
        {
          "tekst": "håper ingen leser dette"
        }
    """.trimIndent()

    factory.createProducer()
        .also {
            val record = it.send(ProducerRecord("tbd.temp-dokumenter-v1", fnr, json))
            it.flush()
            log.info("{}", record.isDone)
        }
}