import ij.ImagePlus;
import ij.gui.Roi;
import java.util.Set;

/**
 * This interface implements the track method which upon receiving an imagePlus,and roi ,an existing SiteNqbCellMap and a list of frame numbers
 * will track objects in the imp and return an updated SiteNqbCellMap including the tracked objects in imp.
 * The tracking will follow the objects in frames included in the frames list, and contained in the given roi. 
 * @author sivan-nqb
 *
 */
public interface Tracker {
	public Cells track(ImagePlus imp, Roi roi, Cells cellStruct, Set<Integer> frames);

}
