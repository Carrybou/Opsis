package altn72.opsis.modele.incident;

import altn72.opsis.modele.prestataire.Prestataire;
import altn72.opsis.modele.programmeur.Utilisateur;
import altn72.opsis.modele.equipement.Equipement;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "incident")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identifiant", nullable = false)
    private Long id;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "date_heure_ouverture", nullable = false)
    private Instant dateHeureOuverture;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private IncidentStatut statut;

    @Column(name = "date_heure_resolution")
    private Instant dateHeureResolution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_equipement", nullable = false)
    private Equipement idEquipement;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_prestataire")
    private Prestataire idPrestataire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur idUtilisateur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getDateHeureOuverture() {
        return dateHeureOuverture;
    }

    public void setDateHeureOuverture(Instant dateHeureOuverture) {
        this.dateHeureOuverture = dateHeureOuverture;
    }

    public IncidentStatut getStatut() {
        return statut;
    }

    public void setStatut(IncidentStatut statut) {
        this.statut = statut;
    }

    public Instant getDateHeureResolution() {
        return dateHeureResolution;
    }

    public void setDateHeureResolution(Instant dateHeureResolution) {
        this.dateHeureResolution = dateHeureResolution;
    }

    public Equipement getIdEquipement() {
        return idEquipement;
    }

    public void setIdEquipement(Equipement idEquipement) {
        this.idEquipement = idEquipement;
    }

    public Prestataire getIdPrestataire() {
        return idPrestataire;
    }

    public void setIdPrestataire(Prestataire idPrestataire) {
        this.idPrestataire = idPrestataire;
    }

    public Utilisateur getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Utilisateur idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

}