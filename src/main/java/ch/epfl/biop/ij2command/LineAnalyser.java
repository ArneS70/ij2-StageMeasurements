package ch.epfl.biop.ij2command;

import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;



public class LineAnalyser {
	private static final int TOP=1,MIDDLE=2,BOTTOM=3;
	private static final int LEFT=4,CENTER=5,RIGHT=6;
	private double [] profile;
	private int profileLength;
	private Calibration cal;
	private double [] x;
	private double mean, stdev;
	private Line line;
	double pixelSize;
	private double width,height;
	private int position;
	private double linePositionX,linePositionY;
	private ImageProcessor ip_line;
	double x1=0,x2=width,y1=0,y2=height;
	private ImagePlus imp;
	
	/**   
	 * Constructors
	 */
		LineAnalyser(){
			
		}
		LineAnalyser(ImagePlus imp, int pos){
			if (pos<7&&pos>0) this.position=pos;
			else pos=1;
			this.width=imp.getWidth();
			this.height=imp.getHeight();
			this.ip_line=imp.getProcessor();
			cal=imp.getCalibration();
			this.imp=imp;
		}
		LineAnalyser(ImagePlus imp, Line inputLine){
			
			this.line=inputLine;
			imp.setRoi(inputLine);
			imp.draw();
			this.width=imp.getWidth();
			cal=imp.getCalibration();
			ImageProcessor ip=imp.getProcessor();
			profile=ip.getLine((double)line.x1,(double)line.y1,(double)line.x2,(double)line.y2);
			profileLength=profile.length;
			x=new double [profileLength];
			pixelSize=cal.pixelWidth;
			for (int n=0;n<profileLength;n++) {
				x[n]=n*pixelSize;
			}
		}
		
		void setProfile(int pos) {
			x1=1;x2=width-1;y1=1;y2=height-1;
			
			switch (pos) {
				case 1:
					y1=y2=1;
					break;
				case 2:
					y1=y2=0.5*height;
					break;
				case 3:
					y1=y2=height-1;
					break;
				case 4:
					x1=x2=1;
					break;
				case 5:
					x1=x2=0.5*width;
					break;
				case 6:
					x1=x2=width-1;
					
					break;
			}
		}
		int []alignMaxima(int[]max1,int[]max2) {				//requires still some testing
			int len1=max1.length;
			int len2=max2.length;
			int [] modified;
			if (len2>len1)return null;							//len1 must be bigger than len2 
			else {
				double min=0;
				int minPos=0;
				modified=new int[len2];
				int shift=len1-len2;
				double []diff=new double [shift+1];
				for (int s=0;s<=shift;s++) {
						
					for (int l=0;l<len2;l++) {
						diff[s]+=Math.abs(max1[l+s]-max2[l]);
					}
					if (s==0) {min=diff[s];minPos=0;}else
						{if (diff[s]<min) {min=diff[s];minPos=s;}};
//						IJ.log(s+"  "+diff[s]);
					}
					for (int n=0;n<len2;n++) {
						modified[n]=max1[n+minPos];
					}
						
					}
			return modified;
		}
		
