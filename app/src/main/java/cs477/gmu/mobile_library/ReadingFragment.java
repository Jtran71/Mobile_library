package cs477.gmu.mobile_library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cs477.gmu.mobile_library.DialogUtils;

public class ReadingFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private DatabaseReference booksRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBooks);
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
        loadReadingBooks();

        return view;
    }

    private void loadReadingBooks() {
        booksRef.orderByChild("status").equalTo("reading")
                .addValueEventListener(new ValueEventListener() {
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Failed to load reading books", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showBookDetailsDialog(Book book) {
        DialogUtils.showBookDetailsDialog(getContext(), book, booksRef, true, false, null);
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

        if (position >= 0) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                return viewHolder.itemView;
            }
        }
        return null;
    }
}