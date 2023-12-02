package graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import datastructures.PQueue;
import datastructures.SlowPQueue;
import org.junit.jupiter.api.Test;

public class SlowPQueueTest {
    @Test void reversed() {
        PQueue<Integer> q = new SlowPQueue<>();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        for (int i = 10; i >= 0; i--) q.add(i, i);
        assertEquals(11, q.size());
        for (int i = 0; i <= 10; i++) {
            int k = q.peek();
            int j = q.extractMin();
            assertEquals(i, j, k);
        }
        assertTrue(q.isEmpty());
    }
    @Test void inorder() {
        PQueue<Integer> q = new SlowPQueue<>();
        assertTrue(q.isEmpty());
        for (int i = 0; i < 10; i++) q.add(i, i);
        assertEquals(10, q.size());
        for (int i = 0; i < 10; i++) {
            int k = q.peek();
            int j = q.extractMin();
            assertEquals(i, j, k);
        }
        assertTrue(q.isEmpty());
    }
    @Test void throwTest() {
        PQueue<Integer> q = new SlowPQueue<>();
        q.add(1,1);
        assertThrows(IllegalArgumentException.class, () -> q.add(1,2));
    }
}
