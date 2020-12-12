package quaz.compiler.parser;

import java.util.ArrayList;

import quaz.compiler.exception.UnexpectedTokenException;
import quaz.compiler.lexer.Token;
import quaz.compiler.lexer.TokenType;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.block.BreakNode;
import quaz.compiler.parser.nodes.block.ContinueNode;
import quaz.compiler.parser.nodes.block.ForNode;
import quaz.compiler.parser.nodes.block.IfNode;
import quaz.compiler.parser.nodes.block.PassNode;
import quaz.compiler.parser.nodes.block.StatementsNode;
import quaz.compiler.parser.nodes.block.WhileNode;
import quaz.compiler.parser.nodes.classes.CastNode;
import quaz.compiler.parser.nodes.classes.ImportNode;
import quaz.compiler.parser.nodes.classes.InstanceArrayNode;
import quaz.compiler.parser.nodes.classes.InstanceNode;
import quaz.compiler.parser.nodes.classes.MemberAccessNode;
import quaz.compiler.parser.nodes.classes.PackageNode;
import quaz.compiler.parser.nodes.function.ArgumentListNode;
import quaz.compiler.parser.nodes.function.FunctionCallNode;
import quaz.compiler.parser.nodes.function.FunctionDefinitionNode;
import quaz.compiler.parser.nodes.function.ParameterListNode;
import quaz.compiler.parser.nodes.function.ReturnNode;
import quaz.compiler.parser.nodes.operation.BinaryOperationNode;
import quaz.compiler.parser.nodes.value.BooleanNode;
import quaz.compiler.parser.nodes.value.ByteNode;
import quaz.compiler.parser.nodes.value.CharNode;
import quaz.compiler.parser.nodes.value.DoubleNode;
import quaz.compiler.parser.nodes.value.FloatNode;
import quaz.compiler.parser.nodes.value.IntNode;
import quaz.compiler.parser.nodes.value.LongNode;
import quaz.compiler.parser.nodes.value.ShortNode;
import quaz.compiler.parser.nodes.value.StringNode;
import quaz.compiler.parser.nodes.variable.VariableAccessNode;
import quaz.compiler.parser.nodes.variable.VariableDeclarationNode;
import quaz.compiler.position.Position;
import quaz.compiler.standardLibrary.Pair;

public class Parser {

	private int index;
	private Token currentToken;
	private Token[] tokens;

	private static Token EOF_TOKEN;

	public Node parse(Token[] tokens) throws UnexpectedTokenException {

		this.tokens = tokens;

		EOF_TOKEN = new Token(TokenType.EOF, tokens[tokens.length - 1].getEnd());

		advance(false);

		return program();

	}

	private void advance(boolean eof) throws UnexpectedTokenException {
		Token prevToken = currentToken;
		currentToken = index >= tokens.length ? EOF_TOKEN : tokens[index];

		if(eof && currentToken == EOF_TOKEN) {
			throw new UnexpectedTokenException("Unexpected EOF", prevToken.getEnd(), prevToken.getEnd());
		}

		index++;
	}

	private void eof() throws UnexpectedTokenException {
		if(currentToken.getType() == TokenType.EOF) {
			throw new UnexpectedTokenException("Unexpected EOF", tokens[tokens.length - 1].getStart(),
					tokens[tokens.length - 1].getEnd());
		}
	}

	private void reverse() {
		index--;

		if(currentToken.getType() != TokenType.EOF)
			currentToken = tokens[index];
	}
	
