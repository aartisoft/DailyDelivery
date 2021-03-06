package in.dailydelivery.dailydelivery;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.RcOrderDetails;

public class ReccurringOrdersActivity extends AppCompatActivity implements RCOrdersAdapter.DeleteRco {
    AppDatabase db;
    List<RcOrderDetails> rcOrders;
    RCOrdersAdapter rcOrdersAdapter;
    RecyclerView recyclerView;
    TextView noRCOTV;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reccurring_orders);
        db = AppDatabase.getAppDatabase(this);
        recyclerView = findViewById(R.id.RcOrdersList);
        noRCOTV = findViewById(R.id.noRCOTV);
        LinearLayoutManager l1 = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(l1);
        DividerItemDecoration dividerItemDecoration1 = new DividerItemDecoration(recyclerView.getContext(),
                l1.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration1);
        sharedPreferences = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        new GetRcOrders().execute();

        getSupportActionBar().setTitle("Repeating Orders");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public boolean onSupportNavigateUp() {
        Intent userHomeActivityIntent = new Intent(this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(userHomeActivityIntent);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent userHomeActivityIntent = new Intent(this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(userHomeActivityIntent);
        finish();
    }

    @Override
    public void deleteRco(final int rcoId) {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Confirm Order Delete");
        builder.setMessage("Are you sure you want to delete the order?\n\nNOTE: ANY CONFIRMED ORDERS FOR TOMORROW WILL STILL BE DELIVERED");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String query = "orderId=" + rcoId;
                new DeleteOrder(query, rcoId).execute(getString(R.string.server_addr_release) + "del_rc_order.php");
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = builder.create();
        mDialog.show();

    }

    private class GetRcOrders extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            //Log.d("DD", "RC ORders size" + rcOrders.size());
            if (rcOrders.size() == 0) {
                noRCOTV.setVisibility(View.VISIBLE);
            } else {
                List<RcOrderDetails> r = new ArrayList<>();
                for (RcOrderDetails rcOrderDetails : rcOrders) {
                    if (rcOrderDetails.getStatus() != 2) r.add(rcOrderDetails);
                }
                rcOrders = r;
                rcOrdersAdapter = new RCOrdersAdapter(rcOrders, ReccurringOrdersActivity.this);
                recyclerView.setAdapter(rcOrdersAdapter);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            rcOrders = db.rcOrderDetailsDao().getRcOrders();
            return null;
        }
    }

    private class DeleteOrder extends AsyncTask<String, Void, String> {
        String query;
        int orderId;

        public DeleteOrder(String query, int orderId) {
            this.query = query;
            this.orderId = orderId;
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid." + e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(ReccurringOrdersActivity.this, "Order Deleted", Toast.LENGTH_LONG).show();
            ReccurringOrdersActivity.this.recreate();
            //refreshActivity();
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 1500;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);//* milliseconds *//*);
                conn.setConnectTimeout(15000); //* milliseconds *//*);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();
                ;
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                out.write(query.getBytes());
                out.flush();
                out.close();
                is = conn.getInputStream();

                String result = readIt(is, len);

                //Log.d("DD", "Result from webserver: " + result);
                try {
                    JSONObject resultJson = new JSONObject(result);
                    //Log.d("DD","Result:" + resultJson.getInt("result") + orderId);
                    if (resultJson.getInt("result") == 273) {
                        db.rcOrderDetailsDao().deleteByOrderId(orderId);
                        /*SharedPreferences.Editor editor = sharedPreferences.edit();
                        String key = "del_rco_"+orderId;
                        editor.putBoolean(key,true);
                        editor.commit();*/

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (SocketTimeoutException e) {
                return "timeout";
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            return "ok";
        }

        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException {
            int count;
            InputStreamReader reader;

            reader = new InputStreamReader(stream, "UTF-8");
            String str = new String();
            char[] buffer = new char[len];
            while ((count = reader.read(buffer, 0, len)) > 0) {
                str += new String(buffer, 0, count);
            }
            return str;
        }
    }
}
