package manager;

import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> tasksHistory = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public Map<Integer, Node> getHistory() {
        return tasksHistory;
    }

    @Override
    public void remove(int id) {
        if (tasksHistory.containsKey(id)) {
            removeNode(id);
            tasksHistory.remove(id);
        }
    }

    @Override
    public List<Task> getTasksHistory() {
        List<Task> taskHistory = new ArrayList<>();
        Node temp = head;

        while (temp != null) {
            taskHistory.add(temp.task);
            temp = temp.next;
        }
        return taskHistory;
    }

    @Override
    public void add(Task task) {
        Node node = new Node(task);
        remove(task.getTaskId());
        linkLast(node);
        tasksHistory.put(task.getTaskId(), node);
    }

    private void linkLast(Node node) {

        if (isEmpty()) {
            head = node;
        } else {
            tail.next = node;
        }

        node.prev = tail;
        tail = node;
    }

    private boolean isEmpty() {
        return head == null;
    }

    private void removeNode(int key) {
        Node node = tasksHistory.get(key);

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

     static class Node {

        public Task task;
        public Node next;
        public Node prev;

        public Node(Task task) {
            this.task = task;
            this.next = null;
            this.prev = null;
        }

         public Task getTask() {
             return task;
         }

         @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(task, node.task);
        }

        @Override
        public int hashCode() {
            return Objects.hash(task);
        }
    }
}

