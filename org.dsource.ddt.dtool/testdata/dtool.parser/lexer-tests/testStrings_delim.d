//#SPLIT_SOURCE_TEST _____________________ DELIM STRING - basic delim
q"/asdf" asdfd/"
q".asdf" asdfd."
q".."
q""asdf# asdfd""
q"(( )"  (())  asdf"  [<}  (xx"xx))"
q"[[ ]"  [[]]  asdf"  <{)  [xx"xx]]"
q"<< >"  <<>>  asdf"  {(]  <xx"xx>>"
q"{{ }"  {{}}  asdf"  ([>  {xx"xx}}"
/+#LEXERTEST
STRING_DELIM, EOL,
STRING_DELIM, EOL,
STRING_DELIM, EOL,
STRING_DELIM, EOL,
STRING_DELIM, EOL,
STRING_DELIM, EOL,
STRING_DELIM, EOL,
STRING_DELIM, EOL,
+/

//#SPLIT_SOURCE_TEST _____________________ DELIM STRING - basic delim
q"/asdf/ asdfd"
q".. asdfd"
q""asdf" asdfd"
q"( asdf (asdf)) asdf" q"( asdf (asdf)) asdf)"
q"[ asdf [asdf]] asdf" q"[ asdf [asdf]] asdf]"
q"< asdf <asdf>> asdf" q"< asdf <asdf>> asdf>"
q"{ asdf {asdf}} asdf" q"{ asdf {asdf}} asdf}"
/+#LEXERTEST
ERROR, EOL,
ERROR, EOL,
ERROR, EOL,
ERROR, WS, ERROR,EOL,
ERROR, WS, ERROR,EOL,
ERROR, WS, ERROR,EOL,
ERROR, WS, ERROR,EOL,
+/

//#SPLIT_SOURCE_TEST __________________
q"/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST __________________
q"//+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST __________________
q"(/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST __________________
q"/asdf"
/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST __________________
q"(asdf("
/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST __________________
q"( asdf (asdf)" xxx/+#LEXERTEST
ERROR+/


//#SPLIT_SOURCE_TEST _____________________ DELIM STRING - identifier delim
q"EOS
This is a multi-line " EOS
EOS
heredoc string
EOS"EOS
q"abc
"
"abc
abc
abc"
q"a
a"
/+#LEXERTEST
STRING_DELIM,ID,EOL,
STRING_DELIM,EOL,
STRING_DELIM,EOL,
+/
//#SPLIT_SOURCE_TEST __________________
q"xx asdf 
xx"
foobar/+#LEXERTEST
ERROR,EOL,ID
+/
//#SPLIT_SOURCE_TEST __________________
q"xxx/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST __________________
q"xxx"asdf/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST __________________
q"xxx blah/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST __________________ (invalid char(space) after id)
q"xxx 
xxx"foobar/+#LEXERTEST
ERROR,ID+/
//#SPLIT_SOURCE_TEST __________________ (invalid char after id, test recovery)
q"xxx!
asd "
xxx 
xxx" foobar/+#LEXERTEST
ERROR,WS,ID+/
//#SPLIT_SOURCE_TEST __________________
q"xxx
xxx "foobar/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST __________________
q"xxx
xxx/+#LEXERTEST
ERROR+/


// TODO Token String
//#SPLIT_SOURCE_TEST _____________________ TOKEN STRING
q{}
q{ asdf __TIME__  {nest braces} q"[{]" { q{nestedToken } String} }
q{asdf 
/* } */ {
// }  
}
"}" blah  }xxx
q{asdf {
` aaa` }
}
q{#!/usrs }
/+#LEXERTEST
STRING_TOKENS,EOL,
STRING_TOKENS,EOL,
STRING_TOKENS,ID,EOL,
STRING_TOKENS,EOL,
STRING_TOKENS,EOL,
+/

//#SPLIT_SOURCE_TEST ____________________
q{/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST ____________________
q{ aasdf
/+#LEXERTEST
ERROR+/
//#SPLIT_SOURCE_TEST ____________________
q{ aasdf
/*asdf
/+#LEXERTEST
ERROR
+/
//#SPLIT_SOURCE_TEST _____________
q{ asas sdas }
/+#LEXERTEST
ERROR,ID,WS,CLOSE_BRACE,EOL+/
//#SPLIT_SOURCE_TEST _____________
q{ sdaasdf }
/+#LEXERTEST
ERROR,ID,WS,CLOSE_BRACE,EOL+/

//TODO:   #SPLIT_SOURCE_TEST ____________________
q{ __EOF__ }
/+#LEXERTEST
ERROR+/
//TODO