package test.ir.myhome.agent;

import ir.myhome.agent.queue.BoundedSpanQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundedSpanQueueTest {

    @Test
    void offerAndTake() throws InterruptedException {
        BoundedSpanQueue q = new BoundedSpanQueue(2);
        assertTrue(q.offer("a"));
        assertTrue(q.offer("b"));
        // third should block/return false because offer waits 2ms
        assertFalse(q.offer("c"));

        Object t1 = q.take();
        Object t2 = q.take();

        assertEquals("a", t1);
        assertEquals("b", t2);
    }
}
