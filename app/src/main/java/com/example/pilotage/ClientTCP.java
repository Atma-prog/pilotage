package com.example.pilotage;

/**
 * \file ClientTCP.java
 * \author Charles BOYER
 * \date 18 Mars 2020
 * \brief Ce fichier contient la definition
 *      de la classe TCPClient
 * \version 1.0
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * \class ClientTCP
 * \brief Classe gerant les connexions TCP entre l'application "pilotage" et
 *      le programme "commandeDistante".
 *
 */
public class ClientTCP
{
    //AFIN DE TESTER, SERA SUPPRIMER A LA FIN DU DEVELOPPEMENT
    private static final String TAG = "ClientTCP";

    private Socket monSocket; /**< Le socket permettant la connexion */
    private PrintWriter monFluxEcriture; /**< L'objet permettant d'ecrire sur le flux de communication */
    private BufferedReader monFluxLecture; /**< L'objet permettant de lire sur le flux de communication */

    /**
     * \brief Constructeur sans paramètres
     *
     * Permet d'instancier un objet de la classe ClientTCP
     *
     */
    public ClientTCP()
    {
        monSocket = null;
        monFluxEcriture = null;
        monFluxLecture = null;
    }

    /**
     * \fn public void ouvrirConnexion(String adresseIP, int port)
     * \brief Methode permettant de commencer une communication TCP
     *
     * La methode initialise le socket avec l'adresse et le port en paramètre de la methode
     *
     * \param [in] adresseIP l'adresse IP du système avec qui communiquer.
     * \param [in] port le port du système avec qui communiquer.
     */
    public void ouvrirConnexion(String adresseIP, int port)
    {
        try
        {
            monSocket = new Socket(adresseIP, port);
            monFluxEcriture = new PrintWriter(new BufferedWriter(new OutputStreamWriter(monSocket.getOutputStream())), true);
            monFluxLecture = new BufferedReader(new InputStreamReader(monSocket.getInputStream()));
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Log.i(TAG + "::ouvrirConnexion", "Connexion ouverte...");
    }

    /**
     * \fn public void fermerCommunication()
     * \brief Methode permettant d'arrêter une communication ouverte
     */
    public void fermerConnexion()
    {
        try
        {
            monSocket.close();
            monFluxEcriture.close();
            monFluxLecture.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Log.i(TAG + "::fermerConnexion", "Connexion fermer...");
    }


    /**
     * \fn public void emettreMessage(String message)
     * \brief Methode permettant d'emettre un message
     *
     * La methode envoie le message qu'elle a en paramètre grace au socket
     * ouvert avec la methode "ouvrirConnexion()".
     *
     * \param [in] message : Le message a emettre.
     */
    public void emettreMessage(String message)
    {
        monFluxEcriture.write(message);
        monFluxEcriture.flush();

        Log.i(TAG + "::emettreMessage", "Message émit : " + message);
    }

    /**
     * \fn public String recevoirMessage()
     * \brief Methode permettant de recevoir un message
     * \return message : Le message recu du serveur
     */
    public String recevoirMessage()
    {
        String message = "";
        try
        {
            message = monFluxLecture.readLine();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Log.i(TAG + "::recevoirMessage", "Message reçu : " + message);
        return message;
    }
}