		void setProfile() {	
			
			profile=ip_line.getLine(x1,y1,x2,y2);
			profileLength=profile.length;
			x=new double [profileLength];
			pixelSize=cal.pixelWidth;
			for (int n=0;n<profileLength;n++) {
				x[n]=n*pixelSize;
			}
		}
		void findHorizontalMaxima(int linewidth){
			//ImageProcessor ip_maxima=ip_edge;
			ip_line.setLineWidth(linewidth);
			setProfile(this.TOP);
			double [] lineTop=ip_line.getLine(x1, y1, x2, y2);
			profile=lineTop;
			double maxTop=this.getMax();
			double minTop=this.getMin();
			
			setProfile(this.BOTTOM);
			double [] lineBottom=ip_line.getLine(x1, y1, x2, y2);
			profile=lineBottom;
			double maxBottom=this.getMax();
			double minBottom=this.getMin();

			int prominence=(int)(0.5*(maxTop-minTop));
			int [] topMaximum=MaximumFinder.findMaxima(lineTop, prominence, false);
			prominence=(int)(0.5*(maxBottom-minBottom));
			int [] bottomMaximum=MaximumFinder.findMaxima(lineBottom, prominence, false);
			
			Arrays.sort(topMaximum);
			Arrays.sort(bottomMaximum);
			IJ.log("top: "+topMaximum.length);
			IJ.log("bottom: "+bottomMaximum.length);
			
			
		}
		double calulateVertcalSpacing(int linewidth){
			ip_line.setLineWidth(linewidth);
			setProfile(LineAnalyser.CENTER);
			double [] line=ip_line.getLine(x1, y1, x2, y2);
			profile=line;
			double max=this.getMax();
			double min=this.getMin();
			int prominence=(int)(0.5*(max-min));
			int [] points=MaximumFinder.findMaxima(line, prominence, false);
			
			int length=points.length;
			double []x=new double[length];
			double []y=new double[length];
			for (int i=0;i<length;i++){
				x[i]=i;
				y[i]=points[i];
			}
			
			CurveFitter cf=new CurveFitter(x,y);
			cf.doFit(CurveFitter.STRAIGHT_LINE);
			double []param=cf.getParams();
			return param[1];
			
			
		}
		Roi [] findVerticalMaxima(int linewidth,int shift){
			//ImageProcessor ip_maxima=ip_edge;
			//new ImagePlus("test",ip_line).show();
			ip_line.setLineWidth(linewidth);
			setProfile(LineAnalyser.CENTER);
			int [] rightMaximum,leftMaximum;
			do {
				double [] lineLeft=ip_line.getLine(x1-shift, y1, x2-shift, y2);
				profile=lineLeft;
				double maxLeft=this.getMax();
				double minLeft=this.getMin();
				
				setProfile(LineAnalyser.CENTER);
				double [] lineRight=ip_line.getLine(x1+shift, y1, x2+shift, y2);
				profile=lineRight;
				double maxRight=this.getMax();
				double minRight=this.getMin();
	
				int prominence=(int)(0.5*(maxLeft-minLeft));
				leftMaximum=MaximumFinder.findMaxima(lineLeft, prominence, false);
				prominence=(int)(0.5*(maxRight-minRight));
				rightMaximum=MaximumFinder.findMaxima(lineRight, prominence, false);
				shift=shift-10;
			} while (leftMaximum.length!=rightMaximum.length);
			
			Arrays.sort(leftMaximum);
			Arrays.sort(rightMaximum);
			
			if (leftMaximum.length>rightMaximum.length)leftMaximum=alignMaxima(leftMaximum,rightMaximum);
			else rightMaximum=alignMaxima(rightMaximum,leftMaximum);
			
			
			IJ.log("left: "+leftMaximum.length);
			IJ.log("right: "+rightMaximum.length);
			int len=0;		
			
			
			if (leftMaximum.length<rightMaximum.length) len=leftMaximum.length;
			else len=rightMaximum.length;
			Roi [] lines=new Roi [len-1];
			
			//this.imp.show();
			for (int i=1;i<len;i+=1) {
				double middle=this.width/2.0;
				double left=((leftMaximum[i-1]+leftMaximum[i])/2.0);
				double right=((rightMaximum[i-1]+rightMaximum[i])/2.0);
				double slope=(left-right)/(2.0*shift);
				
				double p1=left+slope*(middle-shift);
				double p2=right-slope*(middle-shift);
				
				
				//IJ.log(""+slope+"   "+p1+"    "+p2);
				lines[i-1]=new Line(0,p1,this.width,p2);
				//this.imp.setRoi(lines[i],true);
			}
			return lines;
		}
		
		double [] getProfile() {
			profile=ip_line.getLine(x1,y1,x2,y2);
			profileLength=profile.length;
			x=new double [profileLength];
			pixelSize=cal.pixelWidth;
			for (int n=0;n<profileLength;n++) {
				x[n]=n*pixelSize;
			}
			return profile;
		}
		double getMin() {
			return new ArrayStatistics(profile).getMin();
		}
		double getMax() {
			return new ArrayStatistics(profile).getMax();
		}
		double getMean() {
			return new ArrayStatistics(profile).getMean();
		}
		double getSTEDV() {
			return new ArrayStatistics(profile).getSTDEV();
		}
		double xPosition() {
			return pixelSize*(line.x1+line.x2)/2.0;
		}
}
