package geral;
import dim.Unidade;
import dim.DimTable;
import dim.DimMain;
import dim.DimExecute;
import org.antlr.v4.runtime.*;
import org.stringtemplate.v4.*;
import java.io.*;
import java.util.*;
import java.lang.Process;

 
public class GeralCompiler extends GeralBaseVisitor<ST> {

	
	protected SymbolTable tabela;
	protected Set<Unidade> tabelaDimensoes;
	protected STGroup stg;

	protected String filename;
	protected String className;

	protected String tipoInput = "inputDouble";
	protected String stringAimprimir = "";

	// nossoNome -> varX
	protected Map<String,String> varMap = new HashMap<String,String>();
	protected long varNumber = 0;

	/**
	 * conversao de nomes das variáveis de código fonte para nomes válidos no código
	 * destino, neste caso, Java
	 * @return nome da variável Java: 'varX', em que 'X' é um inteiro autogerado
	 */
	public String createVar(){
		varNumber++;
		return "var" + varNumber;
	}

	public GeralCompiler(String filename){
		this.filename = filename;
		stg = new STGroupFile("geral/java.stg");
		prepareClassName();
	}

	public void setTabelaDimensoes(Set<Unidade> t){
		this.tabelaDimensoes = t;
	}

	public void setSymbolTable(SymbolTable t){
		this.tabela = t;
	}

	/**
	 * 
	 * @param s resultado do render do String Template para ser escrito
	 * no ficheiro Java para que possa ser compilado
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void compile(String s) throws IOException, InterruptedException {

		// escrever no ficheiro java o resultado do render do ST
		String fileToCompile = className + ".java";
		PrintWriter writer = new PrintWriter(fileToCompile, "UTF-8");
		writer.println(s);
		writer.close();
		

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec("javac " + fileToCompile);
			p.waitFor();
			int exitStatus = p.exitValue();

			if(exitStatus!=0){
				System.err.println(fileToCompile + " não compilado :(");
			}
			else
				System.err.println(fileToCompile + " compilado com sucesso! :)");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * processar o nome do ficheiro para obter o nome da classe a compilar	
	 */
	public void prepareClassName(){
		String[] splitted = filename.split("[.]");
		String[] splitted2 = splitted[0].split("/");
		
		String classFile = splitted2[splitted2.length-1];
		String classNome = classFile.substring(0, 1).toUpperCase() + classFile.substring(1);

		className = classNome;
	}

	@Override public ST visitMain(GeralParser.MainContext ctx) {

		ST res = stg.getInstanceOf("main");
		
		if(ctx.head()!=null){
			Iterator<GeralParser.HeadContext> iter = ctx.head().iterator();

			while(iter.hasNext()){
				GeralParser.HeadContext head = iter.next();
				res.add("head", visit(head).render());
			}

		}

		res.add("body", visit(ctx.body()).render());
		

		return res; 
	
	}

	@Override public ST visitHead(GeralParser.HeadContext ctx) { 
		ST res = stg.getInstanceOf("head");

		visit(ctx.pacote());
		res.add("library_name", ctx.pacote().result);
		
		return res;
	 }

	@Override public ST visitBody(GeralParser.BodyContext ctx) { 

		ST res = stg.getInstanceOf("body");
		res.add("className", className);

		if(ctx.stat()!=null){
			Iterator<GeralParser.StatContext> iter = ctx.stat().iterator();

			while(iter.hasNext()){
				GeralParser.StatContext stat = iter.next();
				res.add("stat", visit(stat).render());
			}

		}

		return res; 
	 }

	@Override public ST visitStatLinha(GeralParser.StatLinhaContext ctx) {
		ST res = stg.getInstanceOf("statLinha");

		res.add("linha",visit(ctx.linha()).render());
		if(ctx.end.getText().equals(";"))
			res.add("end", ";\n");
		else
			res.add("end", ctx.EOF().getText());
		
		
		return res; }
	 
	@Override public ST visitStatIf(GeralParser.StatIfContext ctx) {
		ST res = stg.getInstanceOf("statIf");
		
		res.add("condicaoIf", visit(ctx.condicaoif()).render());
		return res; }

	@Override public ST visitLinhaConta(GeralParser.LinhaContaContext ctx) {
		ST res = stg.getInstanceOf("linhaConta");

		res.add("conta", visit(ctx.conta()).render());

		res.add("possivelPrint", stringAimprimir);


		return res;
	}
	
	@Override public ST visitAttrDefined(GeralParser.AttrDefinedContext ctx) { 
		String id = ctx.WORD().getText();
		ST res = stg.getInstanceOf("attrDefined");

		String tipo = "";
		// ver se é uma das var já compiladas
		if(varMap.containsKey(id)){
			res.add("word", varMap.get(id));
		}
		else{
			String newVAR = createVar();
			res.add("word", newVAR);
			varMap.put(id, newVAR);
		}

		tipo = tabela.get(id).getTipo();
		

		if(tipo.equals("integer")){
			tipoInput = "inputInt";
		}else{
			tipoInput = "inputDouble";
		}
		
		res.add("conta", visit(ctx.conta()).render());
		res.add("possivelPrint", stringAimprimir);
			
		return res; 
	
	}

