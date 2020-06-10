package com.example.pilotage;

/**
 * \file EcranPilotage.java
 * \author Charles BOYER
 * \date 02 Avril 2020
 * \brief Ce fichier contient la definition de la classe EcranPilotage
 * \version 0.5
 */

//Todo Optimiser le code
//Todo Finir la documentation Doxygen (CUSeConnecter, EcranPilotage)
//Todo Faire le Toast pour le Handler
//Todo Ajout du support de la manette PS4


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * \class EcranPilotage
 * \brief Activitee secondaire de l'application "pilotage".
 *
 * C'est l'interface de pilotage de la voiture. L'utilisateur peut controler la voiture avec
 * les sliders, obtenir des informations sur le vehicule et regarder le flux video de la camera
 * embarquee sur la voiture.
 * Cette classe est heritee de la classe "AppCompatActivity".
 *
 */
public class EcranPilotage extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener
{
    //AFIN DE TESTER, SERA SUPPRIMER A LA FIN DU DEVELOPPEMENT
    private static final String TAG = "EcranPilotage";

    private SeekBar mDirection;
    private SeekBar mVitesse;
    private WebView mCamera;
    private TextView mVitesseMoyenne;
    private TextView mTempMoteur;
    private TextView mTempAmbiante;
    private TextView mConsommation;
    private TextView mBatterie;

    private Handler handlerThread;

    private BNYCommandeDistante bnyCommandeDistante;
    private CUSeConnecter cuSeConnecter;
    private CUVisualiserInfo cuVisualiserInfo;
    private CUPiloter cuPiloter;


    /**
     * \fn protected void onCreate(Bundle savedInstanceState)
     * \brief Redefinition de la methode "onCreate" de la classe "AppCompatActivity".
     *
     *  Methode gerant l'initialisation de l'activite. Cette dernière est appelee
     *  automatiquement au lancement de l'application.
     *  Pour plus d'information, voir la documentation officielle
     *  Android.
     *
     * \param [in] savedInstanceState L'etat de l'activite precedemment sauvegarde.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecran_pilotage);

        //On met en pleine écran
        View fullscreen = getWindow().getDecorView();
        fullscreen.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        //On associe chaque attributs aux widgets de l'activitee
        mDirection      = findViewById(R.id.seekBarDirection);
        mVitesse        = findViewById(R.id.seekBarVitesse);
        mCamera         = findViewById(R.id.webViewCamera);
        mVitesseMoyenne = findViewById(R.id.textViewVitesse);
        mTempMoteur     = findViewById(R.id.textViewTempMoteur);
        mTempAmbiante   = findViewById(R.id.textViewTempAmbiante);
        mConsommation   = findViewById(R.id.textViewConsommation);
        mBatterie       = findViewById(R.id.textViewBatterie);

        //On configure les Seekbar (joysticks)
        mDirection.setMin(-10);
        mVitesse.setMin(-10);
        mDirection.setMax(10);
        mVitesse.setMax(10);
        mDirection.setOnSeekBarChangeListener(this);
        mVitesse.setOnSeekBarChangeListener(this);

        //On extrait bnyCommandeDistante de l'objet Intent
        Intent ecranPilotage = getIntent();
        if (ecranPilotage != null)
        {
            bnyCommandeDistante = ecranPilotage.getParcelableExtra("bnyCommandeDistante");
            Log.i(TAG + "::onCreate","Objet bnyCommandeDistante : RECUPERER");
        }


        //On créer le Handler et la méthode pour afficher les infos du vehicule sur l'UI
        handlerThread = new Handler(getMainLooper())
        {
            @Override
            public void handleMessage(Message msg)
            {
                Log.i(TAG + "::handleMessage", "Message : RECU");
                switch (msg.obj.toString())
                {
                    case "erreurConnexion":
                        String titre = "Erreur connexion voiture";
                        String message = "La connexion avec la voiture a échoué";
                        genererMessageErreur(titre, message);
                        finish();
                        break;

                    case "valeurs":
                        Log.i(TAG + "::handleMessage", "Affichage des valeurs reçu sur l'IHM");
                        mVitesseMoyenne.setText(cuVisualiserInfo.get_valeurs()[0] + "km/h");
                        mTempAmbiante.setText(cuVisualiserInfo.get_valeurs()[1] + "°C");
                        mTempMoteur.setText(cuVisualiserInfo.get_valeurs()[2] + "°C");
                        mBatterie.setText(cuVisualiserInfo.get_valeurs()[3] + "%");
                        mConsommation.setText(cuVisualiserInfo.get_valeurs()[4] + " W");
                        break;
                }
            }
        };
        Log.i(TAG + "::onCreate", "handlerVisualiserInfo : CREER");


        //On créer les différents objets gérant les cas d'utilisations
        cuSeConnecter = new CUSeConnecter(bnyCommandeDistante, handlerThread);
        Log.i(TAG + "::onCreate", "Objet CUSeConnecter : CREER");

        cuPiloter = new CUPiloter(bnyCommandeDistante);
        Log.i(TAG + "::onCreate", "Objet CUPiloter : CREER");

        cuVisualiserInfo = new CUVisualiserInfo(bnyCommandeDistante, handlerThread);
        Log.i(TAG + "::onCreate", "Objet CUVisualierInfo : CREER");
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        //On lance la méthode pour se connecter à la voiture
        Log.i(TAG + "::onStart", "Methode connexionVoiture : LANCEMENT");
        connexionVoiture();

        //On lance les différentes méthodes pour piloter la voiture
        Log.i(TAG + "::onStart", "Méthode executerVisualiserInfo : LANCEMENT");
        executerVisualiserInfo();

        Log.i(TAG + "::onStart", "Methode executerPiloter : LANCEMENT");
        executerPiloter();

        Log.i(TAG + "::onStart", "Methode executerCameraVoiture : LANCEMENT");
        executerCameraVoiture();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        //On se deconnecte de commandeDistante
        Log.i(TAG + "::onDestroy", "Methode deconnexion : LANCEMENT");
        bnyCommandeDistante.deconnexion();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        JSONObject jsonOrdre = null;

        Vibrator vibreur = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibreur.vibrate(VibrationEffect.createOneShot(500,100));

        try
        {
            Log.i(TAG + "::onProgressChanged", "Methode analyserOrdre : LANCEMENT");
            jsonOrdre = analyserOrdre(mDirection.getProgress(), mVitesse.getProgress());

        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        Log.i(TAG + "::onProgressChanged", "Methode set_Ordre : LANCEMENT");
        cuPiloter.set_Ordre(jsonOrdre);
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        seekBar.setProgress(0);
    }

    /**
     * \fn public void afficherMessageErreur(String titre, String message)
     * \brief Methode permettant de generer un message d'erreur
     *
     * Le message d'erreur est une boite de dialogue de type
     * "AlertDialog" et ne contient qu'un bouton pour fermer la fenêtre.
     * Une fois le message d'erreur generer, la methode l'affiche a l'utilisateur
     *
     * \param [in] titre Le titre de la fenêtre du message d'erreur
     * \param [in] message Le message a afficher a l'utilisateur
     */
    public void genererMessageErreur(String titre, String message)
    {
        AlertDialog.Builder messageAlerte;

        messageAlerte = new AlertDialog.Builder(this);

        messageAlerte.setTitle(titre);
        messageAlerte.setMessage(message);
        messageAlerte.setPositiveButton("Ok", null);
        messageAlerte.create().show();
    }