	private Object[] typeName(boolean arrayLength) throws UnexpectedTokenException {
		
		String typeName;
		
		boolean isKeyword = false;
		
		Node length = null;
		
		if(currentToken.getType() == TokenType.IDENTIFIER || (currentToken.getType() == TokenType.KEYWORD && Token.JAVA_NON_KEYWORDS_ARRAY.contains(currentToken.getValue()))) {
			typeName = currentToken.getValue();

			advance(true);

			while(currentToken.getType() == TokenType.DOT) {
				advance(true);
				if(currentToken.getType() != TokenType.IDENTIFIER
						&& !(currentToken.getType() == TokenType.KEYWORD
								&& Token.JAVA_NON_KEYWORDS_ARRAY.contains(currentToken.getValue()))) {
					throw new UnexpectedTokenException("Expected type", currentToken);
				}

				typeName += "/" + currentToken.getValue();

				advance(true);
				
			}
			
			if(currentToken.getType() == TokenType.LSQBR) {
				typeName += "[]";
				
				advance(true);
				
				if(arrayLength) {
					
					length = expr();
					
				}
				
				if(currentToken.getType() != TokenType.RSQBR) {
					throw new UnexpectedTokenException("Expected ']'", currentToken);
				}
				
				if(!arrayLength)
					advance(true);
				
				//TODO Only supports single dim. arrays.
				
			}

		} else if(currentToken.getType() == TokenType.KEYWORD) {

			if(Token.TYPE_KEYWORDS_ARRAY.contains(currentToken.getValue())) {
				typeName = currentToken.getValue();
				isKeyword = true;
				advance(true);
				
				if(currentToken.getType() == TokenType.LSQBR) {
					typeName += "[]";
					
					advance(true);
					
					if(arrayLength) {
						
						length = expr();
						
					}
					
					if(currentToken.getType() != TokenType.RSQBR) {
						throw new UnexpectedTokenException("Expected ']'", currentToken);
					}
					
					advance(true);
					
					// Only supports single dim. arrays.
					
				}
				
			} else {
				throw new UnexpectedTokenException("Unexpected keyword", currentToken);
			}

		} else {
			throw new UnexpectedTokenException("Expected type", currentToken);
		}
		
		return new Object[] {typeName, isKeyword, length};
		
	}

	private Node program() throws UnexpectedTokenException {

		ArrayList<Node> nodes = new ArrayList<>();

		Position start = currentToken.getStart();

		Position end = currentToken.getEnd();

		while(currentToken.getType() != TokenType.EOF) {

			if(currentToken.getType() == TokenType.NEWLINE) {
				end = currentToken.getEnd();
				advance(false);
				continue;
			}
			if(currentToken.getType() == TokenType.DEDENT) {
				end = currentToken.getEnd();
				advance(false);
				continue;
			}
			
			else if(currentToken.matches(TokenType.KEYWORD, "function")) {
				nodes.add(functionDefinition());
				end = currentToken.getEnd();
				advance(false);
				continue;
			}

			else if(currentToken.matches(TokenType.KEYWORD, "import")) {
				nodes.add(importStatement());
				end = currentToken.getEnd();
				advance(false);
				continue;
			}
			
			else if(currentToken.matches(TokenType.KEYWORD, "package")) {
				nodes.add(packageStatement());
				end = currentToken.getEnd();
				advance(false);
				continue;
			}
			
			throw new UnexpectedTokenException("Expected declaration", currentToken);
		}

		return new StatementsNode(nodes.toArray(new Node[] {}), start, end);

	}

	private Node importStatement() throws UnexpectedTokenException {

		Position start = currentToken.getStart();

		advance(true);

		if(currentToken.getType() != TokenType.IDENTIFIER)
			throw new UnexpectedTokenException("Expected identifier.", currentToken);

		String typeName = currentToken.getValue();

		advance(true);

		while(currentToken.getType() == TokenType.DOT) {
			advance(true);
			if(currentToken.getType() != TokenType.IDENTIFIER && !(currentToken.getType() == TokenType.KEYWORD
					&& Token.JAVA_NON_KEYWORDS_ARRAY.contains(currentToken.getValue()))) {
				throw new UnexpectedTokenException("Expected type", currentToken);
			}

			typeName += "." + currentToken.getValue();

			advance(false);
		}

		return new ImportNode(typeName, start, currentToken.getEnd());
	}

