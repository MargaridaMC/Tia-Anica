package app;
import java.util.Comparator;

public class GeocachingTourSummaryNameSorter implements Comparator<GeocachingTourSummary>
{
    public int compare(GeocachingTourSummary git1, GeocachingTourSummary git2)
    {
        return (int) (git1.getName().compareTo(git2.getName()));
    }
}
