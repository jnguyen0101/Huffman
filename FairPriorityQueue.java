/*  Student information for assignment:
 *
 *  On MY honor, Jenny Nguyen, this programming assignment is MY own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1
 *  UTEID: jtn2497
 *  email address: nguyenjenny01012006@gmail.com
 *  Grader name: Lauren
 *
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class FairPriorityQueue<E extends Comparable<? super E>> {
    
    // the internal container for a FairPriorityQueue
    private LinkedList<E> con;
    
    /**
     * Create a new empty FairPriorityQueue.
     */
    public FairPriorityQueue() {
        con = new LinkedList<E>();
    }
    
    /**
     * Adds the element in its appropriate spot in the queue
     * If the element is equal to an existing element in the queue, the element
     * is added behind the existing element
     * pre: val != null
     * @param val the element to be added to the queue
     */
    public void add(E val) {
        // check preconditions
        if (val == null) {
            throw new IllegalArgumentException("val cannot be null.");
        }
        
        if (con.isEmpty() || val.compareTo(con.getLast()) >= 0) {
            // simply add the item to the end if queue is empty or if the item is
            // greater than or equal to all items already in the queue
            con.add(val);
        } else {
            // traverse through the queue and find correct spot to add
            Iterator<E> it = con.iterator();
            boolean added = false;
            int index = 0;
            while (it.hasNext() && !added) {
                E item = it.next();
                if (val.compareTo(item) < 0) {
                    // add item after all items that are less than or equal to val
                    // but before the first item that is greater than val
                    con.add(index, val);
                    added = true;
                }
                index++;
            }
        }
    }
    
    /**
     * Removes and returns the first element from this queue
     * @return the first element from this queue
     * @throws NoSuchElementException if this queue is empty
     */
    public E removeFirst() {
        return con.removeFirst();
    }

    /**
     * Get the size of the queue
     * @return the size of the queue
     */
    public int size() {
        return con.size();
    }
}
