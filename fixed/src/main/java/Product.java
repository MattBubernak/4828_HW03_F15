public class Product {

  private static int _id = 0;
  private String name;
  private int id;
  private boolean done;

  public Product() {
    id   = _id; // this is not protected. 
    name = "Product<" + id + ">";
    _id++;
    done = false;
  }

  public String toString() {
    return name;
  }

  public int id() {
    return id;
  }

  public boolean isDone() {
    return done;
  }

  public void productionDone() {
    done = true;
    _id--;
  }

}
