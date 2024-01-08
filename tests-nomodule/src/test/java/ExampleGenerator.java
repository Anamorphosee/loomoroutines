import java.math.BigInteger;

import static dev.reformator.loomoroutines.common.GeneratorUtils.loomStream;

public class ExampleGenerator {
    public static void main(String[] args) {
        var fibinacciStream = loomStream(scope -> {
            var previous = BigInteger.ZERO;
            var current = BigInteger.ONE;
            while (true) {
                scope.emit(current);
                var tmp = previous.add(current);
                previous = current;
                current = tmp;
            }
        });
        fibinacciStream.limit(50).forEach(System.out::println);
    }
}
