package de.schottky.expression

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ExpressionTests {

    @Test
    fun `A literal node will evaluate to it's actual value`() {
        val testNode = Expression.Node.Literal(5.0)
        Assertions.assertEquals(testNode.evaluate(10.0, 10), 5.0)
    }

    @Test
    fun `A variable node evaluates to either the current level or the previous level`() {
        var testNode = Expression.Node.Variable(Expression.Node.Variable.Kind.LEVEL)
        Assertions.assertEquals(testNode.evaluate(10.0, 2), 2.0)
        testNode = Expression.Node.Variable(Expression.Node.Variable.Kind.PREVIOUS)
        Assertions.assertEquals(testNode.evaluate(10.0, 2), 10.0)
    }

    @Test
    fun `An operation node evaluates to the values of it's children`() {
        val testLeftNode = Expression.Node.Literal(3.0)
        val testRightNode = Expression.Node.Literal(5.0)
        val testNode = Expression.Node.Operation(
            Expression.Node.Operation.Type.ADDITION,
            testRightNode,
            testLeftNode
        )
        Assertions.assertEquals(testNode.evaluate(0.0, 0), 8.0)
    }

    @Test
    fun `The result of an operation matches the pre-computed result`() {
        Assertions.assertEquals(
            Expression.Node.Operation.Type.MULTIPLICATION.apply(9.5, 8.5),
            9.5 * 8.5
        )
        Assertions.assertEquals(
            Expression.Node.Operation.Type.ADDITION.apply(3.6, 9.8),
            3.6 + 9.8
        )
        Assertions.assertEquals(
            Expression.Node.Operation.Type.SUBTRACTION.apply(5.0, 77.7),
            5 - 77.7
        )
        Assertions.assertEquals(
            Expression.Node.Operation.Type.DIVISION.apply(8.0, 4.0),
            8.0 / 4
        )
    }

    @Test
    fun `The result of an operation matches the pre-computed result for nested nodes`() {
        val leftNested = Expression.Node.Literal(4.0)
        val rightNested = Expression.Node.Literal(6.0)
        val nestedExpression = Expression.Node.Operation(
            Expression.Node.Operation.Type.ADDITION,
            leftNested,
            rightNested
        )
        val rightActual = Expression.Node.Variable(Expression.Node.Variable.Kind.PREVIOUS)
        val testNode = Expression.Node.Operation(
            Expression.Node.Operation.Type.MULTIPLICATION,
            nestedExpression,
            rightActual
        )
        // result should be (4 + 6) * previous
        Assertions.assertEquals(testNode.evaluate(2.0, 0), 20.0)
    }

    @Test
    fun `An expression will be simplified when  there are only constants involved`() {
        val left = Expression.Node.Literal(4.0)
        val right = Expression.Node.Literal(4.0)
        val operation = Expression.Node.Operation(
            Expression.Node.Operation.Type.ADDITION,
            left,
            right
        )
        val expression = Expression(operation)
        Assertions.assertEquals(Expression.Node.Literal(8.0), expression.simplify().root())
    }
}