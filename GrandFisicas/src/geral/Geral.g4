grammar Geral;

@header { package geral; }

main : head* body EOF ;

head returns[String result]: ('import' pacote ';') ;
body returns[String result]: stat* ;


stat : linha end=(';' | EOF) 	#statLinha
	| condicaoif 				#statIf
	| condicaowhile				#statWhile
	;		

linha: 		atribuicao	#linhaAttr
		| 	conta 		#linhaConta
		| 	print		#linhaPrint
		;

atribuicao: 	WORD '=' conta					 			#AttrDefined		
			| 	g1=grandeza WORD '=' conta 					#AttrGrandeza
			|	type=('real' | 'integer') WORD '=' conta 	#AttrPrimitivo	
			;



conta returns [double value, String nomeGrandeza]:
		v1 = conta op=('*'|'/') v2 = conta 							#MultDivConta
	| 	v1 = conta op=('+'|'-') v2 = conta	 						#SumSubConta
	| 	'('conta')' 												#Paranthesis
	| 	valor		  												#Value
	|	input														#ContaInput
	;	  

valor returns [double value, String nomeGrandeza]: 		WORD 							#ValorVariavel
													| 	('('g=grandeza')')? NUMBER 		#ValorNumero
													| 	('('g=grandeza')')? '-' NUMBER 	#ValorNumeroNegativo
													;

condicaoif: 'if''(' condicao ')''{' stat* '}' 									#ifStat
			| 'if''(' condicao ')''{' stat* '}' condicaoelif+ condicaoelse? 	#ifStatElif
			| 'if''(' condicao ')''{' stat* '}' condicaoelse					#ifStatElse
			;

condicaoelif:  'else if''(' condicao ')''{' stat* '}';

condicaoelse:  'else''{' stat* '}';

condicaowhile: 'while''(' condicao ')''{' stat*'}';

condicao returns [boolean value]: c1=condicao 'AND' c2=condicao 	#AndCondicao
			| c1=condicao 'OR' c2=condicao 							#OrCondicao
			| '(' condicao ')'										#ParanthesisCondicao
			| comparacao											#ComparacaoCondicao
			| condicaoBoolean										#BooleanCondicao
			;		

print: 'print' ( conta | STRING );

input: 'input' STRING;

condicaoBoolean returns [boolean value]: op=('true' | 'false');

comparacao returns [boolean value]: '!'? ( c1=conta op=('==' | '!=' | '<' | '>' | '>=' | '<=') c2=conta) ;

grandeza: WORD;

pacote returns[String result] : ( (FOLDER | '../' ) )* FILE;

NUMBER:  [0-9]+ ('.'  [0-9]+)?; // inteiro ou real
STRING: '"' .*? '"';
FILE: [a-z0-9_]+ '.' [a-z]+;
WORD: [a-z_]+;
FOLDER: [_a-zA-Z0-9]+ '/';
NEWLINE : '\r'? '\n' -> skip;
WS: [ \t]+ -> skip ;
COMMENT: '//' .*? ('\n' | EOF) -> skip;
MULTILINECOMMENT: '/*' .*? '*/' -> skip;
ERROR: .;