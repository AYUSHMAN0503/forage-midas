package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Exposes GET /balance?userId=<id>
 * Returns a Balance {amount=<float>} JSON object.
 * If user not found, returns Balance {amount=0.0}
 */
@RestController
@RequestMapping("/balance")
public class BalanceController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public Balance getBalance(@RequestParam String userId) {
        Optional<UserRecord> userOpt = userRepository.findById(userId);
        float amount = userOpt.map(u -> (float) u.getBalance()).orElse(0.0f);
        return new Balance(amount);
    }
}
