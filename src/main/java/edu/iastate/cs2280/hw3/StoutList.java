package edu.iastate.cs2280.hw3;

import java.util.*;



/**
 * Implementation of the list interface based on linked nodes
 * that store multiple items per node.  Rules for adding and removing
 * elements ensure that each node (except possibly the last one)
 * is at least half full.
 */

/**
 * @author Ayr Nasser Neto
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E>
{
    /**
     * Default number of elements that may be stored in each node.
     */
    private static final int DEFAULT_NODESIZE = 4;

    /**
     * Number of elements that can be stored in each node.
     */
    private final int nodeSize;

    /**
     * Dummy node for head.  It should be private but set to public here only
     * for grading purpose.  In practice, you should always make the head of a
     * linked list a private instance variable.
     */
    public Node head;

    /**
     * Dummy node for tail.
     */
    private Node tail;

    /**
     * Number of elements in the list.
     */
    private int size;

    /**
     * Constructs an empty list with the default node size.
     */
    public StoutList()
    {
        this(DEFAULT_NODESIZE);
    }

    /**
     * Constructs an empty list with the given node size.
     * @param nodeSize number of elements that may be stored in each node, must be
     *   an even number
     */
    public StoutList(int nodeSize)
    {
        if (nodeSize <= 0 || nodeSize % 2 != 0) throw new IllegalArgumentException();

        // dummy nodes
        head = new Node();
        tail = new Node();
        head.next = tail;
        tail.previous = head;
        this.nodeSize = nodeSize;
    }

    /**
     * Constructor for grading only.  Fully implemented.
     * @param head
     * @param tail
     * @param nodeSize
     * @param size
     */
    public StoutList(Node head, Node tail, int nodeSize, int size)
    {
        this.head = head;
        this.tail = tail;
        this.nodeSize = nodeSize;
        this.size = size;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public boolean add(E item)
    {
        // Should it return false somewhere??

        // Exception
        if(item == null){
            throw new NullPointerException();
        }

        // Empty list
        if(size() == 0){
            Node newNode = new Node();
            newNode.addItem(item);

            head.next = newNode;
            newNode.previous = head;

            newNode.next = tail;
            tail.previous = newNode;
        }

        // Expected list
        else{
            // Room in last node
            if(tail.previous.count < nodeSize){
                tail.previous.addItem(item);
            }

            // Full last node
            else{
                Node newNode = new Node();
                newNode.addItem(item);

                Node previousNode = tail.previous;
                previousNode.next = newNode;

                newNode.next = tail;
                tail.previous = newNode;
            }
        }

        size++;
        return true;
    }

    @Override
    public void add(int pos, E item)
    {
        if(pos < 0 || pos > size){
            throw new IndexOutOfBoundsException();
        }

        if(head.next == tail){
            add(item);
        }

        NodeInfo nodeInfo = find(pos);
        int offset = nodeInfo.offset;
        Node tempNode = nodeInfo.node;

        if(offset == 0){
            if((tempNode.previous.count < nodeSize) && (tempNode.previous != head)){
                tempNode.previous.addItem(item);
                size++;
                return;
            }

            else if(tempNode == tail){
                add(item);
                size++;
                return;
            }
        }

        if(tempNode.count < nodeSize){
            tempNode.addItem(offset, item);
        }

        else{
            Node newNode = new Node();
            int counter = 0;

            while(counter < (nodeSize / 2)){
                newNode.addItem(tempNode.data[(nodeSize / 2)]);
                tempNode.removeItem(nodeSize / 2);
                counter++;
            }

            Node tempNext = tempNode.next;
            tempNode.next = newNode;
            newNode.previous = tempNode;
            newNode.next = tempNext;
            tempNext.previous = newNode;

            if(offset <= (nodeSize / 2)){
                tempNode.addItem(offset, item);
            }

            if(offset > (nodeSize / 2)){
                newNode.addItem((offset - nodeSize / 2), item);
            }
        }

        size++;
    }

    @Override
    public E remove(int pos)
    {
        if (pos < 0 || pos > size)
            throw new IndexOutOfBoundsException();

        NodeInfo nodeInfo = find(pos);
        Node tempNode = nodeInfo.node;

        int offset = nodeInfo.offset;
        E nodeData = tempNode.data[offset];

        if ((tempNode.next == tail) && (tempNode.count == 1)) {
            Node tempPrevious = tempNode.previous;
            tempPrevious.next = tempNode.next;
            tempNode.next.previous = tempPrevious;
            tempNode = null;
        }

        else if ((tempNode.next == tail) || (tempNode.count > nodeSize / 2)) {
            tempNode.removeItem(offset);
        }

        else {
            tempNode.removeItem(offset);
            Node tempNext = tempNode.next;

            if (tempNext.count > nodeSize / 2) {
                tempNode.addItem(tempNext.data[0]);
                tempNext.removeItem(0);
            }

            else if (tempNext.count <= nodeSize / 2) {
                for (int i = 0; i < tempNext.count; i++) {
                    tempNode.addItem(tempNext.data[i]);
                }
                tempNode.next = tempNext.next;
                tempNext.next.previous = tempNode;
                tempNext = null;
            }
        }

        size--;
        return nodeData;
    }

    /**
     * Sort all elements in the stout list in the NON-DECREASING order. You may do the following.
     * Traverse the list and copy its elements into an array, deleting every visited node along
     * the way.  Then, sort the array by calling the insertionSort() method.  (Note that sorting
     * efficiency is not a concern for this project.)  Finally, copy all elements from the array
     * back to the stout list, creating new nodes for storage. After sorting, all nodes but
     * (possibly) the last one must be full of elements.
     *
     * Comparator<E> must have been implemented for calling insertionSort().
     */
    public void sort()
    {
        E[] sorted = (E[]) new Comparable[size];
        int index = 0;
        Node tempNode = head.next;

        while(tempNode != tail){
            for(int i = 0; i < tempNode.count; i++){
                sorted[index] = tempNode.data[i];
                index++;
            }
            tempNode = tempNode.next;
        }

        head.next = tail;
        tail.previous = head;

        insertionSort(sorted, new ElementComparator());

        size = 0;

        for(int i = 0; i < sorted.length; i++){
            add(sorted[i]);
        }
    }

    /**
     * Sort all elements in the stout list in the NON-INCREASING order. Call the bubbleSort()
     * method.  After sorting, all but (possibly) the last nodes must be filled with elements.
     *
     * Comparable<? super E> must be implemented for calling bubbleSort().
     */
    public void sortReverse()
    {
        E[] sorted = (E[]) new Comparable[size];
        int index = 0;
        Node tempNode = head.next;

        while(tempNode != tail){
            for(int i = 0; i < tempNode.count; i++){
                sorted[index] = tempNode.data[i];
                index++;
            }
            tempNode = tempNode.next;
        }

        head.next = tail;
        tail.previous = head;

        bubbleSort(sorted);

        size = 0;

        for(int i = 0; i < sorted.length; i++){
            add(sorted[i]);
        }
    }

    @Override
    public Iterator<E> iterator()
    {
        return new StoutListIterator();
    }

    @Override
    public ListIterator<E> listIterator()
    {
        return new StoutListIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index)
    {
        return new StoutListIterator();
    }

    /**
     * Returns a string representation of this list showing
     * the internal structure of the nodes.
     */
    public String toStringInternal()
    {
        return toStringInternal(null);
    }

    /**
     * Returns a string representation of this list showing the internal
     * structure of the nodes and the position of the iterator.
     *
     * @param iter
     *            an iterator for this list
     */
    public String toStringInternal(ListIterator<E> iter)
    {
        int count = 0;
        int position = -1;
        if (iter != null) {
            position = iter.nextIndex();
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        Node current = head.next;
        while (current != tail) {
            sb.append('(');
            E data = current.data[0];
            if (data == null) {
                sb.append("-");
            } else {
                if (position == count) {
                    sb.append("| ");
                    position = -1;
                }
                sb.append(data.toString());
                ++count;
            }

            for (int i = 1; i < nodeSize; ++i) {
                sb.append(", ");
                data = current.data[i];
                if (data == null) {
                    sb.append("-");
                } else {
                    if (position == count) {
                        sb.append("| ");
                        position = -1;
                    }
                    sb.append(data.toString());
                    ++count;

                    // iterator at end
                    if (position == size && count == size) {
                        sb.append(" |");
                        position = -1;
                    }
                }
            }
            sb.append(')');
            current = current.next;
            if (current != tail)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }


    /**
     * Node type for this list.  Each node holds a maximum
     * of nodeSize elements in an array.  Empty slots
     * are null.
     */
    private class Node
    {
        /**
         * Array of actual data elements.
         */
        // Unchecked warning unavoidable.
        public E[] data = (E[]) new Comparable[nodeSize];

        /**
         * Link to next node.
         */
        public Node next;

        /**
         * Link to previous node;
         */
        public Node previous;

        /**
         * Index of the next available offset in this node, also
         * equal to the number of elements in this node.
         */
        public int count;

        /**
         * Adds an item to this node at the first available offset.
         * Precondition: count < nodeSize
         * @param item element to be added
         */
        void addItem(E item)
        {
            if (count >= nodeSize)
            {
                return;
            }
            data[count++] = item;
            //useful for debugging
            //      System.out.println("Added " + item.toString() + " at index " + count + " to node "  + Arrays.toString(data));
        }

        /**
         * Adds an item to this node at the indicated offset, shifting
         * elements to the right as necessary.
         *
         * Precondition: count < nodeSize
         * @param offset array index at which to put the new element
         * @param item element to be added
         */
        void addItem(int offset, E item)
        {
            if (count >= nodeSize)
            {
                return;
            }
            for (int i = count - 1; i >= offset; --i)
            {
                data[i + 1] = data[i];
            }
            ++count;
            data[offset] = item;
            //useful for debugging
//      System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
        }

        /**
         * Deletes an element from this node at the indicated offset,
         * shifting elements left as necessary.
         * Precondition: 0 <= offset < count
         * @param offset
         */
        void removeItem(int offset)
        {
            E item = data[offset];
            for (int i = offset + 1; i < nodeSize; ++i)
            {
                data[i - 1] = data[i];
            }
            data[count - 1] = null;
            --count;
        }
    }

    private class StoutListIterator implements ListIterator<E>
    {
        // constants you possibly use ...

        // instance variables ...
        int cursor;
        String prevORnext;
        E[] iterList;

        /**
         * Default constructor
         */
        public StoutListIterator()
        {
            cursor = 0;
            prevORnext = null;
            arrayIteratorList();
        }

        /**
         * Constructor finds node at a given position.
         * @param pos
         */
        public StoutListIterator(int pos)
        {
            cursor = pos;
            prevORnext = null;
            arrayIteratorList();
        }

        @Override
        public boolean hasNext()
        {
            return cursor < size;
        }

        @Override
        public E next()
        {
            if(!hasNext()){
                throw new NoSuchElementException("No next element");
            }

            prevORnext = "NEXT";
            return iterList[cursor++];
        }

        @Override
        public void remove()
        {
            if(prevORnext.equals("NEXT")){
                StoutList.this.remove(cursor - 1);
                arrayIteratorList();
                cursor--;
                prevORnext = null;

                if(cursor < 0){
                    cursor = 0;
                }
            }

            else if(prevORnext.equals("PREV")){
                StoutList.this.remove(cursor);
                arrayIteratorList();
                prevORnext = null;
            }

            else{
                throw new IllegalStateException("Method next() or previous() hasn't been called");
            }
        }

        // Other methods you may want to add or override that could possibly facilitate
        // other operations, for instance, addition, access to the previous element, etc.
        //
        // ...
        //
        @Override
        public int nextIndex(){
            return cursor;
        }

        @Override
        public int previousIndex(){
            return cursor - 1;
        }

        @Override
        public boolean hasPrevious(){
            return cursor > 0;
        }

        @Override
        public E previous(){
            if(!hasPrevious()){
                throw new NoSuchElementException("No previous element");
            }

            cursor--;
            prevORnext = "PREV";

            return iterList[cursor];
        }

        @Override
        public void set(E e){
            if(Objects.equals(prevORnext, "NEXT")){
                NodeInfo nodeInfo = find(cursor - 1);
                nodeInfo.node.data[nodeInfo.offset] = e;
                iterList[cursor - 1] = e;
            }

            else if(prevORnext.equals("PREV")){
                NodeInfo nodeInfo = find(cursor);
                nodeInfo.node.data[nodeInfo.offset] = e;
                iterList[cursor] = e;
            }

            else{
                throw new IllegalArgumentException("Can't set Node");
            }
        }

        @Override
        public void add(E e){
            if(e == null){
                throw new NullPointerException("Argument is null");
            }

            StoutList.this.add(cursor, e);
            cursor++;
            arrayIteratorList();
            prevORnext = null;
        }

        private void arrayIteratorList(){
            iterList = (E[]) new Comparable[size];
            int index = 0;
            Node tempNode = head.next;

            while(tempNode != tail){
                for(int i = 0; i < tempNode.count; i++){
                    iterList[index] = tempNode.data[i];
                    index++;
                }
                tempNode = tempNode.next;
            }
        }
    }


    /**
     * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING order.
     * @param arr   array storing elements from the list
     * @param comp  comparator used in sorting
     */
    private void insertionSort(E[] arr, Comparator<? super E> comp)
    {
        int j;

        for (int i = 1; i < arr.length; i++) {
            E key = arr[i];
            j = i - 1;

            while (j >= 0 && comp.compare(arr[j], key) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    /**
     * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a
     * description of bubble sort please refer to Section 6.1 in the project description.
     * You must use the compareTo() method from an implementation of the Comparable
     * interface by the class E or ? super E.
     * @param arr  array holding elements from the list
     */
    private void bubbleSort(E[] arr)
    {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j].compareTo(arr[j + 1]) < 0) {
                    E temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    // Extra helper class / methods
    private class NodeInfo{
        public Node node;
        public int offset;

        public NodeInfo(Node node, int offset){
            this.node = node;
            this.offset = offset;
        }
    }

    private NodeInfo find(int pos){
        Node tempNode = head.next;
        int current = 0;

        while(tempNode != tail){
            if((current + tempNode.count) <= pos){
                current += tempNode.count;
                tempNode = tempNode.next;
                continue;
            }

            return new NodeInfo(tempNode, pos - current);
        }
        return null;
    }

    private static class ElementComparator<E extends Comparable<E>> implements Comparator<E>{
        @Override
        public int compare(E a, E b){
            return a.compareTo(b);
        }
    }


}