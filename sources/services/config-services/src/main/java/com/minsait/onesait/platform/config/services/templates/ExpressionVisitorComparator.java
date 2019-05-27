/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.config.services.templates;

import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.ValueListExpression;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

public class ExpressionVisitorComparator implements ExpressionVisitor {

	private Expression otherExpression;
	private MatchResult result;

	public ExpressionVisitorComparator(Expression otherExpression, MatchResult result) {
		this.otherExpression = otherExpression;
		this.result = result;
	}

	@Override
	public void visit(NotExpression aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DateTimeLiteralExpression literal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimeKeyExpression timeKeyExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OracleHint hint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RowConstructor rowConstructor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ValueListExpression valueList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MySQLGroupConcat groupConcat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(KeepExpression aexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NumericBind bind) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(UserVariable var) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RegExpMySQLOperator regExpMySQLOperator) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JsonOperator jsonExpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JsonExpression jsonExpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RegExpMatchOperator rexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IntervalExpression iexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExtractExpression eexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AnalyticExpression aexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Modulo modulo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CastExpression cast) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Matches matches) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Concat concat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(WhenClause whenClause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CaseExpression caseExpression1) {
		boolean sameClass = true;
		CaseExpression caseExpression2 = null;
		try {
			caseExpression2 = (CaseExpression) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			caseExpression1.getSwitchExpression()
					.accept(new ExpressionVisitorComparator(caseExpression2.getSwitchExpression(), result));
			if (result.isMatch()) {
				List<WhenClause> whenList1 = caseExpression1.getWhenClauses();
				List<WhenClause> whenList2 = caseExpression2.getWhenClauses();

				Iterator<WhenClause> it1 = whenList1.iterator();
				Iterator<WhenClause> it2 = whenList2.iterator();

				while (it1.hasNext() && it2.hasNext()) {
					WhenClause when1 = it1.next();
					WhenClause when2 = it2.next();
					when1.getWhenExpression()
							.accept(new ExpressionVisitorComparator(when2.getWhenExpression(), result));
					if (result.isMatch()) {
						when1.getThenExpression()
								.accept(new ExpressionVisitorComparator(when2.getThenExpression(), result));
						if (!result.isMatch()) {
							break;
						}
					}
					if (!result.isMatch()) {
						break;
					}
				}

				if (result.isMatch()) {
					caseExpression1.getElseExpression()
							.accept(new ExpressionVisitorComparator(caseExpression2.getElseExpression(), result));
				}
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(SubSelect subSelect) {
		// TODO Auto-generated method stub
		System.out.println("Entra");

	}

	@Override
	public void visit(Column tableColumn1) {
		if (otherExpression.getClass().equals(UserVariable.class)) {
			UserVariable userVariable = (UserVariable) otherExpression;
			result.addVariable(userVariable.getName(), tableColumn1.getColumnName(), VariableData.Type.STRING);
			result.setResult(true);
		} else {
			boolean sameClass = true;
			Column tableColumn2 = null;
			try {
				tableColumn2 = (Column) otherExpression;
			} catch (ClassCastException e) {
				sameClass = false;
			}

			if (sameClass) {
				SqlComparator.matchColumn(tableColumn1, tableColumn2, result);
			} else {
				result.setResult(false);
			}
		}
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo1) {
		boolean sameClass = true;
		NotEqualsTo notEqualsTo2 = null;
		try {
			notEqualsTo2 = (NotEqualsTo) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			notEqualsTo1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(notEqualsTo2.getLeftExpression(), result));
			if (result.isMatch()) {
				notEqualsTo1.getRightExpression()
						.accept(new ExpressionVisitorComparator(notEqualsTo2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(MinorThanEquals minorThanEquals1) {
		boolean sameClass = true;
		MinorThanEquals minorThanEquals2 = null;
		try {
			minorThanEquals2 = (MinorThanEquals) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			minorThanEquals1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(minorThanEquals2.getLeftExpression(), result));
			if (result.isMatch()) {
				minorThanEquals1.getRightExpression()
						.accept(new ExpressionVisitorComparator(minorThanEquals2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(MinorThan minorThan1) {
		boolean sameClass = true;
		MinorThan minorThan2 = null;
		try {
			minorThan2 = (MinorThan) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			minorThan1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(minorThan2.getLeftExpression(), result));
			if (result.isMatch()) {
				minorThan1.getRightExpression()
						.accept(new ExpressionVisitorComparator(minorThan2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(LikeExpression likeExpression1) {
		boolean sameClass = true;
		LikeExpression likeExpression2 = null;
		try {
			likeExpression2 = (LikeExpression) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			likeExpression1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(likeExpression2.getLeftExpression(), result));
			if (result.isMatch()) {
				likeExpression1.getRightExpression()
						.accept(new ExpressionVisitorComparator(likeExpression2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(InExpression inExpression1) {
		boolean sameClass = true;
		InExpression inExpression2 = null;
		try {
			inExpression2 = (InExpression) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			inExpression1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(inExpression2.getLeftExpression(), result));
			if (result.isMatch()) {
				inExpression1.getRightItemsList()
						.accept(new ItemListVisitorComparator(inExpression2.getRightItemsList(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals1) {
		boolean sameClass = true;
		GreaterThanEquals greaterThanEquals2 = null;
		try {
			greaterThanEquals2 = (GreaterThanEquals) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			greaterThanEquals1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(greaterThanEquals2.getLeftExpression(), result));
			if (result.isMatch()) {
				greaterThanEquals1.getRightExpression()
						.accept(new ExpressionVisitorComparator(greaterThanEquals2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(GreaterThan greaterThan1) {
		boolean sameClass = true;
		GreaterThan greaterThan2 = null;
		try {
			greaterThan2 = (GreaterThan) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			greaterThan1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(greaterThan2.getLeftExpression(), result));
			if (result.isMatch()) {
				greaterThan1.getRightExpression()
						.accept(new ExpressionVisitorComparator(greaterThan2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(EqualsTo equalsTo1) {
		boolean sameClass = true;
		EqualsTo equalsTo2 = null;
		try {
			equalsTo2 = (EqualsTo) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			equalsTo1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(equalsTo2.getLeftExpression(), result));
			if (result.isMatch()) {
				equalsTo1.getRightExpression()
						.accept(new ExpressionVisitorComparator(equalsTo2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(Between between1) {
		boolean sameClass = true;
		Between between2 = null;
		try {
			between2 = (Between) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			between1.getLeftExpression().accept(new ExpressionVisitorComparator(between2.getLeftExpression(), result));
			if (result.isMatch()) {
				between1.getBetweenExpressionStart()
						.accept(new ExpressionVisitorComparator(between2.getBetweenExpressionStart(), result));
				if (result.isMatch()) {
					between1.getBetweenExpressionEnd()
							.accept(new ExpressionVisitorComparator(between2.getBetweenExpressionEnd(), result));
				}
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(OrExpression orExpression1) {
		boolean sameClass = true;
		OrExpression orExpression2 = null;
		try {
			orExpression2 = (OrExpression) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			orExpression1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(orExpression2.getLeftExpression(), result));
			if (result.isMatch()) {
				orExpression1.getRightExpression()
						.accept(new ExpressionVisitorComparator(orExpression2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(AndExpression andExpression1) {
		boolean sameClass = true;
		AndExpression andExpression2 = null;
		try {
			andExpression2 = (AndExpression) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			andExpression1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(andExpression2.getLeftExpression(), result));
			if (result.isMatch()) {
				andExpression1.getRightExpression()
						.accept(new ExpressionVisitorComparator(andExpression2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(Subtraction subtraction1) {
		boolean sameClass = true;
		Subtraction subtraction2 = null;
		try {
			subtraction2 = (Subtraction) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			subtraction1.getLeftExpression()
					.accept(new ExpressionVisitorComparator(subtraction2.getLeftExpression(), result));
			if (result.isMatch()) {
				subtraction1.getRightExpression()
						.accept(new ExpressionVisitorComparator(subtraction2.getRightExpression(), result));
			}
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(Multiplication multiplication) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Division division) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Addition addition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StringValue stringValue1) {
		if (StringValue.class.equals(otherExpression.getClass())) {
			StringValue stringValue2 = (StringValue) otherExpression;
			SqlComparator.matchStringValue(stringValue1, stringValue2, result);
		} else if (UserVariable.class.equals(otherExpression.getClass())) {
			UserVariable userVariable = (UserVariable) otherExpression;
			SqlComparator.matchStringValueWithVariable(stringValue1, userVariable, result);
		} else {
			result.setResult(false);
		}
	}

	@Override
	public void visit(Parenthesis parenthesis1) {
		boolean sameClass = true;
		Parenthesis parenthesis2 = null;
		try {
			parenthesis2 = (Parenthesis) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			parenthesis1.getExpression().accept(new ExpressionVisitorComparator(parenthesis2.getExpression(), result));
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(TimestampValue timestampValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimeValue timeValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DateValue dateValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(HexValue hexValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LongValue longValue1) {
		if (LongValue.class.equals(otherExpression.getClass())) {
			LongValue longValue2 = (LongValue) otherExpression;
			SqlComparator.matchLongValueValue(longValue1, longValue2, result);
		} else if (UserVariable.class.equals(otherExpression.getClass())) {
			UserVariable userVariable = (UserVariable) otherExpression;
			SqlComparator.matchLongValueValueWithVariable(longValue1, userVariable, result);
		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(DoubleValue doubleValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter1) {
		boolean sameClass = true;
		JdbcNamedParameter jdbcNamedParameter2 = null;
		try {
			jdbcNamedParameter2 = (JdbcNamedParameter) otherExpression;
		} catch (ClassCastException e) {
			sameClass = false;
		}

		if (sameClass) {
			SqlComparator.matchJdbcNamedParameter(jdbcNamedParameter1, jdbcNamedParameter2, result);

		} else {
			result.setResult(false);
		}

	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(SignedExpression signedExpression) {
		if (otherExpression.getClass().equals(UserVariable.class)) {
			UserVariable userVariable = (UserVariable) otherExpression;
			result.addVariable(userVariable.getName(), signedExpression.toString(), VariableData.Type.STRING);
			result.setResult(true);
		}
	}

	@Override
	public void visit(Function function1) {
		if (otherExpression.getClass().equals(UserVariable.class)) {
			UserVariable userVariable = (UserVariable) otherExpression;
			result.addVariable(userVariable.getName(), function1.toString(), VariableData.Type.STRING);
			result.setResult(true);
		} else if (otherExpression.getClass().equals(Function.class)) {
			Function function2 = (Function) otherExpression;
			if (function1.getParameters() != null && function2.getParameters() != null) {
				function1.getParameters().accept(new ItemListVisitorComparator(function2.getParameters(), result));
			}
			if (function1.getKeep() != null) {
				function1.getKeep().accept(this);
			}
		}
	}

	@Override
	public void visit(NullValue nullValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseLeftShift aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseRightShift aThis) {
		// TODO Auto-generated method stub

	}

}
