public class lineregression {
    public static double[] calculate_lineregression(double[] stock_price, int start, int end){
        double start_double = (double) start;
        double end_double = (double) end;
        double t_bar = (end_double+start_double)/2;
        double sum = 0.00d;
        for(double j:stock_price){
            sum += j; 
        }
        double Y_bar = sum/(end_double-start_double+1);
        // Calculate b1
        double numerator = 0.00d;
        double denominator = 0.00d;
        double t = start_double;
        for(double j:stock_price){
            numerator += (t-t_bar)*(j-Y_bar);
            denominator += (t-t_bar)*(t-t_bar);
            t=t+1.0000d;
        }

        double b1 = numerator/denominator;
        // calculate b0
        double b0 = Y_bar-(b1*t_bar);

        double[] List = {b0, b1};
        return List;
    }

    
}