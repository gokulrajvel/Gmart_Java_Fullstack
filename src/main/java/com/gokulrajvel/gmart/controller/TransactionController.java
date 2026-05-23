package com.gokulrajvel.gmart.controller;

import com.gokulrajvel.gmart.data.dto.InventoryTransaction;
import com.gokulrajvel.gmart.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<InventoryTransaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @PostMapping
    public InventoryTransaction recordTransaction(@RequestBody InventoryTransaction transaction) {
        return transactionService.recordTransaction(transaction);
    }
}
