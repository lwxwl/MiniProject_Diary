package com.example.lwxwl.justtry;

import android.app.Dialog;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lwxwl on 2017/2/13.
 */

//和登陆有好多重复的地方，但是不知道该怎么简化
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.error)
    TextView error;
    private Dialog registerDialog;
    private String responseMsg = "";
    private static final int REQUEST_TIMEOUT = 5 * 1000;  // 设置请求超时10秒钟
    private static final int SO_TIMEOUT = 10 * 1000;      // 设置等待数据超时时间10秒钟
    private static final int LOGIN_OK = 1;

    @BindView(R.id.btn1)
    Button btn1;
    @BindView(R.id.btn2)
    Button btn2;
    @BindView(R.id.edt1)
    EditText mailbox;
    @BindView(R.id.edt2)
    EditText password;
    @BindView(R.id.btn3)
    Button Registerbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn1, R.id.btn2, R.id.edt1, R.id.edt2, R.id.btn3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                break;
            case R.id.btn2:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.edt1:
                error.setVisibility(View.GONE);
                break;
            case R.id.edt2:
                error.setVisibility(View.GONE);
                break;
            case R.id.btn3:
                Registerbtn();
                break;
            default:
                break;
        }
    }

    public void Registerbtn() {
        if (mailbox.getText().toString().trim().equals("")) {
            error.setVisibility(View.VISIBLE);
            error.setText("邮箱或密码错误");
            //还是和登陆一样的问题，怎么判断邮箱格式？？？
        } else if (password.getText().toString().trim().equals("")
                || password.getText().toString().trim().length() > 16
                || password.getText().toString().trim().length() < 6) {
            error.setVisibility(View.VISIBLE);
            error.setText("邮箱或密码错误");
        }
        //加LoadingDialog？显示"注册中，请稍等"，时间不够，害怕
        Thread registerThread = new Thread(new RegisterThread());
        registerThread.start();
    }

    // 初始化HttpClient，并设置超时
    public HttpClient getHttpClient() {
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
        HttpClient client = new DefaultHttpClient(httpParams);
        return client;
    }

    private boolean registerServer(String mailbox, String password) {
        boolean loginValidate = false;
        // 使用apache HTTP客户端实现
        String urlStr = "https://github.com/muxi-mini-project/2017-diary-backend/blob/master/app/api_1_0/users.py";
        HttpPost request = new HttpPost(urlStr);
        // 如果传递参数多的话，可以丢传递的参数进行封装
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        // 添加用户名和密码
        params.add(new BasicNameValuePair("mailbox", mailbox));
        params.add(new BasicNameValuePair("password", password));
        try {
            // 设置请求参数项
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpClient client = getHttpClient();
            // 执行请求返回相应
            HttpResponse response = client.execute(request);

            // 判断是否请求成功
            if (response.getStatusLine().getStatusCode() == 200) {
                loginValidate = true;
                // 获得响应信息
                responseMsg = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loginValidate;
    }

    // RegisterThread线程类
    class RegisterThread implements Runnable {

        @Override
        public void run() {
            String edt1 = mailbox.getText().toString();
            String edt2 = password.getText().toString();

            // URL合法，但是这一步并不验证密码是否正确
            boolean registerValidate = registerServer(edt1, edt2);
            Message msg = handler.obtainMessage();
            if (registerValidate) {
                if (responseMsg.equals("success")) {
                    msg.what = 0;
                    handler.sendMessage(msg);
                } else {
                    msg.what = 1;
                    handler.sendMessage(msg);
                }

            } else {
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }

        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        registerDialog.cancel();
                        Bundle bundle = new Bundle();
                        bundle.putString("mailbox", mailbox.getText()
                                .toString());
                        bundle.putString("password", password.getText().toString());
                        Intent intent = new Intent();
                        intent.putExtras(bundle);
                        setResult(RESULT_OK, intent);
                        RegisterActivity.this.finish();
                        Toast.makeText(RegisterActivity.this, "注册成功", 1).show();
                        break;
                    case 1:
                        registerDialog.cancel();
                        Toast.makeText(getApplicationContext(), "注册失败",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        registerDialog.cancel();
                        Toast.makeText(getApplicationContext(), "服务器连接失败！",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }
}



