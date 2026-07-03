package cs477.gmu.mobile_library;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cs477.gmu.mobile_library.DialogUtils;
import cs477.gmu.mobile_library.BookDialogAnimations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BooksFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private DatabaseReference booksRef;
    private FloatingActionButton fabAddBook;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView dialogCoverImage;
    private String newlyAddedBookId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBooks);
        fabAddBook = view.findViewById(R.id.fabAddBook);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList, new BookAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                showBookDetailsDialog(book);
            }

            @Override
            public void onDeleteClick(Book book) {
                deleteBook(book);
            }
        });
        recyclerView.setAdapter(bookAdapter);

        booksRef = FirebaseUtils.getBooksReference();

        fabAddBook.setOnClickListener(v -> showAddBookDialog());

        loadBooks();

        return view;
    }

    private void loadBooks() {
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (book != null) {
                        bookList.add(book);
                    }
                }
                bookAdapter.notifyDataSetChanged();
                
                if (newlyAddedBookId != null) {
                    animateNewlyAddedBook(newlyAddedBookId);
                    newlyAddedBookId = null;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load books", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void animateNewlyAddedBook(String bookId) {
        int position = -1;
        for (int i = 0; i < bookList.size(); i++) {
            if (bookList.get(i).getId().equals(bookId)) {
                position = i;
                break;
            }
        }
        
        final int finalPosition = position;
        
        if (finalPosition >= 0) {
            recyclerView.post(() -> {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(finalPosition);
                if (viewHolder instanceof BookAdapter.BookViewHolder) {
                    BookAdapter.BookViewHolder bookViewHolder = (BookAdapter.BookViewHolder) viewHolder;
                    ImageView coverImage = bookViewHolder.getCoverImage();
                    if (coverImage != null) {
                        BookDialogAnimations.animateBouncePop(coverImage);
                    }
                }
            });
        }
    }

    private void showAddBookDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_book, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAuthor = dialogView.findViewById(R.id.etAuthor);
        EditText etIsbn = dialogView.findViewById(R.id.etIsbn);
        EditText etEdition = dialogView.findViewById(R.id.etEdition);
        EditText etGenre = dialogView.findViewById(R.id.etGenre);
        EditText etTotalPages = dialogView.findViewById(R.id.etTotalPages);
        EditText etTotalChapters = dialogView.findViewById(R.id.etTotalChapters);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        dialogCoverImage = dialogView.findViewById(R.id.ivCoverPreview);

        imageUri = null;
        if (dialogCoverImage != null) {
            dialogCoverImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String author = etAuthor.getText().toString().trim();
            String isbn = etIsbn.getText().toString().trim();
            String edition = etEdition.getText().toString().trim();
            String genre = etGenre.getText().toString().trim();
            String totalPagesStr = etTotalPages.getText().toString().trim();
            String totalChaptersStr = etTotalChapters.getText().toString().trim();

            if (title.isEmpty() || author.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in title and author", Toast.LENGTH_SHORT).show();
                return;
            }

            if (totalPagesStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter total pages", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalPages;
            try {
                totalPages = Integer.parseInt(totalPagesStr);
                if (totalPages == 0) {
                    Toast.makeText(getContext(), "Total pages must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid number for total pages", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalChapters = totalChaptersStr.isEmpty() ? 0 : Integer.parseInt(totalChaptersStr);

            Book book = new Book(title, author, isbn, edition, genre, totalPages, totalChapters);
            book.setId(booksRef.push().getKey());
            newlyAddedBookId = book.getId();
            saveBookWithImage(book, imageUri, false);
            imageUri = null;
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditBookDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_book, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        if (dialogTitle != null) {
            dialogTitle.setText("Edit Book");
        }
        
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAuthor = dialogView.findViewById(R.id.etAuthor);
        EditText etIsbn = dialogView.findViewById(R.id.etIsbn);
        EditText etEdition = dialogView.findViewById(R.id.etEdition);
        EditText etGenre = dialogView.findViewById(R.id.etGenre);
        EditText etTotalPages = dialogView.findViewById(R.id.etTotalPages);
        EditText etTotalChapters = dialogView.findViewById(R.id.etTotalChapters);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        
        btnSave.setText("Update Book");
        dialogCoverImage = dialogView.findViewById(R.id.ivCoverPreview);

        etTitle.setText(book.getTitle());
        etAuthor.setText(book.getAuthor());
        etIsbn.setText(book.getIsbn() != null ? book.getIsbn() : "");
        etEdition.setText(book.getEdition() != null ? book.getEdition() : "");
        etGenre.setText(book.getGenre() != null ? book.getGenre() : "");
        etTotalPages.setText(String.valueOf(book.getTotalPages()));
        etTotalChapters.setText(String.valueOf(book.getTotalChapters()));

        if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) {
            ImageUtils.loadBookCover(dialogCoverImage, book.getCoverImageUrl(), android.R.drawable.ic_menu_gallery);
        }

        imageUri = null;
        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String author = etAuthor.getText().toString().trim();
            String isbn = etIsbn.getText().toString().trim();
            String edition = etEdition.getText().toString().trim();
            String genre = etGenre.getText().toString().trim();
            String totalPagesStr = etTotalPages.getText().toString().trim();
            String totalChaptersStr = etTotalChapters.getText().toString().trim();

            if (title.isEmpty() || author.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in title and author", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalPages = totalPagesStr.isEmpty() ? 0 : Integer.parseInt(totalPagesStr);
            int totalChapters = totalChaptersStr.isEmpty() ? 0 : Integer.parseInt(totalChaptersStr);

            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbn);
            book.setEdition(edition);
            book.setGenre(genre);
            book.setTotalPages(totalPages);
            book.setTotalChapters(totalChapters);
            saveBookWithImage(book, imageUri, true);
            imageUri = null;
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveBookWithImage(Book book, Uri imageUri, boolean isUpdate) {
        if (imageUri != null) {
            try {
                Bitmap compressedBitmap = ImageUtils.compressImage(getContext(), imageUri);
                if (compressedBitmap != null) {
                    if (isUpdate) {
                        ImageUtils.deleteLocalImage(book.getCoverImageUrl());
                    }
                    String localImagePath = ImageUtils.saveImageLocally(getContext(), compressedBitmap, book.getId());
                    if (localImagePath != null) {
                        book.setCoverImageUrl(localImagePath);
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
            }
        }
        saveBook(book, isUpdate);
    }

    private void saveBook(Book book, boolean isUpdate) {
        String message = isUpdate ? "Book updated successfully" : "Book added successfully";
        String errorMessage = isUpdate ? "Failed to update book" : "Failed to add book";
        booksRef.child(book.getId()).setValue(book)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show());
    }

    private void showBookDetailsDialog(Book book) {
        DialogUtils.showBookDetailsDialog(getContext(), book, booksRef, false, false, 
                this::showEditBookDialog);
    }

    private void deleteBook(Book book) {
        View itemView = findBookItemView(book);
        DialogUtils.deleteBook(getContext(), book, booksRef, itemView);
    }

    private View findBookItemView(Book book) {
        int position = -1;
        for (int i = 0; i < bookList.size(); i++) {
            if (bookList.get(i).getId().equals(book.getId())) {
                position = i;
                break;
            }
        }

        if (position >= 0 && recyclerView != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                return viewHolder.itemView;
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (dialogCoverImage != null) {
                Picasso.get()
                        .load(imageUri)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(dialogCoverImage);
            }
        }
    }
}