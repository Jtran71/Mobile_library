package cs477.gmu.mobile_library;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

public class DialogUtils {

    public interface OnEditBookListener {
        void onEditBook(Book book);
    }

    public static void showBookDetailsDialog(Context context, Book book, DatabaseReference booksRef, 
                                             boolean hideMarkAsReading, boolean hideMarkAsFinished, 
                                             OnEditBookListener editListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_book_details, null);
        builder.setView(dialogView);

        ImageView ivCover = dialogView.findViewById(R.id.ivCover);
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvAuthor = dialogView.findViewById(R.id.tvAuthor);
        TextView tvIsbn = dialogView.findViewById(R.id.tvIsbn);
        TextView tvEdition = dialogView.findViewById(R.id.tvEdition);
        TextView tvGenre = dialogView.findViewById(R.id.tvGenre);
        TextView tvPages = dialogView.findViewById(R.id.tvPages);
        TextView tvChapters = dialogView.findViewById(R.id.tvChapters);
        TextView tvProgress = dialogView.findViewById(R.id.tvProgress);
        Button btnEditBook = dialogView.findViewById(R.id.btnEditBook);
        Button btnUpdateProgress = dialogView.findViewById(R.id.btnUpdateProgress);
        Button btnMarkAsReading = dialogView.findViewById(R.id.btnMarkAsReading);
        Button btnMarkAsFinished = dialogView.findViewById(R.id.btnMarkAsFinished);

        if (hideMarkAsReading) btnMarkAsReading.setVisibility(View.GONE);
        if (hideMarkAsFinished) btnMarkAsFinished.setVisibility(View.GONE);
        if (editListener == null) btnEditBook.setVisibility(View.GONE);

        ImageUtils.loadBookCover(ivCover, book.getCoverImageUrl(), android.R.drawable.ic_menu_gallery);
        tvTitle.setText(book.getTitle());
        tvAuthor.setText("by " + book.getAuthor());
        tvIsbn.setText("ISBN: " + (book.getIsbn() != null ? book.getIsbn() : "N/A"));
        tvEdition.setText("Edition: " + (book.getEdition() != null ? book.getEdition() : "N/A"));
        tvGenre.setText("Genre: " + (book.getGenre() != null ? book.getGenre() : "N/A"));
        tvPages.setText(book.getPagesRead() + "/" + book.getTotalPages());
        tvChapters.setText(book.getChaptersRead() + "/" + book.getTotalChapters());
        tvProgress.setText("Progress: " + String.format("%.1f%%", book.getReadingProgress()));

        AlertDialog dialog = builder.create();

        if (editListener != null) {
            btnEditBook.setOnClickListener(v -> {
                editListener.onEditBook(book);
                dialog.dismiss();
            });
        }

        btnUpdateProgress.setOnClickListener(v -> {
            showUpdateProgressDialog(context, book, booksRef);
            dialog.dismiss();
        });

        btnMarkAsReading.setOnClickListener(v -> {
            updateBookStatus(context, book, booksRef, "reading");
            dialog.dismiss();
        });

        btnMarkAsFinished.setOnClickListener(v -> {
            updateBookStatus(context, book, booksRef, "finished");
            dialog.dismiss();
        });

        dialog.show();
        
        dialogView.post(() -> {
            BookDialogAnimations.animateDialogSlideIn(context, dialogView);
        });
    }

    public static void showUpdateProgressDialog(Context context, Book book, DatabaseReference booksRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_update_progress, null);
        builder.setView(dialogView);

        TextView tvCurrentPages = dialogView.findViewById(R.id.tvCurrentPages);
        TextView tvCurrentChapters = dialogView.findViewById(R.id.tvCurrentChapters);
        EditText etPagesRead = dialogView.findViewById(R.id.etPagesRead);
        EditText etChaptersRead = dialogView.findViewById(R.id.etChaptersRead);
        Button btnSaveProgress = dialogView.findViewById(R.id.btnSaveProgress);
        Button btnCancelProgress = dialogView.findViewById(R.id.btnCancelProgress);

        tvCurrentPages.setText("Current: " + book.getPagesRead() + "/" + book.getTotalPages() + " pages");
        tvCurrentChapters.setText("Current: " + book.getChaptersRead() + "/" + book.getTotalChapters() + " chapters");
        etPagesRead.setText(String.valueOf(book.getPagesRead()));
        etChaptersRead.setText(String.valueOf(book.getChaptersRead()));

        AlertDialog dialog = builder.create();

        btnSaveProgress.setOnClickListener(v -> {
            try {
                int pagesRead = Integer.parseInt(etPagesRead.getText().toString());
                int chaptersRead = Integer.parseInt(etChaptersRead.getText().toString());

                if (pagesRead > book.getTotalPages() || chaptersRead > book.getTotalChapters()) {
                    Toast.makeText(context, "Invalid progress values", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean wasFinished = "finished".equals(book.getStatus());
                book.setPagesRead(pagesRead);
                book.setChaptersRead(chaptersRead);

                boolean isNowFinished = false;
                if (pagesRead == book.getTotalPages() && book.getTotalPages() > 0) {
                    book.setStatus("finished");
                    isNowFinished = true;
                } else if (pagesRead > 0 && !"finished".equals(book.getStatus())) {
                    book.setStatus("reading");
                }

                final boolean shouldShowConfetti = isNowFinished && !wasFinished;
                final int finalPagesRead = pagesRead;
                final int finalTotalPages = book.getTotalPages();
                booksRef.child(book.getId()).setValue(book)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Progress updated", Toast.LENGTH_SHORT).show();
                            BookDialogAnimations.showProgressPopup(context, finalPagesRead, finalTotalPages);
                            if (shouldShowConfetti) {
                                BookDialogAnimations.showConfetti(context);
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to update progress", Toast.LENGTH_SHORT).show());

                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelProgress.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static void updateBookStatus(Context context, Book book, DatabaseReference booksRef, String status) {
        boolean wasFinished = "finished".equals(book.getStatus());
        book.setStatus(status);
        boolean isNowFinished = "finished".equals(status);
        final boolean shouldShowConfetti = isNowFinished && !wasFinished;
        
        booksRef.child(book.getId()).setValue(book)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Book status updated", Toast.LENGTH_SHORT).show();
                    if (shouldShowConfetti) {
                        BookDialogAnimations.showConfetti(context);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

    public static void deleteBook(Context context, Book book, DatabaseReference booksRef, View itemView) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete " + book.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (itemView != null && itemView.getParent() != null) {
                        BookDialogAnimations.animateCollapseRemove(itemView, () -> {
                            performBookDeletion(book, booksRef, context);
                        });
                    } else {
                        performBookDeletion(book, booksRef, context);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void performBookDeletion(Book book, DatabaseReference booksRef, Context context) {
        booksRef.child(book.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    ImageUtils.deleteLocalImage(book.getCoverImageUrl());
                    Toast.makeText(context, "Book deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete book", Toast.LENGTH_SHORT).show());
    }
}

