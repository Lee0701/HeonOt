package io.github.lee0701.heonot.inputmethod.scripting;

import io.github.lee0701.heonot.inputmethod.scripting.nodes.*;

import java.util.HashMap;
import java.util.Map;

public class TreeEvaluator {

	private Map<String, Long> variables = new HashMap<>();
	private Map<String, Long> constants = new HashMap<>();

	public long eval(TreeNode node) {
		if (node instanceof ConstantTreeNode) {
			ConstantTreeNode dataTreeNode = (ConstantTreeNode) node;
			return dataTreeNode.getValue();
		} else if (node instanceof VariableTreeNode) {
			VariableTreeNode variableTreeNode = (VariableTreeNode) node;
			if (variables.containsKey(variableTreeNode.getName())) {
				return variables.get(variableTreeNode.getName());
			} else
				return 0;
		} else if (node instanceof UnaryTreeNode) {
			return unaryOperation((UnaryTreeNode) node);
		} else if (node instanceof BinaryTreeNode) {
			return binaryOperation((BinaryTreeNode) node);
		} else if (node instanceof TernaryTreeNode) {
			return ternaryOperation((TernaryTreeNode) node);
		} else if (node instanceof ListTreeNode) {
			return listOperation((ListTreeNode) node);
		} else {
			throw new RuntimeException("Unsupported node type.");
		}
	}

	private long unaryOperation(UnaryTreeNode node) {
		switch(node.getOperator()) {
		case PLUS:
			return eval(node.getCenter());

		case MINUS:
			return -eval(node.getCenter());

		case NOT:
			long value = eval(node.getCenter());
			return (value == 0) ? 1 : 0;

		case INVERT:
			return ~eval(node.getCenter());

		case INCREMENT_LEFT:
		case INCREMENT_RIGHT:
		case DECREMENT_LEFT:
		case DECREMENT_RIGHT:
			if(node.getCenter() instanceof VariableTreeNode) {
				VariableTreeNode variableTreeNode = (VariableTreeNode) node.getCenter();
				String varName = variableTreeNode.getName();
				Long varValue = variables.get(varName);
				if(varValue == null) {
					throw new RuntimeException("Variable value is not set.");
				}
				if(node.getOperator() == Operator.INCREMENT_LEFT) {
					variables.put(varName, ++varValue);
				} else if(node.getOperator() == Operator.DECREMENT_LEFT) {
					variables.put(varName, --varValue);
				} else if(node.getOperator() == Operator.INCREMENT_RIGHT) {
					variables.put(varName, varValue + 1);
				} else if(node.getOperator() == Operator.DECREMENT_RIGHT) {
					variables.put(varName, varValue - 1);
				}
				return varValue;
			} else {
				throw new RuntimeException("Variable expected.");
			}

		default:
			throw new RuntimeException("Unsupported operator for unary operation.");
		}
	}

	private long binaryOperation(BinaryTreeNode node) {
		switch(node.getOperator()) {
		case ASSIGNMENT:
		case ASSIGNMENT_ADDITION:
		case ASSIGNMENT_SUBTRACTION:
		case ASSIGNMENT_MULTIPLICATION:
		case ASSIGNMENT_DIVISION:
		case ASSIGNMENT_MOD:
		case ASSIGNMENT_SHIFT_LEFT:
		case ASSIGNMENT_SHIFT_RIGHT:
		case ASSIGNMENT_AND:
		case ASSIGNMENT_OR:
		case ASSIGNMENT_XOR:
			TreeNode leftNode = node.getLeft(), rightNode = node.getRight();
			if(leftNode instanceof VariableTreeNode) {
				VariableTreeNode variable = (VariableTreeNode) leftNode;
				String name = variable.getName();
				long value = eval(rightNode);
				Long resultValue = variables.get(name);
				if(resultValue == null) resultValue = 0L;
				switch(node.getOperator()) {
				case ASSIGNMENT:
					resultValue = value;
					break;
				case ASSIGNMENT_ADDITION:
					resultValue += value;
					break;
				case ASSIGNMENT_SUBTRACTION:
					resultValue -= value;
					break;
				case ASSIGNMENT_MULTIPLICATION:
					resultValue *= value;
					break;
				case ASSIGNMENT_DIVISION:
					resultValue /= value;
					break;
				case ASSIGNMENT_MOD:
					resultValue %= value;
					break;
				case ASSIGNMENT_SHIFT_LEFT:
					resultValue <<= value;
					break;
				case ASSIGNMENT_SHIFT_RIGHT:
					resultValue >>= value;
					break;
				case ASSIGNMENT_AND:
					resultValue &= value;
					break;
				case ASSIGNMENT_OR:
					resultValue |= value;
					break;
				case ASSIGNMENT_XOR:
					resultValue ^= value;
					break;
				}
				variables.put(name, resultValue);
				return resultValue;
			} else {
				throw new RuntimeException("Left side of assignment must be variable!");
			}
		}

		long left = eval(node.getLeft()), right = eval(node.getRight());
		switch(node.getOperator()) {
		case ADDITION:
			return left + right;

		case SUBTRACTION:
			return left - right;

		case MULTIPLICATION:
			return left * right;

		case DIVISION:
			return left / right;

		case MOD:
			return left % right;

		case SHIFT_LEFT:
			return left << right;

		case SHIFT_RIGHT:
			return left >> right;

		case COMPARE_GREATER:
			return (left > right) ? 1 : 0;

		case COMPARE_SMALLER:
			return (left < right) ? 1 : 0;

		case COMPARE_GREATER_OR_EQUAL:
			return (left >= right) ? 1 : 0;

		case COMPARE_SMALLER_OR_EQUAL:
			return (left <= right) ? 1 : 0;

		case EQUALS:
			return (left == right) ? 1 : 0;

		case NOT_EQUALS:
			return (left != right) ? 1 : 0;

		case BITWISE_AND:
			return left & right;

		case BITWISE_OR:
			return left | right;

		case BITWISE_XOR:
			return left ^ right;

		case LOGICAL_AND:
			return (left != 0 && right != 0) ? 1 : 0;

		case LOGICAL_OR:
			return (left != 0 || right != 0) ? 1 : 0;

		default:
			throw new RuntimeException("Unsupported operator for binary operation.");
		}
	}

	private long ternaryOperation(TernaryTreeNode node) {
		long left = eval(node.getLeft()), right = eval(node.getRight()), center = eval(node.getCenter());
		switch(node.getOperator()) {
		case CONDITION:
			return (left == 0) ? right : center;

		default:
			throw new RuntimeException("Unsupported operator for unary operation.");
		}
	}

	private long listOperation(ListTreeNode node) {
		switch(node.getOperator()) {
		case COMMA:
			long last = 0;
			for(TreeNode n : node.getNodes()) {
				last = eval(n);
			}
			return last;

		default:
			throw new RuntimeException("Unsupported operator for list operation.");
		}
	}

	public Map<String, Long> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, Long> variables) {
		this.variables = variables;
	}

	public Map<String, Long> getConstants() {
		return constants;
	}

	public void setConstants(Map<String, Long> constants) {
		this.constants = constants;
	}
}
