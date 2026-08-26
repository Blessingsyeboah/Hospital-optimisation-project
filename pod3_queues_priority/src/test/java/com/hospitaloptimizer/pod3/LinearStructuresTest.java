package com.hospitaloptimizer.pod3;

import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class LinearStructuresTest {

    // ---- CustomStack ----

    public void testStack_pushPopPeek_normalCase() {
        CustomStack<Integer> s = new CustomStack<>();
        s.push(1); s.push(2); s.push(3);
        assertEquals(3, s.peek());
        assertEquals(3, s.pop());
        assertEquals(2, s.pop());
        assertEquals(1, s.size());
    }

    public void testStack_boundaryCase_popEmptyThrows() {
        CustomStack<Integer> s = new CustomStack<>();
        assertThrows(EmptyStackException.class, s::pop);
    }

    // ---- CustomQueue (FIFO) ----

    public void testQueue_fifoOrder_normalCase() {
        CustomQueue<String> q = new CustomQueue<>();
        q.enqueue("first"); q.enqueue("second"); q.enqueue("third");
        assertEquals("first", q.dequeue());
        assertEquals("second", q.dequeue());
        assertEquals(1, q.size());
    }

    public void testQueue_boundaryCase_dequeueEmptyThrows() {
        CustomQueue<String> q = new CustomQueue<>();
        assertThrows(NoSuchElementException.class, q::dequeue);
    }

    // ---- CircularQueue ----

    public void testCircularQueue_wrapAround_trace() {
        CircularQueue<Integer> cq = new CircularQueue<>(3);
        cq.enqueue(1); cq.enqueue(2); cq.enqueue(3);
        assertTrue(cq.isFull(), "queue of capacity 3 should be full after 3 enqueues");
        assertEquals(1, cq.dequeue()); // front moves from 0 -> 1
        cq.enqueue(4); // wraps into index 0
        int[] fr = cq.frontRearIndices();
        assertEquals(1, fr[0]); // front index
        assertEquals(0, fr[1]); // rear index wrapped to 0
        assertEquals(2, cq.dequeue());
        assertEquals(3, cq.dequeue());
        assertEquals(4, cq.dequeue());
        assertTrue(cq.isEmpty(), "queue should be empty after 4 dequeues");
    }

    public void testCircularQueue_invalidInput_fullThrows() {
        CircularQueue<Integer> cq = new CircularQueue<>(1);
        cq.enqueue(1);
        assertThrows(IllegalStateException.class, () -> cq.enqueue(2));
    }

    // ---- CustomDeque ----

    public void testDeque_urgentRequestJumpsFront() {
        CustomDeque<String> dq = new CustomDeque<>();
        dq.addRear("normal-1");
        dq.addRear("normal-2");
        dq.addFront("URGENT-1"); // urgent request jumps the line
        assertEquals("URGENT-1", dq.peekFront());
        assertEquals("normal-2", dq.peekRear());
        assertEquals(3, dq.size());
    }

    public void testDeque_boundaryCase_emptyThrows() {
        CustomDeque<String> dq = new CustomDeque<>();
        assertThrows(NoSuchElementException.class, dq::removeFront);
    }

    public static void main(String[] args) {
        run(LinearStructuresTest.class);
    }
}
