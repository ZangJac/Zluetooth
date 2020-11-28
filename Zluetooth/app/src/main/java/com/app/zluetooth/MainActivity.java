package com.app.zluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.zluetooth.FSK.Receiver;
import com.app.zluetooth.FSK.Transmitter;
import com.app.zluetooth.Utils.Permissions;
import com.app.zluetooth.Utils.RigidData;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Transmitter transmitter;
    private Receiver receiver;
    private String src;
    private double sample_freq;
    private double pulse_sep;
    private double sample_period;
    private int number_of_carriers;
    private MediaPlayer mediaplayer;

    private String recovered_string;
    private EditText mEdit;
    private Button decode_btn;
    private Button transmit_btn;
    private Button receive_btn;
    private Button encode_btn;
    private TextView recovered_textView;
    private static String TAG = "Permission";
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sample_freq = RigidData.sample_freq;
        pulse_sep = RigidData.pulse_sep;
        sample_period = 1.0 / sample_freq;
        number_of_carriers = RigidData.number_of_carriers;

        Permissions.requestWritePermissions(this, MainActivity.this);
        Permissions.requestRecordPermissions(this, MainActivity.this);
        decode_btn = findViewById(R.id.decode_btn);
        transmit_btn = findViewById(R.id.transmit_btn);
        receive_btn = findViewById(R.id.receive_btn);
        encode_btn = findViewById(R.id.encode_btn);
        recovered_textView = findViewById(R.id.decode_data_txt);

        decode_btn.setOnClickListener(this);
        transmit_btn.setOnClickListener(this);
        receive_btn.setOnClickListener(this);
        encode_btn.setOnClickListener(this);

        mEdit = findViewById(R.id.raw_data_txt);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length == 0
                    || grantResults[0] !=
                    PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission denied ", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "permission denied ");
            } else {
                Toast.makeText(this, "permission granted ", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "permission granted ");
            }
        }
    }

    public void initReceive() {
        try {
            record();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initTransmit() {
        System.gc();
        mediaplayer = new MediaPlayer();
        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root, "0ZlueTooth");
        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            mediaplayer.setDataSource(dir + File.separator + "FSK.wav");
            mediaplayer.prepare();
            mediaplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generate() {
        System.gc();
        transmitter = new Transmitter(src, sample_freq, pulse_sep, number_of_carriers, getApplicationContext());
        System.out.println("Writing WavFile");
        transmitter.writeAudio();
        System.out.println("WaveFile Written. Thread waiting");
    }

    public void record() {
        recovered_textView.setText("");
        Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show();
        receiver = new Receiver("recorded.wav", sample_freq, pulse_sep, number_of_carriers, getApplicationContext());
        receiver.record_start();
    }

    public void decode() {
        Toast.makeText(this, "录音结束", Toast.LENGTH_SHORT).show();
        receiver.record_stop();
        receiver.recover_signal();
        recovered_string = "";
        recovered_string = receiver.getRecoverd_string();
        recovered_textView.setText(recovered_string);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.receive_btn: {
                initReceive();
                break;
            }
            case R.id.decode_btn:{
                decode();
                break;
            }
            case R.id.transmit_btn:{
                initTransmit();
                break;
            }
            case R.id.encode_btn:{
                src = mEdit.getText().toString();
                Log.e("待发送的字符串为：",src);
                if(src.length()<5){
                    int tmp=5-src.length();
                    for(int i=0;i<tmp;i++){
                        src += " ";
                    }
                }
                generate();
                Toast.makeText(this, "编码完成", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }
}
