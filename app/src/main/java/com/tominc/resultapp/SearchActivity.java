package com.tominc.resultapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tominc.resultapp.DataBase.DbContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    Toolbar toolbar;
    private final String GET_URL = Config.BASEURL + "4.php";
    ListView list;
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> rollno = new ArrayList<>();
    ArrayList<String> cgpis = new ArrayList<>();
    ArrayList<String> year_rank = new ArrayList<>();
    ArrayList<String> college_rank = new ArrayList<>();
    SemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);

        final EditText seach = (EditText) toolbar.findViewById(R.id.toolbar_edit);
        list = (ListView) findViewById(R.id.search_list);

        adapter = new SemAdapter(names, rollno, cgpis,year_rank,college_rank, getApplicationContext(), 4);
        list.setAdapter(adapter);

        seach.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (i == EditorInfo.IME_ACTION_DONE)) {
                String q=seach.getText().toString();
                    ContentValues v=new ContentValues();
                    v.put(DbContract.SEARCH_TABLE.TITLE,"Searched Result of "+q);
                    v.put(DbContract.SEARCH_TABLE.PARAMETER,q);
                    v.put(DbContract.SEARCH_TABLE.TARGET_ACTIVITY,"");
                    getContentResolver().insert(DbContract.insertHistory(),v);
                    requestSearch(q);
                }
                return false;
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String s=rollno.get(i);
                ContentValues v=new ContentValues();
                v.put(DbContract.SEARCH_TABLE.TITLE,"Searched "+s);
                v.put(DbContract.SEARCH_TABLE.PARAMETER,s);
                v.put(DbContract.SEARCH_TABLE.TARGET_ACTIVITY,"Semwise_result");
                getContentResolver().insert(DbContract.insertHistory(),v);
                Log.d("d",s);
                Intent in = new Intent(SearchActivity.this, Semwise_result.class);
                in.putExtra("roll",s );
                startActivity(in);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private void requestSearch(final String search){
        final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.show();

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, GET_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    names.clear();
                    rollno.clear();
                    cgpis.clear();
                    year_rank.clear();
                    college_rank.clear();
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray ar = jsonObject.getJSONArray("result");

                    for(int i=0;i<ar.length();i++){
                        JSONObject ob = ar.getJSONObject(i);
                        names.add(ob.getString("Name"));
                        cgpis.add(ob.getString("CGPI"));
                        rollno.add(ob.getString("roll_no"));
                        year_rank.add(ob.getString("Year"));
                        college_rank.add(ob.getString("College"));
                    }
                    dialog.dismiss();
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("VolleyError", volleyError.toString());
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", search);
                return params;
            }
        };

        rq.add(request);
    }
}
