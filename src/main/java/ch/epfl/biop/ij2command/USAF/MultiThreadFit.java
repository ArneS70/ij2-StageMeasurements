package ch.epfl.biop.ij2command.USAF;

import ij.*;  
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;

import java.util.concurrent.atomic.AtomicInteger;


import ch.epfl.biop.ij2command.stage.general.FitterFunction;
import ch.epfl.biop.ij2command.stage.general.GlobalFitter;
import ch.epfl.biop.ij2command.stage.general.Poly3Fitter;  
  

public class MultiThreadFit extends HorizontalLineAnalysis {

	HorizontalLineAnalysis parameters;
	private final ResultsTable fitResults=new ResultsTable();
	private final ImageStack fitPlots=new ImageStack(696,415);
	
	MultiThreadFit(HorizontalAnalysis ha){
		super(ha);
		this.analysis=super.analysis;
	}
	MultiThreadFit(HorizontalLineAnalysis hla){
		this.parameters=hla;
		this.analysis=hla.analysis;
	}
  
    public void run() { 
    	  
    	horizontalLine=analysis.getHorizontalLine();
		ImagePlus inputImage=analysis.getImage();
		this.cal=inputImage.getCalibration();
		inputImage.getProcessor().setLineWidth(1);
		
    	final AtomicInteger ai = new AtomicInteger(1);
    	
    	final Thread[] threads = newThreadArray();  
  
    		for (int ithread = 0; ithread < threads.length; ithread++) {   
    			// Concurrently run in as many threads as CPUs  
  
    			threads[ithread] = new Thread() {  
  
    				public void run() {  
    					
    					final int last=parameters.lineProfiles.getLastColumn();
    					final int length=Poly3Fitter.header.length;
    					
    					method=FitterFunction.Poly3;
    					fitFunc=new Poly3Fitter(parameters.lineProfiles.getColumnAsDoubles(0),parameters.lineProfiles.getColumnAsDoubles(last/2));
    					fitFunc.setHeader(Poly3Fitter.header);
    					double [] results=fitFunc.getParameter();
    					final String function=new GlobalFitter().createFormula(new double[]{results[0],results[1],results[2],results[3]});
    					try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					for (int i = ai.getAndIncrement(); i < last; i = ai.getAndIncrement()) { 
    					
    						IJ.log("Stack position: "+i);
    						CurveFitter cf=new CurveFitter(parameters.lineProfiles.getColumnAsDoubles(0),parameters.lineProfiles.getColumnAsDoubles(i));
    						cf.doCustomFit(function, new double [] {1, 1,1},false);
    						results=cf.getParams();
    						
    						fitPlots.addSlice(cf.getPlot().getImagePlus().getProcessor());
    						fitResults.addRow();
    						for (int n=0;n<length-1;n++) {
    							
    							fitResults.addValue("z / slice", i);
    							fitResults.addValue("p"+n, results[n]);
    								//fitResults.addValue(fitFunc.header[i], results[i]);
    						}
    						
    					}
    						
    					
    					
            }  
        
        };  
    }  
    
    startAndJoin(threads);
    
    
  
        // now the results array is full. Just show them in a stack:  
 //   final ImageStack stack = new ImageStack(dot_blot.getWidth(), dot_blot.getHeight());  
 //   for (int i=0; i< results.length; i++) {  
 //           stack.addSlice(Integer.toString(i), results[i]);  
//    }  
 //       new ImagePlus("Results", stack).show();  
    }  
    ImageStack getFitPlots() {
    	return fitPlots; 
    }
    ResultsTable getFitResults() {
    	return fitResults;
    }
    /** Create a Thread[] array as large as the number of processors available. 
    * From Stephan Preibisch's Multithreading.java class. See: 
    * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
    */  
    private Thread[] newThreadArray() {  
        int n_cpus = Runtime.getRuntime().availableProcessors();  
        return new Thread[n_cpus];  
    }  
  
    /** Start all given threads and wait on each of them until all are done. 
    * From Stephan Preibisch's Multithreading.java class. See: 
    * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
    */  
    public static void startAndJoin(Thread[] threads)  
    {  
        for (int ithread = 0; ithread < threads.length; ++ithread)  
        {  
            threads[ithread].setPriority(Thread.NORM_PRIORITY);  
            threads[ithread].start();  
        }  
  
        try  
        {     
            for (int ithread = 0; ithread < threads.length; ++ithread)  
                threads[ithread].join();  
        } catch (InterruptedException ie)  
        {  
            throw new RuntimeException(ie);  
        }  
    }

    
  
}  