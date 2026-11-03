package altn72.opsis.model.evaluation;

// Projection de la requête native de moyenne des notes : Number, car MySQL renvoie AVG en DECIMAL et COUNT en BIGINT.
public interface NotePrestataire {

    Long getIdPrestataire();

    Number getMoyenne();

    Number getNombre();
}
