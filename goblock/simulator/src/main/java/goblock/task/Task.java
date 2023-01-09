package goblock.task;

public interface Task {

  long getInterval();

  void run();
}