	private Node packageStatement() throws UnexpectedTokenException {
		Position start = currentToken.getStart();

		advance(true);

		if(currentToken.getType() != TokenType.IDENTIFIER)
			throw new UnexpectedTokenException("Expected identifier.", currentToken);

		String typeName = currentToken.getValue();

		advance(true);

		while(currentToken.getType() == TokenType.DOT) {
			advance(true);
			if(currentToken.getType() != TokenType.IDENTIFIER && !(currentToken.getType() == TokenType.KEYWORD
					&& Token.JAVA_NON_KEYWORDS_ARRAY.contains(currentToken.getValue()))) {
				throw new UnexpectedTokenException("Expected type", currentToken);
			}

			typeName += "." + currentToken.getValue();

			advance(false);
		}

		return new PackageNode(typeName, start, currentToken.getEnd());
	}

	private Node functionDefinition() throws UnexpectedTokenException {

		Position start = currentToken.getStart();

		advance(true);

		if(currentToken.getType() != TokenType.IDENTIFIER) {
			throw new UnexpectedTokenException("Expected an indentifier", currentToken);
		}

		Token id = currentToken;

		advance(true);

		Node arguments = defArguments();

		advance(true);

		Token returnT = null;
		
		if(currentToken.getType() == TokenType.ARROW) {
			
			String returnType = "";
			
			advance(true);
			
			if(currentToken.getType() != TokenType.IDENTIFIER
					&&
					!Token.JAVA_NON_KEYWORDS_ARRAY.contains(currentToken.getValue())
					&&
					!Token.TYPE_KEYWORDS_ARRAY.contains(currentToken.getValue())
					&&
					!currentToken.matches(TokenType.KEYWORD, "void")
					) {
				
				throw new UnexpectedTokenException("Expected type", currentToken);
				
			}
			
			returnT = currentToken;
			
			Object[] typeNameRes = typeName(false);
			
			if(!currentToken.matches(TokenType.KEYWORD, "void"))
				returnType = (String) typeNameRes[0];
			else
				returnType = "void";
			
			returnT.setValue(returnType);
		}
		
		if(currentToken.getType() != TokenType.COLON) {
			throw new UnexpectedTokenException("Expected ':'", currentToken);
		}

		advance(true);

		Node statement = blockStatement();

		return new FunctionDefinitionNode(id, arguments, returnT, statement, start, statement.getEnd());
	}

	private Node functionCall() throws UnexpectedTokenException {

		Token id = currentToken;

		Position start = currentToken.getStart();

		advance(false);

		if(currentToken.getType() == TokenType.LPAREN) {

			Node arguments = callArguments();

			Position end = currentToken.getEnd();

			return new FunctionCallNode(id, arguments, start, end);

		}

		reverse();

		return new VariableAccessNode(id.getValue(), id);
	}

	private Node defArguments() throws UnexpectedTokenException {
		Position start = currentToken.getStart();

		if(currentToken.getType() != TokenType.LPAREN) {
			throw new UnexpectedTokenException("Expected '('", currentToken);
		}
		
		ArrayList<Pair<Token, String>> variables = new ArrayList<>();
		
		do {
			
			advance(true);
			
			if(currentToken.getType() == TokenType.RPAREN) {
				break;
			}
			
			if(currentToken.getType() != TokenType.IDENTIFIER) {
				throw new UnexpectedTokenException("Expected identifier.", currentToken);
			}
			
			Token variableName = currentToken;
			
			advance(true);
			
			String typeName = "java/lang/Object";
			
			if(currentToken.getType() == TokenType.ARROW) {
				
				advance(true);
				
				Object[] typeNameRes = typeName(false);
				
				typeName = (String) typeNameRes[0];
				
			}
			
			variables.add(new Pair<Token, String>(variableName, typeName));
			
		} while(currentToken.getType() == TokenType.COMMA);

		if(currentToken.getType() != TokenType.RPAREN) {
			throw new UnexpectedTokenException("Expected ')'", currentToken);
		}

		return new ParameterListNode(variables.toArray(new Pair<?, ?>[] {}), start, currentToken.getEnd());
	}

