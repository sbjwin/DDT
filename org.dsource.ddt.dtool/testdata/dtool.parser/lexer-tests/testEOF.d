//#SPLIT_SOURCE_TEST _____________________
/+#LEXERTEST#
EOF
+/
//#SPLIT_SOURCE_TEST _____________________
 aaa
/+#LEXERTEST#
EOF,ID,EOF,ID,*
+/
//#SPLIT_SOURCE_TEST _____________________
aa
a 
/+#LEXERTEST#
ID,EOF,ID,EOL,
ID,EOF,EOL
+/
//#SPLIT_SOURCE_TEST _____________________
/+#LEXERTEST#
EOF,
+/