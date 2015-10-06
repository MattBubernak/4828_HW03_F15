import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.*;


public class ProductionLine {

   private volatile List<Product> products;
   ReentrantLock lock1 = new ReentrantLock();
   Condition condition1 = lock1.newCondition();    

   public ProductionLine() {
     products = new LinkedList<Product>();
   }

   public int size() {
     return products.size();
   }

   public void append(Product p) {
     lock1.lock();
     try {
      while (!(products.size() < 10)) { condition1.await();}
      products.add(p);
      condition1.signal();
    } catch (InterruptedException e) {} finally { lock1.unlock();}
   }

   public Product retrieve() throws InterruptedException {
     // should never return this. 
    Product tmp;
    lock1.lock(); 
    try {
        while (!(products.size() > 0)) { condition1.await();}
        tmp = products.remove(0);
        condition1.signal();

     } catch (InterruptedException e) {tmp = new Product();} finally {lock1.unlock();}
    
    return tmp;
   }

}
