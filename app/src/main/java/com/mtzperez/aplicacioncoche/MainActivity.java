package com.mtzperez.aplicacioncoche;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mtzperez.aplicacioncoche.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
public class MainActivity extends AppCompatActivity implements SensorEventListener
{


    boolean empezado = false;
    ArrayList<Button> botones = new ArrayList<Button>();

    private SoundPool soundPool;
    private MediaPlayer motor;
    private int idDerrape;

    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = getApplicationContext();

        //sonidos


        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
        motor = MediaPlayer.create(context,R.raw.a);
        idDerrape = soundPool.load(context,R.raw.derrape,1);

        final TextView texto = (TextView) findViewById(R.id.editText);



        botones.add((Button) findViewById(R.id.jugador1));
        botones.add((Button) findViewById(R.id.jugador2));
        botones.add((Button) findViewById(R.id.jugador3));
        botones.add((Button) findViewById(R.id.jugador4));
        botones.add((Button) findViewById(R.id.jugador5));
        botones.add((Button) findViewById(R.id.jugador6));
        botones.add((Button) findViewById(R.id.jugador7));
        botones.add((Button) findViewById(R.id.jugador8));
        botones.add((Button) findViewById(R. id.jugador9));

        for(Button boton : botones)
        {
            final Button boton2 = boton;
            boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ip = texto.getText().toString();
                    puerto = 9090+Integer.parseInt(boton2.getText().toString().substring(boton2.getText().toString().length()-1));
                    if(!empezado)
                    {
                        activarSensores();
                        empezar();
                        empezado = true;
                    }
                }
            });
        }

        sMgr = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        mSensorGyr = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    private void botones()
    {
        for(Button boton : botones)
        {
            boton.setEnabled(false);
        }
    }

    SensorManager sMgr;
    Sensor mSensorGyr;

    public void activarSensores(){
        mSensorGyr = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sMgr.registerListener(this, mSensorGyr,SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void desactivarSensores() {
        //sMgr.unregisterListener(this);
        System.exit(0);
    }


    @Override
    protected void onResume(){
        super.onResume();
        activarSensores();
    }

    @Override
    protected void onPause(){
        desactivarSensores();
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        desactivarSensores();
        super.onDestroy();
    }

    @Override
    protected void onRestart(){
        activarSensores();
        super.onRestart();
    }

    @Override
    protected void onStop(){
        desactivarSensores();
        super.onStop();
    }

    @Override
    protected void onStart(){
        activarSensores();
        super.onStart();
    }



    public static int x = 0;
    public static int y = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        x = (int) event.values[0];
        y = (int) event.values[1];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    protected void empezar() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                asdasdsa();
            }
        };
        thread.start();

    }

    String ip="192.168.1.224";
    int puerto=9090;


    private void asdasdsa() {
        InetAddress inetAddress;
        DatagramSocket socket = null;
        try {
            inetAddress = InetAddress.getByName(ip);
            socket = new DatagramSocket();
            boolean derrapando = false;

            while (true) {
                if(x>=5 && y >= 3 && !derrapando){
                    soundPool.play(idDerrape,(float)0.03,(float)0.3,1,0,1);
                    derrapando = true;
                    motor.pause();
                }else if (x<= -4 && y <= 6 && !derrapando){
                    soundPool.play(idDerrape,(float)0.03,(float)0.3,1,0,1);
                    derrapando = true;
                    motor.pause();
                }else{
                    soundPool.stop(idDerrape);
                    derrapando = false;
                    motor.start();

                }

                byte[] buffer = {Byte.parseByte(String.valueOf(x).trim()),Byte.parseByte(String.valueOf(y).trim())};
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, puerto);
                socket.send(datagramPacket);
                Thread.sleep(100);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket!=null)
                socket.close();
        }
    }
}
