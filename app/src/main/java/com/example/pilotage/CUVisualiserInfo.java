package com.example.pilotage;

/**
 * \file CUPiloter.java
 * \author Charles BOYER
 * \date 05 Avril 2020
 * \brief Ce fichier contient la definition de la classe CUPiloter
 * \version 0.2
 */

import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * \class CUPiloter
 * \brief Classe gerant le cas d'utilisation "Visualiser les informations" de l'application
 *
 * Cette classe est heritee de la classe Thread.
 *
 * \version 0.2
 */
public class CUVisualiserInfo extends Thread
{
    //AFIN DE TESTER, SERA SUPPRIMER A LA FIN DU DEVELOPPEMENT
    private static final String TAG = "CUVisualiserInfo";

    private BNYCommandeDistante bnyCommandeDistante;
    private Handler handlerThread;
    private String[] valeurs;

    /**
     * \brief Constructeur avec paramètres
     *
     * Permet d'instancier un objet de la classe CUVisualiser.
     *
     * \param [in] bnyCommandeDistante : L'objet de la classe BNYCommandeDistante
     */
    public CUVisualiserInfo(BNYCommandeDistante bnyCommandeDistante, Handler handlerThread)
    {
        this.bnyCommandeDistante = bnyCommandeDistante;
        this.handlerThread = handlerThread;
        valeurs = new String[5];
        for (int i = 0; i < valeurs.length; i++)
        {
            valeurs[i] = null;
        }
    }

    /**
     * \fn public String get_ipVoiture()
     * \brief Accesseur en lecture renvoyant l'attribut valeurs.
     * \return valeurs : Le tableau contenant toutes les valeurs recuperer
     */
    public String[] get_valeurs()
    {
        return valeurs;
    }

    /**
     * \fn public void run()
     * \brief Cette methode contient le code qui sera executer dans un thread.
     *
     * Redefinition de la methode "run()" presente dans la classe "Thread".
     * C'est dans cette methode que sera executer toutes les methodes pour permettre
     * la reussite du cas d'utilisation.
     * Elle va lancer la methode "demanderInfoVehicule()" avec l'objet bnyCommandeDistante
     * et va stocker les valeurs recu dans un tableau de String.
     * Les valeurs recu sont ensuite ajouter dans l'attribut "valeurs" de la classe.
     *
     */
    @Override
    public void run()
    {
        String[] valeurRecu = new String[5];
        int i;

        while(bnyCommandeDistante.get_statutConnexion())
        {
            //On obtient un message de la file de message
            Message leMessage = handlerThread.obtainMessage();
            Log.i(TAG + "::run", "Message de la file : OBTENUE");

            //On demande les valeurs à commandeDistante
            Log.i(TAG + "::run", "Méthode demanderInfoVehicule : LANCEMENT");
            bnyCommandeDistante.demanderInfoVehicule(valeurRecu);

            //On affecte les valeurs recu dans l'attribut valeurs
            for (i = 0; i < valeurRecu.length; i++)
            {
                valeurs[i] = valeurRecu[i];
                Log.i(TAG + "::run", "Contenu de l'attribut valeurs : " + valeurs[i]);
            }

            //On envoie le message pour afficher les valeurs
            leMessage.obj = "valeurs";
            handlerThread.sendMessage(leMessage);
            Log.i(TAG + "::run", "Message : ENVOYER");
        }
    }
}
