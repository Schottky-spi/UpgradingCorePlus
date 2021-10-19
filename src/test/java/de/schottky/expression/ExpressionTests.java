package de.schottky.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionTests {

    @Test
    void A_literal_node_evaluates_to_its_literal_value() {
        var testNode = new Expression.Node.Literal(5);
        assertEquals(testNode.evaluate(10, 10), 5);
    }

    @Test
    void A_variable_node_evaluates_to_level_or_previous() {
        var testNode = new Expression.Node.Variable(Expression.Node.Variable.Kind.LEVEL);
        assertEquals(testNode.evaluate(10, 2), 2);
        testNode = new Expression.Node.Variable(Expression.Node.Variable.Kind.PREVIOUS);
        assertEquals(testNode.evaluate(10, 2), 10);
    }

    @Test
    void An_operation_node_evaluates_to_the_result_of_its_children() {
        var testLeftNode = new Expression.Node.Literal(3);
        var testRightNode = new Expression.Node.Literal(5);
        var testNode = new Expression.Node.Operation(
                Expression.Node.Operation.Type.ADDITION,
                testRightNode,
                testLeftNode
        );
        assertEquals(testNode.evaluate(0, 0), 8);
    }

    @Test
    void the_result_of_an_operation_is_the_expected_result() {
        assertEquals(
                Expression.Node.Operation.Type.MULTIPLICATION.apply(9.5, 8.5),
                9.5 * 8.5
        );
        assertEquals(
                Expression.Node.Operation.Type.ADDITION.apply(3.6, 9.8),
                3.6 + 9.8
        );
        assertEquals(
                Expression.Node.Operation.Type.SUBTRACTION.apply(5, 77.7),
                5 - 77.7
        );
        assertEquals(
                Expression.Node.Operation.Type.DIVISION.apply(8, 4),
                8.0 / 4
        );
    }

    @Test
    void An_operation_node_evaluates_correctly_nested() {
        var leftNested = new Expression.Node.Literal(4);
        var rightNested = new Expression.Node.Literal(6);
        var nestedExpression = new Expression.Node.Operation(
                Expression.Node.Operation.Type.ADDITION,
                leftNested,
                rightNested
        );
        var rightActual = new Expression.Node.Variable(Expression.Node.Variable.Kind.PREVIOUS);
        var testNode = new Expression.Node.Operation(
                Expression.Node.Operation.Type.MULTIPLICATION,
                nestedExpression,
                rightActual
        );
        // result should be (4 + 6) * previous
        assertEquals(testNode.evaluate(2, 0), 20);
    }

    @Test
    void an_expression_can_be_simplified_with_constants() {
        var left = new Expression.Node.Literal(4);
        var right = new Expression.Node.Literal(4);
        var operation = new Expression.Node.Operation(
                Expression.Node.Operation.Type.ADDITION,
                left,
                right
        );
        var expression = new Expression(operation);
        assertEquals(new Expression.Node.Literal(8), expression.simplify().root());
    }
}
