package lunartools.apng;

/**
 * Callback to the calling application to inform about the progress when creating an APNG.
 * 
 * @author Thomas Mattel
 */
public interface ProgressCallback {

	public void setProgressStep(int step);
	
}