import ij.ImagePlus;
import ij.measure.Measurements;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class OfTrack_ extends  MTrack3_ 
{
	@Override
	
	public int setup(String arg, ImagePlus imp) 
	{
		return super.setup(arg,imp);	
	}
	
	@Override
	public void run(ImageProcessor ip) 
	{
		track(imp, 50, 200,(float) 10.0, "","") ;
				
	}

}
