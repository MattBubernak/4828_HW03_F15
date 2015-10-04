public class Monitor implements Runnable {

  private ProductionLine queue;

  public Monitor(ProductionLine queue) {
    this.queue = queue;
  }

  public void run() {
    while (true) {
      int size = queue.size();
      if (size > 10) {
        System.out.println("Too many items in the queue: " + size + "!");
      } else if (size > 0) {
        System.out.println("Queue Size: " + size);
      } else if (size == 0) {
        System.out.println("Queue empty!");
      } else if (size < 0) {
        System.out.println("WARNING: Queue size is negative!");
      }
      try {
        Thread.sleep(500);
      } catch (Exception ex) {
      }
    }
  }

}
