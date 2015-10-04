#1

There are clearly multiple concurrency related issues occurring with “broken” The behavior we see is generally something along the lines of this: Producers produce too many products(>10), monitor identifies the queue is too full(19), consumers begin consuming, there are sometimes null pointer exceptions, and then there is only visible monitor behavior from that point on. 

The first potential issue can be visible in this output: 
```
Producer 0 Produced: Product<0> on iteration 0
Producer 3 Produced: Product<11> on iteration 2
Producer 0 Produced: Product<12> on iteration 1
Producer 7 Produced: Product<6> on iteration 0
Producer 1 Produced: Product<0> on iteration 0
```

Notice that the Product Id’s that producer 0 and producer 1 produced are the same(Product<0>). Products should all have unique ID’s, so this indicates some race condition is occuring with product creation. 

```
private static int _id = 0;

 public Product() {
    id   = _id;
    name = "Product<" + id + ">";
    _id++;
    done = false;
  }
  ```
  
  The issue is revealed by the constructor for Product. They are sharing one instance of _id, and using it to assign Id's. However, the creation of products is not synchronized in any way, so this critical section is unprotected. 
  
#2
  
#3
