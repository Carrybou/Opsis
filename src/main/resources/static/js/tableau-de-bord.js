// Tableau de bord (F11) : filtres appliqués à chaque changement, infobulles des graphiques,
// réticule de la courbe et mise à jour automatique. La page reste utilisable sans ce script.
(function () {
    const formulaire = document.getElementById('filtres');

    // --- Filtres : chaque changement recharge la page avec les nouveaux paramètres (sauf la recherche : Entrée ou sortie du champ).
    document.getElementById('bouton-filtrer').hidden = true;
    formulaire.querySelectorAll('input[type=checkbox], select').forEach(function (champ) {
        champ.addEventListener('change', function () { formulaire.requestSubmit(); });
    });
    formulaire.querySelector('input[type=search]').addEventListener('change', function () { formulaire.requestSubmit(); });

    // Le tri est affiché au-dessus de la liste mais fait partie des filtres (champ caché du formulaire).
    const tri = document.getElementById('tri-liste');
    tri.addEventListener('change', function () {
        document.getElementById('filtre-tri').value = tri.value;
        formulaire.requestSubmit();
    });

    // Un clic sur une station garde les autres filtres (le lien seul, sans JS, ne garde que la station).
    document.querySelectorAll('.tdb-barre-ligne').forEach(function (ligne) {
        ligne.addEventListener('click', function (e) {
            e.preventDefault();
            const station = formulaire.querySelector('select[name=station]');
            station.value = station.value === ligne.dataset.station ? '' : ligne.dataset.station;
            formulaire.requestSubmit();
        });
    });

    // Champs vides retirés de l'URL, pour qu'elle reste lisible et partageable.
    formulaire.addEventListener('submit', function () {
        formulaire.querySelectorAll('input[name], select[name]').forEach(function (champ) {
            if (champ.type !== 'checkbox' && champ.value === '') {
                champ.disabled = true;
            }
        });
        if (document.getElementById('filtre-tri').value === 'anciennete') {
            document.getElementById('filtre-tri').disabled = true;
        }
    });

    // --- Infobulle unique, au survol comme au focus clavier. Contenu inséré en textContent.
    const infobulle = document.getElementById('infobulle');
    function montrer(cible, valeur, libelle, x, y) {
        infobulle.querySelector('strong').textContent = valeur;
        infobulle.querySelector('span').textContent = libelle;
        infobulle.hidden = false;
        const largeur = infobulle.offsetWidth;
        const gauche = Math.min(Math.max(8, x - largeur / 2), window.innerWidth - largeur - 8);
        infobulle.style.left = gauche + window.scrollX + 'px';
        infobulle.style.top = y + window.scrollY - infobulle.offsetHeight - 10 + 'px';
    }
    function cacher() {
        infobulle.hidden = true;
    }
    document.querySelectorAll('[data-infobulle-valeur]').forEach(function (marque) {
        function afficher() {
            const r = marque.getBoundingClientRect();
            montrer(marque, marque.dataset.infobulleValeur, marque.dataset.infobulleLibelle, r.left + r.width / 2, r.top);
        }
        marque.addEventListener('pointerenter', afficher);
        marque.addEventListener('focus', afficher);
        marque.addEventListener('pointerleave', cacher);
        marque.addEventListener('blur', cacher);
    });

    // --- Courbe : le réticule se cale sur le jour le plus proche du pointeur ; flèches gauche / droite au clavier.
    const courbe = document.querySelector('.tdb-courbe');
    if (courbe) {
        const zone = courbe.querySelector('.tdb-courbe-zone');
        const points = Array.from(zone.querySelectorAll('.tdb-point'));
        const reticule = zone.querySelector('.tdb-reticule');
        const repere = zone.querySelector('.tdb-reticule-point');
        let courant = points.length - 1;

        function placer(index) {
            courant = Math.max(0, Math.min(points.length - 1, index));
            const p = points[courant];
            reticule.style.left = p.dataset.x + '%';
            repere.style.left = p.dataset.x + '%';
            repere.style.top = p.dataset.y + '%';
            reticule.hidden = false;
            repere.hidden = false;
            const r = repere.getBoundingClientRect();
            const valeur = p.dataset.valeur;
            montrer(repere, valeur, (valeur === '1' ? 'incident actif' : 'incidents actifs') + ' · ' + p.dataset.jour,
                r.left + r.width / 2, zone.getBoundingClientRect().top);
        }
        function effacer() {
            reticule.hidden = true;
            repere.hidden = true;
            cacher();
        }
        zone.addEventListener('pointermove', function (e) {
            const r = zone.getBoundingClientRect();
            placer(Math.round((e.clientX - r.left) / r.width * (points.length - 1)));
        });
        zone.addEventListener('pointerleave', effacer);
        courbe.addEventListener('focus', function () { placer(courant); });
        courbe.addEventListener('blur', effacer);
        courbe.addEventListener('keydown', function (e) {
            if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
                e.preventDefault();
                placer(courant + (e.key === 'ArrowLeft' ? -1 : 1));
            }
        });
    }

    // --- Fraîcheur : « mis à jour il y a X min », puis rechargement toutes les 5 minutes
    // (les niveaux d'attention dépendent de l'heure), sauf si l'utilisateur est en train de saisir.
    const maj = document.getElementById('maj');
    const genereA = new Date(maj.dataset.genereA);
    const texte = document.getElementById('maj-texte');
    document.getElementById('maj-rafraichir').addEventListener('click', function (e) {
        e.preventDefault();
        window.location.reload();
    });
    function actualiserTexte() {
        const minutes = Math.floor((Date.now() - genereA.getTime()) / 60000);
        texte.textContent = minutes < 1 ? 'Mis à jour à l’instant' : 'Mis à jour il y a ' + minutes + ' min';
        const saisie = document.activeElement && ['INPUT', 'SELECT'].includes(document.activeElement.tagName);
        if (minutes >= 5 && !document.hidden && !saisie) {
            window.location.reload();
        }
    }
    actualiserTexte();
    setInterval(actualiserTexte, 30000);
})();
