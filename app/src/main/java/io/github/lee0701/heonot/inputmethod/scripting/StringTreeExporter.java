package io.github.lee0701.heonot.inputmethod.scripting;

import java.util.List;

import io.github.lee0701.heonot.inputmethod.scripting.nodes.*;

import static io.github.lee0701.heonot.inputmethod.scripting.nodes.Operator.*;

public class StringTreeExporter implements TreeExporter {

	protected ConstantHandler constantHandler;

	public ConstantHandler getConstantHandler() {
		return constantHandler;
	}

	public void setConstantHandler(ConstantHandler constantHandler) {
		this.constantHandler = constantHandler;
	}

	@Override
	public Object export(final TreeNode node) {
		return new Object() {

			StringBuilder result = new StringBuilder();

			public Object export() {
				export(node);
				return result.toString();
			}

			public void export(TreeNode node) {
				if (node instanceof ConstantTreeNode) {
					long value = ((ConstantTreeNode) node).getValue();
					if(constantHandler != null) result.append(constantHandler.onConstant(value));
					else result.append(value);
				} else if (node instanceof VariableTreeNode) {
					result.append(((VariableTreeNode) node).getName());
				} else if (node instanceof UnaryTreeNode) {
					UnaryTreeNode unaryTreeNode = (UnaryTreeNode) node;
					switch(unaryTreeNode.getOperator()) {
					case PLUS:
						result.append("+");
						break;
					case MINUS:
						result.append("-");
						break;
					case NOT:
						result.append("!");
						break;
					case INVERT:
						result.append("~");
						break;
					case INCREMENT_LEFT:
						result.append("++");
						break;
					case DECREMENT_LEFT:
						result.append("--");
						break;
					}
					boolean parentheses = false;
					TreeNode child = unaryTreeNode.getCenter();
					if(child instanceof UnaryTreeNode || child instanceof BinaryTreeNode || child instanceof TernaryTreeNode) {
						if(child.getOperator().getCode() > unaryTreeNode.getOperator().getCode()) parentheses = true;
					}
					if(parentheses) result.append('(');
					export(unaryTreeNode.getCenter());
					if(parentheses) result.append(')');
					switch(unaryTreeNode.getOperator()) {
					case INCREMENT_RIGHT:
						result.append("++ ");
						break;
					case DECREMENT_RIGHT:
						result.append("-- ");
						break;
					}
				} else if (node instanceof BinaryTreeNode) {
					BinaryTreeNode binaryTreeNode = (BinaryTreeNode) node;
					boolean parentheses = false;
					TreeNode child = binaryTreeNode.getLeft();
					if(child instanceof UnaryTreeNode || child instanceof BinaryTreeNode || child instanceof TernaryTreeNode) {
						if(child.getOperator().getCode() > binaryTreeNode.getOperator().getCode()) parentheses = true;
					}
					if(parentheses) result.append('(');
					export(child);
					if(parentheses) result.append(')');
					switch(binaryTreeNode.getOperator()) {
					case ADDITION:
						result.append('+');
						break;
					case SUBTRACTION:
						result.append('-');
						break;
					case MULTIPLICATION:
						result.append('*');
						break;
					case DIVISION:
						result.append('-');
						break;
					case MOD:
						result.append('%');
						break;
					case COMPARE_GREATER:
						result.append(">");
						break;
					case COMPARE_SMALLER:
						result.append("<");
						break;
					case COMPARE_GREATER_OR_EQUAL:
						result.append(">=");
						break;
					case COMPARE_SMALLER_OR_EQUAL:
						result.append("<=");
						break;
					case SHIFT_LEFT:
						result.append("<<");
						break;
					case SHIFT_RIGHT:
						result.append(">>");
						break;
					case EQUALS:
						result.append("==");
						break;
					case NOT_EQUALS:
						result.append("!=");
						break;
					case BITWISE_AND:
						result.append('&');
						break;
					case BITWISE_XOR:
						result.append('^');
						break;
					case BITWISE_OR:
						result.append('|');
						break;
					case LOGICAL_AND:
						result.append("&&");
						break;
					case LOGICAL_OR:
						result.append("||");
						break;
					case ASSIGNMENT:
						result.append('=');
						break;
					case ASSIGNMENT_ADDITION:
						result.append("+=");
						break;
					case ASSIGNMENT_SUBTRACTION:
						result.append("-=");
						break;
					case ASSIGNMENT_MULTIPLICATION:
						result.append("*=");
						break;
					case ASSIGNMENT_DIVISION:
						result.append("/=");
						break;
					case ASSIGNMENT_MOD:
						result.append("%=");
						break;
					case ASSIGNMENT_SHIFT_LEFT:
						result.append("<<=");
						break;
					case ASSIGNMENT_SHIFT_RIGHT:
						result.append(">>=");
						break;
					case ASSIGNMENT_AND:
						result.append("&=");
						break;
					case ASSIGNMENT_OR:
						result.append("|=");
						break;
					case ASSIGNMENT_XOR:
						result.append("^=");
						break;
					}
					parentheses = false;
					child = binaryTreeNode.getRight();
					if(child instanceof UnaryTreeNode || child instanceof BinaryTreeNode || child instanceof TernaryTreeNode) {
						if(child.getOperator().getCode() > binaryTreeNode.getOperator().getCode()) parentheses = true;
					}
					if(parentheses) result.append('(');
					export(child);
					if(parentheses) result.append(')');
				} else if (node instanceof TernaryTreeNode) {
					TernaryTreeNode ternaryTreeNode = (TernaryTreeNode) node;
					boolean parentheses = false;
					TreeNode child = ternaryTreeNode.getLeft();
					if(child instanceof UnaryTreeNode || child instanceof BinaryTreeNode || child instanceof TernaryTreeNode) {
						if(child.getOperator().getCode() > ternaryTreeNode.getOperator().getCode()) parentheses = true;
					}
					if(parentheses) result.append('(');
					export(child);
					if(parentheses) result.append(')');
					switch(ternaryTreeNode.getOperator()) {
					case CONDITION:
						result.append('?');
						break;
					}
					parentheses = false;
					child = ternaryTreeNode.getCenter();
					if(child instanceof UnaryTreeNode || child instanceof BinaryTreeNode || child instanceof TernaryTreeNode) {
						if(child.getOperator().getCode() > ternaryTreeNode.getOperator().getCode()) parentheses = true;
					}
					if(parentheses) result.append('(');
					export(child);
					if(parentheses) result.append(')');
					switch(ternaryTreeNode.getOperator()) {
					case CONDITION:
						result.append(':');
						break;
					}
					parentheses = false;
					child = ternaryTreeNode.getRight();
					if(child instanceof UnaryTreeNode || child instanceof BinaryTreeNode || child instanceof TernaryTreeNode) {
						if(child.getOperator().getCode() > ternaryTreeNode.getOperator().getCode()) parentheses = true;
					}
					if(parentheses) result.append('(');
					export(child);
					if(parentheses) result.append(')');
				} else if (node instanceof ListTreeNode) {
					ListTreeNode listTreeNode = (ListTreeNode) node;
					List<TreeNode> nodes = listTreeNode.getNodes();
					for(TreeNode n : nodes) {
						export(n);
						if(nodes.indexOf(n) < nodes.size()-1) {
							switch(listTreeNode.getOperator()) {
							case COMMA:
								result.append(',');
								break;
							}
						}
					}
				} else {
					throw new RuntimeException("Unsupported node type.");
				}
			}

		}.export();
	}

	public static interface ConstantHandler {
		String onConstant(long constant);
	}

}
