package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.dto.Bill;
import com.gokulrajvel.gmart.repository.BillRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BillService {
    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public Bill saveBill(Bill bill) {
        return billRepository.save(bill);
    }
}
