package cs477.gmu.mobile_library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
        void onDeleteClick(Book book);
    }

    public BookAdapter(List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bind(book, listener);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCover;
        private TextView tvTitle;
        private TextView tvAuthor;
        private TextView tvProgress;
        private ImageView ivDelete;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(Book book, OnBookClickListener listener) {
            tvTitle.setText(book.getTitle());
            tvAuthor.setText(book.getAuthor());
            tvProgress.setText(String.format("%.1f%%", book.getReadingProgress()));

            ImageUtils.loadBookCover(ivCover, book.getCoverImageUrl(), R.drawable.ic_book_cover_placeholder);

            itemView.setOnClickListener(v -> listener.onBookClick(book));
            ivDelete.setOnClickListener(v -> listener.onDeleteClick(book));
        }

        public ImageView getCoverImage() {
            return ivCover;
        }
    }
}