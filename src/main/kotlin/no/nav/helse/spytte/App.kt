package no.nav.helse.spytte

import no.nav.rapids_and_rivers.cli.AivenConfig
import no.nav.rapids_and_rivers.cli.ConsumerProducerFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("spytte")

fun main() {
    val fnr = "FNR" // TODO les fra liste

    val factory = ConsumerProducerFactory(AivenConfig.default)

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