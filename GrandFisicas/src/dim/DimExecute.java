package dim;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.antlr.v4.runtime.*;


public class DimExecute extends DimBaseVisitor<String>{

	public Set<Unidade> getDimTable(){
		return DimParser.dimTable.table;
	}
	
	@Override public String visitRealType(DimParser.RealTypeContext ctx) {
		String grandeza = visit(ctx.grandeza());
		String sigla = ctx.WORD().getText();

		Unidade elemento = new Unidade(grandeza, sigla, "real");
		DimParser.dimTable.put(elemento);
		
		return ""; 
	}
	
	@Override public String visitIntegerType(DimParser.IntegerTypeContext ctx) {
		String grandeza = visit(ctx.grandeza());
		String sigla = ctx.WORD().getText();

		Unidade elemento = new Unidade(grandeza, sigla, "integer");
		DimParser.dimTable.put(elemento);
		
		return "";  
	}
	
	@Override public String visitGrandeza(DimParser.GrandezaContext ctx) {
		String res = ctx.WORD().getText();
		return res;
	}


}