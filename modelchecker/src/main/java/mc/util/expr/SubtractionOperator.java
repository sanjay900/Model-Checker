package mc.util.expr;

public class SubtractionOperator extends BothOperator {

	public SubtractionOperator(Expression lhs, Expression rhs){
		super(lhs, rhs);
	}

	public int evaluate(){
		return getLeftHandSide().evaluate() - getRightHandSide().evaluate();
	}
}
