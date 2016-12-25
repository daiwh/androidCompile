package com.example.wenhaidai.compile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Handler handler;
    ClientThread clientThread;  //定义与服务通讯的子线程

    //定义ContextMenu中每个菜单选项的Id
    final int Menu_1 = Menu.FIRST;
    final int Menu_2 = Menu.FIRST + 1;
    final int Menu_3 = Menu.FIRST + 2;
    EditText Et;
    private ClipboardManager mClipboard = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获得布局中的控件
        Et = (EditText)findViewById(R.id.Code);
        //给EditText注册上下文菜单
        registerForContextMenu(Et);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);//右下角的浮动按钮
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://www.baidu.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);//侧滑菜单
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 0x123){//如果消息来自于子线程
                    String m = msg.obj.toString(); //读取服务端发来的消息
                    EditText console = (EditText) findViewById(R.id.Console);
                    console.append(m);
                }
            }
        };
        clientThread = new ClientThread(handler);
        //客户端启动ClientThread线程创建网络连接，读取来自于服务器的数据
        new Thread(clientThread).start();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.c) {

        } else if (id == R.id.JAVA) {

        } else if (id == R.id.Perl) {

        } else if (id == R.id.Python) {

        } else if (id == R.id.compile) {
            try{
                EditText input = (EditText) findViewById(R.id.Code);
                EditText console = (EditText) findViewById(R.id.Console);
                console.setText("");
                Message msg = new Message();
                msg.what = 0x345;
                msg.obj = input.getText().toString();
                clientThread.revHandler.sendMessage(msg);
            } catch (Exception e) {
                Log.e(e.toString(),  ((EditText) findViewById(R.id.Code)).getText().toString());
            }

        } else if (id == R.id.run) {
            if(clientThread == null)
                Log.e("cuozuo", "caonidaye");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //创建ContextMenu菜单的回调方法
    public void onCreateContextMenu(ContextMenu m, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(m,v,menuInfo);

        //在上下文菜单选项中添加选项内容
        //add方法的参数：add(分组id,itemid, 排序, 菜单文字)
        m.add(0, Menu_1, 0, "复制文字");
        m.add(0, Menu_2, 0, "粘贴文字");
        m.add(0, Menu_3, 0, "全选文字");
    }
    private void copyFromEditText1() {

        // Gets a handle to the clipboard service.
        if (null == mClipboard) {
            mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        }

        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("simple text",Et.getText());

        // Set the clipboard's primary clip.
        mClipboard.setPrimaryClip(clip);
    }
    private void pasteToResult() {
        // Gets a handle to the clipboard service.
        if (null == mClipboard) {
            mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        }

        String resultString = "";
        // 检查剪贴板是否有内容
        if (!mClipboard.hasPrimaryClip()) {
            Toast.makeText(MainActivity.this,
                    "Clipboard is empty", Toast.LENGTH_SHORT).show();
        }
        else {
            ClipData clipData = mClipboard.getPrimaryClip();
            int count = clipData.getItemCount();

            for (int i = 0; i < count; ++i) {

                ClipData.Item item = clipData.getItemAt(i);
                CharSequence str = item
                        .coerceToText(MainActivity.this);
                Log.i("mengdd", "item : " + i + ": " + str);

                resultString += str;
            }

        }
        Toast.makeText(this, resultString, Toast.LENGTH_SHORT).show();
        Et.setText(resultString);
    }


    //ContextMenu菜单选项的选项选择的回调事件
    public boolean onContextItemSelected(MenuItem item) {
        //参数为用户选择的菜单选项对象
        //根据菜单选项的id来执行相应的功能
        switch (item.getItemId()) {
            case 1:
                Toast.makeText(this, "复制文字", Toast.LENGTH_SHORT).show();
                copyFromEditText1();
                break;
            case 2:
                Toast.makeText(this, "粘贴文字", Toast.LENGTH_SHORT).show();
                pasteToResult();
                break;
            case 3:
                Toast.makeText(this, "全选文字", Toast.LENGTH_SHORT).show();
                Et.selectAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
