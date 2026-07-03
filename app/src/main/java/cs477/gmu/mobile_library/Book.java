package cs477.gmu.mobile_library;

import java.util.HashMap;
import java.util.Map;

public class Book {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String edition;
    private String genre;
    private String coverImageUrl;
    private int totalPages;
    private int totalChapters;
    private int pagesRead;
    private int chaptersRead;
    private String status;

    public Book() {
    }

    public Book(String title, String author, String isbn, String edition, String genre,
                int totalPages, int totalChapters) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.edition = edition;
        this.genre = genre;
        this.totalPages = totalPages;
        this.totalChapters = totalChapters;
        this.pagesRead = 0;
        this.chaptersRead = 0;
        this.status = "to_read";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getTotalChapters() { return totalChapters; }
    public void setTotalChapters(int totalChapters) { this.totalChapters = totalChapters; }

    public int getPagesRead() { return pagesRead; }
    public void setPagesRead(int pagesRead) { this.pagesRead = pagesRead; }

    public int getChaptersRead() { return chaptersRead; }
    public void setChaptersRead(int chaptersRead) { this.chaptersRead = chaptersRead; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("author", author);
        result.put("isbn", isbn);
        result.put("edition", edition);
        result.put("genre", genre);
        result.put("coverImageUrl", coverImageUrl);
        result.put("totalPages", totalPages);
        result.put("totalChapters", totalChapters);
        result.put("pagesRead", pagesRead);
        result.put("chaptersRead", chaptersRead);
        result.put("status", status);
        return result;
    }

    public double getReadingProgress() {
        if (totalPages > 0) {
            return (double) pagesRead / totalPages * 100;
        }
        return 0;
    }
}