package com.mai;

import com.mai.annotataions.RequestMapping;
import com.mai.annotataions.RequestBody;
import com.mai.server.HttpMethod;
import com.mai.server.SerializationHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BookResource {
    private SerializationHelper<Book> bookSerializationHelper;

    public BookResource() {
        bookSerializationHelper = new SerializationHelper<>(Book.class);
    }

    @RequestMapping(url = "/books")
    public String getBooks() {
        File f = new File("books");
        return "[" + Arrays
                .stream(f.listFiles())
                .map(s -> "/books/" + s.getName())
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(", ")) + "]";
    }

    @RequestMapping(url = "/books/{id}")
    public Book getBook(int id) {
        return bookSerializationHelper.loadFromFile("books/" + id);
    }

    @RequestMapping(url="/books/{id}", method = HttpMethod.DELETE)
    public void removeBook(int id) throws FileNotFoundException {
        File bookFile = new File("books/" + id);
        if (!bookFile.delete()) {
            throw new FileNotFoundException("Book with id " + id + " does not exist");
        }
    }

    @RequestMapping(url = "/books", method = HttpMethod.POST)
    public boolean saveBook(@RequestBody String requestBody) {
        Book toSave = bookSerializationHelper.parseJson(requestBody);
        return bookSerializationHelper.saveToFile("books/" + toSave.getIsbn(), toSave);
    }
}