	private Node callArguments() throws UnexpectedTokenException {

		Position start = currentToken.getStart();

		if(currentToken.getType() != TokenType.LPAREN) {
			throw new UnexpectedTokenException("Expected '('", currentToken);
		}

		advance(true);

		ArrayList<Node> args = new ArrayList<>();

		if(currentToken.getType() != TokenType.RPAREN) {
			
			args.add(expr());
			
			eof();

		}

		while(currentToken.getType() == TokenType.COMMA) {

			advance(true);

			args.add(expr());
			
			eof();

		}

		if(currentToken.getType() != TokenType.RPAREN) {
			throw new UnexpectedTokenException("Expected ')'", currentToken);
		}

		return new ArgumentListNode(args.toArray(new Node[] {}), start, currentToken.getEnd());
	}

	private Node blockStatement() throws UnexpectedTokenException {

		if(currentToken.getType() == TokenType.NEWLINE) {
			Node s = statements();
			advance(false);
			//reverse();
			return s;
		}

		return statement();
	}

	private Node statement() throws UnexpectedTokenException {

		if(currentToken.matches(TokenType.KEYWORD, "var")) {
			return variableDeclaration();
		}
		
		else if(currentToken.matches(TokenType.KEYWORD, "if")) {
			return ifStatement();
		}
		
		else if(currentToken.matches(TokenType.KEYWORD, "while")) {
			return whileLoop();
		}
		
		else if(currentToken.matches(TokenType.KEYWORD, "for")) {
			return forLoop();
		}
		
		else if(currentToken.matches(TokenType.KEYWORD, "return")) {
			return returnStatement();
		}
		
		else if(currentToken.matches(TokenType.KEYWORD, "pass")) {
			advance(false);
			return new PassNode(currentToken);
		}
		
		else if(currentToken.matches(TokenType.KEYWORD, "break")) {
			advance(false);
			return new BreakNode(currentToken);
		}
		
		else if(currentToken.matches(TokenType.KEYWORD, "continue")) {
			advance(false);
			return new ContinueNode(currentToken);
		}
		
		return expr();
	}
	
	private Node variableDeclaration() throws UnexpectedTokenException {
		Position start = currentToken.getStart();

		advance(true);

		boolean isExplicit = false;
		boolean isKeyword = false;
		String typeName = "";

		if(currentToken.getType() == TokenType.ARROW) {

			isExplicit = true;

			advance(true);

			Object[] typeNameRes = typeName(false);
			
			typeName = (String) typeNameRes[0];
			
			isKeyword = (boolean) typeNameRes[1];
			
		}

		if(currentToken.getType() != TokenType.IDENTIFIER) {
			throw new UnexpectedTokenException("Expected indentifier", currentToken);
		}

		String name = currentToken.getValue();

		Position end = currentToken.getEnd();

		advance(false);

		if(currentToken.getType() == TokenType.NEWLINE || currentToken.getType() == TokenType.DEDENT || currentToken.getType() == TokenType.SEMI) {
			return new VariableDeclarationNode(name, typeName, null, isExplicit, isKeyword, true, start, end);
		} else if(currentToken.getType() != TokenType.EQUALS) {
			throw new UnexpectedTokenException("Expected assignment", currentToken);
		}

		advance(true);

		Node value = expr();

		// advance(false);

		return new VariableDeclarationNode(name, typeName, value, isExplicit, isKeyword, false, start, end);
	}
	
	private Node ifStatement() throws UnexpectedTokenException {
		Position start = currentToken.getStart();
		
		advance(true);
		
		if(currentToken.getType() != TokenType.LPAREN) {
			throw new UnexpectedTokenException("Expected '('", currentToken);
		}
		
		advance(true);
		
		Node condition = expr();
		
		if(currentToken.getType() != TokenType.RPAREN) {
			throw new UnexpectedTokenException("Expected ')'", currentToken);
		}
		
		advance(true);
		
		if(currentToken.getType() != TokenType.COLON) {
			throw new UnexpectedTokenException("Expected ':'", currentToken);
		}
		
		advance(true);
		
		Node body = blockStatement();
		
		//advance(false);
		
		while(currentToken.getType() == TokenType.NEWLINE) {
			advance(false);
		}
		
		Node elseBody = null;
		
		if(currentToken.matches(TokenType.KEYWORD, "else")) {
			
			advance(true);
			
			if(currentToken.getType() == TokenType.COLON) {
				advance(true);
				
				elseBody = blockStatement();
				
				//advance(false);
				
			}
			else if(currentToken.matches(TokenType.KEYWORD, "if")) {
				elseBody = statement();
			}
			
			else {
				throw new UnexpectedTokenException("Expected ':' or 'if'", currentToken);
			}
			
		}
		else { // No idea why this works
			reverse();
			reverse();
		}
		
		return new IfNode(condition, body, elseBody, start, currentToken.getEnd());
	}
	