	@Override public ST visitAttrGrandeza(GeralParser.AttrGrandezaContext ctx) {
		ST res = stg.getInstanceOf("attrGrandeza");
		

		String nomeGrandeza = ctx.g1.WORD().getText();
		String tipoGrandeza = "";

		for(Unidade u : tabelaDimensoes){
			if(u.getNome().equals(nomeGrandeza)){
				tipoGrandeza = u.getTipo();
				break;
			}
		}

		res.add("grandeza", tipoGrandeza);

		if(tipoGrandeza.equals("integer")){
			tipoInput = "inputInt";
		}else{
			tipoInput = "inputDouble";
		}

		String id = ctx.WORD().getText();
		if(varMap.containsKey(id)){
			res.add("var", varMap.get(id));
		}
		else{
			String newVAR = createVar();
			res.add("var", newVAR);
			varMap.put(id, newVAR);
		}

		res.add("conta", visit(ctx.conta()).render());

		res.add("possivelPrint", stringAimprimir);
		
	 	return res; 
	}

	@Override public ST visitAttrPrimitivo(GeralParser.AttrPrimitivoContext ctx) {
		ST res = stg.getInstanceOf("attrPrimitivo");

		res.add("type", ctx.type.getText());

		String id = ctx.WORD().getText();
		if(varMap.containsKey(id)){
			res.add("word", varMap.get(id));
		}
		else{
			String newVAR = createVar();
			res.add("word", newVAR);
			varMap.put(id, newVAR);
		}

		if(ctx.type.getText().equals("integer")){
			tipoInput = "inputInt";
		}else{
			tipoInput = "inputDouble";
		}

		res.add("conta", visit(ctx.conta()).render());

		res.add("possivelPrint", stringAimprimir);


		return res;
	}
	
	@Override public ST visitParanthesis(GeralParser.ParanthesisContext ctx) {
		ST res = stg.getInstanceOf("paranthesis");
		res.add("conta", visit(ctx.conta()).render());
	 	return res;
	}

	@Override public ST visitSumSubConta(GeralParser.SumSubContaContext ctx) {
		ST res = stg.getInstanceOf("contaSumSub");

		res.add("conta1", visit(ctx.v1).render());
		res.add("op", ctx.op.getText());
		res.add("conta2", visit(ctx.v2).render());

		return res; 
	}
	
	@Override public ST visitMultDivConta(GeralParser.MultDivContaContext ctx) {
		ST res = stg.getInstanceOf("contaMultDiv");

		res.add("conta1", visit(ctx.v1).render());
		res.add("op", ctx.op.getText());
		res.add("conta2", visit(ctx.v2).render());

		return res; 
	}
	
	@Override public ST visitValorVariavel(GeralParser.ValorVariavelContext ctx) {
		ST res = stg.getInstanceOf("valorVariavel");

		stringAimprimir = "";

		String id = ctx.WORD().getText();
		if(varMap.containsKey(id)){
			res.add("id", varMap.get(id));
		}
		else{
			String newVAR = createVar();
			res.add("id", newVAR);
			varMap.put(id, newVAR);
		}
		return res;
	}
	
	@Override public ST visitValorNumero(GeralParser.ValorNumeroContext ctx) {
		ST res = stg.getInstanceOf("valorNumero");
		stringAimprimir = "";

		res.add("numero", ctx.NUMBER().getText());
	 	return res;
	}

	@Override public ST visitValorNumeroNegativo(GeralParser.ValorNumeroNegativoContext ctx) {
		ST res = stg.getInstanceOf("valorNumeroNeg");
		stringAimprimir = "";

		res.add("numeroNeg", ctx.NUMBER().getText());
	 	return res;
	}

	@Override public ST visitIfStat(GeralParser.IfStatContext ctx) {
		ST res = stg.getInstanceOf("condicaoif");
		res.add("condicao", visit(ctx.condicao()).render());

		if(ctx.stat()!=null){
			Iterator<GeralParser.StatContext> iter = ctx.stat().iterator();

			while(iter.hasNext()){
				GeralParser.StatContext stat = iter.next();
				res.add("stat", visit(stat).render());
			}

		}

	 	return res;
	 }

	@Override public ST visitIfStatElif(GeralParser.IfStatElifContext ctx) {
		ST res = stg.getInstanceOf("condicaoifElif");
		res.add("condicao", visit(ctx.condicao()).render());

		if(ctx.stat()!=null){
			Iterator<GeralParser.StatContext> iter = ctx.stat().iterator();

			while(iter.hasNext()){
				GeralParser.StatContext stat = iter.next();
				res.add("stat", visit(stat).render());
			}

		}

		if(ctx.condicaoelif()!=null){
			Iterator<GeralParser.CondicaoelifContext> iter = ctx.condicaoelif().iterator();

			while(iter.hasNext()){
				GeralParser.CondicaoelifContext elif = iter.next();
				res.add("condicaoElif", visit(elif).render());
			}

		}

		if(ctx.condicaoelse()!=null){
			res.add("condicaoElse", visit(ctx.condicaoelse()).render());
		}else{
			res.add("condicaoElse", "\n");
		}

		 return res;
	}
	
