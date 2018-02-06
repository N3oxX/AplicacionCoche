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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener
{

    private boolean empezado = false;
    private ArrayList<Button> botones = new ArrayList<>();
    private SoundPool soundPool;
    private MediaPlayer motor;
    private MediaPlayer nitrosound;
    private int idDerrape;
    private boolean nitroActivado = false;
    private SensorManager sMgr;
    private Sensor mSensorGyr;
    private String ip;
    private int puerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ip y puerto por defecto
        this.ip="192.168.1.224";
        this.puerto=9090;

        //sonidos
        Context context = getApplicationContext();
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
        motor = MediaPlayer.create(context,R.raw.a);
        nitrosound = MediaPlayer.create(context,R.raw.nitro);
        idDerrape = soundPool.load(context,R.raw.derrape,1);

        //Direccion ip
        final TextView ipTextview = (TextView) findViewById(R.id.editText);

        //si el boton de nitro se pulsa se activa el nitro y el sonido
        Button nitroBoton = findViewById(R.id.buttonNitro);
        nitroBoton.setOnClickListener(new View.OnClickListener() {
                                          public void onClick(View v) {
                                             nitroActivado = true;
                                              nitrosound.start();
                                          }
                                      });

        //añadimos los botones a un array
        botones.add((Button) findViewById(R.id.jugador1));
        botones.add((Button) findViewById(R.id.jugador2));
        botones.add((Button) findViewById(R.id.jugador3));
        botones.add((Button) findViewById(R.id.jugador4));

        //recorremos los botones para asignarles un puerto y activar los sensores y empezar el envio
        for(Button boton : botones)
        {
            final Button boton2 = boton;
            boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //cogemos la ip a donde vamos a enviar los paquetes udp de el textbox
                    ip = ipTextview.getText().toString();
                    //asignamos un puerto que sera igual a 9090 mas el numero de el boton
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

        //asignmos el sensor acelerometro a la variable sensor
        sMgr = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        mSensorGyr = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    public void activarSensores(){
        mSensorGyr = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sMgr.registerListener(this, mSensorGyr,SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void desactivarSensores() {
        //sMgr.unregisterListener(this);
        System.exit(0);
    }




    //creamos las variables para la direccion
    public static int x = 0;
    public static int y = 0;


    //cada vez que cambie el sensor se asignara a (x,y) un valor
    @Override
    public void onSensorChanged(SensorEvent event) {
        x = (int) event.values[0];
        y = (int) event.values[1];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


    //crea un hilo y empieza el envio de paquetes udp con las coordenadas de los sensores
    protected void empezar() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                envio();
            }
        };
        thread.start();
    }


    private void envio() {
        //creamos el socket udp
        InetAddress inetAddress;
        DatagramSocket socket = null;

        boolean derrapando = false;

        try {
            inetAddress = InetAddress.getByName(ip);
            socket = new DatagramSocket();


            while (true) {
                //creamos los sonidos de derrape al girar los sensores
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

                /*
                si el nitro esta activado enviamos un paquete indicandolo
                sino se enviara (x,y) en un vector de byte
                */
                if(nitroActivado){
                    byte nitro = 55;
                    byte[] buffer = {Byte.parseByte(String.valueOf(x).trim()), nitro};
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, puerto);
                    socket.send(datagramPacket);
                    nitroActivado = false;
                }else {
                    byte[] buffer = {Byte.parseByte(String.valueOf(x).trim()), Byte.parseByte(String.valueOf(y).trim())};
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, puerto);
                    socket.send(datagramPacket);
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            if(socket!=null)
                socket.close();
        }
    }


    //comportamiento actividad
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
}
