package net.teamtruta.tiaires;
import java.util.List;

public interface PostGeocachingScrapping {
    void onGeocachingScrappingTaskResult(List<Geocache> newlyLoadedCaches);
}
