package geral;
import dim.Unidade;
import dim.DimTable;
import dim.DimMain;
import dim.DimExecute;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import java.util.*;
import java.io.*;

public class GeralSemanticAnalysis extends GeralBaseVisitor<Boolean> {

	protected SymbolTable tabela;
	protected Set<Unidade> tabelaDimensoes = new TreeSet<>();
	protected Map<String, Double> tabelaValores = new HashMap<>();


	public GeralSemanticAnalysis(SymbolTable t){
		this.tabela = t;
	}

	public Set<Unidade> getTabelaDimensoes(){
		return tabelaDimensoes;
	}

	public SymbolTable getSymbolTable(){
		return tabela;
	}



	@Override public Boolean visitMain(GeralParser.MainContext ctx) { 
		boolean res = true;

		// adicionar o tipo escalar que sera o tipo de variaveis sem dimensao
		tabelaDimensoes.add(new Unidade("real", null, "real"));
		tabelaDimensoes.add(new Unidade("integer", null, "integer"));

		if(ctx.head()!=null){
			Iterator<GeralParser.HeadContext> iter = ctx.head().iterator();

			while(res && iter.hasNext()){
				GeralParser.HeadContext head = iter.next();
				res = visit(head);
				if(!res)
					return false;
			}
		}

		res = visit(ctx.body());

		return res;
	}
	
	@Override public Boolean visitHead(GeralParser.HeadContext ctx) {
	 	return visit(ctx.pacote());
	
	}
	
	@Override public Boolean visitBody(GeralParser.BodyContext ctx) { 
		
		boolean res = true;

		if(ctx.stat()!=null){
			Iterator<GeralParser.StatContext> iter = ctx.stat().iterator();

			while(res && iter.hasNext()){
				GeralParser.StatContext stat = iter.next();
				visit(stat);
			}

		}
	
		return res;
	
	}
	
	@Override public Boolean visitAttrDefined(GeralParser.AttrDefinedContext ctx) { 

		
		boolean res = visit(ctx.conta());

		if(res){
			String id = ctx.WORD().getText();

			if(!tabela.exists(id)){

				ErrorHandling.printError(ctx, "variável <" + id + "> não existente!");
				System.exit(1);

			}
			else{
				double d = ctx.conta().value;
				String type = tabela.get(id).getTipo();
				String nomeGrand = tabela.get(id).getNome();

				/*	verificar se o que esta no lado direito da atribuicao e compativel com o tipo da dimensao
					e, se for, ver se o que esta do lado direito e da mesma dimensao tambem*/
				if(ctx.conta().nomeGrandeza!=null && !ctx.conta().nomeGrandeza.equals(type)){
					if(!ctx.conta().nomeGrandeza.equals(nomeGrand)){
						ErrorHandling.printError(ctx, "dimensão da variável <" + id + "> não compatível com " + ctx.conta().nomeGrandeza);
						System.exit(1);
					}
					
				}
				tabelaValores.put(id, ctx.conta().value); // guardar o valor da variavel
			}
		}

		return res; 
	
	}
	
	@Override public Boolean visitAttrGrandeza(GeralParser.AttrGrandezaContext ctx) { 
		visit(ctx.conta());
		boolean grandezaExiste = visit(ctx.g1);
		
		
		String var = ctx.WORD().getText();


		if(!grandezaExiste){
			ErrorHandling.printError(ctx, "dimensao <" + ctx.g1.WORD().getText() + "> não reconhecida.");
			System.exit(1);
		}
		

		if(tabela.exists(var)){
			ErrorHandling.printError(ctx, "variável <" + var + "> já existente!");
			System.exit(1);
		}

		String g1 = ctx.g1.WORD().getText();
		String g2 = ctx.conta().nomeGrandeza;

		// se g2 for null, trata-se de um input
		if(g2 == null){
			ctx.conta().nomeGrandeza = g1;
			g2 = g1;
		}
			

		if(!g1.equals(g2)){
			ErrorHandling.printError(ctx, "dimensões incompatíveis: " + g1 + ", " + g2);
			System.exit(1);
		}




		// verificar a unidade com o nome da grandeza atual e adicionar essa variavel e o seu tipo a SymbolTable
		for(Unidade x : tabelaDimensoes){
			if(x.getNome().equals(Globals.nomeGrandeza)){
				double d = ctx.conta().value;
				
				
				String type = x.getTipo();
				String nomeGrand = x.getNome();
				
				/*	verificar se o que esta no lado direito da atribuicao e compativel com o tipo da dimensao
					e, se for, ver se o que esta do lado direito e da mesma dimensao tambem*/
				if(!ctx.conta().nomeGrandeza.equals(type)){
					if(!ctx.conta().nomeGrandeza.equals(nomeGrand)){
						ErrorHandling.printError(ctx, "dimensão da variável a criar <" + var + "> não compatível com " + ctx.conta().nomeGrandeza);
						System.exit(1);
					}
					
				}
				tabela.put(var, x);
				tabelaValores.put(var, d);
				break;
			}
		}

		return true;

	 }

