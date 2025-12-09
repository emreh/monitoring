package test.ir.myhome.agent;

import ir.myhome.agent.util.SpanIdGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpanIdGeneratorTest {

    @Test
    void uniqueIds() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(SpanIdGenerator.nextId());
        }
        assertEquals(1000, ids.size(), "ids should be unique");
    }
}
