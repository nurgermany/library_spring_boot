package practice.library.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.library.models.Book;
import practice.library.models.Person;
import practice.library.repositories.BooksRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BooksRepository booksRepository;

    private final PeopleService peopleService;

    @Autowired
    public BookService(BooksRepository booksRepository, PeopleService peopleService) {
        this.booksRepository = booksRepository;
        this.peopleService = peopleService;
    }

    public List<Book> index(boolean sorted) {
        if (sorted) {
            return booksRepository.findAll(Sort.by("yearOfProd"));
        }
        else {
            return booksRepository.findAll();
        }
    }

    public List<Book> index(Integer page, Integer booksPerPage, boolean sorted) {
        if(sorted) {
            return booksRepository.findAll(PageRequest.of(page, booksPerPage,
                    Sort.by("yearOfProd"))).getContent();
        } else return booksRepository.findAll(PageRequest.of(page, booksPerPage)).getContent();
    }

    public Book show(long id) {
        Optional<Book> foundBook = booksRepository.findById(id);
        return foundBook.orElse(null);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void save(Book book) {
        booksRepository.save(book);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void update(long id, Book updatedBook) {
        updatedBook.setUpdatedAt(new Date());
        updatedBook.setUpdatedBy(peopleService.getPersonDetails().getUsername());
        updatedBook.setId(id);
        booksRepository.save(updatedBook);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(long id) {
        booksRepository.deleteById(id);
    }

    public Person getOwner(long id) {
        Optional<Book> bookOptional = booksRepository.findById(id);
        return bookOptional.map(Book::getOwner).orElse(null);
    }

    @Transactional
    public void freeBook(long id) {
        Optional<Book> bookOptional = booksRepository.findById(id);
        bookOptional.ifPresent(book -> book.setOwner(null));
        bookOptional.ifPresent(book -> book.setTakenAt(null));
        bookOptional.ifPresent(booksRepository::save);
    }

    @Transactional
    public void takeBook(long id, Person person) {
        Optional<Book> bookOptional = booksRepository.findById(id);
        bookOptional.ifPresent(book -> book.setOwner(person));
        bookOptional.ifPresent(book -> book.setTakenAt(new Date()));
        bookOptional.ifPresent(booksRepository::save);
    }

    public List<Book> search(String contain) {
        return booksRepository.findBookByTitleContainingIgnoreCase(contain);
    }
}
