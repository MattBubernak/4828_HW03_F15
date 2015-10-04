import java.util.concurrent.ConcurrentHashMap;

public class Consumer implements Runnable {

  public static ConcurrentHashMap<Integer,Product> products =
    new ConcurrentHashMap<Integer,Product>();

  private int id;
  private ProductionLine queue;

  public Consumer(int id, ProductionLine queue) {
    this.id    = id;
    this.queue = queue;
  }

  public void run() {
    while (true) {
      synchronized (queue) {
        if (queue.size() > 0) {
          Product p = queue.retrieve();
          if (p.isDone()) {
            String msg = "Consumer %d received done notification. Goodbye.";
            System.out.println(String.format(msg, id));
            return;
          } else {
            products.put(p.id(), p);
            String msg = "Consumer %d Consumed: %s";
            System.out.println(String.format(msg, id, p));
          }
        }
      }
    }
  }
}
