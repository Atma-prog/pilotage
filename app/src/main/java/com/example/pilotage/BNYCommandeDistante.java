package com.example.pilotage;

/**
 * \file BNYCommandeDistante.java
 * \author Charles BOYER
 * \date 21 Mars 2020
 * \brief Ce fichier contient la definition de la classe BNYCommandeDistante
 * \version 1.0
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * \class BNYCommandeDistante
 * \brief Classe dialoguant avec le programme "commandeDistante".
 *
 * Cette classe est heritee de la classe "Thread" et implemente l'interface "Serializable".
 * Cela est dû au fait que l'objet de cette classe va naviguer entre les differentes
 * activitees de l'application et va donc être integre dans un objet de la classe "Intent".
 * Cependant, pour qu'un objet (hors type de base) puisse être integre, il faut que sa classe
 * implemente l'interface "Serializable".
 * Pour plus d'information sur l'interface, voir la documentation officielle Android.
 *
 * \version 1.0
 */
public class BNYCommandeDistante implements Parcelable
{
    //AFIN DE TESTER, SERA SUPPRIMER A LA FIN DU DEVELOPPEMENT
    private static final String TAG = "BNYCommandeDistante";

    private ClientTCP clientTCP;
    private boolean statutConnexion;
    private String login;
    private String motDePasse;
    private String ipVoiture;


    /**
     * \brief Constructeur avec paramètres
     *
     * Permet d'instancier un objet de la classe BNYCommandeDistante.
     *
     * \param [in] login : L'identifiant de l'utilisateur.
     * \param [in] motDePasse : Le mot de passe de l'utilisateur.
     * \param [in] ipVoiture : L'adresse IP de la voiture.
     */
    public BNYCommandeDistante(String login, String motDePasse, String ipVoiture)
    {
        statutConnexion = false;
        this.login = login;
        this.motDePasse = motDePasse;
        this.ipVoiture = ipVoiture;
    }

    /**
     * \fn public boolean get_statutConnexion()
     * \brief Accesseur en lecture renvoyant l'attribut statutConnexion.
     * \return statutConnexion Le statue de connexion de l'utilisateur.
     */
    public boolean get_statutConnexion()
    {
        return statutConnexion;
    }

    public String get_ipVoiture()
    {
        return ipVoiture;
    }

    /**
     * \fn public void validerCUSeConnecter()
     * \brief Methode permettant de valider le CU "Se Connecter"
     *
     * Methode mettant l'attribut booleen "statutConnexion" a l'etat "true".
     * Cette methode est necessaire afin de valider le cas "Se Connecter"
     * et permettre a l'utilisateur d'acceder a l'ecran de pilotage.
     *
     */
    private void validerCUSeConnecter()
    {
        statutConnexion = true;
    }

