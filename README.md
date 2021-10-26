# lettuce-v6-performance-test

## What's this project?

This is a minimum reproducing project to investigate a performance degradation between Lettuce v5 -> v6.

I'm observing a slight performance degradation(about 10%) when I upgraded Lettuce v5 to v6.
https://github.com/lettuce-io/lettuce-core

It is acceptable for almost all projects, however, some high-performance projects have been affected.

## Test Environments

All of them are virtual machine with CentOS 7 bounded by 1Gbps network.

Redis Cluster

```
12 nodes cluster(6 primary, 6 replica)
4Core CPU, 32GB Memory, 100GB SSD

Redis : 5.0.10
```

Client machine

```
8Core CPU, 32GB Memory, 100GB SSD

Java : AdoptOpenJDK 11
```

## Test Procedure

Open 2 shells on client machine.

Execute gradle on a shell.

```
$ git clone https://github.com/toduq/lettuce-v6-performance-test.git
$ ./gradlew bootRun --args='redis.nodes=redis://192.0.2.1,redis://192.0.2.2,redis://192.0.2.3'
```

Execute ab on the other shell. (No warm up runs for JVM, etc...)

```
$ ab -n 900 -c 30 localhost:8080/hash/10000
```

## Results

### Summary

```
900 req, 10  parallel => v5: 5.31req/sec, v6: 4.61req/sec
900 req, 30  parallel => v5: 5.35req/sec, v6: 4.65req/sec
900 req, 50  parallel => v5: 5.02req/sec, v6: 4.25req/sec
900 req, 80  parallel => v5, 4.95req/sec, v6: 4.22req/sec
900 req, 100 parallel => v5, 4.92req/sec, v6: timeout
```

### Detail of 900req, 30 parallel

v5

```
Concurrency Level:      30
Time taken for tests:   168.295 seconds
Complete requests:      900
Failed requests:        0
Write errors:           0
Total transferred:      120600 bytes
HTML transferred:       1800 bytes
Requests per second:    5.35 [#/sec] (mean)
Time per request:       5609.839 [ms] (mean)
Time per request:       186.995 [ms] (mean, across all concurrent requests)
Transfer rate:          0.70 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       1
Processing:   777 5567 512.0   5497    7348
Waiting:      773 5566 512.0   5496    7348
Total:        778 5567 512.0   5497    7348

Percentage of the requests served within a certain time (ms)
  50%   5497
  66%   5682
  75%   5853
  80%   5980
  90%   6237
  95%   6346
  98%   6552
  99%   7229
 100%   7348 (longest request)
```

v6

```
Concurrency Level:      30
Time taken for tests:   193.663 seconds
Complete requests:      900
Failed requests:        0
Write errors:           0
Total transferred:      120600 bytes
HTML transferred:       1800 bytes
Requests per second:    4.65 [#/sec] (mean)
Time per request:       6455.445 [ms] (mean)
Time per request:       215.181 [ms] (mean, across all concurrent requests)
Transfer rate:          0.61 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       1
Processing:   637 6411 508.9   6357    8461
Waiting:      634 6411 508.8   6356    8461
Total:        638 6411 508.9   6357    8461

Percentage of the requests served within a certain time (ms)
  50%   6357
  66%   6523
  75%   6643
  80%   6746
  90%   6994
  95%   7279
  98%   7607
  99%   7933
 100%   8461 (longest request)
 ```

## Discussion

### Command per sec

Lettuce v5 offers about 5.0rps, and v6 offers about 4.5rps.

The difference is about 10%.

### CPU Usage

Both of v5 and v6 uses 85% of CPUs, 65% for users, 20% for kernels.

There are no significant difference.

### Network Usages

The v5 produces 100Mbps outbound network traffic, and v6 produces 130Mbps.

The v6 produces 30% more network traffic with 10% less commands compared with v5.

It's probably problematic...
