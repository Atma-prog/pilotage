package com.example.pilotage;

/**
 * \file MainActivity.java
 * \author Charles BOYER
 * \date 15 Mars 2020
 * \brief Ce fichier contient la definition de la classe MainActivity
 * \version 1.0
 */

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * \class MainActivity
 * \brief Activitee principal de l'application "pilotage".
 *
 * Elle est utiliser comme interface homme-machine pour permettre à l'utilisateur de
 * se connecter au systeme.
 * Cette classe est heritee de la classe "AppCompatActivity".
 *
 */
public class MainActivity extends AppCompatActivity
{
    //AFIN DE TESTER, SERA SUPPRIMER A LA FIN DU DEVELOPPEMENT
    private static final String TAG = "MainActivity";

    private Button mConnexion;
    private EditText mLogin;
    private EditText mMotDePasse;

    private BNYCommandeDistante bnyCommandeDistante;
    private BNYGestionPilote bnyGestionPilote;


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
        TextView mTitre;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //On associe chaque attributs aux widgets de l'activitee
        mConnexion  = (Button) findViewById(R.id.buttonConnexion);
        mLogin      = (EditText) findViewById(R.id.editTextLogin);
        mMotDePasse = (EditText) findViewById(R.id.editTextMdp);
        mTitre      = (TextView) findViewById(R.id.textViewTitre);

        mConnexion.setText("Connexion");
        mLogin.setHint("Identifiant");
        mMotDePasse.setHint("Mot de passe");
        mTitre.setText("Projet Récréatif de Voiture Télécommandée: \n");
        mTitre.append("pilotage");

        //UNIQUEMENT POUR TESTER (SERA ENLEVER EN VERSION 1.0)
        mLogin.setText("Test");
        mMotDePasse.setText("testtest");
        Log.i(TAG + "::onCreate", "Lancement de l'application");
    }


    @Override
    protected void onStart()
    {
        super.onStart();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    /**
     * \fn public void cliqueEnvoyer(View view)
     * \brief Methode gerant le signal "click" du bouton "Envoyer"
     *
     * Quand l'utilisateur appuye sur le bouton "Connexion". Cette methode est
     * automatiquement appeler
     *
     * \param [in] view : La vue où l'utilisateur a clique.
     */
    public void cliqueEnvoyer(View view)
    {
        String titre, message;

        if(!(mLogin.getText().toString().isEmpty() || mMotDePasse.getText().toString().isEmpty()))
        {
            Log.i(TAG + "::cliqueEnvoyer", "Methode executerSeConnecter : LANCEMENT");
            exectuerSeConnecter();
        }
        else
        {
            titre = "Attention";
            message = "Veuillez saisir vos informations de connexion";
            genererMessageErreur(titre, message);
        }
    }

    /**
     * \fn public void demarrerEcranPilotage()
     * \brief Methode permettant de lancer l'activite "ecranPilotage".
     *
     * Une fois que l'utilisateur est authentifie, cette methode est appelee et
     * permet d'afficher a ce dernier l'interface de pilotage du vehicule.
     *
     */
    public void demarrerEcranPilotage()
    {
        //On creer l'objet qui nous permettera de lancer l'autre activitee
        //On y ajoute l'objet "bnyCommandeDistante" car elle nous servira dans l'autre activitee
        Intent ecranPilotage = new Intent(this, EcranPilotage.class);

        Log.i(TAG + "::onCreate","Objet bnyCommandeDistante : AJOUT AU INTENT");
        ecranPilotage.putExtra("bnyCommandeDistante", bnyCommandeDistante);

        //On lance l'activitee
        startActivity(ecranPilotage);
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
     * \fn public void executerSeConnecter()
     * \brief Methode gerant le cas d'utilisation "Se Connecter" de l'application.
     *
     * Cette dernière va lancer les Threads present dans les classes BNYGestionPilote et
     * BNYCommandeDistante.
     * Si il y a une erreur durant l'execution de ces threads, un message d'erreur sera genere
     * a l'aide la methode "genererMessageErreur()".
     * Si aucune erreur n'est rencontree, la methode lancera la methode "demarrerEcranPilotage()"
     * et affichera a l'utilisateur l'interface de pilotage du vehicule.
     *
     */
    public void exectuerSeConnecter()
    {
        String titre, message;

        //On lance le thread de la classe BNYGestionPilote qui va verifier le login/mot de passe
        bnyGestionPilote = new BNYGestionPilote(mLogin.getText().toString(), mMotDePasse.getText().toString());
        Log.i(TAG + "::exectuerSeConnecter", "Objet BNYGestionPilote : CREER");

        Log.i(TAG + "::exectuerSeConnecter", "Thread BNYGestionPilote : LANCEMENT");
        bnyGestionPilote.start();
        try
        {
            //On attend que le thread de verification soit fini car il va obtenir l'adresse IP de la voiture, necessaire pour bnyCommandeDistante
            //try-catch obligatoire pour utiliser la methode join() [Java]
            bnyGestionPilote.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        //Si bnyGestionPilote confirme la presence du pilote, on continue
        if (!(bnyGestionPilote.get_ipVoiture().equals("non")))
        {

            //On lance le thread de la classe BNYCommandeDistante qui va se connecter a la voiture
            bnyCommandeDistante = new BNYCommandeDistante(bnyGestionPilote.get_login(), bnyGestionPilote.get_motDePasse(), bnyGestionPilote.get_ipVoiture());
            Log.i(TAG + "::exectuerSeConnecter", "Objet BNYCommandeDistante : CREER");

            Log.i(TAG + "::exectuerSeConnecter", "Méthode demarrerEcranPilotage : LANCEMENT");
            demarrerEcranPilotage();
        }
        else
        {
            titre = "Erreur: Enregistrement";
            message = "Votre login et/ou mot de passe est incorrecte";

            genererMessageErreur(titre, message);
        }
    }
}
