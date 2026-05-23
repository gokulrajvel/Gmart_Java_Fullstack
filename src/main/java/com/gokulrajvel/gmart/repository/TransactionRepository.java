package com.gokulrajvel.gmart.repository;

import com.gokulrajvel.gmart.data.dto.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<InventoryTransaction, Integer> {
}