	@Override public Boolean visitAttrPrimitivo(GeralParser.AttrPrimitivoContext ctx) { 


		visit(ctx.conta());
		
		String var = ctx.WORD().getText();
		String type = ctx.type.getText();
		double d = ctx.conta().value;

		if(tabela.exists(var)){
			ErrorHandling.printError(ctx, "variável <" + var + "> já existente!");
			System.exit(1);
		}
		

		
		if(ctx.conta().nomeGrandeza!=null && !ctx.conta().nomeGrandeza.equals(type) ){
			ErrorHandling.printError(ctx, "dimensão da variável a criar <" + var + "> não compatível com " + ctx.conta().nomeGrandeza);
			System.exit(1);
		}



		// verificar a unidade com o nome da grandeza atual e adicionar essa variavel e o seu tipo a SymbolTable
		for(Unidade x : tabelaDimensoes){
			if(x.getNome().equals(type)){
				tabela.put(var, x);
				tabelaValores.put(var, d);
				break;
			}
		}

		
		 
		return true; 
	
	}

	@Override public Boolean visitParanthesis(GeralParser.ParanthesisContext ctx) { 
		boolean res = visit(ctx.conta());
		ctx.value = ctx.conta().value;
		ctx.nomeGrandeza = ctx.conta().nomeGrandeza;
		return res; }
	
	@Override public Boolean visitValue(GeralParser.ValueContext ctx) { 

		boolean res = visit(ctx.valor());

		ctx.value = ctx.valor().value;

		ctx.nomeGrandeza = ctx.valor().nomeGrandeza;

		return res;
	
	
	}

	@Override public Boolean visitContaInput(GeralParser.ContaInputContext ctx) {
		
		ctx.value = 1; // valor por omissao para correto funcionamente do input

		return true; }
	
	@Override public Boolean visitSumSubConta(GeralParser.SumSubContaContext ctx) { 

		visit(ctx.v1); visit(ctx.v2); // os dois operandos

		double v1 = ctx.v1.value;
		double v2 = ctx.v2.value;

		String grandezaLeft = ctx.v1.nomeGrandeza;
		String grandezaRight = ctx.v2.nomeGrandeza;


		if(!grandezaLeft.equals(grandezaRight)){
			ErrorHandling.printError(ctx,"não é possível somar operandos de dimensões diferentes: " + grandezaLeft + ", " + grandezaRight);
			System.exit(1);
		}
		

		// se chegar aqui, guardar o novo valor e o seu tipo
		String sinal = ctx.op.getText();


		if(sinal.equals("+"))
			ctx.value = v1 + v2;
		else
			ctx.value = v1 - v2;
			
			// 1 dos dois, tanto a esquerda como a direita tem a mesma dimensao
			ctx.nomeGrandeza = grandezaLeft; 
	
		
		return true; 

	}
	
