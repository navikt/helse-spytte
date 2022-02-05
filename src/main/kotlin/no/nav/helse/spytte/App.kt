package no.nav.helse.spytte

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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

private fun produserEnkeltmelding(factory: ConsumerProducerFactory, topic: String, key: String, message: String) {
    factory.createProducer().apply {
        send(ProducerRecord(topic, key, message)).get()
    }.close()
}

private val mapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

fun main() {
    val fnr = "FNR" // TODO les fra liste

    val factory = ConsumerProducerFactory(AivenConfig.default)

    /**
     * Sette offsets basert på tid, f.eks. til 1. desember 2021:
     * setOffset(factory, "min-consumer-gruppe", "tbd.rapid.v1", LocalDateTime.of(2021, 12, 1, 0, 0, 0))
     */

    /**
     * Sende en enkeltmelding:
     * val message = mapper.writeValueAsString(obj)
     * produserEnkeltmelding(factory, "tbd.rapid.v1", "fnr", message)
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