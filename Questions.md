#1: Broken

There are clearly multiple concurrency related issues occurring with “broken” The behavior we see is generally something along the lines of this: Producers produce too many products(>10), monitor identifies the queue is too full(19), consumers begin consuming, there are sometimes null pointer exceptions, and then there is only visible monitor behavior from that point on. 

The first potential issue can be visible in this output: 
```
Producer 0 Produced: Product<0> on iteration 0
Producer 3 Produced: Product<11> on iteration 2
Producer 0 Produced: Product<12> on iteration 1
Producer 7 Produced: Product<6> on iteration 0
Producer 1 Produced: Product<0> on iteration 0
```

####Race Condition 1
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
 
####Race Condition 2
 
  The next issue is another race condition. The following section of code is a critical section for the producers, but is not protected. 
  
  ```
        if (queue.size() < 10) {
        Product p = new Product();
        String msg = "Producer %d Produced: %s on iteration %d";
        System.out.println(String.format(msg, id, p, count));
        queue.append(p);
        count++;
      }
  ```
   Because all producers can enter this section concurrently, they could all do a read on the queue size at say, 19, and all decide to produce one more product. Then, even though the queue is now full after one more production, an extra 9 products have been added. The below output, from the beginnig of the execution, shows that far too many products are creating, indicating another race condition problem.
  
  ```
Producer 5 Produced: Product<5> on iteration 0, 0
Producer 1 Produced: Product<2> on iteration 0, 0
Producer 6 Produced: Product<6> on iteration 0, 0
Producer 4 Produced: Product<4> on iteration 0, 0
Producer 1 Produced: Product<10> on iteration 1, 2
Producer 4 Produced: Product<12> on iteration 1, 4
Producer 1 Produced: Product<13> on iteration 2, 5
Producer 4 Produced: Product<14> on iteration 2, 6
Producer 9 Produced: Product<9> on iteration 0, 0
Producer 4 Produced: Product<16> on iteration 3, 8
Producer 9 Produced: Product<17> on iteration 1, 9
Producer 3 Produced: Product<0> on iteration 0, 0
Producer 8 Produced: Product<8> on iteration 0, 0
Producer 2 Produced: Product<0> on iteration 0, 0
Producer 1 Produced: Product<15> on iteration 3, 7
Producer 7 Produced: Product<7> on iteration 0, 0
Producer 0 Produced: Product<3> on iteration 0, 0
Producer 5 Produced: Product<10> on iteration 1, 2
Producer 6 Produced: Product<11> on iteration 1, 3
  ```
 
####Producers/Consumers stall out

When this code is run, this happens to my CPU. 

<img src="CPU_Utilization.PNG" alt="Drawing" style="width: 200px; height:200px;"/>

Very high CPU utilization, with no progress being made. If we had locks, we may suspect some sort of livelock situation. However, since there is no syncronization implemented, the issue must lie with the "while" loops in the producers/consumers...

  
  
#2 Broken2
  
#3 Fixed
