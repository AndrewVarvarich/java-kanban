package manager;

import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new ArrayList<>();
    private final Map<Integer, Node> tasksHistory = new HashMap<>();
    private Node head;
    private Node tail;


    public InMemoryHistoryManager() {
        this.head = null;
        this.tail = null;
    }



    @Override
    public List<Task> getHistory() {
        return history;
    }

    @Override
    public void remove(int id) {
        if (tasksHistory.containsKey(id)) {
            removeNode(id);
            tasksHistory.remove(id);
        }
    }

    public void linkLast(Node node) {

        if (isEmpty()) {
            head = node;
        } else {
            tail.next = node;
        }

        node.prev = tail;
        tail = node;
    }

    @Override
    public ArrayList<Task> getTasksHistory() {
        ArrayList<Task> taskHistory = new ArrayList<>();
        Node temp = head;

        while (temp != null) {
            taskHistory.add(temp.task);
            temp = temp.next;
        }
        return taskHistory;
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

    @Override
    public void add(Task task) {
        Node node = new Node(task);
        remove(task.getTaskId());
        linkLast(node);
        tasksHistory.put(task.getTaskId(), node);
    }
}

