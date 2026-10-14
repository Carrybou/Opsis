package altn72.opsis.modele.programmeur;

import jakarta.persistence.*;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identifiant", nullable = false)
    private Long id;

    @Column(name = "identifiant_connexion", nullable = false, length = 100)
    private String identifiantConnexion;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "profil", nullable = false, length = 50)
    private UtilisateurType profil;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifiantConnexion() {
        return identifiantConnexion;
    }

    public void setIdentifiantConnexion(String identifiantConnexion) {
        this.identifiantConnexion = identifiantConnexion;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public UtilisateurType getProfil() {
        return profil;
    }

    public void setProfil(UtilisateurType profil) {
        this.profil = profil;
    }

}