import java.util.concurrent.locks.*;

public class Producer implements Runnable {

  private ProductionLine queue;
  private int id;

  public Producer(int id, ProductionLine queue) {
    this.id    = id;
    this.queue = queue;
  }

  public void run() {
    int count = 0;
    while (count < 20) {

      // Synchronize this
      synchronized (queue) { 
      if (queue.size() < 10) {
        Product p = new Product();
        queue.append(p);
        String msg = "Producer %d Produced: %s on iteration %d";
        System.out.println(String.format(msg, id, p, count));
        count++;
      }
      }
      
    }
    // Synchronize this 
    synchronized (queue) { 
      Product p = new Product();
      p.productionDone();
      queue.append(p);
    }
    String msg = "Producer %d is done. Shutting down.";
    System.out.println(String.format(msg, id));
  }

}
