# helse-spytte

Spytter meldinger på kafka

## Kult, men hvor starter jeg?

### Autentisering

1. Koble til `aiven-prod` gateway via naisdevice (`aiven-dev` er automatisk tilgjengelig)
2. Hent credentials fra en app som har tilgang på topicen du ønsker å jobbe mot.

Her finner vi credentials for en `spesialist`-pod kjørende i dev-fss: 
```
% k describe po spesialist-588f7cbcf-56g9k -n tbd | grep SecretName | grep aiven
    SecretName:  aiven-spesialist-jgtwydre
```

Her får vi navn på secreten vår: `aiven-spesialist-jgtwydre`. Alle secrets er definert i tilhørende gcp-kluster: 
```
kx dev-gcp
k get secret aiven-spesialist-jgtwydre -n tbd -o jsonpath='{.data.client\.keystore\.p12}' | base64 -D > data/keystore-dev.p12
k get secret aiven-spesialist-jgtwydre -n tbd -o jsonpath='{.data.client\.truststore\.jks}' | base64 -D > data/truststore-dev.jks
```

#### Sørg for tilgang til topic
Sørg for at appen du later som at du kjører som har tilgang til å lese eller lese og skrive til ønsket topic.

### Klar til å kjøre?

Enten kan du nå kjøre en av run configurationsa i `.idea/runConfigurations`, eller du kan modifisere mer manuelt:

Velg URL til Kafka broker for clusteret:
- `nav-dev-kafka-nav-dev.aivencloud.com:26484`
- `nav-prod-kafka-nav-prod.aivencloud.com:26484`

Eventuelt hent fra en pod i clusteret:
`kubectl exec -ti <en eller annen pod> -- env | grep KAFKA_BROKER`

Vi kan nå opprette en konfigurasjon i prosjektet vårt:

```kotlin
val aivenDev = AivenConfig(
    brokers = listOf("<URL TIL BROKER>"),
    truststorePath = "data/truststore-dev.jks",
    truststorePw = "changeme",
    keystorePath = "data/keystore-dev.p12",
    keystorePw = "changeme"
)
```

Truststore trengs bare hentes én gang (én for dev og én for prod), mens keystore må oppdaters hver gang den roteres.