	private Node whileLoop() throws UnexpectedTokenException {
		Position start = currentToken.getStart();
		
		advance(true);
		
		if(currentToken.getType() != TokenType.LPAREN) {
			throw new UnexpectedTokenException("Expected '('", currentToken);
		}
		
		advance(true);
		
		Node condition = expr();
		
		if(currentToken.getType() != TokenType.RPAREN) {
			throw new UnexpectedTokenException("Expected ')'", currentToken);
		}
		
		advance(true);
		
		if(currentToken.getType() != TokenType.COLON) {
			throw new UnexpectedTokenException("Expected ':'", currentToken);
		}
		
		advance(true);
		
		Node body = blockStatement();
		
		return new WhileNode(condition, body, start, currentToken.getEnd());
	}
	
	private Node statements() throws UnexpectedTokenException {

		advance(true);

		Position start = currentToken.getStart();
		
		while(currentToken.getType() == TokenType.NEWLINE) {
			advance(true);
		}
		
		if(currentToken.getType() != TokenType.INDENT) {
			throw new UnexpectedTokenException("Expected an indention", currentToken);
		}

		ArrayList<Node> statements = new ArrayList<>();

		// advance(true);

		// statements.add(statement());

		do {
			advance(true);
			
			if(currentToken.getType() == TokenType.DEDENT) {
				break;
			}
			
			if(currentToken.getType() == TokenType.NEWLINE) {
				continue;
			}

			statements.add(statement());
		} while(currentToken.getType() == TokenType.NEWLINE);
		
		return new StatementsNode(statements.toArray(new Node[] {}), start, currentToken.getEnd());
	}
	
	private Node returnStatement() throws UnexpectedTokenException {
		Position start = currentToken.getStart();
		
		advance(false);
		
		Node expr;
		
		if(currentToken.getType() == TokenType.EOF || currentToken.getType() == TokenType.NEWLINE || currentToken.getType() == TokenType.DEDENT) {
			expr = null;
		}
		else {
			expr = expr();
		}
		
		return new ReturnNode(expr, start, currentToken.getEnd());
	}

	private Node forLoop() throws UnexpectedTokenException {
		
		Position start = currentToken.getStart();
		
		advance(true);
		
		if(currentToken.getType() != TokenType.LPAREN) {
			throw new UnexpectedTokenException("Expected '('", currentToken);
		}
		
		advance(true);
		
		Node init = null;
		
		if(currentToken.matches(TokenType.KEYWORD, "var")) {
			init = variableDeclaration();
		}
		else {
			init = expr();
		}
		
		if(currentToken.getType() != TokenType.SEMI) {
			throw new UnexpectedTokenException("Expected ';'", currentToken);
		}
		
		advance(true);
		
		Node condition = expr();
		
		if(currentToken.getType() != TokenType.SEMI) {
			throw new UnexpectedTokenException("Expected ';'", currentToken);
		}
		
		advance(true);
		
		Node increment = expr();
		
		if(currentToken.getType() != TokenType.RPAREN) {
			throw new UnexpectedTokenException("Expected ')'", currentToken);
		}
		
		advance(true);
		
		if(currentToken.getType() != TokenType.COLON) {
			throw new UnexpectedTokenException("Expected ':'", currentToken);
		}
		
		advance(true);
		
		Node body = blockStatement();
		
		return new ForNode(init, condition, increment, body, start, currentToken.getEnd());
		
	}
	
