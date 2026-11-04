package altn72.opsis.exception.gestion;

// Message en français d'une erreur que le code métier n'a pas levée lui-même : le texte d'origine de Spring ou de Java
// est technique (noms de classes, types attendus) et ne doit jamais atteindre l'utilisateur.
final class MessageErreurHttp {

    private MessageErreurHttp() {
    }

    static String pour(int status) {
        return switch (status) {
            case 400 -> "La requête est invalide";
            case 401 -> "Vous devez être connecté";
            case 403 -> "Accès refusé";
            case 404 -> "La ressource demandée est introuvable";
            case 405 -> "Cette action n'est pas possible sur cette adresse";
            case 406 -> "Ce format de réponse n'est pas disponible";
            case 415 -> "Ce format de données n'est pas accepté";
            default -> "Une erreur interne est survenue. Réessayez dans quelques instants.";
        };
    }
}
