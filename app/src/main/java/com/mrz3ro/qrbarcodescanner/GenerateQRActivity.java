package com.mrz3ro.qrbarcodescanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;

public class GenerateQRActivity extends AppCompatActivity {

    Toolbar toolbar;
    private TextView QROutput;
    private EditText QRInput;
    private Button GenerateBtn;
    private ImageView outputImage;
    private String text2Qr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Generate QR code");

        QRInput = findViewById(R.id.editText_input);
        outputImage = findViewById(R.id.imageView4);
        GenerateBtn = findViewById(R.id.btn_gnrt);
        QROutput = findViewById(R.id.textView_result);

        GenerateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(QRInput.getText().toString())){
                    Toast.makeText(GenerateQRActivity.this, "Input can not be empty.", Toast.LENGTH_SHORT).show();
                } else {
                    text2Qr = QRInput.getText().toString().trim();
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE, 500, 500);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        outputImage.setImageBitmap(bitmap);

                        QROutput.setText(QRInput.getText().toString());
                        QRInput.setText("");

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
}
