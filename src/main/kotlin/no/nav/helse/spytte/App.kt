package no.nav.helse.spytte

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.rapids_and_rivers.cli.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import kotlin.system.exitProcess

private val log = LoggerFactory.getLogger("no.nav.helse.spytte.App")

/**
 * Sette offsets basert på tid, f.eks. til 1. desember 2021:
 * setOffset(factory, "min-consumer-gruppe", "tbd.rapid.v1", LocalDateTime.of(2021, 12, 1, 0, 0, 0))
 *
 * Husk å scale ned poddene først!
 * k scale deployment/minapp --replicas=0 -n tbd
 */
private fun setOffset(factory: ConsumerProducerFactory, consumerGroup: String, topic: String, time: LocalDateTime) {
    RapidsCliApplication(factory)
        .start(consumerGroup, listOf(topic)) { consumer ->
            consumer.seekTo(topic, time)
            println("Finished setting consumer offsets for $consumerGroup")
            exitProcess(0)
        }
}

/**
 * Sende en enkeltmelding:
 * val message = mapper.writeValueAsString(obj)
 * produserEnkeltmelding(factory, "tbd.rapid.v1", "fnr", message)
 */
private fun produserEnkeltmelding(factory: ConsumerProducerFactory, topic: String, key: String, message: String) {
    factory.createProducer().apply {
        send(ProducerRecord(topic, key, message)).get()
    }.close()
}

/**
 * Konsumere meldinger:
 * konsumereMeldinger(factory, "min-consumergruppe", "tbd.rapid.v1")
 */
private fun konsumereMeldinger(factory: ConsumerProducerFactory, consumerGroup: String, topic: String) {
    // kan definere egne consumer properties som overskriver defaults.
    // For å se hva som er defaults, sjekk Config-implementasjonen som
    // ConsumerProducerFactory er satt opp med, f.eks.
    // @see no.nav.rapids_and_rivers.cli.AivenConfig#consumerConfig
    val consumerProperties = Properties()
    RapidsCliApplication(factory).apply {
        JsonRiver(this).apply {
            // prerequisite er en spesiell type validation som kaster exception dersom den feiler.
            // det betyr i praksis at meldingen blir ignorert, og riveren fortsetter med neste melding.
            // onError() blir _ikke_ kalt
            // prerequisite er det samme som demandKey, demandValue osv. i Rapids and rivers
            prerequisite { record, node, _ ->
                true // godta alt
            }
            // kan filtrere meldinger basert på info i ConsumerRecord eller JsonNode.
            // kan legge til årsaker til hvorfor man ikke validerte i $reasons (ikke påkrevd).
            // validate er det samme som requireKey, requireValue osv. i Rapids and rivers
            validate { record, node, reasons ->
                node.path("@event_name").asText() == "vedtaksperiode_endret" // bare lytte på spesielle events
            }
            onError { record, node, reasons ->
                // blir kalt dersom validering feiler
                println("Validerte ikke melding fordi:")
                println(reasons.joinToString(separator = "\n"))
                println("Melding:\n$node")
            }
            // onMessage handlers blir kalt i den rekkefølgen man legger dem til
            onMessage { record, node ->
                println("leser melding: $node")
            }
            onMessage { record, node ->

            }
        }
    }.start(consumerGroup, listOf(topic), consumerProperties)
}

private val mapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

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