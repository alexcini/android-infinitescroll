package br.com.heiderlopes.androidinfinitescroll;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.LinkedList;
import java.util.List;

import br.com.heiderlopes.androidinfinitescroll.adapter.MeuAdapter;
import br.com.heiderlopes.androidinfinitescroll.listener.InfiniteScrollListener;

public class ListaActivity extends AppCompatActivity {

    private static final int MAX_ITEMS_POR_REQUISICAO = 20;
    private static final int NUMERO_DE_ITEMS = 100;
    private static final int TEMPO_DE_LOADING_FAKE_EM_MS = 1500;

    public Toolbar toolbar;
    public RecyclerView recyclerView;
    public ProgressBar progressBar;

    private LinearLayoutManager layoutManager;
    private List<String> items;
    private int page;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        this.items = createItems();
        initViews();
        initRecyclerView();
        setSupportActionBar(toolbar);
    }

    private static List<String> createItems() {
        List<String> itemsLocal = new LinkedList<>();
        for (int i = 0; i < NUMERO_DE_ITEMS; i++) {
            String prefix = i < 10 ? "0" : "";
            itemsLocal.add("Produto #".concat(prefix).concat(String.valueOf(i)));
        }
        return itemsLocal;
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new MeuAdapter(items.subList(page, MAX_ITEMS_POR_REQUISICAO)));
        recyclerView.addOnScrollListener(criarInfiniteScrollListener());
    }

    @NonNull
    private InfiniteScrollListener criarInfiniteScrollListener() {
        return new InfiniteScrollListener(MAX_ITEMS_POR_REQUISICAO, layoutManager) {
            @Override public void onScrolledToEnd(final int firstVisibleItemPosition) {
                simularLoading();
                int start = ++page * MAX_ITEMS_POR_REQUISICAO;
                final boolean allItemsLoaded = start >= items.size();
                if (allItemsLoaded) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    int end = start + MAX_ITEMS_POR_REQUISICAO;
                    final List<String> itemsLocal = getItemsToBeLoaded(start, end);
                    refreshView(recyclerView, new MeuAdapter(itemsLocal), firstVisibleItemPosition);
                }
            }
        };
    }

    @NonNull private List<String> getItemsToBeLoaded(int start, int end) {
        List<String> newItems = items.subList(start, end);
        final List<String> oldItems = ((MeuAdapter) recyclerView.getAdapter()).getItems();
        final List<String> itemsLocal = new LinkedList<>();
        itemsLocal.addAll(oldItems);
        itemsLocal.addAll(newItems);
        return itemsLocal;
    }

    private void simularLoading() {
        new AsyncTask<Void, Void, Void>() {
            @Override protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(TEMPO_DE_LOADING_FAKE_EM_MS);
                } catch (InterruptedException e) {
                    Log.e("MainActivity", e.getMessage());
                }
                return null;
            }

            @Override protected void onPostExecute(Void param) {
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }
}