	private Node expr() throws UnexpectedTokenException {
		return assignmentExpr();
	}
	
	private Node assignmentExpr() throws UnexpectedTokenException {
		
		Position start = currentToken.getStart();
		
		Node left = logicalExpr();
		
		while(currentToken.getType() == TokenType.EQUALS) {
			TokenType type = currentToken.getType();

			advance(true);

			Node right = logicalExpr();

			left = new BinaryOperationNode(type, left, right, start, currentToken.getEnd());

		}
		
		//advance(false);

		return left;
		
	}
	
	private Node logicalExpr() throws UnexpectedTokenException {
		Position start = currentToken.getStart();
		
		Node left = bitExpr();
		
		while(currentToken.getType() == TokenType.AND || currentToken.getType() == TokenType.OR) {
			TokenType type = currentToken.getType();

			advance(true);

			Node right = bitExpr();

			left = new BinaryOperationNode(type, left, right, start, currentToken.getEnd());

		}
		
		//advance(false);

		return left;
	}
	
	private Node bitExpr() throws UnexpectedTokenException {
		Position start = currentToken.getStart();

		Node left = equalityExpr();
		
		while(currentToken.getType() == TokenType.BIT_AND || currentToken.getType() == TokenType.BIT_OR || currentToken.getType() == TokenType.BIT_XOR) {
			TokenType type = currentToken.getType();

			advance(true);

			Node right = equalityExpr();

			left = new BinaryOperationNode(type, left, right, start, currentToken.getEnd());

		}

		return left;
	}
	
	private Node equalityExpr() throws UnexpectedTokenException {
		Position start = currentToken.getStart();

		Node left = relationalExpr();
		
		while(currentToken.getType() == TokenType.BOOL_EQ || currentToken.getType() == TokenType.BOOL_NE || currentToken.getType() == TokenType.BOOL_TRI_EQ || currentToken.getType() == TokenType.BOOL_TRI_NE) {
			TokenType type = currentToken.getType();

			advance(true);

			Node right = relationalExpr();

			left = new BinaryOperationNode(type, left, right, start, currentToken.getEnd());

		}

		return left;
	}
	
	private Node relationalExpr() throws UnexpectedTokenException {
		Position start = currentToken.getStart();

		Node left = arithExpr();
		
		if(currentToken.getType() == TokenType.BOOL_LT || currentToken.getType() == TokenType.BOOL_LE || currentToken.getType() == TokenType.BOOL_GT || currentToken.getType() == TokenType.BOOL_GE) {
			TokenType type = currentToken.getType();

			advance(true);

			Node right = arithExpr();

			left = new BinaryOperationNode(type, left, right, start, currentToken.getEnd());

		}

		return left;
	}
	
	private Node arithExpr() throws UnexpectedTokenException {

		Position start = currentToken.getStart();

		Node left = term();
		

		while(currentToken.getType() == TokenType.PLUS || currentToken.getType() == TokenType.MINUS) {
			TokenType type = currentToken.getType();

			advance(true);

			Node right = term();

			left = new BinaryOperationNode(type, left, right, start, currentToken.getEnd());

		}

		return left;
	}

	// 12
	private Node term() throws UnexpectedTokenException {
		Position start = currentToken.getStart();

		Node left = cast();
		

		while(currentToken.getType() == TokenType.MULTIPLY || currentToken.getType() == TokenType.DIVIDE || currentToken.getType() == TokenType.MODULUS) {
			TokenType type = currentToken.getType();

			advance(true);

			Node right = cast();

			left = new BinaryOperationNode(type, left, right, start, currentToken.getEnd());

		}

		return left;
	}
	
