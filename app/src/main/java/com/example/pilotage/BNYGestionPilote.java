/**
 * \file BNYGestionPilote.java
 * \author Charles BOYER
 * \date 19 Mars 2020
 * \brief Ce fichier contient la definition de la classe BNYGestionPilote
 * \version 1.0
 */

package com.example.pilotage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * \class BNYGestionPilote
 * \brief Classe dialoguant avec le programme "gestionPilote".
 *
 * Cette classe sert d'intermédiaire entre le programme "gestionPilote", present
 * sur "systemeCentrale". A noter qu'elle herite de la classe Thread.
 *
 */
public class BNYGestionPilote extends Thread
{
    private String ipVoiture; 
    private ClientTCP clientTCP;
    private String login;
    private String motDePasse;

    /**
     * \brief Constructeur avec paramètres
     * 
     * Permet d'instancier un objet de la classe BNYGestionPilote.
     * 
     * \param [in] login : l'identifiant de l'utilisateur.
     * \param [in] motDePasse : le mot de passe de l'utilisateur.
     */
    BNYGestionPilote(String login, String motDePasse)
    {
        ipVoiture = null;
        clientTCP = new ClientTCP();
        this.login = login;
        this.motDePasse = motDePasse;
    }

    /**
     * \fn public String get_ipVoiture()
     * \brief Accesseur en lecture renvoyant l'attribut ipVoiture.
     * \return ipVoiture L'adresse IP de la voiture.
     */
    public String get_ipVoiture()
    {
        return ipVoiture;
    }


    public String get_login()
    {
        return login;
    }


    public String get_motDePasse()
    {
        return motDePasse;
    }

    /**
     * \fn private void verification(String login, String motDePasse)
     * \brief Methode permettant la verification du pilote auprès du programme "GestionPilote". 
     * 
     * Cette methode est privee car elle utilise des connexions TCP et doit 
     * donc être execute dans un Thread. Si jamais elle ne l'est pas, l'Activity Manager 
     * tuera immediatement l'application.
     * 
     * \param [in] login : L'identifiant de l'utilisateur.
     * \param [in] motDePasse : Le mot de passe de l'utilisateur.
     */
    private void verification()
    {
        JSONObject jsonVerif = new JSONObject();
        String reponseSystemeCentrale;

        try
        {
            jsonVerif.put("login", login);
            jsonVerif.put("motDePasse", motDePasse);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        clientTCP.ouvrirConnexion("192.168.1.16", 55555); //PC test Maison
        clientTCP.emettreMessage(jsonVerif.toString());
        reponseSystemeCentrale = clientTCP.recevoirMessage();
        clientTCP.fermerConnexion();

        ipVoiture = reponseSystemeCentrale;
    }

    /**
     * \fn public void run()
     * \brief Cette methode contenant le code qui sera executer dans un thread.
     * 
     * Redefinition de la methode "run()" presente dans la classe "Thread". 
     * 
     */
    @Override
    public void run()
    {
        verification();
    }
}
