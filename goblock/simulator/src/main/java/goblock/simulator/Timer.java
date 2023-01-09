package goblock.simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import goblock.task.Task;

public class Timer {

  private static final PriorityQueue<ScheduledTask> taskQueue = new PriorityQueue<>();

  private static final Map<Task, ScheduledTask> taskMap = new HashMap<>();

  private static long currentTime = 0L;

  private static class ScheduledTask implements Comparable<ScheduledTask> {
    private final Task task;
    private final long scheduledTime;

    private ScheduledTask(Task task, long scheduledTime) {
      this.task = task;
      this.scheduledTime = scheduledTime;
    }

    private Task getTask() {
      return this.task;
    }

    private long getScheduledTime() {
      return this.scheduledTime;
    }

    public int compareTo(ScheduledTask o) {
      if (this.equals(o)) {
        return 0;
      }
      int order = Long.signum(this.scheduledTime - o.scheduledTime);
      if (order != 0) {
        return order;
      }
      order = System.identityHashCode(this) - System.identityHashCode(o);
      return order;
    }
  }

  public static void runTask() {
    if (taskQueue.size() > 0) {
      ScheduledTask currentScheduledTask = taskQueue.poll();
      Task currentTask = currentScheduledTask.getTask();
      currentTime = currentScheduledTask.getScheduledTime();
      taskMap.remove(currentTask, currentScheduledTask);
      currentTask.run();
    }
  }

  public static void removeTask(Task task) {
    if (taskMap.containsKey(task)) {
      ScheduledTask scheduledTask = taskMap.get(task);
      taskQueue.remove(scheduledTask);
      taskMap.remove(task, scheduledTask);
    }
  }

  public static Task getTask() {
    if (taskQueue.size() > 0) {
      ScheduledTask currentTask = taskQueue.peek();
      return currentTask.getTask();
    } else {
      return null;
    }
  }

  public static void putTask(Task task) {
    ScheduledTask scheduledTask = new ScheduledTask(task, currentTime + task.getInterval());
    taskMap.put(task, scheduledTask);
    taskQueue.add(scheduledTask);
  }

  public static void putTaskAbsoluteTime(Task task, long time) {
    ScheduledTask scheduledTask = new ScheduledTask(task, time);
    taskMap.put(task, scheduledTask);
    taskQueue.add(scheduledTask);
  }

  public static long getCurrentTime() {
    return currentTime;
  }
}