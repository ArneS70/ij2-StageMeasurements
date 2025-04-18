package ch.epfl.biop.ij2command.USAF;

import java.util.Arrays;

import ch.epfl.biop.ij2command.stage.general.ArrayStatistics;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;


public class HorizontalLine  {
	ImageProcessor inputIP;
	int width;
	int height;
	
	HorizontalLine(ImageProcessor ip){
		inputIP=ip;
		this.width=ip.getWidth();
		this.height=ip.getHeight();
	}
	Line checkHorizontalLine(Line input) {
		Line horizontal=input;
		double deltax=input.x2d-input.x1d;
		double deltay=input.y2d-input.y1d;
		double slope=deltay/deltax;
		if (slope>0.001) {
			double [] profileLeft=inputIP.getLine(input.x1d, input.y1d-10, input.x1d, input.y2d+10);
			double [] profileRight=inputIP.getLine(input.x2d, input.y1d-10, input.x2d, input.y2d+10);
			ArrayStatistics left=new ArrayStatistics(profileLeft);
			ArrayStatistics right=new ArrayStatistics(profileLeft);
			
			if (left.getSTDEV()<right.getSTDEV()) horizontal=new Line(input.x1d, input.y1d, input.x2d, input.y1d);
			else horizontal=new Line(input.x1d, input.y2d, input.x2d, input.y2d);
			
		}
		return horizontal;
	}
	Line findHorizontalLine() {
		
		Line horizontal;
		ImageProcessor ip_edge=inputIP.duplicate();
		ip_edge.convertToFloat();
		ip_edge.findEdges();
//		new ImagePlus("Edge",ip_edge).show();
		LineAnalyser la=new LineAnalyser(ip_edge);
		int [] maxima=la.findVerticalMaximum(10);
		int pos=1+maxima.length/2;
		
		double space=this.getHorizontalSpacing();
		
		
		double mean1=new ArrayStatistics(inputIP.getLine(20,maxima[pos]-space/2,width-20,maxima[pos]-space/2)).getMean();
		double mean2=new ArrayStatistics(inputIP.getLine(20,maxima[pos]+space/2,width-20,maxima[pos]+space/2)).getMean();
				
		if (mean1>mean2) {horizontal=new Line(20,maxima[pos]-space/2,width-20,maxima[pos]-space/2);}
		else {horizontal=new Line(20,maxima[pos]+space/2,width-20,maxima[pos]+space/2);}
		
		
/*		
		
		Roi line=la.findVerticalMaximum(width, height);
		
		Roi [] lines=la.findVerticalMaxima(10,3*width/8);
		int pos=1+lines.length/2;
		
		inputIP.setRoi(lines[pos]);
		double mean1=inputIP.getStatistics().mean;
		
		inputIP.setRoi(lines[pos+1]);
		double mean2=inputIP.getStatistics().mean;
		//IJ.log("m1="+mean1+"    m2="+mean2);
		
		
*/		
		return horizontal;
	}
Line findHorizontalLine(int yshift) {
		
		Line horizontal;
		ImageProcessor ip_edge=inputIP.duplicate();
		ip_edge.convertToFloat();
		ip_edge.findEdges();
//		new ImagePlus("Edge",ip_edge).show();
		LineAnalyser la=new LineAnalyser(ip_edge);
		int [] maxima=la.findVerticalMaximum(10);
		int pos=1+maxima.length/2;
		
		double space=this.getHorizontalSpacing();
		
		
		double mean1=new ArrayStatistics(inputIP.getLine(20,maxima[pos]-space/2,width-20,maxima[pos]-space/2)).getMean();
		double mean2=new ArrayStatistics(inputIP.getLine(20,maxima[pos]+space/2,width-20,maxima[pos]+space/2)).getMean();
				
		if (mean1>mean2) {horizontal=new Line(20,maxima[pos]-space/2+yshift,width-20,maxima[pos]-space/2+yshift);}
		else {horizontal=new Line(20,maxima[pos]+space/2+yshift,width-20,maxima[pos]+space/2+yshift);}
		
		
/*		
		
		Roi line=la.findVerticalMaximum(width, height);
		
		Roi [] lines=la.findVerticalMaxima(10,3*width/8);
		int pos=1+lines.length/2;
		
		inputIP.setRoi(lines[pos]);
		double mean1=inputIP.getStatistics().mean;
		
		inputIP.setRoi(lines[pos+1]);
		double mean2=inputIP.getStatistics().mean;
		//IJ.log("m1="+mean1+"    m2="+mean2);
		
		
*/		
		return horizontal;
	}
	
	double getHorizontalSpacing(){
//		inputImage.setSlice(super.stackCenter);
//		LineAnalyser spacing=new LineAnalyser (new ImagePlus("edge",this.inputImage.getProcessor().duplicate()));
//		setProfile(LineAnalyser.CENTER);
//		spacing.getProfilPlot().show();
//		double [] line=spacing.getProfile();		
		
		ImageProcessor ipEdge=this.inputIP.duplicate().convertToFloat();
		ipEdge.findEdges();
//		new ImagePlus("test",ipEdge).show();
		
		
		double [] line=ipEdge.getLine(width/2,0,width/2, height);
		ArrayStatistics stat=new ArrayStatistics(line);

		double max=stat.getMax();
		double min=stat.getMin();
		int prominence=(int)(0.5*(max-min));
		int [] points=MaximumFinder.findMaxima(line, prominence, false);
		Arrays.sort(points);
		int length=points.length;
		double []x=new double[length];
		double []y=new double[length];
		for (int i=0;i<length;i++){
			x[i]=i;
			y[i]=points[i];
		}
		
		CurveFitter cf=new CurveFitter(x,y);
		cf.doFit(CurveFitter.STRAIGHT_LINE);
//		cf.getPlot().show();
		double []param=cf.getParams();
		
		return param[1];
		
		
	}
	Line optimizeHorizontalMaxima(Line line) {
		
		double space=getHorizontalSpacing();		
		double profile []=inputIP.getLine(line.x1d,line.y1d-space,line.x1d,line.y1d+space);
		double [] x=new double[profile.length];
		int profLen=x.length;
		for (int i=0;i<profLen;i++) {
			x[i]=i;
		}
		CurveFitter cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramLeft=cf.getParams();
		
		profile=inputIP.getLine(line.x2d,line.y2d-space,line.x2d,line.y2d+space);
		
		cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramRight=cf.getParams();
		return new Line(line.x1d,line.y1d-space+paramLeft[2],line.x2d,line.y2d-space+paramRight[2]);

	}
Line optimizeHorizontalMaxima(Line line, int yshift) {
		
		double space=getHorizontalSpacing();		
		double profile []=inputIP.getLine(line.x1d,line.y1d-space,line.x1d,line.y1d+space);
		double [] x=new double[profile.length];
		int profLen=x.length;
		for (int i=0;i<profLen;i++) {
			x[i]=i;
		}
		CurveFitter cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramLeft=cf.getParams();
		
		profile=inputIP.getLine(line.x2d,line.y2d-space,line.x2d,line.y2d+space);
		
		cf=new CurveFitter(x,profile);
		cf.doFit(CurveFitter.GAUSSIAN);
//		cf.getPlot().show();
		double [] paramRight=cf.getParams();
		return new Line(line.x1d,line.y1d-space+paramLeft[2]+yshift,line.x2d,line.y2d-space+paramRight[2]+yshift);

	}
}