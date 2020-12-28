package net.teamtruta.tiaires;
import java.util.List;

public interface PostGeocachingScrapping {
    void onGeocachingScrappingTaskResult(List<GeoCache> newlyLoadedCaches, List<Long> cachesAlreadyInDb);
}