	@Override public Boolean visitMultDivConta(GeralParser.MultDivContaContext ctx) { 
		
		visit(ctx.v1); visit(ctx.v2); // os dois operandos

		double v1 = ctx.v1.value;
		double v2 = ctx.v2.value;

		String grandezaLeft = ctx.v1.nomeGrandeza;
		String grandezaRight = ctx.v2.nomeGrandeza;


		// se chegar aqui, guardar o novo valor e o seu tipo
		String sinal = ctx.op.getText();

		boolean esqAdimensional = grandezaLeft.equals("integer") || grandezaLeft.equals("real");
		boolean direitaAdimensional = grandezaRight.equals("integer") || grandezaRight.equals("real");

		boolean esqInteira = grandezaLeft.equals("integer");
		boolean esqReal = grandezaLeft.equals("real");
		boolean direitaInteira = grandezaRight.equals("integer");
		boolean direitaReal = grandezaRight.equals("real");
		


		if(sinal.equals("*")){
			if(esqInteira && direitaInteira){
				ctx.nomeGrandeza = "integer";
			}
			else if(esqReal && direitaReal){
				ctx.nomeGrandeza = "real";
			}

			else if(esqAdimensional && !direitaAdimensional){
				ctx.nomeGrandeza = grandezaRight;
			}

			else if(direitaAdimensional && !esqAdimensional){
				ctx.nomeGrandeza = grandezaLeft;
			}

			else if(esqAdimensional){
				if(!direitaInteira) // confirmar que a direita nao e inteira
					ctx.nomeGrandeza = grandezaRight;
				else
					ctx.nomeGrandeza =  "real";
			}
			else if(direitaAdimensional){
				if(!esqInteira) // confirmar que a esquerda nao e inteira
					ctx.nomeGrandeza = grandezaLeft;
				else
					ctx.nomeGrandeza =  "real";
			} 
			else if((esqReal && direitaInteira) || (esqInteira && direitaReal)) {
				ctx.nomeGrandeza = "real";
			} 
			
			else{
				ctx.nomeGrandeza = grandezaLeft + "" + grandezaRight;
				Iterator<Unidade> it = tabelaDimensoes.iterator();
				String siglal="";
				String siglar="";
				while(it.hasNext()){
					Unidade u = it.next();
					if(u.getNome().equals(grandezaLeft)){
						siglal = u.getSigla();
					}
					if(u.getNome().equals(grandezaRight)){
						siglar = u.getSigla();
					}
				}
				String sigla = siglal +"."+ siglar;


				Unidade nova = new Unidade(ctx.nomeGrandeza,sigla, "real");
				tabelaDimensoes.add(nova);
			}
			ctx.value = v1 * v2;
		}else if(sinal.equals("/")){

			if(v2==0 || v2==0.0){
				ErrorHandling.printError(ctx,"Não é possivel dividir por 0. ");
				System.exit(1);
			}
			else if(esqAdimensional && direitaAdimensional) {
				ctx.nomeGrandeza = "real";
			} 
			else if(esqAdimensional){
				ctx.nomeGrandeza = grandezaRight;
			}
			else if(direitaAdimensional){
				ctx.nomeGrandeza = grandezaLeft;
			} else{
				String siglal="";
				String siglar="";
				Iterator<Unidade> it = tabelaDimensoes.iterator();
				while(it.hasNext()){
					Unidade u = it.next();
					if(u.getNome().equals(grandezaLeft)){
						siglal = u.getSigla();
					}
					if(u.getNome().equals(grandezaRight)){
						siglar = u.getSigla();
					}
				}

				// sigla resultante da operação
				String sigla = siglal +"/"+ siglar;

				/**
				 * ver se a nova sigla já é de uma unidade existente
				 * se da operacao resultar, por exemplo, m/s e tiver sido importada
				 * a dimensao velocidade coma essa sigla, a dimensao da operacao
				 * fica com velocidade */ 
				boolean siglaExistente = false;
				for(Unidade u : tabelaDimensoes){
					if(u.getSigla()!=null && u.getSigla().equals(sigla)){
						ctx.nomeGrandeza = u.getNome();
						siglaExistente = true;
						break;
					}
				}
				
				if(!siglaExistente){
					ctx.nomeGrandeza = grandezaLeft + "por" + grandezaRight;
					Unidade nova = new Unidade(ctx.nomeGrandeza,sigla, "real");
					tabelaDimensoes.add(nova);
				}

			}
			ctx.value = v1 / v2;
		}

		return true;
	}
	
	@Override public Boolean visitValorVariavel(GeralParser.ValorVariavelContext ctx) { 

		String var = ctx.WORD().getText();
		

		if(!tabela.exists(var)){
			ErrorHandling.printError(ctx,"variável <" + var + "> não existe.");
			System.exit(1);
		}

		ctx.value = tabelaValores.get(var);
		
		ctx.nomeGrandeza = tabela.get(var).getNome();
	
		return true; 
	
	}
	
	@Override public Boolean visitValorNumero(GeralParser.ValorNumeroContext ctx) { 


		String g = "";
		if(ctx.g != null){
			boolean grandezaExiste = visit(ctx.grandeza());
			g = ctx.g.WORD().getText();
			if(!grandezaExiste){
				ErrorHandling.printError(ctx, "dimensao <" + g + "> não reconhecida.");
				System.exit(1);
			}

			String tipoGrandeza = ""; // tipo da grandeza real ou inteiro
			Unidade u;
			for(Unidade x : tabelaDimensoes){
				if(x.getNome().equals(g)){
					u = x;
					tipoGrandeza = x.getTipo(); // tipo da grandeza
					break;
				}
			}

			String tipoNumero = "";

			try {
				ctx.value = Integer.parseInt(ctx.NUMBER().getText());
				tipoNumero = "integer";
			} catch (Exception e) {
				ctx.value = Double.parseDouble(ctx.NUMBER().getText());
				tipoNumero = "real";
			}

			if(!tipoNumero.equals(tipoGrandeza) && tipoGrandeza.equals("integer")){
				ErrorHandling.printError(ctx, "dimensões incompatíveis: " + tipoGrandeza + ", " + tipoNumero);
				System.exit(1);
			}
					
			ctx.nomeGrandeza = g;
			
		}
		// ser adimensional
		else{
			try {
				ctx.value = Integer.parseInt(ctx.NUMBER().getText());
				ctx.nomeGrandeza = "integer";
			} catch (Exception e) {
				ctx.value = Double.parseDouble(ctx.NUMBER().getText());
				ctx.nomeGrandeza = "real";
			}
		}


		return true; 
	}
	
