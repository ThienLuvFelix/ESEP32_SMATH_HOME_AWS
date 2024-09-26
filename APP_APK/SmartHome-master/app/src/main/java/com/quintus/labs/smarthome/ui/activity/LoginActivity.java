package com.quintus.labs.smarthome.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.quintus.labs.smarthome.R;

public class LoginActivity extends AppCompatActivity {
    EditText editTextUser, editTextPass;
    public static void setWindowFlag(Activity activity, final int bits, boolean on) {

        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTextUser = findViewById(R.id.editxTextUser);
        editTextPass = findViewById(R.id.editxTextPass);
    }

    public void onLoginClicked(View view) {
        String inputUser, inputPass;
        inputUser = editTextUser.getText().toString();
        inputPass = editTextPass.getText().toString();
        if(inputUser.isEmpty() || inputPass.isEmpty()){
            Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
        }
        else{
            if(inputUser.equals("TranChinh") && inputPass.equals("Doantotnghiep")){
                Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
            else{
                Toast.makeText(LoginActivity.this, "Wrong username or password", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
