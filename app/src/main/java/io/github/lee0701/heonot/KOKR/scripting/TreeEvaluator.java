package io.github.lee0701.heonot.KOKR.scripting;

import java.util.HashMap;
import java.util.Map;

import io.github.lee0701.heonot.KOKR.scripting.nodes.*;

public class TreeEvaluator {

	Map<String, Long> variables = new HashMap<>();
	Map<String, Long> constants = new HashMap<>();

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

	public long unaryOperation(UnaryTreeNode node) {
		switch(node.getOperator()) {
		case Operator.PLUS:
			return eval(node.getCenter());

		case Operator.MINUS:
			return -eval(node.getCenter());

		case Operator.NOT:
			long value = eval(node.getCenter());
			return (value == 0) ? 1 : 0;

		case Operator.INVERT:
			return ~eval(node.getCenter());

		case Operator.INCREMENT_LEFT:
		case Operator.INCREMENT_RIGHT:
		case Operator.DECREMENT_LEFT:
		case Operator.DECREMENT_RIGHT:
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

	public long binaryOperation(BinaryTreeNode node) {
		switch(node.getOperator()) {
		case Operator.ASSIGNMENT:
		case Operator.ASSIGNMENT_ADDITION:
		case Operator.ASSIGNMENT_SUBTRACTION:
		case Operator.ASSIGNMENT_MULTIPLICATION:
		case Operator.ASSIGNMENT_DIVISION:
		case Operator.ASSIGNMENT_MOD:
		case Operator.ASSIGNMENT_SHIFT_LEFT:
		case Operator.ASSIGNMENT_SHIFT_RIGHT:
		case Operator.ASSIGNMENT_AND:
		case Operator.ASSIGNMENT_OR:
		case Operator.ASSIGNMENT_XOR:
			TreeNode leftNode = node.getLeft(), rightNode = node.getRight();
			if(leftNode instanceof VariableTreeNode) {
				VariableTreeNode variable = (VariableTreeNode) leftNode;
				String name = variable.getName();
				long value = eval(rightNode);
				Long resultValue = variables.get(name);
				if(resultValue == null) resultValue = 0L;
				switch(node.getOperator()) {
				case Operator.ASSIGNMENT:
					resultValue = value;
					break;
				case Operator.ASSIGNMENT_ADDITION:
					resultValue += value;
					break;
				case Operator.ASSIGNMENT_SUBTRACTION:
					resultValue -= value;
					break;
				case Operator.ASSIGNMENT_MULTIPLICATION:
					resultValue *= value;
					break;
				case Operator.ASSIGNMENT_DIVISION:
					resultValue /= value;
					break;
				case Operator.ASSIGNMENT_MOD:
					resultValue %= value;
					break;
				case Operator.ASSIGNMENT_SHIFT_LEFT:
					resultValue <<= value;
					break;
				case Operator.ASSIGNMENT_SHIFT_RIGHT:
					resultValue >>= value;
					break;
				case Operator.ASSIGNMENT_AND:
					resultValue &= value;
					break;
				case Operator.ASSIGNMENT_OR:
					resultValue |= value;
					break;
				case Operator.ASSIGNMENT_XOR:
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
		case Operator.ADDITION:
			return left + right;

		case Operator.SUBTRACTION:
			return left - right;

		case Operator.MULTIPLICATION:
			return left * right;

		case Operator.DIVISION:
			return left / right;

		case Operator.MOD:
			return left % right;

		case Operator.SHIFT_LEFT:
			return left << right;

		case Operator.SHIFT_RIGHT:
			return left >> right;

		case Operator.COMPARE_GREATER:
			return (left > right) ? 1 : 0;

		case Operator.COMPARE_SMALLER:
			return (left < right) ? 1 : 0;

		case Operator.COMPARE_GREATER_OR_EQUAL:
			return (left >= right) ? 1 : 0;

		case Operator.COMPARE_SMALLER_OR_EQUAL:
			return (left <= right) ? 1 : 0;

		case Operator.EQUALS:
			return (left == right) ? 1 : 0;

		case Operator.NOT_EQUALS:
			return (left != right) ? 1 : 0;

		case Operator.BITWISE_AND:
			return left & right;

		case Operator.BITWISE_OR:
			return left | right;

		case Operator.BITWISE_XOR:
			return left ^ right;

		case Operator.LOGICAL_AND:
			return (left != 0 && right != 0) ? 1 : 0;

		case Operator.LOGICAL_OR:
			return (left != 0 || right != 0) ? 1 : 0;

		default:
			throw new RuntimeException("Unsupported operator for binary operation.");
		}
	}

	public long ternaryOperation(TernaryTreeNode node) {
		long left = eval(node.getLeft()), right = eval(node.getRight()), center = eval(node.getCenter());
		switch(node.getOperator()) {
		case Operator.CONDITION:
			return (left == 0) ? right : center;

		default:
			throw new RuntimeException("Unsupported operator for unary operation.");
		}
	}

	public long listOperation(ListTreeNode node) {
		switch(node.getOperator()) {
		case Operator.COMMA:
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
