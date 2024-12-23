package ch.epfl.biop.ij2command.stage.general;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

/**
 * This is the example of LevenbergMaaquardt Optimization by using Apache Commons Math (3.4 API)
 * Codes are modified from the eample of Quardratic Problem Example of 
 * the user guide of Apache Commons Math -12 Optimization-
 * http://commons.apache.org/proper/commons-math/userguide/optimization.html
 * @author Yoshiyuki Arai
 * @data 05/01/2015
 * 
 */

public class GaussProblemExample_2 {
	
	public static void main(String[] args) {
		// construct input data storage as ArrayList type
		List<Double> x = new ArrayList<Double>();
		List<Double> y = new ArrayList<Double>();
        
        // construct QuadraticFunction
        AsymGaussFunction qf = new AsymGaussFunction(x,y);
        
        //entry the data
		qf.addPoint(-10, 1.09);
		qf.addPoint(-9, 1.12);
		qf.addPoint(-8, 1.17);
		qf.addPoint(-7, 1.23);
		qf.addPoint(-6, 1.31);
		qf.addPoint(-5, 1.41);
		qf.addPoint(-4, 1.53);
		qf.addPoint(-3, 1.67);
		qf.addPoint(-2, 1.82);
		qf.addPoint(-1, 1.98);
		qf.addPoint(0, 2.13);
		qf.addPoint(1, 2.25);
		qf.addPoint(2, 2.35);
		qf.addPoint(3, 2.39);
		qf.addPoint(4, 2.37);
		qf.addPoint(5, 2.28);
		qf.addPoint(6, 2.13);
		qf.addPoint(7, 1.93);
		qf.addPoint(8, 1.71);
		qf.addPoint(9, 1.51);
		qf.addPoint(10, 1.35);
		

		//prepare construction of LeastSquresProblem by builder
		LeastSquaresBuilder lsb = new LeastSquaresBuilder();

		//set model function and its jacobian
		lsb.model(qf.retMVF(), qf.retMMF());
		double[] newTarget = qf.calculateTarget();
		
		//set target data
		lsb.target(newTarget);
		double[] newStart = {1,1,1,1,1,1};
		//set initial parameters
		lsb.start(newStart);
		//set upper limit of evaluation time
		lsb.maxEvaluations(100000);
		//set upper limit of iteration time
		lsb.maxIterations(1000);

		//construct LevenbergMarquardtOptimizer 
		LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();
		try{
			//do LevenbergMarquardt optimization
			LeastSquaresOptimizer.Optimum lsoo = lmo.optimize(lsb.build());
			
			//get optimized parameters
			final double[] optimalValues = lsoo.getPoint().toArray();			
			//output data
			System.out.println("A: " + optimalValues[0]);
			System.out.println("B: " + optimalValues[1]);
			System.out.println("C: " + optimalValues[2]);
			System.out.println("D: " + optimalValues[3]);
			System.out.println("E: " + optimalValues[4]);
			System.out.println("F: " + optimalValues[5]);
			System.out.println("Iteration number: "+lsoo.getIterations());
			System.out.println("Evaluation number: "+lsoo.getEvaluations());
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}
