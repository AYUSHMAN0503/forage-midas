package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Prints all user balances (especially WALDORF) to the terminal
 * after the Spring Boot test context has finished initializing.
 */
@Component
public class BalancePrinter {

    private final UserRepository userRepository;

    public BalancePrinter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        new Thread(() -> {
            try {
                // Wait for transactions to finish processing
                Thread.sleep(10000); // 10 seconds delay
            } catch (InterruptedException ignored) {}

            System.out.println("\n========== 🧾 BALANCE REPORT (BalancePrinter) ==========");
            userRepository.findAll().forEach(u ->
                    System.out.println(u.getName() + " → " + u.getBalance())
            );

            Optional<UserRecord> waldorfOpt = StreamSupport.stream(userRepository.findAll().spliterator(), false)
                    .filter(u -> u.getName().equalsIgnoreCase("waldorf"))
                    .findFirst();

            if (waldorfOpt.isPresent()) {
                float balance = (float) waldorfOpt.get().getBalance();
                long floored = (long) Math.floor(balance);
                System.out.println("---------------------------------------------------------");
                System.out.println("💰 WALDORF BALANCE (float): " + balance);
                System.out.println("💰 WALDORF BALANCE (floored): " + floored);
                System.out.println("---------------------------------------------------------");
            } else {
                System.out.println("⚠️ WALDORF NOT FOUND IN DATABASE");
            }

            System.out.println("=========================================================\n");
        }).start();
    }
}
