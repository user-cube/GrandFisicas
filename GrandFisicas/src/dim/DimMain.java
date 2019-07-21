package dim;
import java.util.Scanner;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import static java.lang.System.*;
import java.io.*;

public class DimMain {


   protected static Set<Unidade> tabelaDim;

   public static void main(String[] args) throws Exception {
      if(args.length != 1){
         err.println("Usage: DimMain <program-file>");
         exit(1);
      }

      Scanner sc = new Scanner(new File(args[0]));
      String lineText = null;
      int lineNum = 1;
      if (sc.hasNextLine())
         lineText = sc.nextLine();
      DimParser parser = new DimParser(null);

      DimExecute visitor0 = new DimExecute();
      while(lineText != null) {
         // create a CharStream that reads from standard input:
         CharStream input = CharStreams.fromString(lineText + "\n");
         // create a lexer that feeds off of input CharStream:
         DimLexer lexer = new DimLexer(input);
         lexer.setLine(lineNum);
         lexer.setCharPositionInLine(0);
         // create a buffer of tokens pulled from the lexer:
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         // create a parser that feeds off the tokens buffer:
         parser.setInputStream(tokens);
         // begin parsing at main rule:
         ParseTree tree = parser.main();
         if (parser.getNumberOfSyntaxErrors() == 0) {
            visitor0.visit(tree);
         }
         if (sc.hasNextLine())
            lineText = sc.nextLine();
         else
            lineText = null;
         lineNum++;
      }

      tabelaDim = visitor0.getDimTable();
   }

   public static Set<Unidade> getDimTable(){
		return tabelaDim;
	}
}
