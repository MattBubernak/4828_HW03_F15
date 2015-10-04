import java.util.concurrent.locks.*;

public class Producer implements Runnable {

  private ProductionLine queue;
  private int id;
  private static Object obj1 = new Object();
  Product p;
  //ReentrantLock lock1 = new ReentrantLock();

  public Producer(int id, ProductionLine queue) {
    this.id    = id;
    this.queue = queue;
  }

  public void run() {
    int count = 0;
    while (count < 20) {
      synchronized(obj1){
        p = new Product();
      }
      queue.append(p);
      String msg = "Producer %d Produced: %s on iteration %d";
      System.out.println(String.format(msg, id, p, count));
      count++;
      
    }
    synchronized(obj1){
      p = new Product();
    }
    p.productionDone();
    queue.append(p);
    String msg = "Producer %d is done. Shutting down.";
    System.out.println(String.format(msg, id));
    
  }

}