	@Override public ST visitIfStatElse(GeralParser.IfStatElseContext ctx) {
		ST res = stg.getInstanceOf("condicaoifElse");
		res.add("condicao", visit(ctx.condicao()).render());

		if(ctx.stat()!=null){
			Iterator<GeralParser.StatContext> iter = ctx.stat().iterator();

			while(iter.hasNext()){
				GeralParser.StatContext stat = iter.next();
				res.add("stat", visit(stat).render());
			}

		}
		
		res.add("condicaoElse", visit(ctx.condicaoelse()).render());

		return res;
	}
	
	@Override public ST visitCondicaoelif(GeralParser.CondicaoelifContext ctx) {
		ST res = stg.getInstanceOf("condicaoElif");
		res.add("condicao", visit(ctx.condicao()).render());

		if(ctx.stat()!=null){
			Iterator<GeralParser.StatContext> iter = ctx.stat().iterator();

			while(iter.hasNext()){
				GeralParser.StatContext stat = iter.next();
				res.add("stat", visit(stat).render());
			}

		}
		return res;
	}
	
	@Override public ST visitCondicaoelse(GeralParser.CondicaoelseContext ctx) {
		ST res = stg.getInstanceOf("condicaoElse");
		if(ctx.stat()!=null){
			Iterator<GeralParser.StatContext> iter = ctx.stat().iterator();

			while(iter.hasNext()){
				GeralParser.StatContext stat = iter.next();
				res.add("stat", visit(stat).render());
			}

		}
		return res;
	}
	
	@Override public ST visitCondicaowhile(GeralParser.CondicaowhileContext ctx) { 
		ST res = stg.getInstanceOf("condicaoWhile");
		res.add("condicao", visit(ctx.condicao()).render());

		if(ctx.stat()!=null){
			Iterator<GeralParser.StatContext> iter = ctx.stat().iterator();

			while(iter.hasNext()){
				GeralParser.StatContext stat = iter.next();
				res.add("stat", visit(stat).render());
			}

		}

	 	return res;
	}
	
	@Override public ST visitOrCondicao(GeralParser.OrCondicaoContext ctx) { 
		ST res = stg.getInstanceOf("condicaoOr");

		res.add("condicao1", visit(ctx.c1).render());
		res.add("condicao2", visit(ctx.c2).render());
		return res; 
	}
	
	@Override public ST visitAndCondicao(GeralParser.AndCondicaoContext ctx) {
		ST res = stg.getInstanceOf("condicaoAnd");

		res.add("condicao1", visit(ctx.c1).render());
		res.add("condicao2", visit(ctx.c2).render());
		return res; 
	}

	@Override public ST visitParanthesisCondicao(GeralParser.ParanthesisCondicaoContext ctx) { 
		ST res = stg.getInstanceOf("paranthesisCondicao");
		res.add("condicao", visit(ctx.condicao()).render());
	 	return res;
	}
	
	@Override public ST visitPrint(GeralParser.PrintContext ctx) { 
		ST res = stg.getInstanceOf("print");

		stringAimprimir = "";

		if(ctx.STRING() != null){
			res.add("string", ctx.STRING().getText());
		}else{
			res.add("string", visit(ctx.conta()).render());
		}

		res.add("possivelPrint", stringAimprimir);
					
		
		return res;
	}

	@Override public ST visitInput(GeralParser.InputContext ctx) {

		ST res = stg.getInstanceOf(tipoInput);

		stringAimprimir = "System.out.print(" + ctx.STRING().getText() + ");";
		
		return res; 
	
	}
	
	@Override public ST visitCondicaoBoolean(GeralParser.CondicaoBooleanContext ctx) {
		ST res = stg.getInstanceOf("condicaoBoolean");

		res.add("op", ctx.op.getText());
		return res;
	}

	@Override public ST visitComparacao(GeralParser.ComparacaoContext ctx) {
		
		ST res = stg.getInstanceOf("comparacao");

		res.add("conta1", visit(ctx.c1).render());
		res.add("op", ctx.op.getText());
		res.add("conta2", visit(ctx.c2).render());
		return res;
	}

	@Override public ST visitGrandeza(GeralParser.GrandezaContext ctx) {
		ST res = stg.getInstanceOf("grandeza");

		//Transformar grandeza no seu tipo (ex: velocidade-> real)
		for(Unidade u: tabelaDimensoes){
			if(ctx.WORD().getText().equals(u.getNome())){
				res.add("gr", u.getTipo());
			}
		}

	 	return res; }

	@Override public ST visitPacote(GeralParser.PacoteContext ctx) {
		
		ctx.result = ctx.FILE().getText();

		return visitChildren(ctx); 
	
	}
}





