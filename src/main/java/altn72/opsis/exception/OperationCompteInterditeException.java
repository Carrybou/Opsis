package altn72.opsis.exception;

// Garde-fou de la gestion des comptes : un responsable ne peut ni supprimer son propre compte, ni changer son propre profil,
// pour qu'il reste toujours au moins un compte capable d'administrer.
public class OperationCompteInterditeException extends RuntimeException {

    public OperationCompteInterditeException(String message) {
        super(message);
    }
}