    /**
     * \fn public public void boutonQuitter(View view)
     * \brief Methode du bouton "Quitter"
     *
     * C'est la methode du bouton "Quitter". Cette derniere lancera la methode
     * finDeSession().
     *
     * \param [in] view : La vue où l'utilisateur a clique.
     */
    public void boutonQuitter(View view)
    {
        finish();
    }

    /**
     * \fn public String public String analyserOrdre(int mouvement, int vitesse)
     * \brief Methode permettant d'analyser l'ordre recu de l'utilisateur
     *
     * La methode va creer un objet JSON à partir de ce que quelle recoit en parametre.
     * Elle retourne ensuite ce JSON sous forme d'une string.
     *
     * \param [in] mouvement : Le mouvement que doit effectuer la voiture
     * \param [in] vitesse : La vitesse ou doit aller la voiture
     * \return jsonOrdre : Le JSON sous forme d'une string
     */
    private JSONObject analyserOrdre(int mouvement, int vitesse) throws JSONException
    {
        JSONObject jsonOrdre = new JSONObject();

        jsonOrdre.put("mouvement", mouvement);
        jsonOrdre.put("vitesse", vitesse);

        return jsonOrdre;
    }


    public void connexionVoiture()
    {
        Log.i(TAG + "::connexionVoiture", "Thread cuSeConnecter : LANCEMENT");
        cuSeConnecter.start();
        try
        {
            cuSeConnecter.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * \fn public void executerPiloter()
     * \brief Methode gerant le cas d'utilisation "Piloter" de l'application.
     *
     * Cette derniere va lancer le thread present dans la classe "CUPiloter".
     *
     */
    public void executerPiloter()
    {
        //On lance le thread pour piloter la voiture
        Log.i(TAG + "::executerPiloter", "Thread CUPiloter : LANCEMENT");
        cuPiloter.start();
    }

    /**
     * \fn public void executerVisualiserInfo()
     * \brief Methode gerant le cas d'utilisation "Visualiser les info du véhicule" de l'application.
     *
     * Cette derniere va lancer le thread present dans la classe "CUVisualiser" et afficher
     * les valeurs receptionnees
     *
     */
    public void executerVisualiserInfo()
    {
        //On lance le thread pour avoir les informations du véhicule
        Log.i(TAG + "::executerVisualiserInfo", "Thread CUVisualiserInfo : LANCEMENT");
        cuVisualiserInfo.start();
    }

    /**
     * \fn public void executerCameraVoiture()
     * \brief Methode gerant le cas d'utilisation "Visualiser la camera du vehicule" de l'application.
     *
     * Cette derniere va utiliser la methode loadUrl() du widget WebView et charger la page Web
     * de la camera du vehicule. (disponible sur "http://ipVoiture:8081").
     * Plus d'informations sur la configuration de la camera, voir la documentation officiel de
     * MotionEye.
     */
    public void executerCameraVoiture()
    {
        //On affiche la page web où est diffuser le flux de la camera
        mCamera.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }
        });

        mCamera.loadUrl("http://" + bnyCommandeDistante.get_ipVoiture() + ":8081");
    }
}
