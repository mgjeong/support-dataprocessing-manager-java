package com.sec.processing.framework.task.model.libsvm;
import java.util.*;

//
// Kernel evaluation
//
// the static method k_function is for doing single kernel evaluation
// the constructor of Kernel prepares to calculate the l*l kernel matrix
// the member function get_Q is for getting one column from the Q Matrix
//
abstract class QMatrix {
	abstract float[] get_Q(int column, int len);
	abstract double[] get_QD();
	abstract void swap_index(int i, int j);
};

abstract class Kernel extends QMatrix {
	private svm_node[][] x;
	private final double[] x_square;

	// svm_parameter
	private final int kernel_type;
	private final int degree;
	private final double gamma;
	private final double coef0;

	abstract float[] get_Q(int column, int len);
	abstract double[] get_QD();

	void swap_index(int i, int j)
	{
		do {svm_node[] _=x[i]; x[i]=x[j]; x[j]=_;} while(false);
		if(x_square != null) do {double _=x_square[i]; x_square[i]=x_square[j]; x_square[j]=_;} while(false);
	}

	private static double powi(double base, int times)
	{
		double tmp = base, ret = 1.0;

		for(int t=times; t>0; t/=2)
		{
			if(t%2==1) ret*=tmp;
			tmp = tmp * tmp;
		}
		return ret;
	}

	double kernel_function(int i, int j)
	{
		switch(kernel_type)
		{
			case svm_parameter.LINEAR:
				return dot(x[i],x[j]);
			case svm_parameter.POLY:
				return powi(gamma*dot(x[i],x[j])+coef0,degree);
			case svm_parameter.RBF:
				return Math.exp(-gamma*(x_square[i]+x_square[j]-2*dot(x[i],x[j])));
			case svm_parameter.SIGMOID:
				return Math.tanh(gamma*dot(x[i],x[j])+coef0);
			case svm_parameter.PRECOMPUTED:
				return x[i][(int)(x[j][0].value)].value;
			default:
				return 0;	// java
		}
	}

	Kernel(int l, svm_node[][] x_, svm_parameter param)
	{
		this.kernel_type = param.kernel_type;
		this.degree = param.degree;
		this.gamma = param.gamma;
		this.coef0 = param.coef0;

		x = (svm_node[][])x_.clone();

		if(kernel_type == svm_parameter.RBF)
		{
			x_square = new double[l];
			for(int i=0;i<l;i++)
				x_square[i] = dot(x[i],x[i]);
		}
		else x_square = null;
	}

	static double dot(svm_node[] x, svm_node[] y)
	{
		double sum = 0;
		int xlen = x.length;
		int ylen = y.length;
		int i = 0;
		int j = 0;
		while(i < xlen && j < ylen)
		{
			if(x[i].index == y[j].index)
				sum += x[i++].value * y[j++].value;
			else
			{
				if(x[i].index > y[j].index)
					++j;
				else
					++i;
			}
		}
		return sum;
	}

	static double k_function(svm_node[] x, svm_node[] y,
					svm_parameter param)
	{
		switch(param.kernel_type)
		{
			case svm_parameter.LINEAR:
				return dot(x,y);
			case svm_parameter.POLY:
				return powi(param.gamma * dot(x, y) + param.coef0, param.degree);
			case svm_parameter.RBF:
			{
				double sum = 0;
				int xlen = x.length;
				int ylen = y.length;
				int i = 0;
				int j = 0;
				while(i < xlen && j < ylen)
				{
					if(x[i].index == y[j].index)
					{
						double d = x[i++].value - y[j++].value;
						sum += d*d;
					}
					else if(x[i].index > y[j].index)
					{
						sum += y[j].value * y[j].value;
						++j;
					}
					else
					{
						sum += x[i].value * x[i].value;
						++i;
					}
				}

				while(i < xlen)
				{
					sum += x[i].value * x[i].value;
					++i;
				}

				while(j < ylen)
				{
					sum += y[j].value * y[j].value;
					++j;
				}

				return Math.exp(-param.gamma*sum);
			}
			case svm_parameter.SIGMOID:
				return Math.tanh(param.gamma*dot(x,y)+param.coef0);
			case svm_parameter.PRECOMPUTED:
				return	x[(int)(y[0].value)].value;
			default:
				return 0;	// java
		}
	}
}

