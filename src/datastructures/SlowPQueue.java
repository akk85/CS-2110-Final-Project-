package datastructures;

import java.util.ArrayList;

/** A simple but slow implementation of a priority queue where the
 *  priorities are doubles. An asymptotically more efficient implementation
 *  is possible!
 */
public class SlowPQueue<E> implements PQueue<E> {

    record PrioElem<E>(E elem, double priority) {}

    /** Contains all the elements in the queue, along with their
     *  priorities. No element occurs more than once.
     */
    private final ArrayList<PrioElem<E>> data;

    /** Creates: an empty queue
     */
    public SlowPQueue() {
        data = new ArrayList<>();
    }

    @Override public int size() { return data.size(); }
    @Override public boolean isEmpty() { return data.isEmpty(); }
    @Override public E peek() {
        assert data.size() > 0;
        E bestElem = data.get(0).elem;
        double bestPriority = data.get(0).priority;
        for (PrioElem<E> pe : data) {
            if (pe.priority < bestPriority) {
                bestElem = pe.elem;
                bestPriority = pe.priority;
            }
        }
        return bestElem;
    }

    @Override
    public void add(E e, double priority) throws IllegalArgumentException {
        for (PrioElem<E> pe : data) {
            if (pe.elem.equals(e)) throw new IllegalArgumentException();
        }
        data.add(new PrioElem<>(e, priority));
    }

    @Override
    public E extractMin() {
        E bestElem = peek();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).elem.equals(bestElem)) {
                data.remove(i);
                break;
            }
        }
        return bestElem;
    }

    @Override
    public void changePriority(E e, double priority) {
        for (int i = 0; i < data.size(); i++) {
            PrioElem<E> pe = data.get(i);
            if (pe.elem.equals(e)) {
                data.set(i, new PrioElem<>(e, priority));
                return;
            }
        }
        assert false;
    }
}
