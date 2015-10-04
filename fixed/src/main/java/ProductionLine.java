import java.util.LinkedList;
import java.util.List;


public class ProductionLine {

   private volatile List<Product> products;

   public ProductionLine() {
     products = new LinkedList<Product>();
   }

   public synchronized int size() {
     return products.size();
   }

   public synchronized void append(Product p) {
     products.add(p);
   }

   public synchronized Product retrieve() {
     return products.remove(0);
   }

}
