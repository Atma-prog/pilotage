package com.example.pilotage;

/**
 * \file CUPiloter.java
 * \author Charles BOYER
 * \date 04 Avril 2020
 * \brief Ce fichier contient la definition de la classe CUPiloter
 * \version 0.2
 */

import org.json.JSONException;
import org.json.JSONObject;


/**
 * \class CUPiloter
 * \brief Classe gerant le cas d'utilisation "Piloter" de l'application
 *
 * Cette classe est heritee de la classe Thread.
 *
 * \version 0.2
 */
public class CUPiloter extends Thread
{
    private static final String TAG = "CUPiloter";

    private BNYCommandeDistante bnyCommandeDistante;
    private JSONObject jsonOrdre;

    /**
     * \brief Constructeur avec paramètres
     *
     * Permet d'instancier un objet de la classe CUPiloter.
     *
     * \param [in] bnyCommandeDistante : L'objet de la classe BNYCommandeDistante
     */
    public CUPiloter(BNYCommandeDistante bnyCommandeDistante)
    {
        this.bnyCommandeDistante = bnyCommandeDistante;
        jsonOrdre = new JSONObject();

        try
        {
            jsonOrdre.put("mouvement", 0);
            jsonOrdre.put("vitesse", 0);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * \fn public void run()
     * \brief Cette methode contient le code qui sera executer dans un thread.
     *
     * Redefinition de la methode "run()" presente dans la classe "Thread".
     * C'est dans cette methode que sera executer toutes les methodes pour permettre
     * la reussite du cas d'utilisation.
     *
     */
    @Override
    public void run()
    {
        JSONObject ordre;

        while(bnyCommandeDistante.get_statutConnexion())
        {
            ordre = jsonOrdre;
            bnyCommandeDistante.envoyerOrdre(ordre.toString());
        }
    }

    public void set_Ordre(JSONObject jsonOrdre)
    {
        this.jsonOrdre = jsonOrdre;
    }
}

