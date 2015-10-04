public class Producer implements Runnable {

  private ProductionLine queue;
  private int id;

  public Producer(int id, ProductionLine queue) {
    this.id    = id;
    this.queue = queue;
  }

  public void run() {
    int count = 0;
    int size;
    while (count < 20) {
      size = queue.size();
      if (size < 10) {
        Product p = new Product();
        String msg = "Producer %d Produced: %s on iteration %d, %d";
        System.out.println(String.format(msg, id, p, count,size));
        queue.append(p);
        count++;
      }
    }
    Product p = new Product();
    p.productionDone();
    queue.append(p);
    String msg = "Producer %d is done. Shutting down.";
    System.out.println(String.format(msg, id));
  }

}
