package ch.epfl.biop.ij2command.stage.general;

import java.util.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;


public class GlobalFitter {
	
private	ResultsTable inputTable;
private int method;
private int bestFitPos;
private double [][] fitResults;
private ResultsTable fitParameters;
	
	public GlobalFitter() {
		
	}
	public GlobalFitter(ResultsTable rt, int method){
		
		this.inputTable=rt;
		this.method=method;
	}
	public double [][] initialFit() {
		int col=inputTable.getLastColumn();
		double [] x=inputTable.getColumnAsDoubles(0);
		double [] y=inputTable.getColumnAsDoubles(0);
		fitResults=new double [col][CurveFitter.getNumParams(this.method)+2];
		
		for (int n=1;n<=col;n++) {
			CurveFitter cf=new CurveFitter(inputTable.getColumnAsDoubles(0),inputTable.getColumnAsDoubles(n));
			cf.doFit(method);
//			cf.getPlot().show();
			double [] params=cf.getParams();
			for (int p=0;p<params.length;p++) {
				fitResults[n-1][p]=params[p];
			}
			fitResults[n-1][params.length]=cf.getRSquared();
		}
		
		return fitResults;
	}
	public int selectBestFit() {
		
		int col=inputTable.getLastColumn();
		double [] residual=new double[col];
		double resMin=0;
		int resMinPos=0;
		int iter=col*fitResults.length;
		for (int p=0;p<fitResults.length;p++) {
			double []initParam=getRow(fitResults,p,5);
			
			String formula=createFormula(initParam);
			
			for (int n=1;n<=col;n++) {
//				IJ.log(p+"/"+n);
				IJ.showProgress(p*col+n, iter);
				CurveFitter cf=new CurveFitter(inputTable.getColumnAsDoubles(0),inputTable.getColumnAsDoubles(n));
				
				cf.doCustomFit(formula, new double [] {0,1,0}, false);
				residual[p]+=cf.getParams()[3];
	//			cf.getPlot().show();
			}
			if (p==0) {resMin=residual[0];resMinPos=0;}else 
				{if (residual[p]<resMin) {resMin=residual[p];resMinPos=p;}
				
			}
		}
		IJ.log(resMinPos+"  "+resMin);
		this.bestFitPos=resMinPos;
		return resMinPos;
	}
	double [] getRow(double[][] input,int row,int maxCol) {
		int length=input.length;
		int col=input[0].length;
		double [] get=new double [maxCol];
		
		for (int n=0;n<maxCol;n++) {
			get[n]=input[row][n];
		}
		
		return get;
		
	}
	
	public void runBestFit(){
		fitParameters=new ResultsTable();
		double []initParam=getRow(fitResults,this.bestFitPos,5);
		int col=inputTable.getLastColumn();
		String formula=createFormula(initParam);
		ImageStack plots=new ImageStack(696,415); 
		for (int n=1;n<=col;n++) {
			fitParameters.addRow();
			CurveFitter cf=new CurveFitter(inputTable.getColumnAsDoubles(0),inputTable.getColumnAsDoubles(n));
			cf.doCustomFit(formula, new double [] {0,1,0}, false);
//			cf.getPlot().show();
			plots.addSlice(cf.getPlot().getImagePlus().getProcessor());
			double [] params=cf.getParams();
			int numParam=params.length;
			for (int i=0;i<numParam;i++) {
				
				fitParameters.addValue(""+i, params[i]);
			}
		}
		new ImagePlus("Fit Plots",plots).show();
		fitParameters.show("Fit Parameters");
	}
	
	public String createFormula(double []parameters) {
		String formula="y=a+b*("+parameters[0];
		int poly=parameters.length;
		
		for (int n=1;n<poly;n++) {
		
		if (parameters[n]>0)formula=formula.concat("+");  
		formula=formula.concat(IJ.d2s(parameters[n],6));
		formula=formula.concat("*pow(x-c,"+n+")");
		}
		formula=formula.concat(")");
		return formula;
	}
	void setMethod(int method){
		this.method=method;
	}
	
}
