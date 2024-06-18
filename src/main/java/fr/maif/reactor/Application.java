package fr.maif.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class Application {

    public static void main(String[] args) {

        Flux<Long> counterFlux = Flux.create(sink -> {
            AtomicLong counter = new AtomicLong(0);
            sink.onRequest(req -> {
                System.out.println("Request " + req);
                for (int i = 0; i < req; i++) {
                    sink.next(counter.incrementAndGet());
                }
            });
            // Do on cancel
            sink.onCancel(() -> {
                System.out.println("Cancel");
            });
            // Do on terminate
            sink.onDispose(() -> {
                System.out.println("Terminate");
            });
        });

        // 1 - brute
        // 2 - take
        // 3 - delay
        // 4 - buffer
        // 5 - delay + buffer
        // 6 - flatMap
        // 7 - flatMap + delay

        CountDownLatch cdl = new CountDownLatch(1);
        counterFlux
                .concatMap(elt ->
                        Mono.just(elt).delayElement(Duration.ofSeconds(1))
                )
                .subscribe(
                n -> {
                    System.out.println("Next valeur = " + n);
                },
                e -> {
                    cdl.countDown();
                    e.printStackTrace();
                },
                () -> {
                    System.out.println("Complete");
                    cdl.countDown();
                }
            );

        try {
            cdl.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
