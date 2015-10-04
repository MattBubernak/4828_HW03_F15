import java.util.Arrays;

public class Main {

  public static void main(String[] args) throws InterruptedException {
    ProductionLine queue = new ProductionLine();

    Thread monitor = new Thread(new Monitor(queue));

    monitor.setDaemon(true);
    monitor.start();

    Thread[] consumers = new Thread[10];

    Thread[] producers = new Thread[10];

    for (int i = 0; i < producers.length; i++) {
      producers[i] = new Thread(new Producer(i, queue));
      producers[i].start();
    }

    Thread.sleep(1000);

    for (int i = 0; i < consumers.length; i++) {
      consumers[i] = new Thread(new Consumer(i, queue));
      consumers[i].start();
    }

    for (int i = 0; i < consumers.length; i++) {
      try {
        consumers[i].join();
      } catch (Exception ex) {
      }
    }

    Integer [] keys = Consumer.products.keySet().toArray(new Integer[0]);

    Arrays.sort(keys);

    for (Integer key : keys) {
      System.out.println("" + key);
    }

    if (keys.length == 200)
    {
      System.out.println("success");
    }

  }

}
