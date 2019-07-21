package geral;

public class Globals{


    public static double valorAtual = 0;
    public static double valorAnterior = 0;
    public static String nomeGrandeza = "";


    public static void updateValorAtual(double v){
        double temp = valorAtual;
        valorAnterior = temp;
        valorAtual = v;
    }

}