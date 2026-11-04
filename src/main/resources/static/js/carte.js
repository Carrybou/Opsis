// Carte du réseau : un marqueur par station localisée, et un panneau latéral qui affiche la fiche de la station choisie.
// La page reste utilisable sans ce script : la liste des stations mène aux fiches par ancre.
(function () {
    const liste = document.getElementById('carto-liste');
    const panneau = document.getElementById('carto-panneau');
    const fiches = document.querySelectorAll('.carto-fiche');
    const liens = document.querySelectorAll('.carto-station');
    const plan = document.getElementById('plan-reseau');
    const message = document.getElementById('plan-message');
    const marqueurs = new Map();
    let carte = null;
    let selection = null;

    function chevauche(a, b) {
        return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom;
    }

    // Quand les stations sont proches, une étiquette qui recouvrirait un marqueur ou une autre étiquette est masquée
    // jusqu'à ce qu'on zoome. Celle de la station choisie passe en premier ; les noms restent tous dans la liste.
    function placerEtiquettes() {
        const reperes = Array.from(marqueurs.values()).map(function (marqueur) { return marqueur.getElement().getBoundingClientRect(); });
        const entrees = Array.from(marqueurs.entries()).sort(function (a, b) {
            return (b[0] === selection) - (a[0] === selection);
        });
        entrees.forEach(function (entree) {
            const etiquette = entree[1].getTooltip().getElement();
            const cadre = etiquette.getBoundingClientRect();
            const recouvre = entree[0] !== selection && reperes.some(function (repere) { return chevauche(cadre, repere); });
            etiquette.style.visibility = recouvre ? 'hidden' : 'visible';
            if (!recouvre) {
                reperes.push(cadre);
            }
        });
    }

    function surligner(id) {
        if (selection && marqueurs.has(selection)) {
            marqueurs.get(selection).setRadius(9).getElement().classList.remove('selectionne');
        }
        selection = id;
        if (id && marqueurs.has(id)) {
            const marqueur = marqueurs.get(id);
            marqueur.setRadius(13).bringToFront().getElement().classList.add('selectionne');
            if (!carte.getBounds().contains(marqueur.getLatLng())) {
                carte.panTo(marqueur.getLatLng());
            }
        }
        if (carte) {
            placerEtiquettes();
        }
    }

    // L'ancre garde la station choisie : le bouton « Précédent » depuis un équipement revient sur sa fiche.
    function afficherStation(id, avecFocus) {
        const fiche = document.querySelector('.carto-fiche[data-panneau="' + id + '"]');
        if (!fiche) {
            return;
        }
        liste.hidden = true;
        fiches.forEach(function (autre) { autre.hidden = autre !== fiche; });
        panneau.scrollTop = 0;
        surligner(id);
        history.replaceState(null, '', '#' + fiche.id);
        if (avecFocus) {
            fiche.querySelector('h2').focus();
        }
    }

    function afficherListe() {
        const ancienne = selection;
        fiches.forEach(function (fiche) { fiche.hidden = true; });
        liste.hidden = false;
        surligner(null);
        history.replaceState(null, '', location.pathname + location.search);
        const lien = document.querySelector('.carto-station[data-station="' + ancienne + '"]');
        if (lien) {
            lien.focus();
        }
    }

    function initialiserCarte() {
        if (!window.L) {
            message.textContent = 'La carte n\'a pas pu être chargée. Toutes les stations restent accessibles dans la liste.';
            return;
        }
        const localisees = Array.from(liens).filter(function (lien) { return lien.dataset.lat && lien.dataset.lng; });
        if (localisees.length === 0) {
            message.textContent = 'Aucune station n\'a de coordonnées à afficher sur la carte.';
            return;
        }

        message.remove();
        carte = L.map(plan);
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(carte);

        localisees.forEach(function (lien) {
            const marqueur = L.circleMarker([Number(lien.dataset.lat), Number(lien.dataset.lng)], {
                radius: 9,
                className: 'carto-marqueur situation-' + lien.dataset.situation
            }).addTo(carte);
            // Le nom reste affiché : il identifie la station sans dépendre de la couleur ni du survol.
            marqueur.bindTooltip(lien.dataset.nom, { permanent: true, direction: 'top', offset: [0, -8], className: 'carto-etiquette' });
            marqueur.on('click', function () { afficherStation(lien.dataset.station, true); });
            marqueurs.set(lien.dataset.station, marqueur);
        });

        const cadrer = function () {
            carte.fitBounds(L.featureGroup(Array.from(marqueurs.values())).getBounds(), { padding: [48, 48], maxZoom: 14 });
        };
        cadrer();
        carte.on('moveend zoomend', placerEtiquettes);
        placerEtiquettes();

        // Une carte créée dans un cadre encore sans dimensions (onglet ou panneau masqué au chargement) se cadre mal :
        // on la recadre dès que le cadre a une taille. Ensuite, un simple redimensionnement garde la vue de l'utilisateur.
        let cadree = plan.clientWidth > 0 && plan.clientHeight > 0;
        new ResizeObserver(function () {
            carte.invalidateSize();
            placerEtiquettes();
            if (!cadree && plan.clientWidth > 0 && plan.clientHeight > 0) {
                cadrer();
                cadree = true;
            }
        }).observe(plan);
    }

    liens.forEach(function (lien) {
        lien.addEventListener('click', function (e) {
            e.preventDefault();
            afficherStation(lien.dataset.station, true);
        });
    });
    document.querySelectorAll('.carto-retour').forEach(function (retour) {
        retour.addEventListener('click', function (e) {
            e.preventDefault();
            afficherListe();
        });
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && selection) {
            afficherListe();
        }
    });

    initialiserCarte();

    const ancre = location.hash.match(/^#panneau-station-(\d+)$/);
    if (ancre) {
        afficherStation(ancre[1], false);
    }
})();
