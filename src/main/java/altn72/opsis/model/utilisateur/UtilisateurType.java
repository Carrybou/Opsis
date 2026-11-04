package altn72.opsis.model.utilisateur;

public enum UtilisateurType {
    ADMIN("Responsable d'exploitation"),
    AGENT("Agent"),
    TECHNICIEN("Technicien");

    private final String libelle;

    UtilisateurType(String libelle) {
        this.libelle = libelle;
    }

    // Libellé affiché dans l'interface, plutôt que le nom technique du profil.
    public String getLibelle() {
        return libelle;
    }
}