    /**
     * \fn private void connexion(String login, String motDePasse, String ipVoiture)
     * \brief Methode permettant la connexion de l'application au Raspberry Pi
     *
     * Cette methode utilise des connexions TCP et doit donc être execute dans un Thread.
     * Si jamais elle ne l'est pas, l'Activity Manager tuera immediatement l'application.
     *
     * \param [in] login : L'identifiant de l'utilisateur.
     * \param [in] motDePasse : Le mot de passe de l'utilisateur.
     * \param [in] ipVoiture : L'adresse IP de la voiture.
     */
    public void connexion()
    {
        clientTCP = new ClientTCP();
        Log.i(TAG + "::connexion", "Objet ClientTCP : CREER");

        JSONObject jsonConnexion = new JSONObject();
        String reponseVoiture;

        //On remplie le JSON avec les differentes informations dont la voiture a besoin
        try
        {
            jsonConnexion.put("identifiant", login);
            jsonConnexion.put("motDePasse", motDePasse);
            jsonConnexion.put("ipVoiture", ipVoiture);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        Log.i(TAG + "::connexion", "JSON de connexion : REMPLIE");

        //On envoie le JSON au Raspberry Pi et on attend sa réponse
        clientTCP.ouvrirConnexion(ipVoiture, 55554); //PC test Maison
        clientTCP.emettreMessage(jsonConnexion.toString());
        reponseVoiture = clientTCP.recevoirMessage();

        if(!(reponseVoiture.equals("ok")))
        {
            clientTCP.fermerConnexion();
        }
        else
        {
            validerCUSeConnecter();
            Log.i(TAG + "::connexion", "statutConnexion : " + statutConnexion);
        }
    }

    /**
     * \fn public void deconnexion()
     * \brief Methode permettant de se deconnecter de commandeDistante
     *
     * La methode lance la methode fermerConnexion() de la classe ClientTCP et met
     * l'attribut statutConnexion a l'etat "false".
     *
     */
    public void deconnexion()
    {
        clientTCP.fermerConnexion();
        statutConnexion = false;
    }

    /**
     * \fn public void demanderInfoVehicule(String[] listeValeur)
     * \brief Méthode permettant d'obtenir les informations du véhicule
     *
     * Cette methode permet de demander des information sur la voiture a
     * commandeDistante. Elle va donc créer un JSON à partir du message recu de
     * ce dernier et remplira un tableau avec les valeurs recu.
     * PS: Cette methode risque de passer privee car elle utilise une connexion TCP
     * et doit donc être executer dans un Thread.
     * JSON de Test:
     * {"vitesse":"45","temperatureAmbiante":"25","temperatureMoteur":"50","niveauBatterie":"76","consommationEnergetique":"12"}
     * {"vitesse":"22","temperatureAmbiante":"28","temperatureMoteur":"75","niveauBatterie":"22","consommationEnergetique":"12"}
     *
     * \param [out] listeValeurs : Le tableau de valeurs a remplir
     */
    public void demanderInfoVehicule(String[] listeValeur)
    {
        JSONObject valeurs;
        String messageRecu;

        //On receptionne les valeurs provenant de commandeDistante
        messageRecu = clientTCP.recevoirMessage();

        //try-catch obligatoire pour le JSON [Java]
        try
        {
            //Le message recu est un JSON sous forme de String, il faut donc le parser afin de recupérer les valeurs
            valeurs = new JSONObject(messageRecu);
            Log.i(TAG + "::demanderInfoVehicule","Création JSON avec le message : FAIT");

            //On ajoute les valeurs dans notre liste
            Log.i(TAG + "::demanderInfoVehicule","Affectation des valeurs dans le tableau..");
            listeValeur[0] = valeurs.getString("vitesse");
            listeValeur[1] = valeurs.getString("temperatureAmbiante");
            listeValeur[2] = valeurs.getString("temperatureMoteur");
            listeValeur[3] = valeurs.getString("niveauBatterie");
            listeValeur[4] = valeurs.getString("consommationEnergetique");
            Log.i(TAG + "::demanderInfoVehicule","Tableau de valeurs : REMPLIE" + "");

        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * \fn private void envoyerOrdre(String jsonOrdre)
     * \brief Cette methode permet d'envoyer un ordre a commandeDistante
     *
     * Cette methode lance la methode emettreMessage de la classe ClientTCP
     * avec en parametres le JSON sous forme d'une string.
     * PS: La méthode risque de passer en privee. En effet, on utiliser l'objet clientTCP,
     * cette methode doit donc etre executee dans un Thread.
     *
     * \param [in] jsonOrdre : Le JSON sous forme d'une string.
     */
    public void envoyerOrdre(String jsonOrdre)
    {
        clientTCP.emettreMessage(jsonOrdre);
    }




    /**
     * \brief Constructeur spécial avec paramètres
     *
     * Constructeur spécial necessaire pour l'interface "Parcelable"
     * Pour plus d'information, voir la documentation officiel Android
     * \param [in] in :
     */
    protected BNYCommandeDistante(Parcel in) {
        statutConnexion = in.readByte() != 0;
        login = in.readString();
        motDePasse = in.readString();
        ipVoiture = in.readString();
    }

    /**METHODE PARCELABLE POUR REGENERER L'OBJET*/
    public static final Creator<BNYCommandeDistante> CREATOR = new Creator<BNYCommandeDistante>() {
        @Override
        public BNYCommandeDistante createFromParcel(Parcel in) {
            return new BNYCommandeDistante(in);
        }

        @Override
        public BNYCommandeDistante[] newArray(int size) {
            return new BNYCommandeDistante[size];
        }
    };

    /**
     * \fn public int describeContents()
     * \brief Methode de l'interface "Parcelable"
     *
     * Redefinition de la methode "describeContents()" presente dans l'interface "Parcelable".
     * Pour plus d'information, voir la documentation officiel Android
     *
     * \return 0
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * \fn public void writeToParcel(Parcel dest, int flags)
     * \brief Methode de l'interface "Parcelable"
     *
     * Redefinition de la methode "writeToParcel()" presente dans l'interface "Parcelable".
     * Pour plus d'information, voir la documentation officiel Android
     *
     * \param [in] dest : La parcel où l'objet doit être écrit
     * \param [in] flag : Un drapeau additionnel
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (statutConnexion ? 1 : 0));
        dest.writeString(login);
        dest.writeString(motDePasse);
        dest.writeString(ipVoiture);
    }

}
