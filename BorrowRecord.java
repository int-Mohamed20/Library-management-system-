package com.library.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "borrow_records")
@Data
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    private LocalDate borrowDate;

    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        BORROWED, RETURNED
    }
}
