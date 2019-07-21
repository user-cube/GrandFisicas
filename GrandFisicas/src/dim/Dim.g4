grammar Dim;

@header { package dim; }

@parser::members{ public static final DimTable dimTable = new DimTable();}

main: tipo* EOF;

tipo:       grandeza '[' WORD ']' ':' 'REAL' ';'    #realType
        |   grandeza '[' WORD ']' ':' 'INTEGER' ';' #integerType;

grandeza: WORD;

STRING: '"' .*? '"';
WORD: [a-z/]+;
WS: [ \t]+ -> skip ;
COMMENT: '//' .*? '\n' -> skip;
MULTILINECOMMENT: '/*' .*? '*/' -> skip;
NEWLINE : '\r'? '\n' -> skip;