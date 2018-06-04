import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ReactorCoreExamples {

    private static List<String> words = Arrays.asList(
            "the",
            "quick",
            "brown",
            "fox",
            "jumped",
            "over",
            "the",
            "lazy",
            "dog"
    );

    // flatMap or map?
    @Test
    public void fluxExample() {
        Flux<String> letters = Flux.fromIterable(words)
                .concatWith(Mono.just("small"))
                .log()
                .flatMap(word -> Flux.fromArray(word.split("")))
                .distinct()
                .sort()
                .zipWith(Flux.range(1, 26), (s, count) -> String.format("%d. %s", count, s));

        letters.subscribe(System.out::println);
    }

    // Use case for buffer?
    @Test
    public void buffer() throws InterruptedException {
        Flux<String> letters = Flux.fromIterable(words)
                .concatWith(Mono.just("small"))
                .flatMap(word -> Flux.fromArray(word.split("")))
                .distinct()
                .sort()
                .zipWith(Flux.range(1, 26), (s, count) -> String.format("%d. %s", count, s))
                .delayMillis(1000);

        letters
                .buffer(Duration.ofMillis(2000))
//                .sample(Duration.ofSeconds(2))
                .subscribe(System.out::println);
        Thread.sleep(27000);
    }

    // Resubscribe?
    @Test
    public void retry() {
        Flux<String> letters = Flux.fromIterable(words)
                .concatWith(Mono.just("small"))
                .flatMap(word -> Flux.fromArray(word.split("")))
                .distinct()
                .sort()
                .zipWith(Flux.range(1, 26), (s, count) -> String.format("%d. %s", count, s))
                .doOnNext(s -> {
                    if (s.contains("5.")) {
                        throw new RuntimeException("5 is bad exception");
                    }
                });

        //  letters.subscribe(System.out::println);
        // letters.retry(2).subscribe(System.out::println);
    }

    @Test
    public void connectableFlux() throws InterruptedException {
        ConnectableFlux<Object> flux = Flux.create(fluxSink -> {

            while (true) {
                fluxSink.next(System.currentTimeMillis());
            }
        })
                .subscribeOn(Schedulers.parallel())
                .sample(Duration.ofSeconds(1))
                .publish();
//        .replay();

        System.out.println("Subscribing 1");
        flux.subscribe(o -> System.out.println("Sub1 : " + o));

        flux.connect();
        Thread.sleep(2000);

        System.out.println("Subscribing 2");
        flux.subscribe(o -> System.out.println("Sub2 : " + o));

        Thread.sleep(5000);

    }
}
