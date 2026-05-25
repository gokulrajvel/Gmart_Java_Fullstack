package com.gokulrajvel.gmart.repository;

import com.gokulrajvel.gmart.data.dto.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Integer> {
}
