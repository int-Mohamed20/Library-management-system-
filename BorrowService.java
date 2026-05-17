package com.library.service;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;

    public BorrowService(BorrowRecordRepository borrowRecordRepository, BookRepository bookRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
    }

    public String borrowBook(User user, Long bookId) {
        Optional<Book> optBook = bookRepository.findById(bookId);
        if (optBook.isEmpty()) return "Book not found";

        Book book = optBook.get();
        if (book.getAvailableCopies() <= 0) return "No available copies";

        boolean alreadyBorrowed = borrowRecordRepository.existsByUserAndBookIdAndStatus(user, bookId, BorrowRecord.Status.BORROWED);
        if (alreadyBorrowed) return "You already borrowed this book";

        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setStatus(BorrowRecord.Status.BORROWED);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        borrowRecordRepository.save(record);
        return "success";
    }

    public String returnBook(Long recordId, User user) {
        Optional<BorrowRecord> opt = borrowRecordRepository.findById(recordId);
        if (opt.isEmpty()) return "Record not found";

        BorrowRecord record = opt.get();
        if (!record.getUser().getId().equals(user.getId())) return "Unauthorized";
        if (record.getStatus() == BorrowRecord.Status.RETURNED) return "Already returned";

        record.setStatus(BorrowRecord.Status.RETURNED);
        record.setReturnDate(LocalDate.now());

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
        borrowRecordRepository.save(record);
        return "success";
    }

    public List<BorrowRecord> getUserHistory(User user) {
        return borrowRecordRepository.findByUserOrderByBorrowDateDesc(user);
    }

    public List<BorrowRecord> getAllRecords() {
        return borrowRecordRepository.findAll();
    }

    public List<BorrowRecord> getActiveBorrows() {
        return borrowRecordRepository.findByStatus(BorrowRecord.Status.BORROWED);
    }
}
