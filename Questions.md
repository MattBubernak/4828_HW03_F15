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

Very high CPU utilization, with no progress being made. If we had locks, we may suspect some sort of livelock situation. However, since there is no syncronization implemented, the issue must lie with the "while" loops in the producers/consumers. I added some basic output in the form of an "else" statement, in both the proder and consumer. I saw that all consumers stop because they see that `queue.size()` eventually starts returning 0, always. From the producers perspective, `queue.size()` returns some number larger than 10. Therefore, all threads keep looping and using CPU, but nothing is happening. My suspicion here is that there are the obvious race conditions in the queue modification(which also lead to the null pointer exception that ends the life of some threads, an artifcat of race conditions), but it seems like queue.size() reads seem to be getting stuck, as if they are not updating. My suspicion was this could have to do with the java memory barrier. modifying the following line in ProductionLine `private List<Product> products;` to include `volatile` seemed to change the behavior and prevent this infinite stuck loop, most of the time. 

  
  
#2: Broken2
Putting the keyword `synchronized` separately in the methods of 'ProductionLine.java' file sychronizes these methods independent of each other. However, it does not rid the program of the issues that arise because of queue size being used by the producer and the consumer threads. The following output that was obtained while running the producer/consumer program further explains the claim.

* The output of the product ids printed by the main thread looks like below. It doesn't print all the way from 0-199.

```
179
180
181
182
183
184
185
186
187
188
189
190
191
192
193
194
195

```

* On inspecting the code further, it seems that the producers are producing the products from 0-199 as evident by the following output:
```
Producer 7 Produced: Product<190> on iteration 10
Producer 7 Produced: Product<191> on iteration 11
Producer 7 Produced: Product<192> on iteration 12
Producer 7 Produced: Product<193> on iteration 13
Producer 7 Produced: Product<194> on iteration 14
Producer 7 Produced: Product<195> on iteration 15
Consumer 6 Consumed: Product<187>
Consumer 6 Consumed: Product<188>
Consumer 6 Consumed: Product<189>
Consumer 6 received done notification. Goodbye.
Producer 5 is done. Shutting down.
Consumer 9 Consumed: Product<190>
Consumer 9 Consumed: Product<191>
Consumer 9 Consumed: Product<192>
Consumer 9 Consumed: Product<193>
Consumer 9 Consumed: Product<194>
Consumer 9 Consumed: Product<195>
Consumer 9 Consumed: Product<186>
Consumer 9 received done notification. Goodbye.
Producer 7 Produced: Product<196> on iteration 16
Producer 7 Produced: Product<197> on iteration 17
Producer 7 Produced: Product<198> on iteration 18
Producer 7 Produced: Product<199> on iteration 19
Producer 7 is done. Shutting down.
0
1
2
3
4
5
6
7
8
9
10
```
* However, the consumers are only consuming items up until 195. On inspecting the output further, the output shows that there was a single instance of `IndexOutOfBoundsException`.

```
Producer 7 Produced: Product<152> on iteration 4Exception in thread "Thread-18" java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
	at java.util.LinkedList.checkElementIndex(LinkedList.java:553)
	at java.util.LinkedList.remove(LinkedList.java:523)
	at ProductionLine.retrieve(ProductionLine.java:21)
	at Consumer.run(Consumer.java:20)
	at java.lang.Thread.run(Thread.java:745)
```
 * Hence, although the producer and the consumers try to enqueue, dequeue and read the size of the queue in a synchronized fashion, the following scenario is still plausible:
  1. 9 consumer threads see the queue size was greater than zero when executing the following line of code in 'Consumer.java':
  	```
	if (queue.size() > 0)
	```
  2. They enter the `if` statement in 'Consumer.java' before any consumer thread can change the size of the queue. Then, they try to execute the following line of code:
	```
	Product p = queue.retrieve();
	```
  3. If there are only 8 elements in the queue and 9 threads trying to retrieve a product from the queue, it will cause 'IndexOutOfBoundsException'. When the 9th thread tries to retrieve an element, it will be shut down before of 'IndexOutOfBoundsException'. Therefore, even though all the producers produce unique products, not all the products will be dequeued from the queue and hence, the main thread will only print from 0-195.

#3 Fixed