public class svm {
	//
	// construct and solve various formulations
	//
	public static final int LIBSVM_VERSION=312; 
	public static final Random rand = new Random();

	public static int svm_get_svm_type(svm_model model)
	{
		return model.param.svm_type;
	}

	public static int svm_get_nr_class(svm_model model)
	{
		return model.nr_class;
	}

	public static void svm_get_labels(svm_model model, int[] label)
	{
		if (model.label != null)
			for(int i=0;i<model.nr_class;i++)
				label[i] = model.label[i];
	}

	public static double svm_get_svr_probability(svm_model model)
	{
		if ((model.param.svm_type == svm_parameter.EPSILON_SVR || model.param.svm_type == svm_parameter.NU_SVR) &&
		    model.probA!=null)
		return model.probA[0];
		else
		{
			System.err.print("Model doesn't contain information for SVR probability inference\n");
			return 0;
		}
	}

	public static double svm_predict_values(svm_model model, svm_node[] x, double[] dec_values)
	{
		int i;
		if(model.param.svm_type == svm_parameter.ONE_CLASS ||
		   model.param.svm_type == svm_parameter.EPSILON_SVR ||
		   model.param.svm_type == svm_parameter.NU_SVR)
		{
			double[] sv_coef = model.sv_coef[0];
			double sum = 0;
			for(i=0;i<model.l;i++)
				sum += sv_coef[i] * Kernel.k_function(x,model.SV[i],model.param);
			sum -= model.rho[0];
			dec_values[0] = sum;

			if(model.param.svm_type == svm_parameter.ONE_CLASS)
				return (sum>0)?1:-1;
			else
				return sum;
		}
		else
		{
			int nr_class = model.nr_class;
			int l = model.l;

			double[] kvalue = new double[l];
			for(i=0;i<l;i++)
				kvalue[i] = Kernel.k_function(x,model.SV[i],model.param);

			int[] start = new int[nr_class];
			start[0] = 0;
			for(i=1;i<nr_class;i++)
				start[i] = start[i-1]+model.nSV[i-1];

			int[] vote = new int[nr_class];
			for(i=0;i<nr_class;i++)
				vote[i] = 0;

			int p=0;
			for(i=0;i<nr_class;i++)
				for(int j=i+1;j<nr_class;j++)
				{
					double sum = 0;
					int si = start[i];
					int sj = start[j];
					int ci = model.nSV[i];
					int cj = model.nSV[j];

					int k;
					double[] coef1 = model.sv_coef[j-1];
					double[] coef2 = model.sv_coef[i];
					for(k=0;k<ci;k++) {
						sum += coef1[si + k] * kvalue[si + k];
					}
					for(k=0;k<cj;k++) {
						sum += coef2[sj + k] * kvalue[sj + k];
					}

					sum -= model.rho[p];
					dec_values[p] = sum;					

					if(dec_values[p] > 0)
						++vote[i];
					else
						++vote[j];
					p++;
				}

			int vote_max_idx = 0;
			for(i=1;i<nr_class;i++)
				if(vote[i] > vote[vote_max_idx])
					vote_max_idx = i;

			return model.label[vote_max_idx];
		}
	}

	public static double svm_predict(svm_model model, svm_node[] x)
	{
		int nr_class = model.nr_class;
		double[] dec_values;
		if(model.param.svm_type == svm_parameter.ONE_CLASS ||
				model.param.svm_type == svm_parameter.EPSILON_SVR ||
				model.param.svm_type == svm_parameter.NU_SVR)
			dec_values = new double[1];
		else
			dec_values = new double[nr_class*(nr_class-1)/2];

		double pred_result = svm_predict_values(model, x, dec_values);
		return pred_result;
	}

	static final String svm_type_table[] =
	{
		"c_svc","nu_svc","one_class","epsilon_svr","nu_svr",
	};

	static final String kernel_type_table[]=
	{
		"linear","polynomial","rbf","sigmoid","precomputed"
	};
}
