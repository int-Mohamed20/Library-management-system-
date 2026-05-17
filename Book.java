package com.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "books")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotBlank
    private String isbn;

    private String category;

    @Min(0)
    private int totalCopies;

    @Min(0)
    private int availableCopies;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BorrowRecord> borrowRecords;
}