	private Node cast() throws UnexpectedTokenException {
		
		Position start = currentToken.getStart();

		Node left = factor();
		
		
		String typeName;
		
		while(currentToken.matches(TokenType.KEYWORD, "as")) {
			advance(true);

			if(currentToken.getType() != TokenType.IDENTIFIER
					&& !(currentToken.getType() == TokenType.KEYWORD && Token.TYPE_KEYWORDS_ARRAY.contains(currentToken.getValue()))) {
				throw new UnexpectedTokenException("Expected type.", currentToken);
			}
			
			typeName = currentToken.getValue();

			advance(true);

			while(currentToken.getType() == TokenType.DOT) {
				advance(true);
				if(currentToken.getType() != TokenType.IDENTIFIER && !(currentToken.getType() == TokenType.KEYWORD
						&& Token.JAVA_NON_KEYWORDS_ARRAY.contains(currentToken.getValue()))) {
					throw new UnexpectedTokenException("Expected type", currentToken);
				}

				typeName += "." + currentToken.getValue();

				advance(true);
			}
			
			left = new CastNode(left, typeName, start, currentToken.getEnd());
		}

		return left;
		
	}
	
	// 16
	private Node factor() throws UnexpectedTokenException {

		Position start = currentToken.getStart();

		Node left = atom();
		
		advance(false);

		while(currentToken.getType() == TokenType.DOT) {

			advance(true);

			Node right = factor();

			left = new MemberAccessNode(left, right, start, currentToken.getEnd());
		}

		return left;

	}

	// Basic component
	private Node atom() throws UnexpectedTokenException {

		if(currentToken.getType() == TokenType.IDENTIFIER) {
			return functionCall();
		}

		else if(currentToken.matches(TokenType.KEYWORD, "new")) {
			return newObject();
		}

		else if(currentToken.getType() == TokenType.LPAREN) {
			advance(true);
			Node expr = expr();

			if(currentToken.getType() != TokenType.RPAREN) {
				throw new UnexpectedTokenException("Expected a ')'", currentToken);
			}

			return expr;
		}

		else if(currentToken.getType() == TokenType.STRING) {
			return new StringNode(currentToken.getValue(), currentToken);
		}

		else if(currentToken.getType() == TokenType.INT) {
			return new IntNode(currentToken.getValue(), currentToken);
		}

		else if(currentToken.getType() == TokenType.DOUBLE) {
			return new DoubleNode(currentToken.getValue(), currentToken);
		}
		
		else if(currentToken.getType() == TokenType.FLOAT) {
			return new FloatNode(currentToken.getValue(), currentToken);
		}
		
		else if(currentToken.getType() == TokenType.CHAR) {
			return new CharNode(currentToken.getValue(), currentToken);
		}
		
		else if(currentToken.getType() == TokenType.BYTE) {
			return new ByteNode(currentToken.getValue(), currentToken);
		}
		
		else if(currentToken.getType() == TokenType.LONG) {
			return new LongNode(currentToken.getValue(), currentToken);
		}
		
		else if(currentToken.getType() == TokenType.SHORT) {
			return new ShortNode(currentToken.getValue(), currentToken);
		}
		
		else if(currentToken.matches(TokenType.KEYWORD, "false")) {
			return new BooleanNode(false, currentToken);
		}
		
		else if(currentToken.matches(TokenType.KEYWORD, "true")) {
			return new BooleanNode(true, currentToken);
		}
		throw new UnexpectedTokenException("Expected a value", currentToken);

	}

	private Node newObject() throws UnexpectedTokenException {

		Position start = currentToken.getStart();

		advance(true);

		String typeName = "";
		
		if(currentToken.getType() == TokenType.IDENTIFIER) {
			Object[] typeNameRes = typeName(true);
			typeName = (String) typeNameRes[0];
			
			if(typeNameRes[2] != null) {
				return new InstanceArrayNode(typeName, (Node) typeNameRes[2], null, start, currentToken.getEnd());
			}
			
		} else {
			
			Object[] typeNameRes = typeName(true);
			
			if(typeNameRes[2] == null) {
				throw new UnexpectedTokenException("Expected identifier.", start, currentToken.getEnd());
			}
			
			typeName = (String) typeNameRes[0];
			
			return new InstanceArrayNode(typeName, (Node) typeNameRes[2], null, start, currentToken.getEnd());
			
		}

		Node arguments = callArguments();

		return new InstanceNode(typeName, arguments, start, currentToken.getEnd());
	}

}