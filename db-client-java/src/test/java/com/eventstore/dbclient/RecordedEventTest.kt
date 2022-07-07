package com.eventstore.dbclient

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class RecordedEventTest {

    @Test
    fun `use a kotlin data class`() {
        data class FakeRecordedEvent(val aString: String, val aNumber: Int)

        val recordedEvent = RecordedEvent(
            "eventStreamId",
            1,
            UUID.randomUUID(),
            Position(1, 1),
            mapOf(SystemMetadataKeys.CREATED to "1000"),
            """
                {
                  "aString": "something",
                  "aNumber": 1234
                }
            """.trimIndent().toByteArray(),
            "".toByteArray()
        )
        val extractedEvent = recordedEvent.getEventDataAs(FakeRecordedEvent::class.java)

        assertEquals(FakeRecordedEvent("something", 1234), extractedEvent)
    }
}
