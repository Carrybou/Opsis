package altn72.opsis.service.tableaudebord;

import altn72.opsis.dto.tableaudebord.FiltresTableauDeBord;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse;

public interface TableauDeBordInterface {

    TableauDeBordResponse consulter(FiltresTableauDeBord filtres);
}
