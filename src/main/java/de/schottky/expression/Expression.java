package de.schottky.expression;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public record Expression(Node root) {

    double evaluate(double input, int level) {
        return root.evaluate(input, level);
    }

    public Expression simplify() {
        if (root instanceof Node.Operation operation) {
            return operation.foldConstants().map(Expression::new).orElse(this);
        } else {
            return this;
        }
    }

    public interface Node {

        @Contract("_, _, _ -> new")
        static @NotNull Node operation(Operation.Type type, double left, double right) {
            return new Operation(type, new Literal(left), new Literal(right));
        }

        double evaluate(double previous, int level);

        default Node formOperationLeft(Operation.Type type, Node right) {
            return new Operation(type, this, right);
        }

        record Literal(double value) implements Node {
            @Override
            public double evaluate(double previous, int level) {
                return value;
            }
        }

        record Variable(Kind kind) implements Node {
            @Override
            public double evaluate(double previous, int level) {
                return switch (kind) {
                    case LEVEL -> level;
                    case PREVIOUS -> previous;
                };
            }

            enum Kind {
                LEVEL, PREVIOUS,
            }
        }

        class Operation implements Node {

            private Node left;
            private Node right;
            private final Type type;

            public Operation(Type type, Node left, Node right) {
                this.left = left;
                this.right = right;
                this.type = type;
            }

            public double evaluate(double previous, int level) {
                final double leftValue = left.evaluate(previous, level);
                final double rightValue = right.evaluate(previous, level);
                return type.apply(leftValue, rightValue);
            }

            public Optional<Node> foldConstants() {
                if (
                        left instanceof Literal literalLeft &&
                        right instanceof Literal literalRight
                ) {
                    var result = type.apply(literalLeft.value(), literalRight.value());
                    return Optional.of(new Literal(result));
                } else {
                    var leftFolded = foldConstantsChild(left);
                    var rightFolded = foldConstantsChild(right);
                    if (leftFolded != null) {
                        this.left = leftFolded;
                    }
                    if (rightFolded != null) {
                        this.right = rightFolded;
                    }
                    if (leftFolded != null && rightFolded != null) {
                        this.foldConstants();
                    }
                    if (leftFolded != null || rightFolded != null) {
                        return foldConstants();
                    }
                    return Optional.empty();
                }
            }

            private @Nullable Node foldConstantsChild(Node child) {
                if (child instanceof Operation operation) {
                    return operation.foldConstants().orElse(null);
                } else {
                    return null;
                }
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Operation operation = (Operation) o;
                return left.equals(operation.left) && right.equals(operation.right) && type == operation.type;
            }

            @Override
            public int hashCode() {
                return Objects.hash(left, right, type);
            }

            @Override
            public String toString() {
                return "Operation[" +
                        "left=" + left +
                        ", right=" + right +
                        ", type=" + type +
                        ']';
            }

            enum Type {
                ADDITION {
                    @Override
                    double apply(double left, double right) {
                        return left + right;
                    }
                },
                MULTIPLICATION {
                    @Override
                    double apply(double left, double right) {
                        return left * right;
                    }
                },
                DIVISION {
                    @Override
                    double apply(double left, double right) {
                        return left / right;
                    }
                },
                MODULUS {
                    @Override
                    double apply(double left, double right) {
                        return left % right;
                    }
                },
                SUBTRACTION {
                    @Override
                    double apply(double left, double right) {
                        return left - right;
                    }
                };

                abstract double apply(double left, double right);
            }
        }
    }
}
