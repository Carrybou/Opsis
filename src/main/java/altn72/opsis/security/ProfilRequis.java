package altn72.opsis.security;

import altn72.opsis.model.utilisateur.UtilisateurType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfilRequis {

    UtilisateurType[] value();
}