package com.velkonost.controlyourself;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * @author Velkonost
 */

public class AppBlockedActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_blocked);

        setAppIcon();
        setSettingsBtnListener();
    }

    private void setSettingsBtnListener() {
        Button goSettingsBtn = (Button) findViewById(R.id.btn_settings);
        goSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AppBlockedActivity.this, MainActivity.class));
            }
        });
    }

    private void setAppIcon() {
        ImageView appIcon = (ImageView) findViewById(R.id.app_icon);

        Bundle extras = getIntent().getExtras();
        byte[] b = extras.getByteArray("icon");

        Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
        appIcon.setImageBitmap(bmp);
    }
}
