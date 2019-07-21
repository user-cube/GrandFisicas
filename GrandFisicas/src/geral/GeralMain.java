package geral;



import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.stringtemplate.v4.*;
import static java.lang.System.*;
import java.io.*;

public class GeralMain {

   public static void main(String[] args) throws Exception {

      if(args.length != 1){
         err.println("Usage: GeralMain <geral-file>");
         exit(1);
      }


      SymbolTable tabela = new SymbolTable();
      File ficheiroGeral = new File(args[0]);


      // create a CharStream that reads from standard input:
      InputStream fic = new FileInputStream(ficheiroGeral);
      CharStream input = CharStreams.fromStream(fic);
      // create a lexer that feeds off of input CharStream:
      GeralLexer lexer = new GeralLexer(input);
      // create a buffer of tokens pulled from the lexer:
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      // create a parser that feeds off the tokens buffer:
      GeralParser parser = new GeralParser(tokens);
      
      
      // replace error listener:
      parser.removeErrorListeners(); // remove ConsoleErrorListener
      parser.addErrorListener(new ErrorHandlingListener());
      
      
      
      // begin parsing at main rule:
      ParseTree tree = parser.main();
      if (!ErrorHandling.error()) { // true, se houver erros

         GeralSemanticAnalysis semAnalysis = new GeralSemanticAnalysis(tabela);
         GeralCompiler compiler = new GeralCompiler(args[0]);
         
         semAnalysis.visit(tree);

         if(!ErrorHandling.error()){
            compiler.setSymbolTable(semAnalysis.getSymbolTable());
            compiler.setTabelaDimensoes(semAnalysis.getTabelaDimensoes());
            ST result = compiler.visit(tree);

            System.out.println(result.render());

            compiler.compile(result.render());
         }
         


         
      }
   }
}
