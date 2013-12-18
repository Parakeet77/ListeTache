package servlet;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author M. Bourgeois
 */
@WebServlet(name = "ListeAdresses",
        urlPatterns = {"/"},
        initParams = {
    @WebInitParam(name = "fichierTaches", value = "taches.txt")})
public class ListeAdresses extends HttpServlet {

    private ArrayList<String> adresses;
    private String nomFichier;
    private String nomRep;
    private static final long serialVersionUID = -2L;

    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        nomRep = this.getServletContext().getRealPath("");
        nomFichier = config.getInitParameter("fichierTaches");
        if (nomFichier == null) {
            throw new UnavailableException("La propri&eacute;t&eacute; \"fichierTaches\" " + "doit &ecirc;tre un nom de fichier");
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(nomRep + File.separatorChar + nomFichier));
            adresses = (ArrayList<String>) in.readObject();
            in.close();
        } catch (FileNotFoundException e) {
            adresses = new ArrayList<String>();
        } catch (Exception e) {
            throw new UnavailableException("Erreur lecture fichier: " + e);
        }
    }

    protected void doGet(HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/html");
        res.setHeader("pragma", "no-cache");
        PrintWriter out = res.getWriter();
        out.print("<HTML><HEAD><TITLE>Liste Taches</TITLE></HEAD>");
        out.print("<BODY>");
        out.print("<H3>Taches:</H3>");
        out.print("<p>Nom de la taches ________ Estimation de charge ________ Temps restant</p>");
        for (int i = 0; i < adresses.size(); i++) {
            out.print("<p>" + adresses.get(i)+" ________ " +adresses.get(i+1)+" ________ "+adresses.get(i+2)+"</p>");
            i=i+2;
        }
        out.print("<HR><FORM METHOD=\"POST\">");
        out.print("Nom de la nouvelle tache : <INPUT TYPE=\"TEXT\" NAME=\"email\"><BR/><BR/>");
        out.print("Estimation de charge : <INPUT TYPE=\"TEXT\" NAME=\"email1\"><BR/><BR/>");
        out.print("Temps restant : <INPUT TYPE=\"TEXT\" NAME=\"email2\"><BR/><BR/>");
        out.print("<INPUT TYPE=\"SUBMIT\" NAME=\"action\" VALUE=\"Ajouter\">");
        out.print("<INPUT TYPE=\"SUBMIT\" NAME=\"action\" VALUE=\"Enlever\">");
        out.print("</FORM>"
                + "<p>Pour supprimer une tache, entrer le nom de celle-ci.</p></BODY></HTML>");
        out.close();
    }

    protected void doPost(HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String email1 = req.getParameter("email1");
        String email2 = req.getParameter("email2");
        String msg;
        if (email == null) {
            res.sendError(res.SC_BAD_REQUEST,
                    "Pas d'adresse spécifi&eacute;e.");
            return;
        }
        if (req.getParameter("action").equals("Ajouter")) {
            if (subscribe(email, email1, email2)) {
                msg = "L'adresse " + email + " a &eacute;t&eacute; enregistr&eacute;e.";
            } else {
                res.sendError(res.SC_BAD_REQUEST,
                        "L'adresse " + email + " existe d&eacute;j&agrave;.");
                return;
            }
        } else {
            if (unsubscribe(email)) {
                msg = "L'adresse " + email + " a &eacute;t&eacute; supprim&eacute;e.";
            } else {
                res.sendError(res.SC_BAD_REQUEST,
                        "L'adresse " + email + " n'a pas &eacute;t&eacute; enregistr&eacute;e.");
                return;
            }
        }

        res.setContentType("text/html");
        res.setHeader("pragma", "no-cache");
        PrintWriter out = res.getWriter();
        out.print("<HTML><HEAD><TITLE>ListeAdresse</TITLE></HEAD><BODY>");
        out.print(msg);
        out.print("<HR><A HREF=\"");
        out.print(req.getRequestURI());
        out.print("\">Afficher la liste</A></BODY></HTML>");
        out.close();
    }

    private boolean subscribe(String email, String email1, String email2) throws IOException {
        synchronized (this) {
            if (adresses.contains(email)) {
                return false;
            }
        }
        synchronized (this) {
            adresses.add(new String(email));
            adresses.add(new String(email1));
            adresses.add(new String(email2));
        }
        save();
        return true;
    }

    private boolean unsubscribe(String email) throws IOException {
        synchronized (this) {
            if (adresses.contains(email)) {
                for(int i = 0;i<=adresses.size()-1;i++){
                    System.out.println(adresses.get(i));
                    if(adresses.get(i).equals(email)){
                        adresses.remove(i);
                        adresses.remove(i);
                        adresses.remove(i);
                    }
                    System.out.println("size "+adresses.size());
                    System.out.println("i "+i);
                }
            }else{
                return false;
            }
        }
        save();
        return true;
    }

    private void save() throws IOException {
        ObjectOutputStream out =
                new ObjectOutputStream(new FileOutputStream(nomRep + File.separatorChar + nomFichier));
        out.writeObject(adresses);
        out.close();
    }
}