	@Override public Boolean visitValorNumeroNegativo(GeralParser.ValorNumeroNegativoContext ctx) { 

		String g = "";
		if(ctx.g != null){
			boolean grandezaExiste = visit(ctx.grandeza());
			g = ctx.g.WORD().getText();
			if(!grandezaExiste){
				ErrorHandling.printError(ctx, "dimensao <" + g + "> não reconhecida.");
				System.exit(1);
			}

			String tipoGrandeza = ""; // tipo da grandeza real ou inteiro
			Unidade u;
			for(Unidade x : tabelaDimensoes){
				if(x.getNome().equals(g)){
					u = x;
					tipoGrandeza = x.getTipo(); // tipo da grandeza
					break;
				}
			}

			String tipoNumero = "";

			try {
				ctx.value = Integer.parseInt(ctx.NUMBER().getText()) * -1;
				tipoNumero = "integer";
			} catch (Exception e) {
				ctx.value = Double.parseDouble(ctx.NUMBER().getText()) * -1;
				tipoNumero = "real";
			}

			if(!tipoNumero.equals(tipoGrandeza) && tipoGrandeza.equals("integer")){
				ErrorHandling.printError(ctx, "dimensões incompatíveis: " + tipoGrandeza + ", " + tipoNumero);
				System.exit(1);
			}
						
			ctx.nomeGrandeza = g;
			
		}
		// ser adimensional
		else{
			try {
				ctx.value = Integer.parseInt(ctx.NUMBER().getText()) * -1;
				ctx.nomeGrandeza = "integer";
			} catch (Exception e) {
				ctx.value = Double.parseDouble(ctx.NUMBER().getText()) * -1;
				ctx.nomeGrandeza = "real";
			}
		}

		return true; 
	}
	
	@Override public Boolean visitOrCondicao(GeralParser.OrCondicaoContext ctx) { 
		return visit(ctx.c1) && visit(ctx.c2);  
	}

	@Override public Boolean visitAndCondicao(GeralParser.AndCondicaoContext ctx) { 
		return visit(ctx.c1) && visit(ctx.c2); 
	}
	
	@Override public Boolean visitComparacaoCondicao(GeralParser.ComparacaoCondicaoContext ctx) { return visit(ctx.comparacao()); }
	
	@Override public Boolean visitBooleanCondicao(GeralParser.BooleanCondicaoContext ctx) { return true; }

	@Override public Boolean visitComparacao(GeralParser.ComparacaoContext ctx) { 
		
		visit(ctx.c1); visit(ctx.c2); // visitar cada conta a comparar

		String grandezaL = ctx.c1.nomeGrandeza;
		String grandezaR = ctx.c2.nomeGrandeza;

		if(!grandezaL.equals(grandezaR)){
			ErrorHandling.printError(ctx,"não é possível comparar dimensões diferentes (" + grandezaL + ", " + grandezaR + ")");
			System.exit(1);
		}
		
		return true; 
	}
	
	@Override public Boolean visitGrandeza(GeralParser.GrandezaContext ctx) {
		// verificar aqui se a grandeza existe
		String nomeGrandeza = ctx.WORD().getText();

		if(tabelaDimensoes.contains(new Unidade(nomeGrandeza,null,null))){
			Globals.nomeGrandeza = nomeGrandeza;
			return true;
		}
			
		return false;
	}
		
	@Override public Boolean visitPacote(GeralParser.PacoteContext ctx) { 

		String file = ctx.FILE().getText();

		String caminho = "";

		if(ctx.FOLDER().size()>0){
			caminho = ctx.FOLDER(0).getText();
			for(int i = 1; i<ctx.FOLDER().size(); i++){
				caminho += ctx.FOLDER(i).getText();
			}
		}

		String importFinal = caminho + file;

		try {
			String[] args = new String[1];
			args[0] = importFinal;
			DimMain.main(args);
			tabelaDimensoes.addAll(DimMain.getDimTable());

		} catch (Exception e) {
			ErrorHandling.printError(ctx,"ficheiro <" + importFinal + "> não existe.");
			System.exit(1);
		}
		
		return true;

	}
}