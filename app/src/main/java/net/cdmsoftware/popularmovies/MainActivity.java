package net.cdmsoftware.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.stetho.Stetho;

import net.cdmsoftware.popularmovies.adapters.GalleryAdapter;
import net.cdmsoftware.popularmovies.sync.MovieSyncAdapter;

public class MainActivity extends AppCompatActivity implements GalleryAdapter.Callback {
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;
    private String mListType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main);
        mListType = Utilities.getPreferredListType(this);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            if (getSupportActionBar()!=null) {
                getSupportActionBar().setElevation(0f);
            }
            mTwoPane = false;
        }

        MovieSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String listType = Utilities.getPreferredListType(this);
        // update list if user change the preferred list type
        if (listType != null && !listType.equals(mListType)) {
            MovieFragment movieFragment = (MovieFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
            if (null != movieFragment) {
                movieFragment.onListTypeChanged();
            }

            mListType = Utilities.getPreferredListType(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri uri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, uri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(uri);
            startActivity(intent);
        }
    }
}
