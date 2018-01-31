package me.blog.hgl1002.openwnn.KOKR.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.blog.hgl1002.openwnn.KOKR.scripting.nodes.*;

public class StringRecursionTreeBuilder implements TreeBuilder {

	Map<String, Long> constants = new HashMap<>();

	@Override
	public void setConstants(Map<String, Long> constants) {
		this.constants = constants;
	}

	@Override
	public TreeNode build(Object o) {
		final String str = (String) o;
		return new Object() {

			int pos = -1, ch;

			boolean isSymbol(int c) {
				if(c == ' ') return false;
				if(c >= '0' && c <= '9') return false;
				if(c >= 'A' && c <= 'Z') return false;
				if(c >= 'a' && c <= 'z') return false;
				if(c == '_') return false;
				if(c == '(' || c == ')') return false;
				return true;
			}

			public void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while(ch == ' ') nextChar();
				if(ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			boolean eatOnly(int charToEat) {
				while(ch == ' ') nextChar();
				if(ch == charToEat && !isSymbol(str.charAt(pos+1))) {
					nextChar();
					return true;
				}
				return false;
			}

			boolean eat(String strToEat) {
				while(ch == ' ') nextChar();
				int i = pos;
				for(char c : strToEat.toCharArray()) {
					if(i >= str.length()) return false;
					if(c != str.charAt(i)) return false;
					else i++;
				}
				for(int j = pos ; j < i ; j++) {
					nextChar();
				}
				return true;
			}

			boolean eatOnly(String strToEat) {
				while(ch == ' ') nextChar();
				int i = pos;
				for(char c : strToEat.toCharArray()) {
					if(i >= str.length()) return false;
					if(c != str.charAt(i)) return false;
					else i++;
				}
				if(isSymbol(str.charAt(i))) return false;
				for(int j = pos ; j < i ; j++) {
					nextChar();
				}
				return true;
			}

			TreeNode parse() {
				nextChar();
				TreeNode x = parseComma();
				if(pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch + " at: " + pos);
				return x;
			}

			TreeNode parseComma() {
				List<TreeNode> nodes = new ArrayList<>();
				TreeNode x = parseAssignment();
				nodes.add(x);
				for(;;) {
					if(eat(',')) nodes.add(parseAssignment());
					else {
						if(nodes.size() == 1) return nodes.get(0);
						else return new ListTreeNode(Operator.COMMA, nodes);
					}
				}
			}

			TreeNode parseAssignment() {
				TreeNode x = parseCondition();
				for(;;) {
					if(eatOnly('=')) {
						return new BinaryTreeNode(Operator.ASSIGNMENT, x, parseAssignment());
					}
					else if(eat("*=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_MULTIPLICATION, x, parseAssignment());
					}
					else if(eat("/=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_DIVISION, x, parseAssignment());
					}
					else if(eat("%=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_MOD, x, parseAssignment());
					}
					else if(eat("+=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_ADDITION, x, parseAssignment());
					}
					else if(eat("-=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_SUBTRACTION, x, parseAssignment());
					}
					else if(eat("<<=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_SHIFT_LEFT, x, parseAssignment());
					}
					else if(eat(">>=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_SHIFT_RIGHT, x, parseAssignment());
					}
					else if(eat("&=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_AND, x, parseAssignment());
					}
					else if(eat("^=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_XOR, x, parseAssignment());
					}
					else if(eat("|=")) {
						return new BinaryTreeNode(Operator.ASSIGNMENT_OR, x, parseAssignment());
					}
					else {
						return x;
					}
				}
			}

			TreeNode parseCondition() {
				TreeNode x = parseLOr();
				for(;;) {
					if(eat('?')) {
						TreeNode vTrue = parseCondition();
						eat(':');
						TreeNode vFalse = parseCondition();
						return new TernaryTreeNode(Operator.CONDITION, x, vTrue, vFalse);
					}
					else return x;
				}
			}

			TreeNode parseLOr() {
				TreeNode x = parseLAnd();
				for(;;) {
					if(eat("||")) x = new BinaryTreeNode(Operator.LOGICAL_OR, x, parseLAnd());
					else return x;
				}
			}

			TreeNode parseLAnd() {
				TreeNode x = parseOr();
				for(;;) {
					if(eat("&&")) x = new BinaryTreeNode(Operator.LOGICAL_AND, x, parseOr());
					else return x;
				}
			}

			TreeNode parseOr() {
				TreeNode x = parseXor();
				for(;;) {
					if(eatOnly('|')) x = new BinaryTreeNode(Operator.BITWISE_OR, x, parseXor());
					else return x;
				}
			}

			TreeNode parseXor() {
				TreeNode x = parseAnd();
				for(;;) {
					if(eatOnly('^')) x = new BinaryTreeNode(Operator.BITWISE_XOR, x, parseAnd());
					else return x;
				}
			}

			TreeNode parseAnd() {
				TreeNode x = parseEquivelant();
				for(;;) {
					if(eatOnly('&')) x = new BinaryTreeNode(Operator.BITWISE_AND, x, parseEquivelant());
					else return x;
				}
			}

			TreeNode parseEquivelant() {
				TreeNode x = parseCompare();
				for(;;) {
					if(eat("==")) x = new BinaryTreeNode(Operator.EQUALS, x, parseCompare());
					else if(eat("!=")) x = new BinaryTreeNode(Operator.NOT_EQUALS, x, parseCompare());
					else return x;
				}
			}

			TreeNode parseCompare() {
				TreeNode x = parseShift();
				for(;;) {
					if(eatOnly('<')) x = new BinaryTreeNode(Operator.COMPARE_SMALLER, x, parseShift());
					else if(eatOnly('>')) x = new BinaryTreeNode(Operator.COMPARE_GREATER, x, parseShift());
					else if(eat("<=")) x = new BinaryTreeNode(Operator.COMPARE_SMALLER_OR_EQUAL, x, parseShift());
					else if(eat(">=")) x = new BinaryTreeNode(Operator.COMPARE_GREATER_OR_EQUAL, x, parseShift());
					else return x;
				}
			}

			TreeNode parseShift() {
				TreeNode x = parseExpression();
				for(;;) {
					if(eatOnly("<<")) x = new BinaryTreeNode(Operator.SHIFT_LEFT, x, parseExpression());
					else if(eatOnly(">>")) x = new BinaryTreeNode(Operator.SHIFT_RIGHT, x, parseExpression());
					else return x;
				}
			}

			TreeNode parseExpression() {
				TreeNode x = parseTerm();
				for(;;) {
					if(eatOnly('+')) x = new BinaryTreeNode(Operator.ADDITION, x, parseTerm());
					else if(eatOnly('-')) x = new BinaryTreeNode(Operator.SUBTRACTION, x, parseTerm());
					else return x;
				}
			}

			TreeNode parseTerm() {
				TreeNode x = parseFactor();
				for(;;) {
					if(eatOnly('*')) x = new BinaryTreeNode(Operator.MULTIPLICATION, x, parseFactor());
					else if(eatOnly('/')) x = new BinaryTreeNode(Operator.DIVISION, x, parseFactor());
					else return x;
				}
			}

			TreeNode parseFactor() {
				TreeNode x;
				if(eat('(')) {
					x = parseComma();
					eat(')');
					return x;
				}
				if(eat('+')) return new UnaryTreeNode(Operator.PLUS, parseFactor());
				if(eat('-')) return new UnaryTreeNode(Operator.MINUS, parseFactor());
				if(eat('~')) return new UnaryTreeNode(Operator.INVERT, parseFactor());
				if(eat('!')) return new UnaryTreeNode(Operator.NOT, parseFactor());

				long num;
				int startPos = this.pos;
				if(ch >= '0' && ch <= '9') {
					boolean hex = false;
					while((ch >= '0' && ch <= '9') || ch == 'x' ||
							(ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F')) {
						nextChar();
						if(ch == 'x') {
							startPos = this.pos+1;
							hex = true;
						}
					}
					if(hex) num = Long.parseLong(str.substring(startPos, this.pos), 16);
					else num = Long.parseLong(str.substring(startPos, this.pos));
					x = new ConstantTreeNode(num);
				} else if((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_') {
					while((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
							|| (ch >= '0' && ch <= '9') || ch == '_') nextChar();
					String var = str.substring(startPos, this.pos);
					if(constants.containsKey(var)) {
						x = new ConstantTreeNode(constants.get(var));
					} else {
						x = new VariableTreeNode(var);
					}
				} else {
					throw new RuntimeException("Unexpected: " + (char)ch + " at: " + pos);
				}
				return x;
			}

		}.parse();
	}

}
