package com.library.repository;

import com.library.model.BorrowRecord;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUser(User user);
    List<BorrowRecord> findByUserOrderByBorrowDateDesc(User user);
    List<BorrowRecord> findByStatus(BorrowRecord.Status status);
    boolean existsByUserAndBookIdAndStatus(User user, Long bookId, BorrowRecord.Status status);
}
