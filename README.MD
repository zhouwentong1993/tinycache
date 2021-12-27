# tinycache
[![codecov](https://codecov.io/gh/zhouwentong1993/tinycache/branch/master/graph/badge.svg?token=E25NIF4IKF)](https://codecov.io/gh/zhouwentong1993/tinycache)
- 实现简单的 LRU
  ![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/5ae86f7c-e885-4e00-bd33-60875ecd1729/Untitled.png)

可以看到 UUID 占用了 37.6% 的执行时间，我替换成 Commons-lang3 的随机字符实现。 RandomStringUtils.randomAlphanumeric(10);

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/bcd21395-956c-4ae1-b0b5-b9e96ea2add0/Untitled.png)

可以看出，加锁操作占用了大量的时间。有没有办法优化呢？

---

将 ReentrantLock 替换成 synchronized 后。

```sql
Producing test result:
Total test time = 1173705482 ns.
Total item count = 1000000
Producer thread number = 4
Item message length = 6 bytes
Total producing time =  4671951067 ns.
Average producing time = 1167987766 ns.
-----------------------------------------------
Consuming test result:
Total test time = 1713025814 ns.
Total item count = 1000000
Consumer thread number = 4
Item message length = 6 bytes
Total consuming time =  2143403721 ns.
Average consuming time = 535850930 ns.
-----------------------------------------------
```

5218131514 → 4671951067

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/e44114d2-8a98-4d79-8d15-58877f3bd277/Untitled.png)

---

此时占用大量时间的是构造函数的调用，这个暂时无法优化。

剩下的是 HashMap 的优化，resize 占用了 8.6% 的时间。

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/cae1b3ce-0454-41ca-8297-4459852e4cf3/Untitled.png)

生成随机字符串的压力还是挺大的，优化之。

```java
package com.wentong.util;

import java.util.SplittableRandom;

public class RandomUtil {

    private RandomUtil(){}

    private static final SplittableRandom random = new SplittableRandom();

    private static final char[] arr = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static int LEN = arr.length;

    public static String generateRandomString(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(arr[random.nextInt(LEN)]);
        }
        return sb.toString();
    }

}
```

```
-----------------------------------------------
Producing test result:
Total test time = 994469185 ns.
Total item count = 1000000
Producer thread number = 4
Item message length = 6 bytes
Total producing time =  3965398597 ns.
Average producing time = 991349649 ns.
-----------------------------------------------
Consuming test result:
Total test time = 1471500932 ns.
Total item count = 1000000
Consumer thread number = 4
Item message length = 6 bytes
Total consuming time =  1897575975 ns.
Average consuming time = 474393993 ns.
-----------------------------------------------

我的，自定义 Random String 算法
```

4671951067 → 3965398597

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/5e1964d4-1970-4ba4-9c33-986c875eb200/Untitled.png)

| 模式 | 线程数 | 生产时间 | 消费时间 |
| --- | --- | --- | --- |
| ReentrantLock | 8 读 8 写 | 13601604630 | 13628321242 |
| ReentrantReadWriteLock | 8 读 8 写 | 14397303129 | 16170433604 |
| Synchronized | 8 读 8 写 | 10104818032 | 10543233059 |