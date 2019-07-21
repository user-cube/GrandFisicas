package geral;

import java.util.*;
import dim.Unidade;


public class SymbolTable{

    // variavel -> tipo
    private Map<String,Unidade> table = new HashMap<>();

    public boolean exists(String name){
        assert (name!=null);
        return table.containsKey(name);
    }

    public void put(String name, Unidade unidade){
        assert (name!=null);
        assert (unidade!=null);

        table.put(name, unidade);
    }

    public void put(String name){
        assert (name!=null);

        table.put(name, null);
    }


    public Unidade get(String name){
        return table.get(name);
    }


    public Map<String,Unidade> get(){
        return table;
    }

    

}