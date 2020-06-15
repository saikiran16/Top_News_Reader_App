package com.c.appreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
     ArrayList<String> arr = new ArrayList<>();
     static ArrayList<String> urls = new ArrayList<>();
     ArrayAdapter arrayAdapter = null;
    ListView listView ;
    SQLiteDatabase db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView  = (ListView)findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this , android.R.layout.simple_list_item_1 , arr);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long l) {
                Intent intent = new Intent(getApplicationContext() , DisplayNewsActivity.class) ;
                intent.putExtra("item" , item);
                startActivity(intent);
            }
        });
        db = this.openOrCreateDatabase("News" , MODE_PRIVATE, null);
        String q = "create table if not exists newsTable ( id Integer Primary key  , title vachar2(100) , content varchar2(200))";
        db.execSQL(q);
        DownloadTask task = new DownloadTask();
        try{
              task.execute("https://newsapi.org/v2/top-headlines?country=in&category=business&apiKey=9ff6f7d2933f4f309fdf0bbfc1932c03");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        UpdateTheArrayLsit();



    }

    public class DownloadTask extends AsyncTask<String , Void , String>{

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            try {
                db.execSQL("delete from newsTable");
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream ins = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(ins);
                int c ;
                while( (c = reader.read()) != - 1 ){
                    result +=  (char) c;
                }

                Log.i("Url Content" , result);
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("articles");
                for(int i = 0 ; i < jsonArray.length() ; i++){
                    JSONObject everyObjectInArray = (JSONObject) jsonArray.get(i);
                    String urlOfNews = everyObjectInArray.getString("url");
                    String title = everyObjectInArray.getString("title");
                    Log.i("new url " , urlOfNews);
                    String q = "insert into newsTable values ( ? , ? ,? )";
                    SQLiteStatement stmt = db.compileStatement(q);
                    stmt.bindString( 1  , String.valueOf(i));
                    stmt.bindString(2,title);
                    stmt.bindString(3 , urlOfNews);
                    stmt.execute();

                }






            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            UpdateTheArrayLsit();
        }

        @Override
        protected void onPreExecute() {
            UpdateTheArrayLsit();
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private void UpdateTheArrayLsit() {
        Cursor c = db.rawQuery("select * from newsTable", null);

        if(c.moveToFirst()){
            arr.clear();
            urls.clear();
        }
        try {
            int contentIndex = c.getColumnIndex("content");
            int titleIndex = c.getColumnIndex("title");
            Log.i("Async Task " , "is on charge");
            do {
                arr.add(c.getString(titleIndex));
                urls.add(c.getString(contentIndex));
                Log.i("count ", "1");

            } while (c.moveToNext());
            arrayAdapter.notifyDataSetChanged();
        }catch(Exception e){e.printStackTrace();}


    }

}
