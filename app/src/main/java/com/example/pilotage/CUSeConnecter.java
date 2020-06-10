package com.example.pilotage;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CUSeConnecter extends Thread
{
    private static final String TAG = "CUSeConnecter";
    private BNYCommandeDistante bnyCommandeDistante;
    private Handler handlerThread;

    public CUSeConnecter (BNYCommandeDistante bnyCommandeDistante, Handler handlerThread)
    {
        this.bnyCommandeDistante = bnyCommandeDistante;
        this.handlerThread = handlerThread;
    }

    @Override
    public void run()
    {
        Log.i(TAG + "::run", "Méthode connexion : LANCEMENT");
        bnyCommandeDistante.connexion();
        if (!bnyCommandeDistante.get_statutConnexion())
        {
            //On obtient un message de la file de message
            Message leMessage = handlerThread.obtainMessage();
            Log.i(TAG + "::run", "Message de la file : OBTENUE");

            //On envoie le message pour afficher le message d'erreur
            leMessage.obj = "erreurConnexion";
            handlerThread.sendMessage(leMessage);
            Log.i(TAG + "::run", "Message : ENVOYER");
        }
    }
}
