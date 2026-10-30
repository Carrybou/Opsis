package altn72.opsis.model.equipement;

import altn72.opsis.model.station.Station;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "equipement")
public class Equipement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identifiant", nullable = false)
    private Long id;

    @Column(name = "reference_unique", nullable = false, length = 100)
    private String referenceUnique;

    @Column(name = "libelle_description", nullable = false)
    private String libelleDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 100)
    private EquipementType type;

    @Column(name = "etat_fonctionnement", nullable = false, length = 50)
    private String etatFonctionnement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_station", nullable = false)
    private Station idStation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceUnique() {
        return referenceUnique;
    }

    public void setReferenceUnique(String referenceUnique) {
        this.referenceUnique = referenceUnique;
    }

    public String getLibelleDescription() {
        return libelleDescription;
    }

    public void setLibelleDescription(String libelleDescription) {
        this.libelleDescription = libelleDescription;
    }

    public EquipementType getType() {
        return type;
    }

    public void setType(EquipementType type) {
        this.type = type;
    }

    public String getEtatFonctionnement() {
        return etatFonctionnement;
    }

    public void setEtatFonctionnement(String etatFonctionnement) {
        this.etatFonctionnement = etatFonctionnement;
    }

    public Station getIdStation() {
        return idStation;
    }

    public void setIdStation(Station idStation) {
        this.idStation = idStation;
    }

}