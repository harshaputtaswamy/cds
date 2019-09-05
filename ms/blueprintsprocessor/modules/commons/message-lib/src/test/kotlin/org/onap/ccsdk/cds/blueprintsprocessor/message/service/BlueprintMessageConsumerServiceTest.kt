/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.BluePrintMessageLibConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@DirtiesContext
@ContextConfiguration(classes = [BluePrintMessageLibConfiguration::class,
    BlueprintPropertyConfiguration::class, BluePrintProperties::class])
@TestPropertySource(properties =
["blueprintsprocessor.messageclient.sample.type=kafka-basic-auth",
    "blueprintsprocessor.messageclient.sample.bootstrapServers=127:0.0.1:9092",
    "blueprintsprocessor.messageclient.sample.groupId=sample-group",
    "blueprintsprocessor.messageclient.sample.consumerTopic=default-topic"
])
open class BlueprintMessageConsumerServiceTest {

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService

    @Test
    fun testKafkaBasicAuthConsumerService() {
        runBlocking {
            val blueprintMessageConsumerService = bluePrintMessageLibPropertyService
                    .blueprintMessageConsumerService("sample") as KafkaBasicAuthMessageConsumerService
            assertNotNull(blueprintMessageConsumerService, "failed to get blueprintMessageConsumerService")

            val spyBlueprintMessageConsumerService = spyk(blueprintMessageConsumerService, recordPrivateCalls = true)

            val topic = "default-topic"
            val partitions: MutableList<TopicPartition> = arrayListOf()
            val topicsCollection: MutableList<String> = arrayListOf()
            partitions.add(TopicPartition(topic, 1))
            val partitionsBeginningMap: MutableMap<TopicPartition, Long> = mutableMapOf()
            val partitionsEndMap: MutableMap<TopicPartition, Long> = mutableMapOf()

            val records: Long = 10
            partitions.forEach { partition ->
                partitionsBeginningMap[partition] = 0L
                partitionsEndMap[partition] = records
                topicsCollection.add(partition.topic())
            }
            val mockKafkaConsumer = MockConsumer<String, String>(OffsetResetStrategy.EARLIEST)
            mockKafkaConsumer.subscribe(topicsCollection)
            mockKafkaConsumer.rebalance(partitions)
            mockKafkaConsumer.updateBeginningOffsets(partitionsBeginningMap)
            mockKafkaConsumer.updateEndOffsets(partitionsEndMap)
            for (i in 1..10) {
                val record = ConsumerRecord<String, String>(topic, 1, i.toLong(), "key_$i",
                        "I am message $i")
                mockKafkaConsumer.addRecord(record)
            }

            every { spyBlueprintMessageConsumerService.kafkaConsumer(any()) } returns mockKafkaConsumer
            val channel = spyBlueprintMessageConsumerService.subscribe(null)
            launch {
                channel.consumeEach {
                    println("Received message : $it")
                }
            }
            //delay(100)
            spyBlueprintMessageConsumerService.shutDown()
        }
    }
}